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
import leap.web.action.ActionParams;
import leap.web.action.FuncActionBuilder;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.orm.*;
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

    protected static final String NAME = "create";

    @Override
    public void createCrudOperation(ApiConfigurator c, RestdContext context, RestdModel model) {
        if (!context.getConfig().allowCreateModel(model.getName())) {
            return;
        }

        String path = fullModelPath(c, model) + getPathSuffix(model);
        String name = Strings.lowerCamel(NAME, model.getName());

        createCrudOperation(context, model, path, name, null);
    }

    protected String getPathSuffix(RestdModel model) {
        return "";
    }

    public void createCrudOperation(RestdContext context, RestdModel model,
                                    String path, String name, Callback callback) {

        final Crud crud = Crud.of(context, model, path);
        createCrudOperation(crud, name, callback);
    }

    public void createCrudOperation(Crud crud, String name, Callback callback) {
        final RestdContext context = crud.getContext();
        final RestdModel   model   = crud.getModel();
        final String       path    = crud.getPath();

        final FuncActionBuilder action = new FuncActionBuilder(name);
        final RouteBuilder      route  = rm.createRoute("POST", path);

        if (null != callback) {
            callback.preAddArguments(action);
        }

        action.setFunction(createFunction(crud));
        addPathArguments(crud, action);
        addModelArgumentForCreate(crud, action);

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

        context.addDynamicRoute(rm.loadRoute(context.getRoutes(), route));
    }

    protected Function<ActionParams, Object> createFunction(Crud crud) {
        return new CreateFunction(crud);
    }

    public static class CreateFunction extends CrudFunction<ModelCreateInterceptor> {

        public CreateFunction(Crud crud) {
            super(crud, ModelCreateInterceptor.class);
        }

        @Override
        public Object apply(ActionParams params) {
            final Map<String, Object> record = doGetRecord(params);
            return createWithResponse(params, record);
        }

        public ApiResponse createWithResponse(ActionParams params, Map<String, Object> record) {
            CreateOneResult result = createWithResult(params, record);
            return responseCreateResult(result);
        }

        public CreateOneResult createWithResult(ActionParams params, Map<String, Object> record) {
            MApiModel am = api.getMetadata().getModel(model.getName());

            ModelExecutorContext context  = new SimpleModelExecutorContext(api, dao, am, em, params);
            ModelCreateExecutor  executor = newCreateExecutor(context);

            return doCreateRecord(params, executor, record);
        }

        public ApiResponse responseCreateResult(CreateOneResult result) {
            return ApiResponse.created(result.entity);
        }

        protected Map<String, Object> doGetRecord(ActionParams params) {
            return getModelRecord(params);
        }

        protected CreateOneResult doCreateRecord(ActionParams params, ModelCreateExecutor executor, Map<String, Object> record) {
            return executor.createOne(record);
        }

        protected ModelCreateExecutor newCreateExecutor(ModelExecutorContext context) {
            return mef.newCreateExecutor(context, interceptors);
        }

        @Override
        public String toString() {
            return "Function:" + "Create " + model.getName() + "";
        }
    }
}
