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
import leap.lang.meta.MCollectionType;
import leap.lang.meta.MComplexTypeRef;
import leap.lang.meta.MSimpleTypes;
import leap.orm.mapping.FieldMapping;
import leap.web.App;
import leap.web.action.Argument;
import leap.web.action.ArgumentBuilder;
import leap.web.action.FuncActionBuilder;
import leap.web.api.meta.model.*;
import leap.web.api.mvc.ApiResponse;
import leap.web.api.orm.ModelExecutorFactory;
import leap.web.api.restd.RestdContext;
import leap.web.api.restd.RestdModel;
import leap.web.api.restd.RestdOperationBase;
import leap.web.api.restd.RestdProcessor;

import java.util.Map;

public abstract class CrudOperation extends RestdOperationBase implements RestdProcessor {

    protected @Inject ModelExecutorFactory mef;

    protected boolean isOpeationExists(App app, String verb, String path) {
        return null != app.routes().match(verb, path);
    }

    protected ArgumentBuilder addModelArgument(FuncActionBuilder action,RestdModel model) {
        ArgumentBuilder a = newModelArgument(model);

        action.addArgument(a);

        return a;
    }

    protected ArgumentBuilder addIdArgument(FuncActionBuilder action,RestdModel model) {
        ArgumentBuilder a = newIdArgument(model);

        action.addArgument(a);

        return a;
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

    protected ArgumentBuilder newIdArgument(RestdModel model) {
        ArgumentBuilder a = new ArgumentBuilder();

        FieldMapping id = model.getEntityMapping().getKeyFieldMappings()[0];

        a.setName(id.getFieldName());
        a.setLocation(Argument.Location.PATH_PARAM);
        a.setType(id.getJavaType());
        a.setRequired(true);
        a.setExtension(newIdParameter(model));

        return a;
    }

    private MApiParameterBuilder newIdParameter(RestdModel model) {

        //todo : check id fields.
        FieldMapping id = model.getEntityMapping().getKeyFieldMappings()[0];

        MApiParameterBuilder p = new MApiParameterBuilder();
        p.setName(id.getFieldName());
        p.setLocation(MApiParameter.Location.PATH);
        p.setRequired(true);
        p.setType(id.getDataType());

        return p;
    }

    protected void configure(RestdContext context, RestdModel model, FuncActionBuilder action) {
        action.setExtension(new MApiTag[]{new MApiTag(model.getName())});
    }

}