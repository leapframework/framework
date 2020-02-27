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
import leap.web.action.ActionParams;
import leap.web.action.Argument;
import leap.web.action.ArgumentProcessor;
import leap.web.action.FuncActionBuilder;
import leap.web.api.Api;
import leap.web.api.config.ApiConfigurator;
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

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Create a new record operation.
 */
public class CreateOperation extends CrudOperationBase implements CrudOperation {

    protected static final String NAME = "create";

    @Override
    public void createCrudOperation(ApiConfigurator c, RestdContext context, RestdModel model) {
        if (!context.getConfig().allowCreateModel(model.getName())) {
            return;
        }

        String path = fullModelPath(c, model) + getPathSuffix(model);
        String name = Strings.lowerCamel(NAME, model.getName());

        createCrudOperation(c, context, model, path, name, null);
    }

    protected String getPathSuffix(RestdModel model) {
        return "";
    }

    public void createCrudOperation(ApiConfigurator c, RestdContext context, RestdModel model,
                                    String path, String name, Callback callback) {

        FuncActionBuilder action = new FuncActionBuilder(name);
        RouteBuilder      route  = rm.createRoute("POST", path);

        if (null != callback) {
            callback.preAddArguments(action);
        }

        action.setFunction(createFunction(context, model));
        addPathArguments(context, model, path, action);
        addModelArgumentForCreate(context, action, model);
        if (null != callback) {
            callback.postAddArguments(action);
        }
        addModelResponse(action, model).setStatus(201);

        preConfigure(context, model, action);
        route.setAction(action.build());
        setCrudOperation(route, NAME);

        postConfigure(context, model, route);

        if (isOperationExists(context, route)) {
            return;
        }

        c.addDynamicRoute(rm.loadRoute(context.getRoutes(), route));
    }

    protected Function<ActionParams, Object> createFunction(RestdContext context, RestdModel model) {
        return new CreateFunction(context.getApi(), context.getDao(), model);
    }

    protected class CreateFunction extends CrudFunction {
        public CreateFunction(Api api, Dao dao, RestdModel model) {
            super(api, dao, model);
        }

        @Override
        public Object apply(ActionParams params) {
            MApiModel am = api.getMetadata().getModel(model.getName());

            ModelExecutorContext context  = new SimpleModelExecutorContext(api, dao, am, em, params);
            ModelCreateExecutor  executor = newCreateExecutor(context);

            final Map<String, Object> record = doGetRecord(params);

            CreateOneResult result = doCreateRecord(params, executor, record);
            return ApiResponse.created(result.entity);
        }

        protected Map<String, Object> doGetRecord(ActionParams params) {
            return getModelRecord(params);
        }

        protected CreateOneResult doCreateRecord(ActionParams params, ModelCreateExecutor executor, Map<String, Object> record) {
            return executor.createOne(record);
        }

        protected ModelCreateExecutor newCreateExecutor(ModelExecutorContext context) {
            return mef.newCreateExecutor(context);
        }

        @Override
        public String toString() {
            return "Function:" + "Create " + model.getName() + "";
        }
    }
}
