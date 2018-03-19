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

import leap.core.annotation.Inject;
import leap.lang.Arrays2;
import leap.lang.Strings;
import leap.orm.dao.Dao;
import leap.web.action.ActionParams;
import leap.web.action.FuncActionBuilder;
import leap.web.api.Api;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.orm.*;
import leap.web.api.restd.CrudOperation;
import leap.web.api.restd.CrudOperationBase;
import leap.web.api.restd.RestdModel;
import leap.web.api.restd.RestdContext;
import leap.web.route.RouteBuilder;

import java.util.Map;
import java.util.function.Function;

/**
 * Update a record operation.
 */
public class UpdateOperation extends CrudOperationBase implements CrudOperation {

    @Inject
    private RestdValidator[] validators;

    @Override
    public void createCrudOperation(ApiConfigurator c, RestdContext context, RestdModel model) {
        if(!context.getConfig().allowUpdateModel(model.getName())) {
            return;
        }

        String verb = "PATCH";
        String path = fullModelPath(c, model) + getIdPath(model);

        Dao               dao    = context.getDao();
        FuncActionBuilder action = new FuncActionBuilder();
        RouteBuilder      route  = rm.createRoute(verb, path);

        if(isOperationExists(context, route)) {
            return;
        }

        action.setName(Strings.lowerCamel("update", model.getName()));
        action.setFunction(new UpdateFunction(context.getApi(), dao, model));
        addIdArguments(context, action, model);
        addModelArgumentForUpdate(context, action, model);
        addNoContentResponse(action, model);

        preConfigure(context, model, action);
        route.setAction(action.build());
        setCrudOperation(route, "update");

        postConfigure(context, model, route);
        c.addDynamicRoute(rm.loadRoute(context.getRoutes(), route));
    }

    private final class UpdateFunction extends CrudFunction implements Function<ActionParams, Object> {

        public UpdateFunction(Api api, Dao dao, RestdModel model) {
            super(api, dao, model);
        }

        @Override
        public Object apply(ActionParams params) {
            ApiMetadata amd = api.getMetadata();
            MApiModel   am  = amd.getModel(model.getName());

            Object             id     = id(params);
            Map<String,Object> record = record(params);

            ModelExecutorContext context  = new SimpleModelExecutorContext(api, am, dao, em);
            ModelUpdateExecutor  executor = mef.newUpdateExecutor(context);

            if(Arrays2.isNotEmpty(validators)) {
                for (RestdValidator validator : validators) {
                    validator.validate(model.getName(), record);
                }
            }

            UpdateOneResult result = executor.partialUpdateOne(id, record);

            if (result.affectedRows > 0) {
                return ApiResponse.NO_CONTENT;
            } else {
                return ApiResponse.NOT_FOUND;
            }
        }

        @Override
        public String toString() {
            return "Function:" + "Update " + model.getName() + "";
        }
    }


}
