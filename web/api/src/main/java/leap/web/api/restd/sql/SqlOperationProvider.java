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
import leap.lang.convert.Converts;
import leap.lang.http.HTTP;
import leap.lang.meta.MCollectionType;
import leap.lang.meta.MDictionaryType;
import leap.lang.meta.MSimpleTypes;
import leap.lang.meta.MType;
import leap.orm.OrmContext;
import leap.orm.OrmMetadata;
import leap.orm.dao.Dao;
import leap.orm.query.Query;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlMetadata;
import leap.web.action.ActionParams;
import leap.web.action.ArgumentBuilder;
import leap.web.action.FuncActionBuilder;
import leap.web.api.config.ApiConfigException;
import leap.web.api.meta.model.MApiOperationBuilder;
import leap.web.api.meta.model.MApiParameter;
import leap.web.api.meta.model.MApiParameterBuilder;
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
    public void createApiOperation(RestdContext context, RestdOperationDef od) {
        String opPath = od.getPath();
        if(Strings.isEmpty(opPath)) {
            opPath = "/" + Strings.lowerUnderscore(od.getName());
        }

        String path = fullPath(context.getApi().getConfigurator(), opPath);

        String key = od.getArgument(ARG_SQL_KEY);
        SqlOperationDef sod = new SqlOperationDef(od.getName(), path, key, od.getScript());

        createSqlOperation(context, null, od, sod);
    }

    @Override
    public void createModelOperation(RestdContext context, RestdModel model, RestdOperationDef od) {
        String opPath = od.getPath();
        if(Strings.isEmpty(opPath)) {
            opPath = "/" + Strings.lowerUnderscore(od.getName());
        }

        String path = fullModelPath(context.getApi().getConfigurator(), model, opPath);

        String key = od.getArgument(ARG_SQL_KEY);
        SqlOperationDef sod = new SqlOperationDef(od.getName(), path, key, od.getScript());

        createSqlOperation(context, model, od, sod);
    }

    protected void createSqlOperation(RestdContext ctx, RestdModel model, RestdOperationDef od, SqlOperationDef sod) {
        OrmMetadata om = ctx.getDao().getOrmContext().getMetadata();

        SqlCommand sc = null;

        if(!Strings.isEmpty(sod.getKey())) {
            sc = om.tryGetSqlCommand(sod.getKey());
            if (null == sc) {
                throw new ApiConfigException("Sql key '" + sod.getKey() + "' not found");
            }
        }else if(!Strings.isEmpty(sod.getScript())) {
            OrmContext oc = ctx.getDao().getOrmContext();
            try {
                sc = oc.getSqlFactory().createSqlCommand(oc, sod.getScript()).prepare(oc);
            }catch (Exception e) {
                throw new ApiConfigException("Error parsing sql of operation '" + sod.getName() + "', " + e.getMessage(), e);
            }
        }

        createSqlOperation(ctx, model, od, sod, sc);
    }

    protected void createSqlOperation(RestdContext ctx, RestdModel model, RestdOperationDef od, SqlOperationDef op, SqlCommand sc) {
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

        MApiOperationBuilder mo = od.getMetaOperation();
        if(null == mo) {
            mo = new MApiOperationBuilder();
            mo.setName(op.getName());
            mo.setMethod(HTTP.Method.valueOf(verb));
        }else if(null != mo.getMethod()) {
            verb = mo.getMethod().name();
        }

        String path = op.getPath();

        Dao dao = ctx.getDao();
        FuncActionBuilder action = new FuncActionBuilder();
        RouteBuilder route = rm.createRoute(verb, path);

        action.setName(op.getName());

        //arguments
        if(!sm.getParameters().isEmpty()) {
            for (SqlMetadata.Param param : sm.getParameters()) {
                if(mo.getParameter(param.getName()) != null) {
                    continue;
                }
                MApiParameterBuilder p = new MApiParameterBuilder();
                p.setName(param.getName());
                p.setLocation(MApiParameter.Location.QUERY);
                p.setType(MSimpleTypes.STRING);
                p.setRequired(true);
                mo.addParameter(p);
            }
        }

        for(MApiParameterBuilder p : mo.getParameters()) {
            ArgumentBuilder arg = new ArgumentBuilder().setName(p.getName()).setRequired(p.getRequired());
            arg.setType(String.class); //todo : type
            //todo: location
            action.addArgument(arg);
        }

        action.setReturnType(ApiResponse.class);
        action.setExtension(mo);

        //response
        if(mo.getResponses().isEmpty()) {
            if (sm.isSelect()) {
                addQueryResponse(ctx, sc, action, mo);
            } else {
                addUpdateResponse(ctx, sc, action, mo);
            }
        }

        final MType returnType = mo.getResponses().get(0).getType();
        action.setFunction((params) -> execute(dao, sc, params, returnType));

        if(null != model) {
            configure(ctx, model, action);
        }
        route.setAction(action.build());

        configure(ctx, model, route);

        if (!od.isExplicitPrior() && isOperationExists(ctx, verb, path)) {
            if(!od.isExplicitNotPrior()) {
                throw new ApiConfigException("Sql operation '" + op.getName() + "' with path '" + path + "' already exists!");
            }else{
                //todo: log
                route.setEnabled(false);
            }
        }

        ctx.getApi().getConfigurator().addDynamicRoute(rm.loadRoute(ctx.getRoutes(), route));
    }

    protected void addQueryResponse(RestdContext ctx, SqlCommand sc, FuncActionBuilder action, MApiOperationBuilder mo) {
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

        mo.addResponse(r);
    }

    protected void addUpdateResponse(RestdContext ctx, SqlCommand sc, FuncActionBuilder action, MApiOperationBuilder mo) {
        //todo :
        MApiResponseBuilder r = new MApiResponseBuilder();
        r.setStatus(200);
        r.setType(MSimpleTypes.INTEGER);
        r.setDescription("Success");

        mo.addResponse(r);
    }

    protected Object execute(Dao dao, SqlCommand command, ActionParams params, MType returnType) {
        Map<String,Object> map = params.toMap();

        Object result;

        if(command.getMetadata().isSelect()) {
            //todo: page query, total count

            Query query = dao.createQuery(command).params(map);

            if(null != returnType) {
                if(returnType.isSimpleType()) {
                    result = Converts.convert(query.scalarValueOrNull(), returnType.asSimpleType().getJavaType());
                }else if(returnType.isCollectionType() && returnType.asCollectionType().getElementType().isSimpleType()) {
                    result = query.scalars().list(returnType.asCollectionType().getElementType().asSimpleType().getJavaType());
                }else {
                    result = query.list();
                }
            }else {
                result = query.list();
            }
        }else{
            //todo: the return type must be simple type
            result = dao.executeUpdate(command, map);
            if(null != returnType) {
                result = Converts.convert(result, returnType.asSimpleType().getJavaType());
            }
        }

        return ApiResponse.of(result);
    }

    protected static final class SqlOperationDef {
        private final String name;
        private final String path;
        private final String key;
        private final String script;

        public SqlOperationDef(String name, String path, String key, String script) {
            this.name = name;
            this.path = path;
            this.key = key;
            this.script = script;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public String getKey() {
            return key;
        }

        public String getScript() {
            return script;
        }
    }

}
