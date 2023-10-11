/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.orm.query;

import leap.core.jdbc.ResultSetReader;
import leap.lang.annotation.Nullable;
import leap.lang.beans.DynaBean;
import leap.lang.params.ArrayParams;
import leap.lang.params.Params;
import leap.lang.value.Limit;
import leap.lang.value.Page;
import leap.orm.model.Model;

import java.util.Map;

public interface Query<T> extends QueryBase<T> {

    /**
     * Returns the {@link QueryValidator}.
     */
    QueryValidator getValidator();

    /**
     * {@link QueryValidator} is used to validate unsafe methods.
     */
    Query<T> withValidator(QueryValidator validator);

    /**
     * Returns the order by expression or null.
     */
    String getOrderBy();

    /**
     * Sets the given name and value as query parameter.
     */
    Query<T> param(String name, Object value);

    /**
     * Sets all values in the given map as query parameters.
     */
    Query<T> params(@Nullable Map<String, Object> map);

    /**
     * Sets all values in the given {@link Params} as query parameters.
     */

    Query<T> params(@Nullable Params params);

    /**
     * Sets all values in the given {@link Params} as query parameters.
     */

    default Query<T> params(@Nullable Model model) {
        return params(model.fields());
    }

    /**
     * Sets all properties in the given pojo bean as query parameters.
     */
    Query<T> params(@Nullable DynaBean bean);

    /**
     * Sets the single arg as params.
     */
    default Query<T> param(Object arg) {
        return params(new Object[]{arg});
    }

    /**
     * Sets a {@link ArrayParams} for jdbc placeholders in this query.
     */
    default Query<T> params(Object[] args) {
        return params(new ArrayParams(args));
    }

    /**
     * Sets the order by expression in this query.
     *
     * <p>
     * An order by expression is a standand sql order by clause without "order by" prefix..
     *
     * <p>
     * For example :
     * <pre>
     * 	orderBy("col1 asc,col2 desc")
     * </pre>
     */
    Query<T> orderBy(String expression);

    /**
     * Sets the order by expression in this query after validate the expression by {@link QueryValidator}.
     */
    Query<T> unsafeOrderBy(String expression);

    /**
     * Sets to query results limit to the given rows.
     *
     * <p>
     * Starts from 1.
     */
    Query<T> limit(Integer rows);

    /**
     * Sets to query results in the range of the given start and end rows.
     *
     * <p>
     * Starts from 1.
     */
    Query<T> limit(int startRows, int endRows);

    /**
     * Sets to query results in the range of the given page index and size.
     *
     * @see Page
     */
    Query<T> limit(Limit limit);

    /**
     * Select for update.
     */
    Query<T> forUpdate();

    /**
     * todo : doc
     */
    Query<T> withoutFilterColumn();

    /**
     * todo : doc
     */
    Query<T> withoutQueryFilter();

    /**
     * Executes this query and return the query result.
     */
    QueryResult<T> result(Limit limit);

    /**
     * Creates a {@link PageResult} for querying the records in the range of given {@link Page} only.
     */
    PageResult<T> pageResult(Page page);

    /**
     * Creates a {@link PageResult} for querying the records in the range of given page index and page size only.
     */
    default PageResult<T> pageResult(int index, int size) {
        return pageResult(Page.indexOf(index, size));
    }

    /**
     * Executes query and use the given {@link ResultSetReader} to read the result.
     */
    <R> R executeQuery(ResultSetReader<R> reader);

    /**
     * Executes a count(*) query and returns the total count of records.
     */
    long count();
}