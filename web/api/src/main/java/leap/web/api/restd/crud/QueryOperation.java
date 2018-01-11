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
import leap.web.api.config.ApiConfig;
import leap.web.api.config.ApiConfigException;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;
import leap.web.api.orm.*;
import leap.web.api.restd.RestdContext;
import leap.web.api.restd.RestdModel;
import leap.web.api.restd.RestdProcessor;
import leap.web.route.RouteBuilder;

import java.util.function.Function;

/**
 * Query records operation.
 */
public class QueryOperation extends CrudOperation implements RestdProcessor {

    @Override
    public void preProcessModel(ApiConfigurator c, RestdContext context, RestdModel model) {
        if(!context.getConfig().allowQueryModel(model.getName())) {
            return;
        }

        String verb = "GET";
        String path = fullModelPath(c, model);

        Dao               dao    = context.getDao();
        FuncActionBuilder action = new FuncActionBuilder();
        RouteBuilder      route  = rm.createRoute(verb, path);

        if(isOperationExists(context, route)) {
            return;
        }

        action.setName(Strings.lowerCamel("query", model.getName()));
        action.setFunction(new QueryFunction(context.getApi(), dao, model));

        addArgument(context, action, QueryOptions.class, "options");
        addModelQueryResponse(action, model);

        configure(context, model, action);
        route.setAction(action.build());
        setCrudOperation(route, "query");

        configure(context, model, route);

        c.addDynamicRoute(rm.loadRoute(context.getRoutes(), route));
    }

    private final class QueryFunction implements Function<ActionParams, Object> {
        private final Api        api;
        private final Dao        dao;
        private final RestdModel model;

        public QueryFunction(Api api, Dao dao, RestdModel model) {
            this.api = api;
            this.dao = dao;
            this.model = model;
        }

        @Override
        public Object apply(ActionParams params) {
            ApiMetadata amd = api.getMetadata();
            MApiModel   am  = amd.getModel(model.getName());

            ModelExecutorContext context  = new SimpleModelExecutorContext(api, am, dao, model.getEntityMapping());
            ModelQueryExecutor   executor = mef.newQueryExecutor(context);

            QueryOptions options = params.get(0);

            QueryListResult result = executor.queryList(options);

            if (result.count == -1) {
                return ApiResponse.of(result.list);
            } else {
                return ApiResponse.of(result.list).setHeader("X-Total-Count", String.valueOf(result.count));
            }
        }

        @Override
        public String toString() {
            return "Function:" + "Query " + model.getName() + "";
        }
    }

}
