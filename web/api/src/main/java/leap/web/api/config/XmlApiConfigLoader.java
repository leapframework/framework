/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.web.api.config;

import leap.core.AppConfigException;
import leap.core.config.AppConfigContext;
import leap.core.config.AppConfigListener;
import leap.core.config.AppConfigPaths;
import leap.core.config.AppConfigProcessor;
import leap.lang.Classes;
import leap.lang.Collections2;
import leap.lang.Props;
import leap.lang.Strings;
import leap.lang.extension.ExProperties;
import leap.lang.meta.MVoidType;
import leap.lang.xml.XmlReader;
import leap.web.api.config.model.*;
import leap.web.api.config.model.RestdConfig.Model;
import leap.web.api.meta.model.MApiPermission;
import leap.web.api.meta.model.MApiResponseBuilder;
import leap.web.api.permission.ResourcePermission;
import leap.web.api.permission.ResourcePermissions;
import leap.web.api.spec.swagger.SwaggerConstants;
import leap.web.config.DefaultModuleConfig;
import leap.web.config.ModuleConfigExtension;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class XmlApiConfigLoader implements AppConfigProcessor, AppConfigListener {
    private static final String NAMESPACE_URI = "http://www.leapframework.org/schema/webapi";

    protected static final String APIS                 = "apis";
    protected static final String API                  = "api";
    protected static final String GLOBAL               = "global";
    protected static final String PARAMS               = "params";
    protected static final String PARAM                = "param";
    protected static final String PROPERTIES           = "properties";
    protected static final String PROPERTY             = "property";
    protected static final String MODELS               = "models";
    protected static final String MODEL                = "model";
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
    protected static final String FLOW                 = "flow";
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
    protected static final String UNIQUE_OPERATION_ID  = "unique-operation-id";
    protected static final String DEFAULT_ANONYMOUS    = "default-anonymous";
    protected static final String RESTD                = "restd";
    protected static final String RESTD_ENABLED        = "restd-enabled";
    protected static final String RESTD_DATA_SOURCE    = "restd-data-source";
    protected static final String DATA_SOURCE          = "data-source";
    protected static final String INCLUDED_MODELS      = "included-models";
    protected static final String EXCLUDED_MODELS      = "excluded-models";
    protected static final String READONLY_MODELS      = "readonly-models";
    protected static final String ANONYMOUS            = "anonymous";
    protected static final String READONLY             = "readonly";
    protected static final String READ                 = "read";
    protected static final String WRITE                = "write";
    protected static final String CREATE               = "create";
    protected static final String UPDATE               = "update";
    protected static final String DELETE               = "delete";
    protected static final String FIND                 = "find";
    protected static final String QUERY                = "query";
    protected static final String OVERRIDE             = "override";
    protected static final String SQL_OPERATION        = "sql-operation";
    protected static final String SQL_KEY              = "sql-key";

    @Override
    public String getNamespaceURI() {
        return NAMESPACE_URI;
    }

    @Override
    public void preLoadConfig(AppConfigContext context, AppConfigPaths paths) {
        paths.addConfigName("apis");
    }

    @Override
    public void processElement(AppConfigContext context, XmlReader reader) throws AppConfigException {
        readApis(context, reader);
    }

    @Override
    public void postLoadConfig(AppConfigContext context) {
        ApiConfigs configs = context.getExtension(ApiConfigs.class);
        if (configs != null) {
            configs.getApis().forEach((k, v) -> {
                String basePackage = v.config().getBasePackage();
                if (Strings.isNotEmpty(basePackage)) {
                    context.getAdditionalPackages().add(basePackage);
                }
            });
        }
    }

    protected void readApis(AppConfigContext context, XmlReader reader) {
        while (reader.nextWhileNotEnd(APIS)) {

            if(reader.isStartElement(GLOBAL)) {
                readGlobal(context, reader);
                continue;
            }

            if (reader.isStartElement(API)) {
                readApi(context, reader);
                continue;
            }
        }

    }

    protected void readGlobal(AppConfigContext context, XmlReader reader) {
        ApiConfigs configs = context.getOrCreateExtension(ApiConfigs.class);

        reader.loopInsideElement(() -> {

            if (reader.isStartElement(OAUTH)) {
                configs.setOAuthConfig(readOAuth(context, reader));
                return;
            }

            if (reader.isStartElement(RESPONSES)) {
                readCommonResponses(reader).forEach(configs::addCommonResponse);
                return;
            }

            if (reader.isStartElement(PARAMS)) {
                readParams(context, reader, (p) -> {
                    if(p.isOverride()) {
                        configs.removeCommonParam(p);
                    }
                    configs.addCommonParam(p);
                });
                return;
            }

            if (reader.isStartElement(MODELS)) {
                readModels(context, reader, configs::addCommonModel);
                return;
            }

        });

    }

    protected Map<String, MApiResponseBuilder> readCommonResponses(XmlReader reader) {
        Map<String, MApiResponseBuilder> responses = new LinkedHashMap<>();

        reader.loopInsideElement(() -> {

            if (reader.isStartElement(RESPONSE)) {
                String name = reader.getAttribute(NAME);
                int status = reader.getRequiredIntAttribute(STATUS);
                String type = reader.getAttribute(TYPE);

                if (Strings.isEmpty(name)) {
                    name = String.valueOf(status);
                }

                MApiResponseBuilder r = new MApiResponseBuilder();
                r.setName(name);
                r.setStatus(status);
                r.setDescription(reader.getAttribute(DESC));

                if (!Strings.isEmpty(type)) {
                    Class<?> c = Classes.tryForName(type);
                    if (null == c) {
                        throw new ApiConfigException("Invalid response type '" + type + "', check : " + reader.getCurrentLocation());
                    }
                    //todo :
                    r.setTypeClass(c);
                } else {
                    r.setType(MVoidType.TYPE);
                }

                reader.loopInsideElement(() -> {

                    if (reader.isStartElement(DESC)) {
                        r.setDescription(reader.getElementTextAndEnd());
                    }

                });

                responses.put(name, r);
            }

        });

        return responses;
    }

    protected void readParams(AppConfigContext context, XmlReader reader, Consumer<ParamConfigImpl> func) {
        reader.loopInsideElement(() -> {
            if (reader.isStartElement(PARAM)) {

                ParamConfigImpl param = new ParamConfigImpl(reader.getAttribute(NAME));
                param.setClassName(reader.getRequiredAttribute(CLASS));
                param.setOverride(reader.getBooleanAttribute(OVERRIDE, false));

                reader.loopInsideElement(() -> {
                    if(reader.isStartElement(PROPERTIES)) {
                        readWrappedParams(context, reader, (p) -> {
                            if(null != param.getWrappedParam(p.getName())) {
                                throw new ApiConfigException("Found duplicated wrapped param '" + p.getName() + "', at " + reader.getCurrentLocation());
                            }
                            param.addWrappedParam(p);
                        });
                    }
                });

                func.accept(param);
            }
        });

    }

    protected void readWrappedParams(AppConfigContext context, XmlReader reader, Consumer<ParamConfig> func) {
        while (reader.nextWhileNotEnd(PROPERTIES)) {
            if (reader.isStartElement(PROPERTY)) {

                String name = reader.getRequiredAttribute(NAME);

                ParamConfigImpl p = new ParamConfigImpl(name);

                readDocument(reader, p);

                func.accept(p);
            }
        }
    }

    protected void readModels(AppConfigContext context, XmlReader reader, Consumer<ModelConfig> func) {
        reader.loopInsideElement(() -> {
            if (reader.isStartElement(MODEL)) {

                String className = reader.getAttribute(CLASS);
                String modelName = reader.getAttribute(NAME);

                if(Strings.isEmpty(className) && Strings.isEmpty(modelName)) {
                    throw new ApiConfigException("One of the 'name' or 'class' attribute must not be empty, at " + reader.getCurrentLocation());
                }

                ModelConfigImpl model = new ModelConfigImpl();
                model.setName(modelName);
                model.setClassName(className);

                reader.loopInsideElement(() -> {
                    if(reader.isStartElement(PROPERTIES)) {
                        readProperties(context, reader, (p) -> {
                            if(null != model.getProperty(p.getName())) {
                                throw new ApiConfigException("Found duplicated property '" + p.getName() + "', at " + reader.getCurrentLocation());
                            }
                            model.addProperty(p);
                        });
                    }
                });

                func.accept(model);
            }
        });

    }

    protected void readProperties(AppConfigContext context, XmlReader reader, Consumer<ModelConfig.Property> func) {
        while (reader.nextWhileNotEnd(PROPERTIES)) {
            if (reader.isStartElement(PROPERTY)) {

                String name = reader.getRequiredAttribute(NAME);

                ModelConfigImpl.PropertyImpl p = new ModelConfigImpl.PropertyImpl(name);

                readDocument(reader, p);

                func.accept(p);
            }
        }
    }

    protected void readDocument(XmlReader reader, ConfigWithDocument doc) {
        readDocument(reader, doc, null);
    }

    protected void readDocument(XmlReader reader, ConfigWithDocument doc, Runnable subElementsHandler) {

        doc.setTitle(reader.getAttribute(TITLE));
        doc.setSummary(reader.getAttribute(SUMMARY));
        doc.setDescription(reader.getAttribute(DESC));

        reader.loopInsideElement(() -> {

            if(reader.isStartElement(TITLE)) {
                doc.setTitle(reader.getElementTextAndEnd());
                return;
            }

            if(reader.isStartElement(SUMMARY)) {
                doc.setSummary(reader.getElementTextAndEnd());
                return;
            }

            if(reader.isStartElement(DESC)) {
                doc.setDescription(reader.getElementTextAndEnd());
                return;
            }

            if(null != subElementsHandler) {
                subElementsHandler.run();
            }
        });

    }

    protected void readApi(AppConfigContext context, XmlReader reader) {
        ApiConfigs extensions = context.getOrCreateExtension(ApiConfigs.class);

        String name = reader.resolveRequiredAttribute(NAME);
        String basePath = reader.resolveAttribute(BASE_PATH);
        String basePackage = reader.resolveAttribute(BASE_PACKAGE);
        Boolean uniqueOperationId = reader.resolveBooleanAttribute(UNIQUE_OPERATION_ID);
        Boolean defaultAnonymous = reader.resolveBooleanAttribute(DEFAULT_ANONYMOUS);
        boolean restdEnabled = reader.resolveBooleanAttribute(RESTD_ENABLED, false);

        ApiConfigurator api = extensions.getApi(name);
        if (null == api) {
            reader.getRequiredAttribute(BASE_PATH);
            api = new DefaultApiConfig(name, basePath,reader.getSource());
            api.setBasePackage(basePackage);
            extensions.addApi(api);
        }

        if (null != defaultAnonymous) {
            api.setDefaultAnonymous(defaultAnonymous);
        }

        if (null != uniqueOperationId) {
            api.setUniqueOperationId(uniqueOperationId);
        }

        if (restdEnabled && null == api.getRestdConfig()) {
            api.setRestdConfig(new RestdConfig());
        }

        if (null != api.getRestdConfig()) {
            api.getRestdConfig().setDataSourceName(reader.resolveAttribute(RESTD_DATA_SOURCE));
        }

        readApi(context, reader, api);

        addWebModule(context, api);
    }

    protected void readApi(AppConfigContext context, XmlReader reader, ApiConfigurator api) {

        context.setAttribute(ApiConfigurator.class.getName(), api);

        try {
            while (reader.nextWhileNotEnd(API)) {
                if (context.getProcessors().handleXmlElement(context, reader, NAMESPACE_URI)) {
                    continue;
                }

                if (reader.isStartElement(VERSION)) {
                    String v = reader.getElementTextAndEnd();
                    if (!Strings.isEmpty(v)) {
                        api.setVersion(v);
                    }
                    continue;
                }

                if (reader.isStartElement(TITLE)) {
                    String title = reader.getElementTextAndEnd();
                    if (!Strings.isEmpty(title)) {
                        api.setTitle(title);
                    }
                    continue;
                }

                if (reader.isStartElement(SUMMARY)) {
                    String summary = reader.getElementTextAndEnd();
                    if (!Strings.isEmpty(summary)) {
                        api.setSummary(summary);
                    }
                    continue;
                }

                if (reader.isStartElement(DESC)) {
                    String desc = reader.getElementTextAndEnd();
                    if (!Strings.isEmpty(desc)) {
                        api.setDescription(desc);
                    }
                    continue;
                }

                if (reader.isStartElement(PRODUCES)) {
                    String s = reader.getElementTextAndEnd();
                    if (!Strings.isEmpty(s)) {
                        api.setProduces(Strings.splitMultiLines(s));
                    }
                    continue;
                }

                if (reader.isStartElement(CONSUMES)) {
                    String s = reader.getElementTextAndEnd();
                    if (!Strings.isEmpty(s)) {
                        api.setConsumes(Strings.splitMultiLines(s));
                    }
                    continue;
                }

                if (reader.isStartElement(PROTOCOLS)) {
                    String s = reader.getElementTextAndEnd();
                    if (!Strings.isEmpty(s)) {
                        api.setProtocols(Strings.splitMultiLines(s));
                    }
                    continue;
                }

                if (reader.isStartElement(RESPONSES)) {
                    //todo : override exists responses.
                    readCommonResponses(reader).forEach(api::putCommonResponseBuilder);
                    continue;
                }

                if (reader.isStartElement(MODELS)) {
                    readModels(context, reader, api::addModel);
                    continue;
                }

                if (reader.isStartElement(MAX_PAGE_SIZE)) {
                    Integer i = reader.getIntegerElementTextAndEnd();
                    if (null != i) {
                        api.setMaxPageSize(i);
                    }
                    continue;
                }

                if (reader.isStartElement(DEFAULT_PAGE_SIZE)) {
                    Integer i = reader.getIntegerElementTextAndEnd();
                    if (null != i) {
                        api.setDefaultPageSize(i);
                    }
                    continue;
                }

                if (reader.isStartElement(PERMISSIONS)) {
                    readPermissions(context, reader, api);
                    continue;
                }

                if (reader.isStartElement(RESOURCE_PERMISSIONS)) {
                    readResourcePermissions(context, reader, api);
                    continue;
                }

                if (reader.isStartElement(OAUTH)) {
                    api.setOAuthConfig(readOAuth(context, reader));
                    continue;
                }

                if (reader.isStartElement(RESTD)) {
                    readRestd(context, api, reader);
                    continue;
                }
            }
        } finally {
            context.removeAttribute(ApiConfigurator.class.getName());
        }
    }

    protected void addWebModule(AppConfigContext context, ApiConfigurator api) {
        ApiConfig apiConf = api.config();
        String basePackage = apiConf.getBasePackage();
        if (Strings.isNotEmpty(basePackage)) {
            context.getAdditionalPackages().add(basePackage);

            DefaultModuleConfig module = new DefaultModuleConfig();
            module.setName(apiConf.getName());
            module.setBasePath(apiConf.getBasePath());
            module.setBasePackage(apiConf.getBasePackage());
            ModuleConfigExtension extension = context.getOrCreateExtension(ModuleConfigExtension.class);
            extension.addModule(module);
        }

    }

    protected void readPermissions(AppConfigContext context, XmlReader reader, ApiConfigurator api) {
        StringBuilder chars = new StringBuilder();
        boolean hasElement = false;

        while (reader.nextWhileNotEnd(PERMISSIONS)) {
            if (!hasElement && reader.isCharacters()) {
                chars.append(reader.getCharacters());
                continue;
            }

            if (reader.isStartElement(PERMISSION)) {
                hasElement = true;

                String value = reader.getRequiredAttribute(VALUE);
                String desc = reader.getAttribute(DESC);

                if (Strings.isEmpty(desc)) {
                    desc = reader.getElementTextAndEnd();
                }

                api.setPermission(new MApiPermission(value, desc));
                continue;
            }
        }

        if (!hasElement) {
            String text = chars.toString().trim();
            if (text.length() > 0) {
                ExProperties props = Props.loadKeyValues(text);

                props.forEach((k, v) -> {
                    String value = (String) k;
                    String desc = (String) v;

                    api.setPermission(new MApiPermission(value, desc));
                });
            }
        }
    }

    protected void readResourcePermissions(AppConfigContext context, XmlReader reader, ApiConfigurator api) {
        ResourcePermissions rps = new ResourcePermissions();

        reader.loopInsideElement(() -> {

            if (reader.isStartElement(RESOURCE)) {
                String className = reader.getRequiredAttribute(CLASS);

                rps.addResourceClass(className);

                return;
            }

            if (reader.isStartElement(RESOURCES)) {

                String packageName = reader.getRequiredAttribute(PACKAGE);

                rps.addResourcePackage(packageName);

                return;
            }

            if (reader.isStartElement(PERMISSION)) {

                ResourcePermission rp = new ResourcePermission();

                rp.setValue(reader.getRequiredAttribute(VALUE));
                rp.setDescription(reader.getAttribute(DESC));
                rp.setDefault(reader.getBooleanAttribute(DEFAULT, false));

                String httpMethods = reader.getAttribute(HTTP_METHODS);
                if (!Strings.isEmpty(httpMethods)) {
                    //todo :
                }

                String pathPattern = reader.getAttribute(PATH_PATTERN);
                if (!Strings.isEmpty(pathPattern)) {
                    //todo :
                }

                rps.addPermission(rp);
                return;
            }
        });

        if (null == rps.getDefaultPermission() && rps.getPermissions().size() == 1) {
            ResourcePermission rp = rps.getPermissions().iterator().next();

            if (null == rp.getHttpMethods() && null == rp.getPathPattern()) {
                rps.setDefaultPermission(rp);
            }
        }

        api.config().getResourcePermissionsSet().addResourcePermissions(rps);
    }

    protected OAuthConfigImpl readOAuth(AppConfigContext context, XmlReader reader) {
        Boolean enabled = reader.resolveBooleanAttribute(ENABLED);
        String  flow    = reader.resolveAttribute(FLOW, SwaggerConstants.IMPLICIT);

        OAuthConfigImpl oauth = new OAuthConfigImpl(enabled, flow, null, null);

        reader.loopInsideElement(() -> {
            if (reader.isStartElement(AUTHZ_URL)) {
                String url = reader.resolveElementTextAndEnd();
                if (!Strings.isEmpty(url)) {
                    oauth.setAuthorizationUrl(url);
                }
                return;
            }

            if (reader.isStartElement(TOKEN_URL)) {
                String url = reader.resolveElementTextAndEnd();
                if (!Strings.isEmpty(url)) {
                    oauth.setTokenUrl(url);
                }
                return;
            }
        });

        return oauth;
    }

    private void readRestd(AppConfigContext context, ApiConfigurator api, XmlReader reader) {
        boolean enabled = reader.resolveBooleanAttribute(ENABLED, true);
        if (!enabled) {
            api.setRestdConfig(null);
            return;
        }

        RestdConfig c = api.getRestdConfig();
        if (null == c) {
            c = new RestdConfig();
            api.setRestdConfig(c);
        }

        String dataSourceName = reader.resolveAttribute(DATA_SOURCE);
        if (!Strings.isEmpty(dataSourceName)) {
            c.setDataSourceName(dataSourceName);
        }

        c.setReadonly(reader.resolveBooleanAttribute(READONLY, false));

        final RestdConfig rc = c;

        reader.loopInsideElement(() -> {
            //included models
            if (reader.isStartElement(INCLUDED_MODELS)) {
                Collections2.addAll(rc.getIncludedModels(), Strings.splitMultiLines(reader.getElementTextAndEnd(), ','));
                return;
            }

            //excluded models
            if (reader.isStartElement(EXCLUDED_MODELS)) {
                Collections2.addAll(rc.getExcludedModels(), Strings.splitMultiLines(reader.getElementTextAndEnd(), ','));
                return;
            }

            //readonly models
            if (reader.isStartElement(READONLY_MODELS)) {
                Collections2.addAll(rc.getReadonlyModels(), Strings.splitMultiLines(reader.getElementTextAndEnd(), ','));
                return;
            }

            //model
            if (reader.isStartElement(MODEL)) {
                readRestdModel(context, api, reader, rc);
                return;
            }

            //sql-operation
            if (reader.isStartElement(SQL_OPERATION)) {
                rc.addSqlOperation(readSqlOperation(rc, reader));
                return;
            }
        });

    }

    private void readRestdModel(AppConfigContext context, ApiConfigurator api, XmlReader reader, RestdConfig config) {
        String name = reader.getRequiredAttribute(NAME);
        Boolean anonymous = reader.getBooleanAttribute(ANONYMOUS);
        Boolean read = reader.getBooleanAttribute(READ);
        Boolean write = reader.getBooleanAttribute(WRITE);
        Boolean create = reader.getBooleanAttribute(CREATE);
        Boolean update = reader.getBooleanAttribute(UPDATE);
        Boolean delete = reader.getBooleanAttribute(DELETE);
        Boolean find = reader.getBooleanAttribute(FIND);
        Boolean query = reader.getBooleanAttribute(QUERY);

        Model model = config.getModel(name);
        if (null == model) {
            model = new Model(name);
            config.addModel(model);
        }

        model.setAnonymous(api.config().isDefaultAnonymous());

        if (null != anonymous) {
            model.setAnonymous(anonymous);
        }

        if (config.isReadonlyModel(name)) {
            model.setFindOperationEnabled(true);
            model.setQueryOperationEnabled(true);

            model.setCreateOperationEnabled(false);
            model.setUpdateOperationEnabled(false);
            model.setDeleteOperationEnabled(false);
        }

        if (null != read) {
            model.setFindOperationEnabled(read);
            model.setQueryOperationEnabled(read);
        }

        if (null != write) {
            model.setCreateOperationEnabled(write);
            model.setUpdateOperationEnabled(write);
            model.setDeleteOperationEnabled(write);
        }

        if (null != create) {
            model.setCreateOperationEnabled(create);
        }

        if (null != update) {
            model.setUpdateOperationEnabled(update);
        }

        if (null != delete) {
            model.setDeleteOperationEnabled(delete);
        }

        if (null != find) {
            model.setFindOperationEnabled(find);
        }

        if (null != query) {
            model.setQueryOperationEnabled(query);
        }

        final RestdConfig.Model m = model;

        reader.loopInsideElement(() -> {

            if(reader.isStartElement(SQL_OPERATION)) {
                m.addSqlOperation(readSqlOperation(config, reader));
            }
        });
    }

    private RestdConfig.SqlOperation readSqlOperation(RestdConfig config, XmlReader reader) {
        RestdConfig.SqlOperation op = new RestdConfig.SqlOperation();

        op.setName(reader.getRequiredAttribute(NAME));
        op.setSqlKey(reader.getRequiredAttribute(SQL_KEY));

        return op;
    }
}
