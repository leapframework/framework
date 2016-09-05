/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.web.api.config;

import leap.core.annotation.Inject;
import leap.core.meta.MTypeManager;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.meta.MType;
import leap.lang.meta.MVoidType;
import leap.lang.resource.Resource;
import leap.lang.xml.XML;
import leap.lang.xml.XmlReader;
import leap.web.api.Apis;
import leap.web.api.meta.model.MApiResponse;
import leap.web.api.meta.model.MApiResponseBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

public class XmlApiConfigReader implements ApiConfigReader {

    private static final Log log = LogFactory.get(XmlApiConfigReader.class);

    protected static final String APIS              = "apis";
    protected static final String API               = "api";
    protected static final String GLOBAL            = "global";
    protected static final String OAUTH             = "oauth";
    protected static final String VERSION           = "version";
    protected static final String TITLE             = "title";
    protected static final String SUMMARY           = "summary";
    protected static final String DESC              = "desc";
    protected static final String PRODUCES          = "produces";
    protected static final String CONSUMES          = "consumes";
    protected static final String PROTOCOLS         = "protocols";
    protected static final String MAX_PAGE_SIZE     = "max-page-size";
    protected static final String DEFAULT_PAGE_SIZE = "default-page-size";
    protected static final String ENABLED           = "enabled";
    protected static final String AUTHZ_URL         = "authz-url";
    protected static final String TOKEN_URL         = "token-url";
    protected static final String SCOPE             = "scope";
    protected static final String NAME              = "name";
    protected static final String BASE_PATH         = "base-path";
    protected static final String RESPONSES         = "responses";
    protected static final String RESPONSE          = "response";
    protected static final String STATUS            = "status";
    protected static final String TYPE              = "type";

    protected @Inject MTypeManager mTypeManager;

    @Override
    public boolean readConfiguration(Apis apis, ApiConfigReaderContext context, Resource resource) {
        if(Strings.endsWithIgnoreCase(resource.getFilename(), ".xml")) {
            readXml(apis, context, resource);
            return true;
        }
        return false;
    }

    protected void readXml(Apis apis, ApiConfigReaderContext context, Resource resource) {

        XmlReader reader = null;
        try{
            reader = XML.createReader(resource);
            reader.setPlaceholderResolver(context.getAppConfig().getPlaceholderResolver());

            boolean foundValidRootElement = false;

            while(reader.next()){
                if(reader.isStartElement(APIS)){
                    foundValidRootElement = true;

                    readApis(apis, context, resource, reader);
                    break;
                }

                if(reader.isStartElement(API)) {
                    foundValidRootElement = true;

                    readApi(apis, context, resource, reader);
                    break;
                }

                if(reader.isStartElement()) {
                    break;
                }
            }

            if(!foundValidRootElement){
                log.info("No valid root element found in file : " + resource.getClasspath());
            }
        }finally{
            IO.close(reader);
        }

    }

    protected void readApis(Apis apis, ApiConfigReaderContext context, Resource resource, XmlReader reader) {
        while(reader.nextWhileNotEnd(APIS)) {
            if(reader.isStartElement(GLOBAL)) {
                readGlobal(apis, context, resource, reader);
                continue;
            }

            if(reader.isStartElement(API)) {
                readApi(apis, context, resource, reader);
                continue;
            }
        }

    }

    protected void readGlobal(Apis apis, ApiConfigReaderContext context, Resource resource, XmlReader reader) {

        Map<String, MApiResponse> commonResponses = new LinkedHashMap<>();

        while(reader.nextWhileNotEnd(GLOBAL)) {

            if(reader.isStartElement(OAUTH)) {

                apis.setDefaultOAuthEnabled(
                        reader.resolveBooleanAttribute(ENABLED, apis.isDefaultOAuthEnabled()));

                reader.loopInsideElement(() -> {
                    if(reader.isStartElement(AUTHZ_URL)) {
                        String url = reader.resolveElementTextAndEnd();
                        if(!Strings.isEmpty(url)) {
                            apis.setDefaultOAuthAuthorizationUrl(url);
                        }
                        return;
                    }

                    if(reader.isStartElement(TOKEN_URL)) {
                        String url = reader.resolveElementTextAndEnd();
                        if(!Strings.isEmpty(url)) {
                            apis.setDefaultOAuthTokenUrl(url);
                        }
                        return;
                    }
                });

                continue;
            }

            if(reader.isStartElement(RESPONSES)) {
                readCommonResponses(reader).forEach(apis.getCommonResponses()::put);
                continue;
            }
        }

        commonResponses.forEach(apis.getCommonResponses()::put);
    }

