/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package leap.web.api.restd.crud;

import leap.lang.Strings;
import leap.web.action.ActionParams;
import leap.web.action.FuncActionBuilder;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.orm.ModelExecutorContext;
import leap.web.api.orm.ModelUpdateExecutor;
import leap.web.api.orm.SimpleModelExecutorContext;
import leap.web.api.orm.UpdateOneResult;
import leap.web.api.restd.CrudOperation;
import leap.web.api.restd.CrudOperationBase;
import leap.web.api.restd.RestdContext;
import leap.web.api.restd.RestdModel;
import leap.web.exception.NotFoundException;
import leap.web.route.RouteBuilder;

import java.util.Map;
import java.util.function.Function;

public class ReplaceOperation extends CrudOperationBase implements CrudOperation {

    public static final String NAME = "replace";

    @Override
    public void createCrudOperation(ApiConfigurator c, RestdContext context, RestdModel model) {
        if (!context.getConfig().allowModelOperation(model.getName(), NAME)) {
            return;
        }

        String verb = "PUT";
        String path = fullModelPath(c, model) + getIdPath(model);

        final Crud              crud   = Crud.of(context, model, path);
        final FuncActionBuilder action = new FuncActionBuilder();
        final RouteBuilder      route  = rm.createRoute(verb, path);

        action.setName(Strings.lowerCamel(NAME, model.getName()));
        action.setFunction(createFunction(crud));
        addPathArguments(crud, action);
        addModelArgumentForReplace(context, action, model);
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

    protected Function<ActionParams, Object> createFunction(Crud crud) {
        return new ReplaceFunction(crud);
    }

    protected class ReplaceFunction extends CrudFunction {

        public ReplaceFunction(Crud crud) {
            super(crud);
        }

        @Override
        public Object apply(ActionParams params) {
            MApiModel am = am();

            Object              id     = id(params);
            Map<String, Object> record = getModelRecord(params);

            ModelExecutorContext context  = new SimpleModelExecutorContext(api, dao, am, em, params);
            ModelUpdateExecutor  executor = newUpdateExecutor(context);

            UpdateOneResult result = executor.replaceUpdateOne(id, record);
            if (null != result.entity) {
                return ApiResponse.of(result.entity);
            }

            if (result.affectedRows > 0) {
                return ApiResponse.NO_CONTENT;
            } else {
                throw new NotFoundException(am.getName() + "' " + id.toString() + "' not found");
            }
        }

        protected ModelUpdateExecutor newUpdateExecutor(ModelExecutorContext context) {
            return mef.newUpdateExecutor(context);
        }

        @Override
        public String toString() {
            return "Function:" + "Replace " + model.getName() + "";
        }
    }

}
