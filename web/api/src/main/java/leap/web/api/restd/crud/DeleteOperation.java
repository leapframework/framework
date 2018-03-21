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
import leap.web.action.FuncActionBuilder;
import leap.web.api.Api;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.mvc.params.DeleteOptions;
import leap.web.api.orm.ModelDeleteExecutor;
import leap.web.api.orm.ModelExecutorContext;
import leap.web.api.orm.SimpleModelExecutorContext;
import leap.web.api.restd.CrudOperation;
import leap.web.api.restd.CrudOperationBase;
import leap.web.api.restd.RestdContext;
import leap.web.api.restd.RestdModel;
import leap.web.route.RouteBuilder;

import java.util.function.Function;

/**
 * Delete by id operation.
 */
public class DeleteOperation extends CrudOperationBase implements CrudOperation {

    protected static final String NAME = "delete";

    @Override
    public void createCrudOperation(ApiConfigurator c, RestdContext context, RestdModel model) {
        if(!context.getConfig().allowDeleteModel(model.getName())) {
            return;
        }

        String verb = "DELETE";
        String path = fullModelPath(c, model) + getIdPath(model);

        FuncActionBuilder action = new FuncActionBuilder();
        RouteBuilder      route  = rm.createRoute(verb, path);

        if(isOperationExists(context, route)) {
            return;
        }

        action.setName(Strings.lowerCamel(NAME, model.getName()));
        action.setFunction(createFunction(c, context, model));

        addIdArguments(context, action, model);
        addArgument(context, action, DeleteOptions.class, "options");
        addNoContentResponse(action, model);

        preConfigure(context, model, action);
        route.setAction(action.build());
        setCrudOperation(route, NAME);

        postConfigure(context, model, route);

        c.addDynamicRoute(rm.loadRoute(context.getRoutes(), route));
    }

    protected Function<ActionParams, Object> createFunction(ApiConfigurator c, RestdContext context, RestdModel model) {
        return new DeleteFunction(context.getApi(), context.getDao(), model);
    }

    protected class DeleteFunction extends CrudFunction {

        public DeleteFunction(Api api, Dao dao, RestdModel model) {
            super(api, dao, model);
        }

        @Override
        public Object apply(ActionParams params) {
            MApiModel am = api.getMetadata().getModel(model.getName());

            ModelExecutorContext context = new SimpleModelExecutorContext(api, am, dao, model.getEntityMapping());
            ModelDeleteExecutor executor = newDeleteExecutor(context);

            Object        id      = id(params);
            DeleteOptions options = params.get(idLen);

            if(executor.deleteOne(id, options).success) {
                return ApiResponse.NO_CONTENT;
            }else{
                return ApiResponse.NOT_FOUND;
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
