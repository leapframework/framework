/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package leap.web.api.orm;

import leap.core.value.Record;
import leap.web.api.mvc.params.QueryOptions;
import leap.web.api.mvc.params.QueryOptionsBase;

import java.util.List;

public interface ModelQueryInterceptor {

    default boolean processQueryOneOptions(ModelExecutorContext context, QueryOptionsBase options) {
        return false;
    }

    default Object processQueryOneRecord(ModelExecutorContext context, Object id, Record record) {
        return null;
    }

    default boolean processQueryListOptions(ModelExecutorContext context, QueryOptions options) {
        return false;
    }

    default boolean preProcessQueryListWhere(ModelExecutorContext context, QueryOptions options, StringBuilder where, List<Object> args) {
        return false;
    }

    default boolean postProcessQueryListWhere(ModelExecutorContext context, QueryOptions options, StringBuilder where, List<Object> args){
        return false;
    }

}