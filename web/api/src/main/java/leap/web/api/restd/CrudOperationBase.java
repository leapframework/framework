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
import leap.core.web.path.PathTemplate;
import leap.core.web.path.PathTemplateFactory;
import leap.lang.Objects2;
import leap.lang.Strings;
import leap.lang.meta.MCollectionType;
import leap.lang.meta.MComplexTypeRef;
import leap.lang.meta.MSimpleTypes;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.web.action.*;
import leap.web.api.Api;
import leap.web.api.meta.model.*;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.orm.ModelExecutorFactory;
import leap.web.exception.BadRequestException;
import leap.web.route.RouteBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class CrudOperationBase extends RestdOperationBase {

    protected @Inject ModelExecutorFactory mef;
    protected @Inject PathTemplateFactory  ptf;

    protected void setApiExtension(RouteBuilder route, String name, Object value) {
        MApiExtension extension = route.getExtension(MApiExtension.class);
        if (null == extension) {
            extension = new MApiExtension();
            route.setExtension(extension);
        }
        extension.setAttribute(name, value);
    }

    protected void setCrudOperation(RouteBuilder route, String operation) {
        setApiExtension(route, "crud", operation);
    }

    protected int addPathArguments(RestdContext context, RestdModel model, String path, FuncActionBuilder action) {
        return addPathArguments(context, model, path, action, null);
    }

    protected int addPathArguments(RestdContext context, RestdModel model, String path, FuncActionBuilder action, Function<String, String> mapping) {
        PathTemplate pt = ptf.createPathTemplate(path);
        if (!pt.hasVariables()) {
            return 0;
        }

        int i = 0;
        for (String name : pt.getTemplateVariables()) {
            if(action.getArguments().stream().anyMatch(a -> a.getName().equals(name) && a.isPathParam())) {
                continue;
            }

            FieldMapping fm = getParamFieldMapping(model.getEntityMapping(), name, mapping);
            if(null != fm && fm.isPrimaryKey()) {
                addIdArgument(context, action, model, fm);
            }else {
                //            MApiParameterBuilder p = new MApiParameterBuilder();
                //            p.setName(name);
                //            p.setLocation(MApiParameter.Location.PATH);
                //            p.setRequired(true);
                //            p.setType(null == fm ? MSimpleTypes.STRING : fm.getDataType());

                ArgumentBuilder a = new ArgumentBuilder();
                a.setName(name);
                a.setLocation(Argument.Location.PATH_PARAM);
                a.setType(null == fm ? String.class : fm.getJavaType());
                a.setRequired(true);
                //            a.setExtension(p);
                action.addArgument(a);
            }
            i++;
        }
        return i;
    }

    private static FieldMapping getParamFieldMapping(EntityMapping em, String name, Function<String, String> mapping) {
        if (null != mapping) {
            String s = mapping.apply(name);
            if (null != s) {
                name = s;
            }
        }

        FieldMapping fm = em.tryGetFieldMapping(name);
        if (null != fm) {
            return fm;
        }

        if (name.indexOf('_') > 0) {
            fm = em.tryGetFieldMapping(Strings.lowerCamel(name, '_'));
            if (null != fm) {
                return fm;
            }
        }
        return null;
    }

    protected ArgumentBuilder addModelArgumentForCreate(RestdContext context, FuncActionBuilder action, RestdModel model) {
        ArgumentBuilder a = newModelArgument(model);

        for (RestdArgumentSupport vs : argumentSupports) {
            vs.processModelArgumentForCreate(context, model, a);
        }

        a.setProcessor(new CreateRecordProcessor(model));
        action.addArgument(a);

        return a;
    }

    protected ArgumentBuilder addModelArgumentForUpdate(RestdContext context, FuncActionBuilder action, RestdModel model) {
        ArgumentBuilder a = newModelArgument(model);

        for (RestdArgumentSupport vs : argumentSupports) {
            vs.processModelArgumentForUpdate(context, model, a);
        }

        action.addArgument(a);

        return a;
    }

    protected ArgumentBuilder addModelArgumentForReplace(RestdContext context, FuncActionBuilder action, RestdModel model) {
        ArgumentBuilder a = newModelArgument(model);

        for (RestdArgumentSupport vs : argumentSupports) {
            vs.processModelArgumentForReplace(context, model, a);
        }

        action.addArgument(a);

        return a;
    }

    private void addIdArguments(RestdContext context, FuncActionBuilder action, RestdModel model) {
        for (FieldMapping id : model.getEntityMapping().getKeyFieldMappings()) {
            addIdArgument(context, action, model, id);
        }
    }

    protected void addIdArgument(RestdContext context, FuncActionBuilder action, RestdModel model, FieldMapping id) {
        ArgumentBuilder a = newIdArgument(model, id);

        for (RestdArgumentSupport vs : argumentSupports) {
            vs.processIdArgument(context, model, a);
        }

        action.addArgument(a);
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

    protected MApiResponseBuilder addModelQueryResponse(FuncActionBuilder action, String typeName) {
        action.setReturnType(ApiResponse.class);

        MApiResponseBuilder r = newModelQueryResponse(typeName);

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

    protected MApiResponseBuilder newModelQueryResponse(RestdModel model) {
        return newModelQueryResponse(model.getName());
    }

    protected MApiResponseBuilder newModelQueryResponse(String typeName) {
        MApiResponseBuilder r = new MApiResponseBuilder();

        r.setStatus(200);
        r.setType(new MCollectionType(new MComplexTypeRef(typeName)));
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

    public interface Callback {
        default void preAddArguments(FuncActionBuilder action) {

        }

        default void postAddArguments(FuncActionBuilder action) {

        }
    }

    protected abstract static class CrudFunction implements Function<ActionParams, Object> {
        protected final Api            api;
        protected final Dao            dao;
        protected final RestdModel     model;
        protected final EntityMapping  em;
        protected final FieldMapping[] id;

        private MApiModel am;

        protected CrudFunction(Api api, Dao dao, RestdModel model) {
            this.api = api;
            this.dao = dao;
            this.model = model;
            this.em = model.getEntityMapping();
            this.id = em.getKeyFieldMappings();
        }

        protected MApiModel am() {
            if (null == am) {
                am = api.getMetadata().getModel(model.getName());
            }
            return am;
        }

        protected final Object id(ActionParams params) {
            if (id.length > 1) {
                Map m = new HashMap(id.length);
                for (FieldMapping fm : id) {
                    m.put(fm.getFieldName(), params.get(fm.getFieldName()));
                }
                return m;
            } else {
                return params.get(id[0].getFieldName());
            }
        }

        protected <T> T get(ActionParams params, String name) {
            return (T) params.get(name);
        }

        protected <T> T get(ActionParams params, Class<T> type) {
            for (int i = 0; i < params.getArguments().length; i++) {
                if (params.getArguments()[i].getType().equals(type)) {
                    return (T) params.get(i);
                }
            }
            throw new IllegalStateException("No argument with type '" + type + "'");
        }

        protected Map<String, Object> getModelRecord(ActionParams params) {
            return (Map<String, Object>) params.get(model.getName());
        }
    }

    protected static class CreateRecordProcessor implements ArgumentProcessor {
        private final EntityMapping em;

        private FieldMapping[] fieldsAtPath;

        public CreateRecordProcessor(RestdModel model) {
            this.em = model.getEntityMapping();
        }

        @Override
        public Object postProcessArgument(ActionParams params, Argument argument, Object value) {
            if (value instanceof Map) {
                if (null == fieldsAtPath) {
                    List<FieldMapping> list = new ArrayList<>();
                    for (Argument a : params.getArguments()) {
                        if (Argument.Location.PATH_PARAM == a.getLocation()) {
                            FieldMapping field = em.tryGetFieldMapping(a.getName());
                            if (null != field) {
                                list.add(field);
                            }
                        }
                    }
                    fieldsAtPath = list.toArray(new FieldMapping[0]);
                }

                if (fieldsAtPath.length > 0) {
                    Map<String, Object> record = (Map<String, Object>) value;
                    for (FieldMapping field : fieldsAtPath) {
                        final String name = field.getFieldName();

                        Object valueAtPath = params.get(name);
                        Object valueAtRecord = record.get(name);
                        if(isValueChanged(field, valueAtPath, valueAtRecord)) {
                            throw new BadRequestException("Can't change '" + name + "' from value '" + valueAtPath +
                                    "' at path to value '" + valueAtRecord + "' at creating record");
                        }
                        record.put(field.getFieldName(), params.get(field.getFieldName()));
                    }
                }
            }
            return value;
        }

        private boolean isValueChanged(FieldMapping field, Object valueAtPath, Object valueAtRecord) {
            if(null == valueAtRecord) {
                return false;
            }
            boolean same;
            if(valueAtPath instanceof String) {
                same = valueAtPath.equals(valueAtRecord.toString());
            }else if(valueAtRecord instanceof String){
                same = valueAtPath.toString().equals(valueAtRecord);
            }else {
                same = valueAtPath.equals(valueAtRecord);
            }
            return !same;
        }
    }
}