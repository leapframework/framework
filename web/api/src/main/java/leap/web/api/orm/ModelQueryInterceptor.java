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
import leap.lang.jdbc.WhereBuilder;
import leap.orm.query.CriteriaQuery;
import leap.orm.query.PageResult;
import leap.web.api.mvc.params.QueryOptions;

import java.util.List;
import java.util.Map;

public interface ModelQueryInterceptor extends ModelFindInterceptor {

    default boolean processQueryListOptions(ModelExecutionContext context, QueryOptions options) {
        return false;
    }

    default boolean preProcessQueryListWhere(ModelExecutionContext context, QueryOptions options, WhereBuilder where, Map<String, Object> filters) {
        return preProcessQueryListWhere(context, options, where);
    }

    /**
     * @deprecated Use {@link #preProcessQueryListWhere(ModelExecutionContext, QueryOptions, WhereBuilder, Map)}.
     */
    @Deprecated
    default boolean preProcessQueryListWhere(ModelExecutionContext context, QueryOptions options, WhereBuilder where) {
        return false;
    }

    default boolean postProcessQueryListWhere(ModelExecutionContext context, QueryOptions options, WhereBuilder where) {
        return false;
    }

    default boolean preQueryList(ModelExecutionContext context, CriteriaQuery query) {
        return false;
    }

    default List<Record> executeQueryList(ModelExecutionContext context, QueryOptions options, CriteriaQuery<Record> query) {
        return null;
    }

    default Object processQueryListResult(ModelExecutionContext context, PageResult page, long totalCount, List<Record> records) {
        return null;
    }

    default void completeQueryList(ModelExecutionContext context, QueryListResult result, Throwable e) {

    }

    default boolean preCount(ModelExecutionContext context, CriteriaQuery query) {
        return false;
    }

    default void preExpand(ModelExecutionContext context) {

    }

    default void preExpand(ModelExecutionContext context, CriteriaQuery<Record> query) {

    }

    default void completeExpand(ModelExecutionContext context) {
        
    }
}