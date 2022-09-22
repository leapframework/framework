/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.api.restd;

import java.time.ZoneId;
import leap.core.annotation.Inject;
import leap.core.validation.ValidationManager;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.json.JsonSettings;
import leap.lang.meta.MType;
import leap.lang.path.Paths;
import leap.orm.mapping.EntityMapping;
import leap.web.action.Argument;
import leap.web.action.ArgumentBuilder;
import leap.web.action.FuncActionBuilder;
import leap.web.annotation.NonParam;
import leap.web.annotation.ParamsWrapper;
import leap.web.api.Apis;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.config.model.RestdConfig;
import leap.web.api.meta.model.MApiOperationBuilder;
import leap.web.api.meta.model.MApiParameter;
import leap.web.api.meta.model.MApiParameterBuilder;
import leap.web.api.meta.model.MApiTag;
import leap.web.api.route.ApiRoute;
import leap.web.api.spec.swagger.SwaggerConstants;
import leap.web.format.RequestFormat;
import leap.web.format.ResponseFormat;
import leap.web.json.JsonConfig;
import leap.web.route.Route;
import leap.web.route.RouteBuilder;
import leap.web.route.RouteManager;
import java.lang.reflect.Array;
import java.util.Map;

public abstract class RestdOperationBase {

    protected @Inject               RouteManager            rm;
    protected @Inject               Apis                    apis;
    protected @Inject               ValidationManager       validationManager;
    protected @Inject               RestdOperationSupport[] operationSupports;
    protected @Inject               RestdArgumentSupport[]  argumentSupports;
    protected @Inject               RequestFormat[]         supportedConsumes;
    protected @Inject               ResponseFormat[]        supportedProduces;
    protected @Inject               JsonConfig              jsonConfig;
    protected @Inject(name = "api") JsonSettings            apiJsonSettings;

    protected boolean isOperationExists(RestdContext context, RouteBuilder route) {
        for (ApiRoute ar : context.getApiConfig().getApiRoutes()) {
            Route route1 = ar.getRoute();

            if (route.getMethod().equalsIgnoreCase(route1.getMethod()) &&
                    route.getPathTemplate().getTemplate().equals(route1.getPathTemplate().getTemplate())) {

                route.setAction(route1.getAction());
                route.setEnabled(false);
                return true;
            }
        }

        for (RestdOperationSupport support : operationSupports) {
            if (support.isOperationExists(context, route)) {
                if (null == route.getEnabled()) {
                    route.setEnabled(false);
                }
                return route.isDisabledExplicitly();
            }
        }

        return false;
    }

    public static String fullModelPath(ApiConfigurator api, RestdModel model) {
        String basePath = api.config().getBasePath();
        return basePath.equals("/") ? model.getPath() : basePath + model.getPath();
    }

    public static String fullModelPath(ApiConfigurator api, RestdModel model, String path) {
        String modelPath = fullModelPath(api, model);
        return Paths.suffixWithoutSlash(modelPath) + path;
    }

    public static String fullPath(ApiConfigurator api, String path) {
        String basePath = api.config().getBasePath();
        return basePath.equals("/") ? path : basePath + path;
    }

    public static String fullModelPath(RestdContext context, RestdModel model) {
        return fullModelPath(context.getApi().getConfigurator(), model);
    }

    public static String fullModelPath(RestdContext context, RestdModel model, String path) {
        return fullModelPath(context.getApi().getConfigurator(), model, path);
    }

    public static String fullPath(RestdContext context, String path) {
        return fullPath(context.getApi().getConfigurator(), path);
    }

    protected ArgumentBuilder addArgument(RestdContext context, FuncActionBuilder action, Class<?> type, String name) {
        return addArgument(context, action, type, name, null, null);
    }

    protected ArgumentBuilder addArgument(RestdContext context, FuncActionBuilder action, Class<?> type, String name, Boolean required) {
        return addArgument(context, action, type, name, required, null);
    }

    protected ArgumentBuilder addArgument(RestdContext context, FuncActionBuilder action, Class<?> type, String name,
                                          Boolean required, Argument.Location location) {
        ArgumentBuilder a = new ArgumentBuilder();

        a.setName(name);
        a.setType(type);
        a.setRequired(required);

        if (null != location) {
            a.setLocation(location);
        }

        if (Classes.isAnnotationPresent(type, ParamsWrapper.class)) {
            BeanType bt = BeanType.of(type);
            for (BeanProperty bp : bt.getProperties()) {
                if (bp.isField() && !bp.isAnnotationPresent(NonParam.class)) {
                    ArgumentBuilder wrapped = new ArgumentBuilder(validationManager, bp);
                    a.addWrappedArgument(wrapped);
                }
            }
        }

        for (RestdArgumentSupport vs : argumentSupports) {
            vs.processArgument(context, a);
        }

        action.addArgument(a);

        return a;
    }

    protected void preConfigure(RestdContext context, RestdModel model, FuncActionBuilder action) {
        RestdConfig.Model m = context.getConfig().getModel(model.getName());
        if (null != m && !Strings.isEmpty(m.getTitle())) {
            action.setExtension(new MApiTag[]{new MApiTag(model.getName(), m.getTitle())});
        } else {
            action.setExtension(new MApiTag[]{new MApiTag(model.getName())});
        }
    }

    protected void postConfigure(RestdContext context, RestdModel model, RouteBuilder route) {
        route.setCsrfEnabled(false);
        route.setExtension(EntityMapping.class, model.getEntityMapping());
        postConfigure(context, model, route, null);
    }

