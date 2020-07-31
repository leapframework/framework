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
import leap.web.exception.NotFoundException;
import leap.web.route.RouteBuilder;

import java.util.Map;
import java.util.function.Function;

/**
 * Update a record operation.
 */
public class UpdateOperation extends CrudOperationBase implements CrudOperation {

    protected static final String NAME = "update";

    @Override
    public void createCrudOperation(ApiConfigurator c, RestdContext context, RestdModel model) {
        if (!context.getConfig().allowUpdateModel(model.getName())) {
            return;
        }

        String path = fullModelPath(c, model) + getIdPath(model);
        String name = Strings.lowerCamel(NAME, model.getName());

        createCrudOperation(context, model, path, name, null);
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
        final RouteBuilder      route  = rm.createRoute("PATCH", path);

        if (null != callback) {
            callback.preAddArguments(action);
        }

        if (null != crud.getFunction()) {
            action.setFunction(crud.getFunction());
        } else {
            action.setFunction(createFunction(crud));
        }

        addPathArguments(crud, action);
        addModelArgumentForUpdate(context, action, model);

        if (null != callback) {
            callback.postAddArguments(action);
        }

        addNoContentResponse(action, model);

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
        return new UpdateFunction(crud);
    }

    public static class UpdateFunction extends CrudFunction<ModelUpdateInterceptor> {

        public UpdateFunction(Crud crud) {
            super(crud, ModelUpdateInterceptor.class);
        }

        @Override
        public Object apply(ActionParams params) {
            final Object              id     = doGetId(params);
            final Map<String, Object> record = doGetRecord(params);
            return updateWithResponse(params, id, record);
        }

        public ApiResponse updateWithResponse(ActionParams params, Object id, Map<String, Object> record) {
            final UpdateOneResult result = updateWithResult(params, id, record);
            return responseUpdateResult(result);
        }

        public UpdateOneResult updateWithResult(ActionParams params, Object id, Map<String, Object> record) {
            MApiModel am = am();

            ModelExecutorContext context  = new SimpleModelExecutorContext(api, dao, am, em, params);
            ModelUpdateExecutor  executor = newUpdateExecutor(context);

            return doUpdateRecord(params, executor, id, record);
        }

        public ApiResponse responseUpdateResult(UpdateOneResult result) {
            if (null != result.entity) {
                return ApiResponse.of(result.entity);
            }

            if (result.affectedRows <= 0) {
                throw new NotFoundException(em.getEntityName() + "' " + id.toString() + "' not found");
            }
            return ApiResponse.NO_CONTENT;
        }

        protected Object doGetId(ActionParams params) {
            return id(params);
        }

        protected Map<String, Object> doGetRecord(ActionParams params) {
            return getModelRecord(params);
        }

        protected UpdateOneResult doUpdateRecord(ActionParams params, ModelUpdateExecutor executor, Object id, Map<String, Object> record) {
            return executor.partialUpdateOne(id, record);
        }

        protected ModelUpdateExecutor newUpdateExecutor(ModelExecutorContext context) {
            return mef.newUpdateExecutor(context, interceptors);
        }

        @Override
        public String toString() {
            return "Function:" + "Update " + model.getName() + "";
        }
    }


}
