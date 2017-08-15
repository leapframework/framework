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

import leap.core.exception.EmptyRecordsException;
import leap.core.exception.TooManyRecordsException;
import leap.core.value.Scalar;
import leap.core.value.Scalars;
import leap.lang.annotation.Nullable;
import leap.lang.beans.DynaBean;
import leap.lang.params.ArrayParams;
import leap.lang.params.Params;
import leap.lang.value.Limit;
import leap.lang.value.Page;
import leap.orm.model.Model;

import java.util.List;
import java.util.Map;

public interface Query<T> {
	
	/**
	 * Sets the given name and value as query parameter.
	 */
	Query<T> param(String name,Object value);
	
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
	Query<T> limit(int startRows,int endRows);
	
	/**
	 * Sets to query results in the range of the given page index and size.
	 * 
	 * @see Page
	 */
	Query<T> limit(Limit limit);

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
	QueryResult<T> result();
	
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
	default PageResult<T> pageResult(int index,int size) {
		return pageResult(Page.indexOf(index, size));
	}

	/**
	 * Returns true if the result has records, or returns false if no records.
     */
	default boolean exists() {
		return firstOrNull() != null;
	}
	
	/**
	 * Executes a count(*) query and returns the total count of records.
	 */
	long count();
	
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