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
import leap.orm.dao.Dao;
import leap.web.Request;
import leap.web.action.ActionParams;
import leap.web.action.FuncActionBuilder;
import leap.web.api.Api;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.mvc.params.DeleteOptions;
import leap.web.api.orm.DeleteOneResult;
import leap.web.api.orm.ModelDeleteExecutor;
import leap.web.api.orm.ModelExecutorContext;
import leap.web.api.orm.SimpleModelExecutorContext;
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

        createCrudOperation(c, context, model, path, name, null);
    }

    public void createCrudOperation(ApiConfigurator c, RestdContext context, RestdModel model,
                                    String path, String name, Callback callback) {
        FuncActionBuilder action = new FuncActionBuilder(name);
        RouteBuilder      route  = rm.createRoute("DELETE", path);

        if (null != callback) {
            callback.preAddArguments(action);
        }

        action.setFunction(createFunction(context, model, action.getArguments().size()));

        addIdArguments(context, action, model);
        addOtherArguments(c, context, action, model);
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

        c.addDynamicRoute(rm.loadRoute(context.getRoutes(), route));
    }

    protected void addOtherArguments(ApiConfigurator c, RestdContext context, FuncActionBuilder action, RestdModel model) {
        addArgument(context, action, DeleteOptions.class, "options");
    }

    protected Function<ActionParams, Object> createFunction(RestdContext context, RestdModel model, int start) {
        return new DeleteFunction(context.getApi(), context.getDao(), model, start, true);
    }

    protected class DeleteFunction extends CrudFunction {

        private final boolean cascadeDelete;

        public DeleteFunction(Api api, Dao dao, RestdModel model, int start, boolean cascadeDelete) {
            super(api, dao, model, start);
            this.cascadeDelete = cascadeDelete;
        }

        @Override
        public Object apply(ActionParams params) {
            MApiModel am = api.getMetadata().getModel(model.getName());

            ModelExecutorContext context  = new SimpleModelExecutorContext(api, dao, am, em, params);
            ModelDeleteExecutor  executor = newDeleteExecutor(context);

            Object        id      = id(params);
            DeleteOptions options = cascadeDelete ? getWithId(params, 0) : null;

            if (!cascadeDelete) {
                Request request = Request.tryGetCurrent();
                String  param   = request.getParameter("cascade_delete");
                if (!Strings.isEmpty(param) && Converts.toBoolean(param)) {
                    throw new BadRequestException("Cascade delete not supported by this operation, check parameter 'cascade_delete'!");
                }
            }

            DeleteOneResult result = executor.deleteOne(id, options);
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

        protected ModelDeleteExecutor newDeleteExecutor(ModelExecutorContext context) {
            return mef.newDeleteExecutor(context);
        }

        @Override
        public String toString() {
            return "Function:" + "Delete " + model.getName() + "";
        }
    }
}
