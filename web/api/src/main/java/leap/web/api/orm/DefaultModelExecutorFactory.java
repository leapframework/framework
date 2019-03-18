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
import leap.lang.Arrays2;
import leap.lang.annotation.Init;
import leap.web.api.remote.RestResourceFactory;

public class DefaultModelExecutorFactory implements ModelExecutorFactory {

    protected @Inject ModelCreateHandler       createHandler;
    protected @Inject ModelCreateInterceptor[] createInterceptors;

    protected @Inject ModelUpdateHandler        updateHandler;
    protected @Inject ModelUpdateInterceptor[]  updateInterceptors;
    protected @Inject ModelReplaceInterceptor[] replaceInterceptors;

    protected @Inject ModelQueryHandler       queryHandler;
    protected @Inject ModelQueryInterceptor[] queryInterceptors;

    protected @Inject ModelDeleteHandler       deleteHandler;
    protected @Inject ModelDeleteInterceptor[] deleteInterceptors;

    protected @Inject RelationQueryInterceptor[] relationQueryInterceptors;

    protected @Inject RestResourceFactory restResourceFactory;

    private ModelCreateExtension   createExtension;
    private ModelUpdateExtension   updateExtension;
    private ModelQueryExtension    queryExtension;
    private ModelDeleteExtension   deleteExtension;
    private RelationQueryExtension relationQueryExtension;

    @Init
    private void init() {
        this.createExtension = new ModelCreateExtension(createHandler, createInterceptors);
        this.updateExtension = new ModelUpdateExtension(updateHandler, updateInterceptors, replaceInterceptors);
        this.queryExtension = new ModelQueryExtension(queryHandler, queryInterceptors);
        this.deleteExtension = new ModelDeleteExtension(deleteHandler, deleteInterceptors);
        this.relationQueryExtension = new RelationQueryExtension(relationQueryInterceptors);
    }

    @Override
    public ModelCreateExecutor newCreateExecutor(ModelExecutorContext context, ModelCreateInterceptor... interceptors) {
        ModelCreateExtension extension =
                Arrays2.isEmpty(interceptors) ?
                        this.createExtension :
                        new ModelCreateExtension(createHandler, Arrays2.concat(interceptors, createInterceptors));

        return new DefaultModelCreateExecutor(handleContext(context), extension);
    }

    @Override
    public ModelUpdateExecutor newUpdateExecutor(ModelExecutorContext context, ModelUpdateInterceptor... interceptors) {
        ModelUpdateExtension extension =
                Arrays2.isEmpty(interceptors) ?
                        this.updateExtension :
                        new ModelUpdateExtension(updateHandler, Arrays2.concat(interceptors, updateInterceptors), replaceInterceptors);

        return new DefaultModelUpdateExecutor(handleContext(context), extension);
    }

    @Override
    public ModelDeleteExecutor newDeleteExecutor(ModelExecutorContext context, ModelDeleteInterceptor... interceptors) {
        ModelDeleteExtension extension =
                Arrays2.isEmpty(interceptors) ?
                        this.deleteExtension :
                        new ModelDeleteExtension(deleteHandler, Arrays2.concat(interceptors, deleteInterceptors));

        return new DefaultModelDeleteExecutor(handleContext(context), extension);
    }

    @Override
    public ModelQueryExecutor newQueryExecutor(ModelExecutorContext context, ModelQueryInterceptor... interceptors) {
        ModelQueryExtension extension =
                Arrays2.isEmpty(interceptors) ?
                        this.queryExtension :
                        new ModelQueryExtension(queryHandler, Arrays2.concat(interceptors, queryInterceptors));

        return new DefaultModelQueryExecutor(handleContext(context), extension);
    }

    @Override
    public RelationQueryExecutor newRelationQueryExecutor(RelationExecutorContext context, RelationQueryInterceptor... interceptors) {
        RelationQueryExtension extension =
                Arrays2.isEmpty(interceptors) ?
                        this.relationQueryExtension :
                        new RelationQueryExtension(Arrays2.concat(interceptors, relationQueryInterceptors));

        return new DefaultRelationQueryExecutor(handleContext(context), extension);
    }

    protected <T extends ModelExecutorContext> T handleContext(T context) {
        context.setRestResourceFactory(restResourceFactory);
        return context;
    }
}