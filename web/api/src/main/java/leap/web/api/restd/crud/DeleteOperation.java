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
import leap.lang.convert.Converts;
import leap.web.Request;
import leap.web.action.ActionParams;
import leap.web.action.FuncActionBuilder;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.mvc.params.DeleteOptions;
import leap.web.api.orm.*;
import leap.web.api.restd.CrudOperation;
import leap.web.api.restd.CrudOperationBase;
import leap.web.api.restd.RestdContext;
import leap.web.api.restd.RestdModel;
import leap.web.exception.BadRequestException;
import leap.web.exception.NotFoundException;
import leap.web.route.RouteBuilder;

import java.util.function.Function;

/**
 * Delete by id operation.
 */
public class DeleteOperation extends CrudOperationBase implements CrudOperation {

    protected static final String NAME = "delete";

    @Override
    public void createCrudOperation(ApiConfigurator c, RestdContext context, RestdModel model) {
        if (!context.getConfig().allowDeleteModel(model.getName())) {
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
        final RouteBuilder      route  = rm.createRoute("DELETE", path);

        if (null != callback) {
            callback.preAddArguments(action);
        }

        action.setFunction(createFunction(crud));

        addPathArguments(crud, action);
        addOtherArguments(context, action, model);

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

    protected void addOtherArguments(RestdContext context, FuncActionBuilder action, RestdModel model) {
        addArgument(context, action, DeleteOptions.class, "options");
    }

    protected Function<ActionParams, Object> createFunction(Crud crud) {
        return new DeleteFunction(crud, true);
    }

    public static class DeleteFunction extends CrudFunction<ModelDeleteInterceptor> {

        private final boolean cascadeDelete;

        public DeleteFunction(Crud crud, boolean cascadeDelete) {
            super(crud, ModelDeleteInterceptor.class);
            this.cascadeDelete = cascadeDelete;
        }

        @Override
        public Object apply(ActionParams params) {
            MApiModel am = api.getMetadata().getModel(model.getName());

            ModelExecutorContext context  = new SimpleModelExecutorContext(api, dao, am, em, params);
            ModelDeleteExecutor  executor = newDeleteExecutor(context);

            Object        id      = doGetId(params);
            DeleteOptions options = doGetOptions(params);

            if (!cascadeDelete) {
                Request request = params.getContext().getRequest();
                String  param   = request.getParameter("cascade_delete");
                if (!Strings.isEmpty(param) && Converts.toBoolean(param)) {
                    throw new BadRequestException("Cascade delete not supported by this operation, check parameter 'cascade_delete'!");
                }
            }

            DeleteOneResult result = doDeleteOne(params, executor, id, options);
            if (null != result.entity) {
                return ApiResponse.of(result.entity);
            } else {
                if (result.success) {
                    return ApiResponse.NO_CONTENT;
                } else {
                    throw new NotFoundException(am.getName() + " '" + id.toString() + "' not found");
                }
            }
        }

        protected Object doGetId(ActionParams params) {
            return id(params);
        }

        protected DeleteOptions doGetOptions(ActionParams params) {
            return cascadeDelete ? params.getLast() : null;
        }

        protected DeleteOneResult doDeleteOne(ActionParams params, ModelDeleteExecutor executor, Object id, DeleteOptions options) {
            return executor.deleteOne(id, options);
        }

        protected ModelDeleteExecutor newDeleteExecutor(ModelExecutorContext context) {
            return mef.newDeleteExecutor(context, interceptors);
        }

        @Override
        public String toString() {
            return "Function:" + "Delete " + model.getName() + "";
        }
    }
}
