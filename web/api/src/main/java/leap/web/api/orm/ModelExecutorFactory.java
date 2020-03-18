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

public interface ModelExecutorFactory {

    /**
     * Returns the {@link ModelExecutorHelper}.
     */
    ModelExecutorHelper getHelper();

    /**
     * Returns a new {@link ModelCreateExecutor}.
     */
    default ModelCreateExecutor newCreateExecutor(ModelExecutorContext context) {
        return newCreateExecutor(context, (ModelCreateInterceptor[])null);
    }

    /**
     * Returns a new {@link ModelCreateExecutor}.
     */
    ModelCreateExecutor newCreateExecutor(ModelExecutorContext context, ModelCreateInterceptor... interceptors);

    /**
     * Returns a new {@link ModelUpdateExecutor}.
     */
    default ModelUpdateExecutor newUpdateExecutor(ModelExecutorContext context) {
        return newUpdateExecutor(context, (ModelUpdateInterceptor[])null);
    }

    /**
     * Returns a new {@link ModelUpdateExecutor}.
     */
    ModelUpdateExecutor newUpdateExecutor(ModelExecutorContext context, ModelUpdateInterceptor... interceptors);

    /**
     * Returns a new {@link ModelDeleteExecutor}.
     */
    default ModelDeleteExecutor newDeleteExecutor(ModelExecutorContext context) {
        return newDeleteExecutor(context, (ModelDeleteInterceptor[])null);
    }

    /**
     * Returns a new {@link ModelDeleteExecutor}.
     */
    ModelDeleteExecutor newDeleteExecutor(ModelExecutorContext context, ModelDeleteInterceptor... interceptors);

    /**
     * Returns a new {@link ModelQueryExecutor}.
     */
    default ModelQueryExecutor newQueryExecutor(ModelExecutorContext context) {
        return newQueryExecutor(context, (ModelQueryInterceptor[])null);
    }

    /**
     * Returns a new {@link ModelQueryExecutor} for executing query or find operation.
     */
    ModelQueryExecutor newQueryExecutor(ModelExecutorContext context, ModelQueryInterceptor... interceptors);

    /**
     * Returns a new {@link ModelQueryExecutor} for executing find operation.
     */
    ModelQueryExecutor newQueryExecutor(ModelExecutorContext context, ModelFindInterceptor... interceptors);

    /**
     * Returns a new {@link RelationQueryExecutor}.
     */
    default RelationQueryExecutor newRelationQueryExecutor(RelationExecutorContext context) {
        return newRelationQueryExecutor(context, (RelationQueryInterceptor[])null);
    }

    /**
     * Returns a new {@link RelationQueryExecutor}.
     */
    RelationQueryExecutor newRelationQueryExecutor(RelationExecutorContext context, RelationQueryInterceptor... interceptors);
}
