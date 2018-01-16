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
import leap.web.api.mvc.params.QueryOptionsBase;
import leap.web.api.orm.*;
import leap.web.api.restd.RestdContext;
import leap.web.api.restd.RestdModel;
import leap.web.api.restd.RestdProcessor;
import leap.web.route.RouteBuilder;

import java.util.function.Function;

/**
 * Find by id operation.
 */
public class FindOperation extends CrudOperation implements RestdProcessor {

    @Override
    public void preProcessModel(ApiConfigurator c, RestdContext context, RestdModel model) {
        if(!context.getConfig().allowFindModel(model.getName())) {
            return;
        }

        String verb = "GET";
        String path = fullModelPath(c, model) + getIdPath(model);

        Dao               dao    = context.getDao();
        FuncActionBuilder action = new FuncActionBuilder();
        RouteBuilder      route  = rm.createRoute(verb, path);

        if(isOperationExists(context, route)) {
            return;
        }

        action.setName(Strings.lowerCamel("find", model.getName()));
        action.setFunction(new FindFunction(context.getApi(), dao, model));

        addIdArgument(context, action, model);
        addArgument(context, action, QueryOptionsBase.class, "options");
        addModelResponse(action, model);

        preConfigure(context, model, action);
        route.setAction(action.build());
        setCrudOperation(route, "find");

        postConfigure(context, model, route);
        c.addDynamicRoute(rm.loadRoute(context.getRoutes(), route));
    }

    private final class FindFunction implements Function<ActionParams, Object> {
        private final Api        api;
        private final Dao        dao;
        private final RestdModel model;

        public FindFunction(Api api, Dao dao, RestdModel model) {
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

            Object           id      = params.get(0);
            QueryOptionsBase options = params.get(1);

            QueryOneResult result = executor.queryOne(id, options);

            return result.record == null ? ApiResponse.NOT_FOUND : ApiResponse.of(result.record);
        }

        @Override
        public String toString() {
            return "Function:" + "Find " + model.getName() + "";
        }
    }

}
