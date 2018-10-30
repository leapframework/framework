/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.api.restd;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.meta.MCollectionType;
import leap.lang.meta.MComplexTypeRef;
import leap.lang.meta.MSimpleTypes;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.web.action.ActionParams;
import leap.web.action.Argument;
import leap.web.action.ArgumentBuilder;
import leap.web.action.FuncActionBuilder;
import leap.web.api.Api;
import leap.web.api.meta.model.*;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.orm.ModelExecutorFactory;
import leap.web.route.RouteBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class CrudOperationBase extends RestdOperationBase {

    protected @Inject ModelExecutorFactory mef;

    protected void setApiExtension(RouteBuilder route, String name, Object value) {
        MApiExtension extension = route.getExtension(MApiExtension.class);
        if(null == extension) {
            extension = new MApiExtension();
            route.setExtension(extension);
        }
        extension.setAttribute(name, value);
    }

    protected void setCrudOperation(RouteBuilder route, String operation) {
        setApiExtension(route, "crud", operation);
    }

    protected ArgumentBuilder addModelArgumentForCreate(RestdContext context, FuncActionBuilder action, RestdModel model) {
        ArgumentBuilder a = newModelArgument(model);

        for(RestdArgumentSupport vs : argumentSupports) {
            vs.processModelArgumentForCreate(context, model, a);
        }

        action.addArgument(a);

        return a;
    }

    protected ArgumentBuilder addModelArgumentForUpdate(RestdContext context, FuncActionBuilder action, RestdModel model) {
        ArgumentBuilder a = newModelArgument(model);

        for(RestdArgumentSupport vs : argumentSupports) {
            vs.processModelArgumentForUpdate(context, model, a);
        }

        action.addArgument(a);

        return a;
    }

    protected ArgumentBuilder addModelArgumentForReplace(RestdContext context, FuncActionBuilder action, RestdModel model) {
        ArgumentBuilder a = newModelArgument(model);

        for(RestdArgumentSupport vs : argumentSupports) {
            vs.processModelArgumentForReplace(context, model, a);
        }

        action.addArgument(a);

        return a;
    }

    protected void addIdArguments(RestdContext context, FuncActionBuilder action, RestdModel model) {
        for(FieldMapping id : model.getEntityMapping().getKeyFieldMappings()) {
            ArgumentBuilder a = newIdArgument(model, id);

            for (RestdArgumentSupport vs : argumentSupports) {
                vs.processIdArgument(context, model, a);
            }

            action.addArgument(a);
        }
    }

    protected MApiResponseBuilder addModelResponse(FuncActionBuilder action, RestdModel model) {
        action.setReturnType(ApiResponse.class);

        MApiResponseBuilder r = newModelResponse(model);
        r.setDescription("Success");

        action.setExtension(new MApiResponseBuilder[]{r});

        return r;
    }

    protected MApiResponseBuilder addNoContentResponse(FuncActionBuilder action, RestdModel model) {
        action.setReturnType(ApiResponse.class);

        MApiResponseBuilder r = new MApiResponseBuilder();
        r.setStatus(204);
        r.setDescription("Success");

        action.setExtension(new MApiResponseBuilder[]{r});

        return r;
    }

    protected MApiResponseBuilder addModelQueryResponse(FuncActionBuilder action, RestdModel model) {
        action.setReturnType(ApiResponse.class);

        MApiResponseBuilder r = newModelQueryResponse(model);

        action.setExtension(new MApiResponseBuilder[]{r});

        return r;
    }

    protected MApiResponseBuilder addModelCountResponse(FuncActionBuilder action, RestdModel model) {
        action.setReturnType(Long.class);

        MApiResponseBuilder r = new MApiResponseBuilder();

        r.setStatus(200);
        r.setType(MSimpleTypes.BIGINT);
        r.setDescription("Success");

        return r;
    }

    protected MApiResponseBuilder newModelResponse(RestdModel model) {
        MApiResponseBuilder r = new MApiResponseBuilder();

        r.setStatus(200);
        r.setType(new MComplexTypeRef(model.getName()));
        r.setDescription("Success");

        return r;
    }

    private MApiResponseBuilder newModelQueryResponse(RestdModel model) {
        MApiResponseBuilder r = new MApiResponseBuilder();

        r.setStatus(200);
        r.setType(new MCollectionType(new MComplexTypeRef(model.getName())));
        r.setDescription("Success");

        MApiHeaderBuilder header = new MApiHeaderBuilder();
        header.setName("X-Total-Count");
        header.setType(MSimpleTypes.BIGINT);
        header.setDescription("The total count of query records.");
        r.addHeader(header);

        return r;
    }

    protected ArgumentBuilder newModelArgument(RestdModel model) {
        ArgumentBuilder a = new ArgumentBuilder();

        a.setName(model.getName());
        a.setLocation(Argument.Location.REQUEST_BODY);
        a.setType(Map.class);
        a.setRequired(true);
        a.setExtension(newModelParameter(model));

        return a;
    }

    private MApiParameterBuilder newModelParameter(RestdModel model) {

        MApiParameterBuilder p = new MApiParameterBuilder();
        p.setName(Strings.lowerCamel(model.getName()));
        p.setLocation(MApiParameter.Location.BODY);
        p.setRequired(true);
        p.setType(new MComplexTypeRef(model.getName()));

        return p;
    }

    protected ArgumentBuilder newIdArgument(RestdModel model, FieldMapping id) {
        ArgumentBuilder a = new ArgumentBuilder();

        a.setName(id.getFieldName());
        a.setLocation(Argument.Location.PATH_PARAM);
        a.setType(id.getJavaType());
        a.setRequired(true);
        a.setExtension(newIdParameter(model, id));

        return a;
    }

    protected String getIdPath(RestdModel model) {
        return CrudUtils.getIdPathTemplate(model);
    }

    private MApiParameterBuilder newIdParameter(RestdModel model, FieldMapping id) {
        MApiParameterBuilder p = new MApiParameterBuilder();
        p.setName(id.getFieldName());
        p.setLocation(MApiParameter.Location.PATH);
        p.setRequired(true);
        p.setType(id.getDataType());
        return p;
    }

    protected abstract static class CrudFunction implements Function<ActionParams, Object> {
        protected final Api        api;
        protected final Dao        dao;
        protected final RestdModel model;
        protected final EntityMapping em;

        protected final int idLen;

        protected CrudFunction(Api api, Dao dao, RestdModel model) {
            this.api = api;
            this.dao = dao;
            this.model = model;
            this.em = model.getEntityMapping();
            this.idLen = em.getKeyFieldMappings().length;
        }

        protected final Object id(ActionParams params) {
            if(idLen > 1) {
                Map id = new HashMap(idLen);
                for(int i=0;i<idLen;i++) {
                    id.put(em.getKeyFieldNames()[i], params.get(i));
                }
                return id;
            }else {
                return params.get(0);
            }
        }

        protected final Map record(ActionParams params) {
            return params.get(idLen);
        }
    }

}