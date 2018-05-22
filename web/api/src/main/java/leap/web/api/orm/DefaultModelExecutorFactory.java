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

    protected @Inject ModelCreateHandler       createHandler;
    protected @Inject ModelCreateInterceptor[] createInterceptors;

    protected @Inject ModelUpdateHandler       updateHandler;
    protected @Inject ModelUpdateInterceptor[] updateInterceptors;
    protected @Inject ModelReplaceInterceptor[] replaceInterceptors;

    protected @Inject ModelQueryHandler        queryHandler;
    protected @Inject ModelQueryInterceptor[]  queryInterceptors;

    protected @Inject ModelDeleteHandler       deleteHandler;
    protected @Inject ModelDeleteInterceptor[] deleteInterceptors;

    private ModelCreateExtension createExtension;
    private ModelUpdateExtension updateExtension;
    private ModelQueryExtension  queryExtension;
    private ModelDeleteExtension deleteExtension;

    @Init
    private void init() {
        this.createExtension = new ModelCreateExtension(createHandler, createInterceptors);
        this.updateExtension = new ModelUpdateExtension(updateHandler, updateInterceptors, replaceInterceptors);
        this.queryExtension  = new ModelQueryExtension(queryHandler, queryInterceptors);
        this.deleteExtension = new ModelDeleteExtension(deleteHandler, deleteInterceptors);
    }

    @Override
    public ModelCreateExecutor newCreateExecutor(ModelExecutorContext context) {
        return new DefaultModelCreateExecutor(context, createExtension);
    }

    @Override
    public ModelUpdateExecutor newUpdateExecutor(ModelExecutorContext context) {
        return new DefaultModelUpdateExecutor(context, updateExtension);
    }

    @Override
    public ModelDeleteExecutor newDeleteExecutor(ModelExecutorContext context) {
        return new DefaultModelDeleteExecutor(context, deleteExtension);
    }

    @Override
    public ModelQueryExecutor newQueryExecutor(ModelExecutorContext context) {
        return new DefaultModelQueryExecutor(context, queryExtension);
    }

}