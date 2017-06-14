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
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.json.JsonSettings;
import leap.lang.path.Paths;
import leap.web.App;
import leap.web.action.ArgumentBuilder;
import leap.web.action.FuncActionBuilder;
import leap.web.annotation.NonParam;
import leap.web.annotation.ParamsWrapper;
import leap.web.api.Apis;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.config.model.RestdConfig;
import leap.web.api.mvc.ApiFailureHandler;
import leap.web.route.Route;
import leap.web.route.RouteBuilder;
import leap.web.route.RouteManager;

public abstract class RestdOperationBase {

    protected @Inject RouteManager      rm;
    protected @Inject Apis              apis;
    protected @Inject ValidationManager validationManager;
    protected @Inject ApiFailureHandler failureHandler;

    protected boolean isOperationExists(App app, String verb, String path) {
        for(Route route : app.routes()) {
            if(verb.equalsIgnoreCase(route.getMethod()) &&
                    route.getPathTemplate().getTemplate().equals(path)) {
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

    protected ArgumentBuilder addArgument(FuncActionBuilder action, Class<?> type, String name) {
        return addArgument(action, type, name, null);
    }

    protected ArgumentBuilder addArgument(FuncActionBuilder action, Class<?> type, String name, Boolean required) {
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

        action.addArgument(a);

        return a;
    }

    protected void configure(RestdContext context, RestdModel model, RouteBuilder route) {
        RestdConfig c = context.getConfig();

        if(null != model) {
            if (c.isModelAnonymous(model.getName())) {
                route.setAllowAnonymous(true);
            }
        }

        route.setAllowClientOnly(true);
        route.addFailureHandler(failureHandler);

        JsonSettings settings = new JsonSettings.Builder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").build();
        route.setExtension(settings);
    }

}
