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
import leap.web.api.config.ApiConfigurator;

import java.util.Map;

public class CrudOperationProcessor implements RestdProcessor {

    protected @Inject Map<String, CrudOperation> operations;

    @Override
    public void preProcessModel(ApiConfigurator c, RestdContext context, RestdModel model) {
        operations.values().forEach(operation -> {
            operation.createCrudOperation(c, context, model);
        });
    }

}