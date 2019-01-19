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
import leap.web.api.meta.model.MApiModel;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.mvc.params.QueryOptionsBase;
import leap.web.api.orm.ModelExecutorContext;
import leap.web.api.orm.ModelQueryExecutor;
import leap.web.api.orm.QueryOneResult;
import leap.web.api.orm.SimpleModelExecutorContext;
import leap.web.api.restd.CrudOperation;
import leap.web.api.restd.CrudOperationBase;
import leap.web.api.restd.RestdContext;
import leap.web.api.restd.RestdModel;
import leap.web.exception.NotFoundException;
import leap.web.route.RouteBuilder;

import java.util.function.Function;

/**
 * Find by id operation.
 */
public class FindOperation extends CrudOperationBase implements CrudOperation {

    protected static final String NAME = "find";

    @Override
    public void createCrudOperation(ApiConfigurator c, RestdContext context, RestdModel model) {
        if (!context.getConfig().allowFindModel(model.getName())) {
            return;
        }

        String path = fullModelPath(c, model) + getIdPath(model);
        String name = Strings.lowerCamel(NAME, model.getName());

        createCrudOperation(c, context, model, path, name, null);
    }

    public void createCrudOperation(ApiConfigurator c, RestdContext context, RestdModel model,
                                    String path, String name, Callback callback) {
        FuncActionBuilder action = new FuncActionBuilder(name);
        RouteBuilder      route  = rm.createRoute("GET", path);

        if (null != callback) {
            callback.preAddArguments(action);
        }

        action.setFunction(createFunction(context, model, action.getArguments().size()));

        addIdArguments(context, action, model);
        addArgument(context, action, QueryOptionsBase.class, "options");
        if (null != callback) {
            callback.postAddArguments(action);
        }
        addModelResponse(action, model);

        preConfigure(context, model, action);
        route.setAction(action.build());
        setCrudOperation(route, NAME);
        postConfigure(context, model, route);

        if (isOperationExists(context, route)) {
            return;
        }

        c.addDynamicRoute(rm.loadRoute(context.getRoutes(), route));
    }

    protected Function<ActionParams, Object> createFunction(RestdContext context, RestdModel model, int start) {
        return new FindFunction(context.getApi(), context.getDao(), model, start);
    }

    protected class FindFunction extends CrudFunction {

        public FindFunction(Api api, Dao dao, RestdModel model, int start) {
            super(api, dao, model, start);
        }

        @Override
        public Object apply(ActionParams params) {
            MApiModel am = am();

            ModelExecutorContext context  = new SimpleModelExecutorContext(api, dao, am, em);
            ModelQueryExecutor   executor = newQueryExecutor(context);

            Object           id      = id(params);
            QueryOptionsBase options = getWithId(params, 0);

            QueryOneResult result = executor.queryOne(id, options);
            if (null != result.getEntity()) {
                return ApiResponse.of(result.getEntity());
            } else if (result.getRecord() == null) {
                throw new NotFoundException(am.getName() + " '" + id.toString() + "' not found");
            } else {
                return ApiResponse.of(result.getRecord());
            }
        }

        protected ModelQueryExecutor newQueryExecutor(ModelExecutorContext context) {
            return mef.newQueryExecutor(context);
        }

        @Override
        public String toString() {
            return "Function:" + "Find " + model.getName() + "";
        }
    }

}
