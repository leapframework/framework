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
import leap.lang.Strings;
import leap.orm.Orm;
import leap.orm.dao.Dao;
import leap.web.App;
import leap.web.action.ActionParams;
import leap.web.action.FuncActionBuilder;
import leap.web.api.Apis;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.model.MApiModel;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.orm.*;
import leap.web.api.restd.RestdApiCreator;
import leap.web.api.restd.RestdApiConfigContext;
import leap.web.api.restd.RestdModel;
import leap.web.route.RouteBuilder;

import java.util.Map;

/**
 * Defines the 'C' operation in 'CRUD' for all models.
 */
public class COperationCreator extends CRUDOperationCreatorBase implements RestdApiCreator {

    protected @Inject Apis                 apis;
    protected @Inject ModelExecutorFactory mef;

    @Override
    public void process(App app, ApiConfigurator api, RestdApiConfigContext context) {

        for(RestdModel model : context.getModels()) {
            createOperation(app, api, context, model);
        }

    }

    protected void createOperation(App app, ApiConfigurator api, RestdApiConfigContext context, RestdModel model) {
        //todo : check is creatable?

        Dao dao = Orm.dao(context.getOrmContext().getName());

        String path = fullModelPath(api, model);

        FuncActionBuilder action = new FuncActionBuilder();
        RouteBuilder      route  = rm.createRoute("POST", path);

        action.setName(Strings.lowerCamel("create", model.getName()));
        action.setFunction((params) -> execute(api.config(), dao, model, params));
        addModelArgument(action, model);
        addModelResponse(action, model).setStatus(201);

        route.setAction(action.build());

        configRoute(context, route);

        api.addRoute(rm.loadRoute(app.routes(), route));
    }

    protected Object execute(ApiConfig c, Dao dao, RestdModel model, ActionParams params) {
        ApiMetadata md = apis.tryGetMetadata(c.getName());
        MApiModel   am = md.getModel(model.getName());
        ModelExecutorConfig mec = new SimpleModelExecutorConfig(c.getMaxPageSize(), c.getDefaultPageSize());

        Map<String,Object> record = params.get(0);

        ModelCreateExecutor executor = mef.newCreateExecutor(mec, am, dao, model.getEntityMapping());

        CreateOneResult result = executor.createOne(record);

        return ApiResponse.created(dao.find(model.getEntityMapping(), result.id));
    }

}
