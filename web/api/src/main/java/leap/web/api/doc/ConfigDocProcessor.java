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

package leap.web.api.doc;

import leap.lang.Described;
import leap.lang.Titled;
import leap.web.api.config.model.ModelConfig;
import leap.web.api.config.model.ParamConfig;
import leap.web.api.meta.ApiMetadataBuilder;
import leap.web.api.meta.ApiMetadataContext;
import leap.web.api.meta.ApiMetadataProcessor;
import leap.web.api.meta.model.MApiModelBuilder;
import leap.web.api.meta.model.MApiParameterBaseBuilder;
import leap.web.api.meta.model.MApiParameterBuilder;
import leap.web.api.meta.model.MApiPropertyBuilder;

/**
 * Reads doc from configurations.
 */
public class ConfigDocProcessor implements ApiMetadataProcessor {

    @Override
    public void postProcess(ApiMetadataContext context, ApiMetadataBuilder m) {
        //parameters
        m.getPaths().forEach((k,p) -> {
            p.getOperations().forEach(o -> {
                o.getParameters().forEach(param -> processParameter(context, param));
            });
        });

        //models
        m.getModels().forEach((k,model) -> {
            processModel(context, model);
        });
    }

    protected void processParameter(ApiMetadataContext context, MApiParameterBuilder p) {
        ParamConfig pc = null;

        if(null != p.getWrapperArgument()) {
            ParamConfig wrapperParam = context.getConfig().getParam(p.getWrapperArgument().getType());
            if(null != wrapperParam) {
                pc = wrapperParam.getWrappedParam(p.getArgument().getDeclaredName());
            }
        }

        trySetDoc(p, pc, pc);
    }

    protected void processModel(ApiMetadataContext context, MApiModelBuilder m) {
        ModelConfig mc = null;

        if(null != m.getJavaType()) {
            mc = context.getConfig().getModel(m.getJavaType());
        }else{
            mc = context.getConfig().getModel(m.getName());
        }

        //configure the model properties.
        for(MApiPropertyBuilder p : m.getProperties().values()) {
            ModelConfig.Property c = null == mc ? null : mc.getProperty(p.getName());
            trySetDoc(p, c, c);
        }
    }

    protected void trySetDoc(MApiParameterBaseBuilder p, Titled titled, Described described) {
        if(null != titled) {
            p.trySetTitle(titled.getTitle());
        }

        if(null != described) {
            p.trySetSummary(described.getSummary());
            p.trySetDescription(described.getDescription());
        }

        if(!p.getName().equals(p.getTitle())) {
            p.trySetDescription(p.getTitle());
        }
    }
}