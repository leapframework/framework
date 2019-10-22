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
 *
 */

package leap.orm.query;

import leap.core.exception.EmptyRecordsException;
import leap.core.exception.TooManyRecordsException;
import leap.core.value.Scalar;
import leap.core.value.Scalars;

import java.util.List;

public interface QueryBase<T> {

    /**
     * Returns true if the result has records, or returns false if no records.
     */
    default boolean exists() {
        return firstOrNull() != null;
    }

    /**
     * Executes this query and return the query result.
     */
    QueryResult<T> result();

    /**
     * Executes this query and return the first row.
     *
     * @throwsh {@link EmptyRecordsException} if no result returned.
     *
     * @see QueryResult#first()
     */
    T first() throws EmptyRecordsException;

    /**
     * Executes this query and return the first row.
     *
     * <p>
     * Returns <code>null</code> if no result returned.
     *
     * @see QueryResult#firstOrNull()
     */
    T firstOrNull();

    /**
     * Executes this query and return the first row.
     *
     * @throws EmptyRecordsException if no result returned.
     * @throws TooManyRecordsException if two or more rows returned.
     *
     * @see QueryResult#single()
     */
    T single() throws EmptyRecordsException,TooManyRecordsException;

    /**
     * Executes this query and return the first row.
     *
     * <p>
     * Returns <code>null</code> if no result returned.
     *
     * @throws TooManyRecordsException if two or more rows returned.
     *
     * @see QueryResult#singleOrNull()
     */
    T singleOrNull() throws TooManyRecordsException;

    /**
     * Executes this query and return all the rows as a immutable {@link List} object.
     *
     * <p>
     * Returns a empty immutable {@link List} if no result returned.
     *
     * @see QueryResult#list()
     */
    List<T> list();

    /**
     * Returns the {@link Scalar} value in this query result.
     *
     * @throws EmptyRecordsException if no records.
     * @throws TooManyRecordsException if two or more records returned.
     */
    Scalar scalar() throws EmptyRecordsException, TooManyRecordsException;

    /**
     * Returns the {@link Scalar} value in this query result or <code>null</code> if no records returned.
     *
     * @throws TooManyRecordsException if two or more records returned.
     */
    Scalar scalarOrNull() throws TooManyRecordsException;

    /**
     * Returns the scalar value or <code>null</code> if n o records returned.
     *
     * @throws TooManyRecordsException if two or more records returned.
     */
    default <T> T scalarValueOrNull() throws TooManyRecordsException {
        Scalar scalar = scalarOrNull();
        return null == scalar ? null : (T)scalar.get();
    }

    /**
     * Returns the scalar value or <code>null</code> if n o records returned.
     *
     * @throws TooManyRecordsException if two or more records returned.
     */
    default <T> T scalarValueOrNull(Class<T> type) throws TooManyRecordsException {
        Scalar scalar = scalarOrNull();
        return null == scalar ? null : scalar.get(type);
    }

    /**
     * Returns all the scalar values of the first column in this query result.
     */
    Scalars scalars();
}