    protected void preConfigure(RestdContext context, RouteBuilder route, FuncActionBuilder action, MApiOperationBuilder mo) {
        if (null == mo) {
            return;
        }

        for (String consume : mo.getConsumes()) {
            for (RequestFormat format : supportedConsumes) {
                if (null != format.getPrimaryMimeType() && format.getPrimaryMimeType().getMediaType().equalsIgnoreCase(consume)) {
                    action.addConsume(format);
                    break;
                }
            }
        }

        for (String produce : mo.getProduces()) {
            for (ResponseFormat format : supportedProduces) {
                if (null != format.getPrimaryMimeType() && format.getPrimaryMimeType().getMediaType().equalsIgnoreCase(produce)) {
                    action.addProduce(format);
                    break;
                }
            }
        }
    }

    protected void postConfigure(RestdContext context, RestdModel model, RouteBuilder route, MApiOperationBuilder mo) {
        RestdConfig c = context.getConfig();

        if (null != model) {
            if (c.isModelAnonymous(model.getName())) {
                route.setAllowAnonymous(true);
            }
        }

        route.addFailureHandler(context.getApiConfig().getFailureHandler());

        JsonSettings settings = apiJsonSettings;
        if (null == settings && c.isDateFormatEnabled()) {
            String pattern = Strings.isEmpty(c.getDateFormatPattern()) ? SwaggerConstants.DATE_TIME_FORMAT : c.getDateFormatPattern();
            settings = new JsonSettings.Builder()
                    .setDateTimeFormatter(pattern, ZoneId.systemDefault().getId())
                    .setHtmlEscape(jsonConfig.isHtmlEscape())
                    .build();
        }
        if (null != settings) {
            route.setExtension(settings);
        }

        if (null != mo) {
            if (mo.isAllowAnonymous()) {
                route.setAllowAnonymous(true);
            }

            if (mo.isAllowClientOnly()) {
                route.setAllowClientOnly(true);
            }

            route.setPermissions(mo.getPermissions());
            route.setSecurities(mo.getSecurities());
        }
    }

    protected void createArguments(RouteBuilder route, FuncActionBuilder action, MApiOperationBuilder mo) {
        for (MApiParameterBuilder p : mo.getParameters()) {
            addArgument(route, action, p);
        }
    }

    protected void addArgument(RouteBuilder route, FuncActionBuilder action, MApiParameterBuilder p) {
        ArgumentBuilder arg = new ArgumentBuilder();
        arg.setName(p.getName());
        arg.setRequired(p.getRequired());

        //validators
        if (null != p.getValidators() && !p.getValidators().isEmpty()) {
            p.getValidators().forEach(arg::addValidator);
        }

        //type
        resolveArgumentType(route, p, arg);

        //location.
        resolveArgumentLocation(route, p, arg);

        action.addArgument(arg);
    }

    protected void resolveArgumentType(RouteBuilder route, MApiParameterBuilder p, ArgumentBuilder arg) {
        MType type = p.getType();
        if (type.isComplexType() || type.isTypeRef()) {
            arg.setType(Map.class);
            return;
        }

        if (type.isSimpleType()) {
            arg.setType(type.asSimpleType().getJavaType());
            return;
        }

        if (type.isObjectType()) {
            arg.setType(Object.class);
            return;
        }

        if (type.isDictionaryType()) {
            arg.setType(Map.class);
            return;
        }

        if (type.isCollectionType()) {
            MType elementType = type.asCollectionType().getElementType();
            if (elementType.isSimpleType()) {
                arg.setType(Array.newInstance(elementType.asSimpleType().getJavaType(), 0).getClass());
            } else if (elementType.isObjectType()) {
                arg.setType(Object[].class);
            } else {
                arg.setType(Map[].class);
            }
            return;
        }

        throw new IllegalStateException("Unsupported type '" + type + "' at parameter '" + p.getName() + "'");
    }

    protected void resolveArgumentLocation(RouteBuilder route, MApiParameterBuilder p, ArgumentBuilder arg) {
        setDefaultLocation(route, p);

        if (p.getLocation() == MApiParameter.Location.BODY) {
            arg.setLocation(Argument.Location.REQUEST_BODY);
            return;
        }

        if (p.getLocation() == MApiParameter.Location.QUERY) {
            arg.setLocation(Argument.Location.QUERY_PARAM);
            return;
        }

        if (p.getLocation() == MApiParameter.Location.FORM) {
            arg.setLocation(Argument.Location.REQUEST_PARAM);
            return;
        }

        if (p.getLocation() == MApiParameter.Location.PATH) {
            arg.setLocation(Argument.Location.PATH_PARAM);
            return;
        }

        if (p.getLocation() == MApiParameter.Location.HEADER) {
            arg.setLocation(Argument.Location.HEADER_PARAM);
            return;
        }

        throw new IllegalStateException("Location '" + p.getLocation() + "' not implemented!");
    }

    protected boolean setDefaultLocation(RouteBuilder route, MApiParameterBuilder p) {
        if (null == p.getLocation()) {
            if (route.getPathTemplate().getTemplateVariables().contains(p.getName())) {
                p.setLocation(MApiParameter.Location.PATH);
                return true;
            }

            if (p.getType().isComplexType() || p.getType().isTypeRef() || p.getType().isDictionaryType()) {
                p.setLocation(MApiParameter.Location.BODY);
                return true;
            }

            p.setLocation(MApiParameter.Location.QUERY);
            return true;
        }
        return false;
    }

}
