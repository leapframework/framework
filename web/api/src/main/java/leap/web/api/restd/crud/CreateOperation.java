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

import leap.lang.Strings;
import leap.orm.dao.Dao;
import leap.web.App;
import leap.web.action.ActionParams;
import leap.web.action.FuncActionBuilder;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.orm.*;
import leap.web.api.restd.RestdContext;
import leap.web.api.restd.RestdModel;
import leap.web.api.restd.RestdProcessor;
import leap.web.route.RouteBuilder;

import java.util.Map;

/**
 * Create a new record operation.
 */
public class CreateOperation extends CrudOperation implements RestdProcessor {

    @Override
    public void preProcessModel(App app, ApiConfigurator api, RestdContext context, RestdModel model) {
        if(!context.getConfig().allowCreateModel(model.getName())) {
            return;
        }

        String verb = "POST";
        String path = fullModelPath(api, model);
        if(isOperationExists(app, verb, path)) {
            return;
        }

        Dao               dao    = context.getDao();
        FuncActionBuilder action = new FuncActionBuilder();
        RouteBuilder      route  = rm.createRoute(verb, path);

        action.setName(Strings.lowerCamel("create", model.getName()));
        action.setFunction((params) -> execute(api.config(), dao, model, params));
        addModelArgument(action, model);
        addModelResponse(action, model).setStatus(201);

        configure(context, model, action);
        route.setAction(action.build());

        configure(context, model, route);
        api.addRoute(rm.loadRoute(app.routes(), route));
    }

    protected Object execute(ApiConfig ac, Dao dao, RestdModel model, ActionParams params) {
        ApiMetadata amd = apis.tryGetMetadata(ac.getName());
        MApiModel   am  = amd.getModel(model.getName());

        Map<String,Object> record = params.get(0);

        ModelExecutorContext context = new SimpleModelExecutorContext(ac, amd, am, dao, model.getEntityMapping());
        ModelCreateExecutor executor = mef.newCreateExecutor(context);

        CreateOneResult result = executor.createOne(record);

        return ApiResponse.created(dao.find(model.getEntityMapping(), result.id));
    }

}
