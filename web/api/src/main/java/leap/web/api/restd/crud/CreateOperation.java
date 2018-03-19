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
import leap.core.value.Record;
import leap.lang.Arrays2;
import leap.lang.Strings;
import leap.orm.dao.Dao;
import leap.web.action.ActionParams;
import leap.web.action.FuncActionBuilder;
import leap.web.api.Api;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.orm.CreateOneResult;
import leap.web.api.orm.ModelCreateExecutor;
import leap.web.api.orm.ModelExecutorContext;
import leap.web.api.orm.SimpleModelExecutorContext;
import leap.web.api.restd.CrudOperation;
import leap.web.api.restd.CrudOperationBase;
import leap.web.api.restd.RestdContext;
import leap.web.api.restd.RestdModel;
import leap.web.route.RouteBuilder;

import java.util.Map;
import java.util.function.Function;

/**
 * Create a new record operation.
 */
public class CreateOperation extends CrudOperationBase implements CrudOperation {

    @Inject
    private RestdValidator[] validators;

    @Override
    public void createCrudOperation(ApiConfigurator c, RestdContext context, RestdModel model) {
        if(!context.getConfig().allowCreateModel(model.getName())) {
            return;
        }

        String verb = "POST";
        String path = fullModelPath(c, model);

        Dao               dao    = context.getDao();
        FuncActionBuilder action = new FuncActionBuilder();
        RouteBuilder      route  = rm.createRoute(verb, path);

        if(isOperationExists(context, route)) {
            return;
        }

        action.setName(Strings.lowerCamel("create", model.getName()));
        action.setFunction(new CreateFunction(context.getApi(), dao, model));
        addModelArgumentForCreate(context, action, model);
        addModelResponse(action, model).setStatus(201);

        preConfigure(context, model, action);
        route.setAction(action.build());
        setCrudOperation(route, "create");

        postConfigure(context, model, route);

        if(isOperationExists(context, route)) {
            return;
        }

        c.addDynamicRoute(rm.loadRoute(context.getRoutes(), route));
    }

    private final class CreateFunction implements Function<ActionParams, Object> {
        private final Api        api;
        private final Dao        dao;
        private final RestdModel model;

        public CreateFunction(Api api, Dao dao, RestdModel model) {
            this.api = api;
            this.dao = dao;
            this.model = model;
        }

        @Override
        public Object apply(ActionParams params) {
            ApiMetadata amd = api.getMetadata();
            MApiModel   am  = amd.getModel(model.getName());

            Map<String,Object> record = params.get(0);

            ModelExecutorContext context = new SimpleModelExecutorContext(api, am, dao, model.getEntityMapping());
            ModelCreateExecutor executor = mef.newCreateExecutor(context);

            if(Arrays2.isNotEmpty(validators)) {
                for (RestdValidator validator : validators) {
                    validator.validate(model.getName(), record);
                }
            }

            CreateOneResult result = executor.createOne(record);

            Record r = dao.find(model.getEntityMapping(), result.id);
            r.put("$id", result.id);

            return ApiResponse.created(r);
        }

        @Override
        public String toString() {
            return "Function:" + "Create " + model.getName() + "";
        }
    }

}