    protected Map<String,MApiResponse> readCommonResponses(XmlReader reader) {
        Map<String, MApiResponse> responses = new LinkedHashMap<>();

        reader.loopInsideElement(() -> {

            if(reader.isStartElement(RESPONSE)) {
                String name   = reader.getAttribute(NAME);
                int    status = reader.getRequiredIntAttribute(STATUS);
                String type   = reader.getAttribute(TYPE);

                if(Strings.isEmpty(name)) {
                    name = String.valueOf(status);
                }

                MApiResponseBuilder r = new MApiResponseBuilder();
                r.setName(name);
                r.setStatus(status);
                r.setDescription(reader.getAttribute(DESC));

                if(!Strings.isEmpty(type)) {
                    Class<?> c = Classes.tryForName(type);
                    if(null == c) {
                        throw new ApiConfigException("Invalid response type '" + type + "', check : " + reader.getCurrentLocation());
                    }

                    MType mtype = mTypeManager.getMType(c);
                    r.setType(mtype);
                }else{
                    r.setType(MVoidType.TYPE);
                }

                reader.loopInsideElement(() -> {

                    if(reader.isStartElement(DESC)) {
                        r.setDescription(reader.getElementTextAndEnd());
                    }

                });

                responses.put(name, r.build());
            }

        });

        return responses;
    }

    protected void readApi(Apis apis, ApiConfigReaderContext context, Resource resource, XmlReader reader) {
        String name     = reader.resolveRequiredAttribute(NAME);
        String basePath = reader.resolveRequiredAttribute(BASE_PATH);

        ApiConfigurator api = apis.configurators().get(name);
        if(null == api) {
            api = apis.add(name, basePath);
        }

        readApi(context, reader, api);
    }

    protected void readApi(ApiConfigReaderContext context, XmlReader reader, ApiConfigurator api) {
        while(reader.nextWhileNotEnd(API)) {

            if(reader.isStartElement(VERSION)) {
                String v = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(v)) {
                    api.setVersion(v);
                }
                continue;
            }

            if(reader.isStartElement(TITLE)) {
                String title = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(title)) {
                    api.setTitle(title);
                }
                continue;
            }

            if(reader.isStartElement(SUMMARY)) {
                String summary = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(summary)) {
                    api.setSummary(summary);
                }
                continue;
            }

            if(reader.isStartElement(DESC)) {
                String desc = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(desc)) {
                    api.setDescription(desc);
                }
                continue;
            }

            if(reader.isStartElement(PRODUCES)) {
                String s = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(s)) {
                    api.setProduces(Strings.splitMultiLines(s));
                }
                continue;
            }

            if(reader.isStartElement(CONSUMES)) {
                String s = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(s)) {
                    api.setConsumes(Strings.splitMultiLines(s));
                }
                continue;
            }

            if(reader.isStartElement(PROTOCOLS)) {
                String s = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(s)) {
                    api.setProtocols(Strings.splitMultiLines(s));
                }
                continue;
            }

            if(reader.isStartElement(MAX_PAGE_SIZE)) {
                Integer i = reader.getIntegerElementTextAndEnd();
                if(null != i) {
                    api.setMaxPageSize(i);
                }
                continue;
            }

            if(reader.isStartElement(DEFAULT_PAGE_SIZE)) {
                Integer i = reader.getIntegerElementTextAndEnd();
                if(null != i) {
                    api.setDefaultPageSize(i);
                }
                continue;
            }
        }
    }

}