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

package leap.web.api.restd.sql;

import leap.lang.Strings;
import leap.lang.meta.*;
import leap.orm.OrmMetadata;
import leap.orm.dao.Dao;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlMetadata;
import leap.web.App;
import leap.web.action.ActionParams;
import leap.web.action.ArgumentBuilder;
import leap.web.action.FuncActionBuilder;
import leap.web.api.config.ApiConfig;
import leap.web.api.config.ApiConfigException;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.config.model.RestdConfig;
import leap.web.api.meta.model.MApiHeaderBuilder;
import leap.web.api.meta.model.MApiResponseBuilder;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.restd.RestdModel;
import leap.web.api.restd.RestdOperationBase;
import leap.web.api.restd.RestdProcessor;
import leap.web.api.restd.RestdContext;
import leap.web.api.restd.crud.CrudOperation;
import leap.web.route.RouteBuilder;
import org.omg.PortableServer.THREAD_POLICY_ID;

import java.util.Map;

public class SqlOperationProcessor extends CrudOperation implements RestdProcessor {

    @Override
    public void preProcessApi(App app, ApiConfigurator api, RestdContext context) {
        context.getConfig().getSqlOperations().values().forEach((op) -> {
            String path = fullPath(api, "/" + Strings.lowerUnderscore(op.getName()));
            processSqlOperation(app, api, context, op, null, path);
        });
    }

    @Override
    public void preProcessModel(App app, ApiConfigurator api, RestdContext context, RestdModel model) {
        final RestdConfig.Model mc = context.getConfig().getModel(model.getName());

        if(null != mc) {
            mc.getSqlOperations().values().forEach(op -> {
                String path = fullModelPath(api, model, "/" + Strings.lowerUnderscore(op.getName()));
                processSqlOperation(app, api, context, op, model, path);
            });
        }
    }

    protected void processSqlOperation(App app, ApiConfigurator api, RestdContext ctx, RestdConfig.SqlOperation op, RestdModel model, String path) {
        OrmMetadata om = ctx.getDao().getOrmContext().getMetadata();

        SqlCommand sc = om.tryGetSqlCommand(op.getSqlKey());

        if (null == sc) {
            throw new ApiConfigException("Sql key '" + op.getSqlKey() + "' not found");
        }

        if (!sc.hasMetadata()) {
            throw new ApiConfigException("Sql '" + op.getSqlKey() + "' has no metadata!");
        }

        SqlMetadata sm = sc.getMetadata();
        if (sm.isUnknown()) {
            throw new ApiConfigException("Sql '" + op.getSqlKey() + "' is unknown!");
        }

        String verb;
        if (sm.isSelect()) {
            verb = "GET";
        } else if (sm.isInsert() || sm.isUpdate()) {
            verb = "POST";
        } else {
            verb = "DELETE";
        }

        if (isOperationExists(app, verb, path)) {
            throw new ApiConfigException("Sql operation '" + op.getName() + "' with path '" + path + "' already exists!");
        }

        Dao dao = ctx.getDao();
        FuncActionBuilder action = new FuncActionBuilder();
        RouteBuilder route = rm.createRoute(verb, path);

        action.setName(op.getName());
        action.setFunction((params) -> execute(dao, sc, params));

        //arguments
        for (SqlMetadata.Param param : sm.getParameters()) {
            action.addArgument(new ArgumentBuilder().setName(param.getName()).setType(String.class).setRequired(param.isRequired()));
        }

        //response
        if (sm.isSelect()) {
            addQueryResponse(ctx, sc, action);
        } else {
            addUpdateResponse(ctx, sc, action);
        }

        if(null != model) {
            configure(ctx, model, action);
        }
        route.setAction(action.build());

        configure(ctx, model, route);
        api.addRoute(rm.loadRoute(app.routes(), route));
    }

    protected void addQueryResponse(RestdContext ctx, SqlCommand sc, FuncActionBuilder action) {
        action.setReturnType(ApiResponse.class);

        //todo:
        MApiResponseBuilder r = new MApiResponseBuilder();

        r.setStatus(200);
        r.setType(new MCollectionType(MDictionaryType.INSTANCE));
        r.setDescription("Success");

        /*
        MApiHeaderBuilder header = new MApiHeaderBuilder();
        header.setName("X-Total-Count");
        header.setType(MSimpleTypes.BIGINT);
        header.setDescription("The total count of query records.");
        r.addHeader(header);
        */

        action.setExtension(new MApiResponseBuilder[]{r});
    }

    protected void addUpdateResponse(RestdContext ctx, SqlCommand sc, FuncActionBuilder action) {
        action.setReturnType(ApiResponse.class);

        //todo :
        MApiResponseBuilder r = new MApiResponseBuilder();
        r.setStatus(200);
        r.setType(MSimpleTypes.INTEGER);
        r.setDescription("Success");

        action.setExtension(new MApiResponseBuilder[]{r});
    }

    protected Object execute(Dao dao, SqlCommand command, ActionParams params) {
        Map<String,Object> map = params.toMap();

        if(command.getMetadata().isSelect()) {
            //todo: page query, total count

            return ApiResponse.of(dao.createQuery(command).params(map).result().list());
        }else{
            return ApiResponse.of(dao.executeUpdate(command, map));
        }

    }

}
