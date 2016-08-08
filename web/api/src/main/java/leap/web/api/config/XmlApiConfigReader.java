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

import leap.lang.Strings;
import leap.lang.io.IO;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.xml.XML;
import leap.lang.xml.XmlReader;
import leap.web.api.Apis;

public class XmlApiConfigReader implements ApiConfigReader {

    private static final Log log = LogFactory.get(XmlApiConfigReader.class);

    protected static final String APIS_ELEMENT      = "apis";
    protected static final String API_ELEMENT       = "api";
    protected static final String CONFIG_ELEMENT    = "config";
    protected static final String OAUTH_ELEMENT     = "oauth";
    protected static final String VERSION_ELEMENT   = "version";
    protected static final String TITLE_ELEMENT     = "title";
    protected static final String SUMMARY_ELEMENT   = "summary";
    protected static final String DESC_ELEMENT      = "desc";
    protected static final String PRODUCES_ELEMENT  = "produces";
    protected static final String CONSUMES_ELEMENT  = "consumes";
    protected static final String PROTOCOLS_ELEMENT = "protocols";


    protected static final String ENABLED_ATTR      = "enabled";
    protected static final String AUTHZ_URL_ELEMENT = "authz-url";
    protected static final String TOKEN_URL_ELEMENT = "token-url";
    protected static final String SCOPE_ATTR        = "scope";
    protected static final String NAME_ATTR         = "name";
    protected static final String BASE_PATH_ATTR    = "base-path";

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
                if(reader.isStartElement(APIS_ELEMENT)){
                    foundValidRootElement = true;

                    readApis(apis, context, resource, reader);
                    break;
                }

                if(reader.isStartElement(API_ELEMENT)) {
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
        while(reader.nextWhileNotEnd(APIS_ELEMENT)) {
            if(reader.isStartElement(CONFIG_ELEMENT)) {
                readConfig(apis, context, resource, reader);
                continue;
            }

            if(reader.isStartElement(API_ELEMENT)) {
                readApi(apis, context, resource, reader);
                continue;
            }
        }

    }

    protected void readConfig(Apis apis, ApiConfigReaderContext context, Resource resource, XmlReader reader) {

        while(reader.nextWhileNotEnd(CONFIG_ELEMENT)) {

            if(reader.isStartElement(OAUTH_ELEMENT)) {

                apis.setDefaultOAuthEnabled(
                        reader.resolveBooleanAttribute(ENABLED_ATTR, apis.isDefaultOAuthEnabled()));

                while(reader.nextWhileNotEnd(OAUTH_ELEMENT)) {

                    if(reader.isStartElement(AUTHZ_URL_ELEMENT)) {
                        String url = reader.resolveElementTextAndEnd();
                        if(!Strings.isEmpty(url)) {
                            apis.setDefaultOAuthAuthorizationUrl(url);
                        }
                        continue;
                    }

                    if(reader.isStartElement(TOKEN_URL_ELEMENT)) {
                        String url = reader.resolveElementTextAndEnd();
                        if(!Strings.isEmpty(url)) {
                            apis.setDefaultOAuthTokenUrl(url);
                        }
                        continue;
                    }
                }

                continue;
            }

        }

    }

    protected void readApi(Apis apis, ApiConfigReaderContext context, Resource resource, XmlReader reader) {
        String name     = reader.resolveRequiredAttribute(NAME_ATTR);
        String basePath = reader.resolveRequiredAttribute(BASE_PATH_ATTR);

        ApiConfigurator api = apis.configurators().get(name);
        if(null == api) {
            api = apis.add(name, basePath);
        }

        readApi(context, reader, api);
    }

    protected void readApi(ApiConfigReaderContext context, XmlReader reader, ApiConfigurator api) {
        while(reader.nextWhileNotEnd(API_ELEMENT)) {

            if(reader.isStartElement(VERSION_ELEMENT)) {
                String v = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(v)) {
                    api.setVersion(v);
                }
                continue;
            }

            if(reader.isStartElement(TITLE_ELEMENT)) {
                String title = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(title)) {
                    api.setTitle(title);
                }
                continue;
            }

            if(reader.isStartElement(SUMMARY_ELEMENT)) {
                String summary = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(summary)) {
                    api.setSummary(summary);
                }
                continue;
            }

            if(reader.isStartElement(DESC_ELEMENT)) {
                String desc = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(desc)) {
                    api.setDescription(desc);
                }
                continue;
            }

            if(reader.isStartElement(PRODUCES_ELEMENT)) {
                String s = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(s)) {
                    api.setProduces(Strings.splitMultiLines(s));
                }
                continue;
            }

            if(reader.isStartElement(CONSUMES_ELEMENT)) {
                String s = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(s)) {
                    api.setConsumes(Strings.splitMultiLines(s));
                }
                continue;
            }

            if(reader.isStartElement(PROTOCOLS_ELEMENT)) {
                String s = reader.getElementTextAndEnd();
                if(!Strings.isEmpty(s)) {
                    api.setProtocols(Strings.splitMultiLines(s));
                }
                continue;
            }
        }
    }

}