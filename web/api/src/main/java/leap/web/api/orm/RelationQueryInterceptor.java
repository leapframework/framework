/*
 *  Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.api.orm;

import leap.core.value.Record;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;

import java.util.List;

public interface RelationQueryInterceptor {

    default boolean preRelateQueryOne(RelationExecutionContext context, Object relatedId, QueryOptionsBase options) {
        return false;
    }

    default boolean postRelateQueryOne(RelationExecutionContext context, Object relatedId, Record record) {
        return false;
    }

    default boolean completeRelateQueryOne(RelationExecutionContext context, Object relatedId, QueryOneResult result) {
        return false;
    }

    default boolean preRelateQueryList(RelationExecutionContext context, Object relatedId, QueryOptions options) {
        return false;
    }

    default boolean postRelateQueryList(RelationExecutionContext context, Object relatedId, List<Record> records) {
        return false;
    }

    default boolean completeRelateQueryList(RelationExecutionContext context, Object relatedId, QueryListResult result) {
        return false;
    }
}