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

package leap.web.api.restd.crud;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.meta.MComplexTypeRef;
import leap.web.action.Argument;
import leap.web.action.ArgumentBuilder;
import leap.web.action.FuncActionBuilder;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.config.model.RestdConfig;
import leap.web.api.meta.model.MApiParameter;
import leap.web.api.meta.model.MApiParameterBuilder;
import leap.web.api.meta.model.MApiResponseBuilder;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.restd.RestdApiCreator;
import leap.web.api.restd.RestdApiConfigContext;
import leap.web.api.restd.RestdModel;
import leap.web.route.RouteBuilder;
import leap.web.route.RouteManager;

import java.util.Map;

public abstract class CRUDOperationCreatorBase implements RestdApiCreator {

    protected @Inject RouteManager rm;

    protected String fullModelPath(ApiConfigurator api, RestdModel model) {
        String basePath = api.config().getBasePath();
        return basePath.equals("/") ? model.getPath() : basePath + model.getPath();
    }

    protected ArgumentBuilder addModelArgument(FuncActionBuilder action,RestdModel model) {
        ArgumentBuilder a = newModelArgument(model);

        action.addArgument(a);

        return a;
    }

    protected MApiResponseBuilder addModelResponse(FuncActionBuilder action, RestdModel model) {
        action.setReturnType(ApiResponse.class);

        MApiResponseBuilder r = newModelResponse(model);

        action.setExtension(r);

        return r;
    }

    protected MApiResponseBuilder newModelResponse(RestdModel model) {
        MApiResponseBuilder r = new MApiResponseBuilder();

        r.setStatus(200);
        r.setType(new MComplexTypeRef(model.getName()));

        return r;
    }

    protected ArgumentBuilder newModelArgument(RestdModel model) {
        ArgumentBuilder a = new ArgumentBuilder();

        a.setName(model.getName());
        a.setLocation(Argument.Location.REQUEST_BODY);
        a.setType(Map.class);
        a.setRequired(true);
        a.setExtension(newModelParameter(model));

        return a;
    }

    protected MApiParameterBuilder newModelParameter(RestdModel model) {

        MApiParameterBuilder p = new MApiParameterBuilder();
        p.setName(Strings.lowerCamel(model.getName()));
        p.setLocation(MApiParameter.Location.BODY);
        p.setRequired(true);
        p.setType(new MComplexTypeRef(model.getName()));

        return p;
    }

    protected void configRoute(RestdApiConfigContext context, RouteBuilder route) {
        RestdConfig c = context.getConfig();

        if(c.isAnonymous()) {
            route.setAllowAnonymous(true);
        }
    }

}