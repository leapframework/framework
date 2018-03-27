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

import leap.core.annotation.Inject;
import leap.core.validation.ValidationManager;
import leap.lang.Strings;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.json.JsonSettings;
import leap.lang.path.Paths;
import leap.web.action.ArgumentBuilder;
import leap.web.action.FuncActionBuilder;
import leap.web.annotation.NonParam;
import leap.web.annotation.ParamsWrapper;
import leap.web.api.Apis;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.config.model.RestdConfig;
import leap.web.api.meta.model.MApiOperationBuilder;
import leap.web.api.meta.model.MApiSecurity;
import leap.web.api.meta.model.MApiTag;
import leap.web.api.mvc.ApiFailureHandler;
import leap.web.api.route.ApiRoute;
import leap.web.api.spec.swagger.SwaggerConstants;
import leap.web.format.RequestFormat;
import leap.web.format.ResponseFormat;
import leap.web.route.Route;
import leap.web.route.RouteBuilder;
import leap.web.route.RouteManager;

public abstract class RestdOperationBase {

    protected @Inject RouteManager            rm;
    protected @Inject Apis                    apis;
    protected @Inject ValidationManager       validationManager;
    protected @Inject ApiFailureHandler       failureHandler;
    protected @Inject RestdOperationSupport[] operationSupports;
    protected @Inject RestdArgumentSupport[]  argumentSupports;
    protected @Inject RequestFormat[]         supportedConsumes;
    protected @Inject ResponseFormat[]        supportedProduces;

    protected boolean isOperationExists(RestdContext context, RouteBuilder route) {
        for(ApiRoute ar : context.getApiConfig().getApiRoutes()) {
            Route route1 = ar.getRoute();

            if(route.getMethod().equalsIgnoreCase(route1.getMethod()) &&
                    route.getPathTemplate().getTemplate().equals(route1.getPathTemplate().getTemplate())) {

                route.setAction(route1.getAction());
                route.setEnabled(false);
                return true;
            }
        }

        for(RestdOperationSupport support : operationSupports) {
            if(support.isOperationExists(context, route)) {
                if(null == route.getEnabled()) {
                    route.setEnabled(false);
                }
                return true;
            }
        }

        return false;
    }

    protected String fullModelPath(ApiConfigurator api, RestdModel model) {
        String basePath = api.config().getBasePath();
        return basePath.equals("/") ? model.getPath() : basePath + model.getPath();
    }

    protected String fullModelPath(ApiConfigurator api, RestdModel model, String path) {
        String modelPath = fullModelPath(api, model);
        return Paths.suffixWithoutSlash(modelPath) + path;
    }

    protected String fullPath(ApiConfigurator api, String path) {
        String basePath = api.config().getBasePath();
        return basePath.equals("/") ? path : basePath + path;
    }

    protected ArgumentBuilder addArgument(RestdContext context, FuncActionBuilder action, Class<?> type, String name) {
        return addArgument(context, action, type, name, null);
    }

    protected ArgumentBuilder addArgument(RestdContext context, FuncActionBuilder action, Class<?> type, String name, Boolean required) {
        ArgumentBuilder a = new ArgumentBuilder();

        a.setName(name);
        a.setType(type);
        a.setRequired(required);

        if(type.isAnnotationPresent(ParamsWrapper.class)) {
            BeanType bt = BeanType.of(type);
            for (BeanProperty bp : bt.getProperties()) {
                if (bp.isField() && !bp.isAnnotationPresent(NonParam.class)) {
                    ArgumentBuilder wrapped = new ArgumentBuilder(validationManager, bp);
                    a.addWrappedArgument(wrapped);
                }
            }
        }

        for(RestdArgumentSupport vs : argumentSupports) {
            vs.processArgument(context, a);
        }

        action.addArgument(a);

        return a;
    }

    protected void preConfigure(RestdContext context, RestdModel model, FuncActionBuilder action) {
        RestdConfig.Model m = context.getConfig().getModel(model.getName());
        if(null != m && !Strings.isEmpty(m.getTitle())) {
            action.setExtension(new MApiTag[]{new MApiTag(model.getName(), m.getTitle())});
        }else {
            action.setExtension(new MApiTag[]{new MApiTag(model.getName())});
        }
    }

    protected void postConfigure(RestdContext context, RestdModel model, RouteBuilder route) {
        postConfigure(context, model, route, null);
    }

    protected void preConfigure(RestdContext context, RouteBuilder route, FuncActionBuilder action, MApiOperationBuilder mo) {
        if(null == mo) {
            return;
        }

        for(String consume : mo.getConsumes()) {
            for(RequestFormat format : supportedConsumes) {
                if(null != format.getPrimaryMimeType() && format.getPrimaryMimeType().getMediaType().equalsIgnoreCase(consume)) {
                    action.addConsume(format);
                    break;
                }
            }
        }

        for(String produce : mo.getProduces()) {
            for(ResponseFormat format : supportedProduces) {
                if(null != format.getPrimaryMimeType() && format.getPrimaryMimeType().getMediaType().equalsIgnoreCase(produce)) {
                    action.addProduce(format);
                    break;
                }
            }
        }
    }

    protected void postConfigure(RestdContext context, RestdModel model, RouteBuilder route, MApiOperationBuilder mo) {
        RestdConfig c = context.getConfig();

        if(null != model) {
            if (c.isModelAnonymous(model.getName())) {
                route.setAllowAnonymous(true);
            }
        }

        route.addFailureHandler(failureHandler);

        JsonSettings settings = new JsonSettings.Builder().setDateTimeFormatter(SwaggerConstants.DATE_TIME_FORMAT, "GMT").build();
        route.setExtension(settings);

        if(null != mo) {
            if(mo.isAllowAnonymous()) {
                route.setAllowAnonymous(true);
            }

            if(mo.isAllowClientOnly()) {
                route.setAllowClientOnly(true);
            }

            if(null != mo.getSecurity()) {
                for(MApiSecurity security : mo.getSecurity().values()) {
                    if(null != security.getScopes() && !security.getScopes().isEmpty()) {
                        route.setPermissions(security.getScopes().toArray(new String[0]));
                        break;
                    }
                }
            }
        }
    }

}
