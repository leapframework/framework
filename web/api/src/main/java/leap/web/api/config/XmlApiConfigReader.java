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
import leap.lang.Props;
import leap.lang.Strings;
import leap.lang.extension.ExProperties;
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
import leap.web.api.meta.model.MPermission;
import leap.web.api.permission.ResourcePermission;
import leap.web.api.permission.ResourcePermissions;
import leap.web.config.DefaultModuleConfig;

import java.util.LinkedHashMap;
import java.util.Map;

public class XmlApiConfigReader implements ApiConfigReader {

    private static final Log log = LogFactory.get(XmlApiConfigReader.class);

    protected static final String APIS                 = "apis";
    protected static final String API                  = "api";
    protected static final String GLOBAL               = "global";
    protected static final String OAUTH                = "oauth";
    protected static final String VERSION              = "version";
    protected static final String TITLE                = "title";
    protected static final String SUMMARY              = "summary";
    protected static final String DESC                 = "desc";
    protected static final String PRODUCES             = "produces";
    protected static final String CONSUMES             = "consumes";
    protected static final String PROTOCOLS            = "protocols";
    protected static final String MAX_PAGE_SIZE        = "max-page-size";
    protected static final String DEFAULT_PAGE_SIZE    = "default-page-size";
    protected static final String ENABLED              = "enabled";
    protected static final String AUTHZ_URL            = "authz-url";
    protected static final String TOKEN_URL            = "token-url";
    protected static final String SCOPE                = "scope";
    protected static final String NAME                 = "name";
    protected static final String BASE_PATH            = "base-path";
    protected static final String BASE_PACKAGE         = "base-package";
    protected static final String RESPONSES            = "responses";
    protected static final String RESPONSE             = "response";
    protected static final String STATUS               = "status";
    protected static final String TYPE                 = "type";
    protected static final String PERMISSIONS          = "permissions";
    protected static final String PERMISSION           = "permission";
    protected static final String VALUE                = "value";
    protected static final String RESOURCE_PERMISSIONS = "resource-permissions";
    protected static final String RESOURCE             = "resource";
    protected static final String RESOURCES            = "resources";
    protected static final String CLASS                = "class";
    protected static final String PACKAGE              = "package";
    protected static final String DEFAULT              = "default";
    protected static final String HTTP_METHODS         = "http-methods";
    protected static final String PATH_PATTERN         = "path-pattern";

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
        String basePath = reader.resolveAttribute(BASE_PATH);
        String basePackage = reader.resolveAttribute(BASE_PACKAGE);
        ApiConfigurator api = apis.tryGetConfigurator(name);
        if(null == api) {
            reader.getRequiredAttribute(BASE_PATH);
            api = apis.add(name, basePath);
            api.setBasePackage(basePackage);
        }

        readApi(context, reader, api);
        addWebModule(context,api);
    }

    protected void addWebModule(ApiConfigReaderContext context, ApiConfigurator api) {
        ApiConfig apiConf = api.config();
        String basePackage = apiConf.getBasePackage();
        if(Strings.isNotEmpty(basePackage)){
            DefaultModuleConfig module = new DefaultModuleConfig();
            module.setName(apiConf.getName());
            module.setBasePath(apiConf.getBasePath());
            module.setBasePackage(apiConf.getBasePackage());
            context.getWebConfigurator().addModule(module);
        }

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

            if(reader.isStartElement(RESPONSES)) {
                //todo : override exists responses.
                readCommonResponses(reader).forEach(api::putCommonResponse);
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

            if(reader.isStartElement(PERMISSIONS)) {
                readPermissions(context, reader, api);
                continue;
            }

            if(reader.isStartElement(RESOURCE_PERMISSIONS)) {
                readResourcePermissions(context, reader, api);
                continue;
            }
        }
    }

    protected void readPermissions(ApiConfigReaderContext context, XmlReader reader, ApiConfigurator api) {
        StringBuilder chars      = new StringBuilder();
        boolean       hasElement = false;

        while(reader.nextWhileNotEnd(PERMISSIONS)){
            if(!hasElement && reader.isCharacters()) {
                chars.append(reader.getCharacters());
                continue;
            }

            if(reader.isStartElement(PERMISSION)){
                hasElement = true;

                String value = reader.getRequiredAttribute(VALUE);
                String desc  = reader.getAttribute(DESC);

                if(Strings.isEmpty(desc)) {
                    desc = reader.getElementTextAndEnd();
                }

                api.setPermission(new MPermission(value, desc));
                continue;
            }
        }

        if(!hasElement) {
            String text = chars.toString().trim();
            if(text.length() > 0) {
                ExProperties props = Props.loadKeyValues(text);

                props.forEach((k,v) -> {
                    String value = (String)k;
                    String desc = (String)v;

                    api.setPermission(new MPermission(value, desc));
                });
            }
        }
    }

    protected void readResourcePermissions(ApiConfigReaderContext context, XmlReader reader, ApiConfigurator api) {
        ResourcePermissions rps = new ResourcePermissions();

        reader.loopInsideElement(() -> {

            if(reader.isStartElement(RESOURCE)) {
                String className = reader.getRequiredAttribute(CLASS);

                rps.addResourceClass(Classes.forName(className));

                return;
            }

            if(reader.isStartElement(RESOURCES)) {

                String packageName = reader.getRequiredAttribute(PACKAGE);

                rps.addResourcePackage(packageName);

                return;
            }

            if(reader.isStartElement(PERMISSION)) {

                ResourcePermission rp = new ResourcePermission();

                rp.setValue(reader.getRequiredAttribute(VALUE));
                rp.setDescription(reader.getAttribute(DESC));
                rp.setDefault(reader.getBooleanAttribute(DEFAULT, false));

                String httpMethods = reader.getAttribute(HTTP_METHODS);
                if(!Strings.isEmpty(httpMethods)) {
                    //todo :
                }

                String pathPattern = reader.getAttribute(PATH_PATTERN);
                if(!Strings.isEmpty(pathPattern)) {
                    //todo :
                }

                rps.addPermission(rp);
                return;
            }
        });

        if(null == rps.getDefaultPermission() && rps.getPermissions().size() == 1) {
            ResourcePermission rp = rps.getPermissions().iterator().next();

            if(null == rp.getHttpMethods() && null == rp.getPathPattern()) {
                rps.setDefaultPermission(rp);
            }
        }

        api.config().getResourcePermissionsSet().addResourcePermissions(rps);
    }
}