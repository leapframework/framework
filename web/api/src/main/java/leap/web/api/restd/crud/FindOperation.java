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
import leap.web.Request;
import leap.web.action.ActionParams;
import leap.web.action.FuncActionBuilder;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.mvc.params.QueryOptionsBase;
import leap.web.api.orm.*;
import leap.web.api.restd.CrudOperation;
import leap.web.api.restd.CrudOperationBase;
import leap.web.api.restd.RestdContext;
import leap.web.api.restd.RestdModel;
import leap.web.exception.NotFoundException;
import leap.web.route.RouteBuilder;

import java.util.ArrayList;
import java.util.List;
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
        final RouteBuilder      route  = rm.createRoute("GET", path);

        if (null != callback) {
            callback.preAddArguments(action);
        }

        action.setFunction(null != crud.getFunction() ? crud.getFunction() : createFunction(crud));

        addPathArguments(crud, action);
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

        context.addDynamicRoute(rm.loadRoute(context.getRoutes(), route));
    }

    protected Function<ActionParams, Object> createFunction(Crud crud) {
        return new FindFunction(crud);
    }

    public static class FindFunction extends CrudFunction<ModelFindInterceptor> {

        public FindFunction(Crud crud) {
            super(crud, ModelFindInterceptor.class);
        }

        @Override
        public Object apply(ActionParams params) {
            MApiModel am = am();

            ModelExecutorContext context  = new SimpleModelExecutorContext(api, dao, am, em, params);
            ModelQueryExecutor   executor = newQueryExecutor(context);

            final Object           id      = doGetId(params);
            final QueryOptionsBase options = doGetOptions(params);

            QueryOneResult result = doQueryOne(params, executor, id, options);

            final Request request         = params.getContext().getRequest();
            boolean       hasExpandErrors = null != result.getExpandErrors() && !result.getExpandErrors().isEmpty();
            if (hasExpandErrors && !allowExpandErrors(request)) {
                return ApiResponse.err(request.getMessageSource().getMessage(QueryOperation.QUERY_EXPAND_ERROR));
            }

            ApiResponse response;
            if (null != result.getEntity()) {
                response = ApiResponse.of(result.getEntity());
            } else if (result.getRecord() == null) {
                throw new NotFoundException(am.getName() + " '" + id.toString() + "' not found");
            } else {
                response = ApiResponse.of(result.getRecord());
            }

            if (hasExpandErrors) {
                List<String> expands = new ArrayList<>();
                for (ExpandError ee : result.getExpandErrors()) {
                    expands.add(ee.getExpand());
                }
                response.withHeader(QueryOperation.ERROR_EXPANDS, Strings.join(expands, ','));
            }

            return response;
        }

        protected Object doGetId(ActionParams params) {
            return id(params);
        }

        protected QueryOptionsBase doGetOptions(ActionParams params) {
            return (QueryOptionsBase) params.get("options");
        }

        protected QueryOneResult doQueryOne(ActionParams params, ModelQueryExecutor executor, Object id, QueryOptionsBase options) {
            return executor.queryOne(id, options);
        }

        protected ModelQueryExecutor newQueryExecutor(ModelExecutorContext context) {
            return mef.newQueryExecutor(context, interceptors);
        }

        protected boolean allowExpandErrors(Request request) {
            return "1".equals(request.getHeader(QueryOperation.ALLOW_EXPAND_ERROR));
        }

        @Override
        public String toString() {
            return "Function:" + "Find " + model.getName() + "";
        }
    }

}
