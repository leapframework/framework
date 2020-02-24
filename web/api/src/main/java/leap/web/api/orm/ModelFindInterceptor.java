/*
 *
 *  * Copyright 2019 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.web.api.orm;

import leap.core.value.Record;
import leap.orm.query.CriteriaQuery;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;

import java.util.Map;

public interface ModelFindInterceptor {

    default boolean processQueryOneOptions(ModelExecutionContext context, QueryOptionsBase options) {
        return false;
    }

    default boolean preQueryOne(ModelExecutionContext context) {
        return false;
    }

    default boolean preQueryOne(ModelExecutionContext context, Object id, CriteriaQuery query) {
        return false;
    }

    default Object processQueryOneRecord(ModelExecutionContext context, Object id, Record record) {
        return null;
    }

    default boolean preQueryOneByKey(ModelExecutionContext context, Map<String, Object> filters, CriteriaQuery query) {
        return false;
    }

    default Object processQueryOneRecordByKey(ModelExecutionContext context, Map<String, Object> filters, Record record) {
        return null;
    }

    default void completeQueryOne(ModelExecutionContext context, QueryOneResult result, Throwable e) {

    }
}
