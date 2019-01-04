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
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.orm.ModelExecutorContext;
import leap.web.api.orm.ModelQueryExecutor;
import leap.web.api.orm.QueryListResult;
import leap.web.api.orm.SimpleModelExecutorContext;
import leap.web.api.restd.CrudOperation;
import leap.web.api.restd.CrudOperationBase;
import leap.web.api.restd.RestdContext;
import leap.web.api.restd.RestdModel;
import leap.web.route.RouteBuilder;

import java.util.function.Function;

/**
 * Query records operation.
 */
public class QueryOperation extends CrudOperationBase implements CrudOperation {

    protected static final String NAME = "query";

    @Override
    public void createCrudOperation(ApiConfigurator c, RestdContext context, RestdModel model) {
        if(!context.getConfig().allowQueryModel(model.getName())) {
            return;
        }

        String verb = "GET";
        String path = fullModelPath(c, model);

        FuncActionBuilder action = new FuncActionBuilder();
        RouteBuilder      route  = rm.createRoute(verb, path);

        action.setName(Strings.lowerCamel(NAME, model.getName()));
        action.setFunction(createFunction(c, context, model));

        addArgument(context, action, QueryOptions.class, "options");
        addModelQueryResponse(action, model);

        preConfigure(context, model, action);
        route.setAction(action.build());
        setCrudOperation(route, NAME);
        postConfigure(context, model, route);

        if(isOperationExists(context, route)) {
            return;
        }

        c.addDynamicRoute(rm.loadRoute(context.getRoutes(), route));
    }

    protected Function<ActionParams, Object> createFunction(ApiConfigurator c, RestdContext context, RestdModel model) {
        return new QueryFunction(context.getApi(), context.getDao(), model);
    }

    protected class QueryFunction extends CrudFunction implements Function<ActionParams, Object> {

        public QueryFunction(Api api, Dao dao, RestdModel model) {
            super(api, dao, model);
        }

        @Override
        public Object apply(ActionParams params) {
            MApiModel am = am();

            ModelExecutorContext context  = new SimpleModelExecutorContext(api, dao, am, em);
            ModelQueryExecutor   executor = newQueryExecutor(context);

            QueryOptions options = params.get(0);

            QueryListResult result = executor.queryList(options);

            if(null != result.getEntity()) {
                return ApiResponse.of(result.getEntity());
            }

            if (result.getCount() == -1) {
                return ApiResponse.of(result.getList());
            } else {
                return ApiResponse.of(result.getList()).setHeader("X-Total-Count", String.valueOf(result.getCount()));
            }
        }

        protected ModelQueryExecutor newQueryExecutor(ModelExecutorContext context) {
            return mef.newQueryExecutor(context);
        }

        @Override
        public String toString() {
            return "Function:" + "Query " + model.getName() + "";
        }
    }

}
