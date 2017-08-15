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
package leap.web.api.restd;

import leap.core.annotation.Inject;
import leap.web.api.config.ApiConfigException;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.config.model.RestdConfig;
import leap.web.api.restd.crud.CrudOperation;

import java.util.Map;

public class RestdOperationProcessor extends CrudOperation implements RestdProcessor {

    protected @Inject Map<String,RestdOperationProvider> providers;

    @Override
    public void preProcessApi(ApiConfigurator c, RestdContext context) {
        context.getConfig().getOperations().values().forEach(o -> {
            provider(o).createApiOperation(context, o);
        });
    }

    @Override
    public void preProcessModel(ApiConfigurator c, RestdContext context, RestdModel model) {
        RestdConfig.Model mc = context.getConfig().getModel(model.getName());
        if(null == mc) {
            return;
        }

        mc.getOperations().values().forEach(o -> {
            provider(o).createModelOperation(context, model, o);
        });
    }

    protected RestdOperationProvider provider(RestdConfig.Operation o) {
        String type = o.getType();
        RestdOperationProvider provider = providers.get(type);

        if(null == provider) {
            throw new ApiConfigException("Restd operation type '" + type + "' not found");
        }

        return provider;
    }
}