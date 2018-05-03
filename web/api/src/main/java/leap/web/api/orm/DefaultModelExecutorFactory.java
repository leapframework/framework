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

package leap.web.api.orm;

import leap.core.annotation.Inject;
import leap.lang.annotation.Init;

public class DefaultModelExecutorFactory implements ModelExecutorFactory {

    protected @Inject ModelQueryHandler       queryHandler;
    protected @Inject ModelQueryInterceptor[] queryInterceptors;
    protected @Inject ModelCreateHandler      createHandler;
    protected @Inject ModelUpdateHandler      updateHandler;
    protected @Inject ModelDeleteHandler      deleteHandler;

    private ModelQueryExtension queryExtension;

    @Init
    private void init() {
        this.queryExtension = new ModelQueryExtension(queryHandler, queryInterceptors);
    }

    @Override
    public ModelCreateExecutor newCreateExecutor(ModelExecutorContext context) {
        return new DefaultModelCreateExecutor(context, createHandler);
    }

    @Override
    public ModelUpdateExecutor newUpdateExecutor(ModelExecutorContext context) {
        return new DefaultModelUpdateExecutor(context, updateHandler);
    }

    @Override
    public ModelDeleteExecutor newDeleteExecutor(ModelExecutorContext context) {
        return new DefaultModelDeleteExecutor(context, deleteHandler);
    }

    @Override
    public ModelQueryExecutor newQueryExecutor(ModelExecutorContext context) {
        return new DefaultModelQueryExecutor(context, queryExtension);
    }

}