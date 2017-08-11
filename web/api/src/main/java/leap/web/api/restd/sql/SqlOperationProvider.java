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
import leap.lang.meta.MCollectionType;
import leap.lang.meta.MDictionaryType;
import leap.lang.meta.MSimpleTypes;
import leap.orm.OrmMetadata;
import leap.orm.dao.Dao;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlMetadata;
import leap.web.action.ActionParams;
import leap.web.action.ArgumentBuilder;
import leap.web.action.FuncActionBuilder;
import leap.web.api.config.ApiConfigException;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.config.model.RestdConfig;
import leap.web.api.meta.model.MApiResponseBuilder;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.restd.*;
import leap.web.api.restd.crud.CrudOperation;
import leap.web.route.RouteBuilder;

import java.util.Map;

public class SqlOperationProvider extends CrudOperation implements RestdOperationProvider {

    public static final String TYPE = "sql";
    public static final String ARG_SQL_KEY = "sqlKey";

    @Override
    public void createApiOperation(RestdContext context, RestdOperationDef op) {
        String path = fullPath(context.getApi().getConfigurator(), "/" + Strings.lowerUnderscore(op.getName()));

        String key = op.getArgument(ARG_SQL_KEY);
        SqlOperation sop = new SqlOperation(op.getName(), key, op.getScript());

        processSqlOperation(context.getApi().getConfigurator(), context, sop, null, path);
    }

    @Override
    public void createModelOperation(RestdContext context, RestdModel model, RestdOperationDef op) {
        String path = fullModelPath(context.getApi().getConfigurator(), model, "/" + Strings.lowerUnderscore(op.getName()));

        String key = op.getArgument(ARG_SQL_KEY);
        SqlOperation sop = new SqlOperation(op.getName(), key, op.getScript());

        processSqlOperation(context.getApi().getConfigurator(), context, sop, model, path);
    }

    protected void processSqlOperation(ApiConfigurator api, RestdContext ctx, SqlOperation op, RestdModel model, String path) {
        OrmMetadata om = ctx.getDao().getOrmContext().getMetadata();

        SqlCommand sc = om.tryGetSqlCommand(op.getKey());
        if (null == sc) {
            throw new ApiConfigException("Sql key '" + op.getKey() + "' not found");
        }

        if (!sc.hasMetadata()) {
            throw new ApiConfigException("Sql '" + op.getKey() + "' has no metadata!");
        }

        SqlMetadata sm = sc.getMetadata();
        if (sm.isUnknown()) {
            throw new ApiConfigException("Sql '" + op.getKey() + "' is unknown!");
        }

        String verb;
        if (sm.isSelect()) {
            verb = "GET";
        } else if (sm.isInsert()) {
            verb = "POST";
        } else if (sm.isUpdate()) {
            verb = "PATCH";
        } else {
            verb = "DELETE";
        }

        if (isOperationExists(ctx, verb, path)) {
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
        api.addDynamicRoute(rm.loadRoute(ctx.getRoutes(), route));
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

    protected static final class SqlOperation {
        private final String name;
        private final String key;
        private final String script;

        public SqlOperation(String name, String key, String script) {
            this.name = name;
            this.key = key;
            this.script = script;
        }

        public String getName() {
            return name;
        }

        public String getKey() {
            return key;
        }

        public String getScript() {
            return script;
        }
    }

}
