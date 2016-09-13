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

import java.util.Map;
import java.util.function.Predicate;

import leap.core.metamodel.ReservedMetaFieldName;
import leap.lang.annotation.Nullable;
import leap.lang.beans.DynaBean;
import leap.lang.params.ArrayParams;
import leap.lang.params.Params;
import leap.lang.value.Limit;
import leap.lang.value.Page;
import leap.orm.linq.Condition;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;


public interface CriteriaQuery<T> extends Query<T> {
	
	String DEFAULT_ALIAS_NAME = "t";

	/**
	 * Sets the given name and value as CriteriaQuery parameter.
	 */
	CriteriaQuery<T> param(String name,Object value);
	
	/**
	 * Sets all values in the given map as CriteriaQuery parameters.
	 */
	CriteriaQuery<T> params(@Nullable Map<String, Object> map);
	
	/**
	 * Sets all values in the given {@link Params} as CriteriaQuery parameters.
	 */
	CriteriaQuery<T> params(@Nullable Params params);
	
	/**
	 * Sets all properties in the given pojo bean as CriteriaQuery parameters. 
	 */
	CriteriaQuery<T> params(@Nullable DynaBean bean);
	
	/**
	 * Sets the single arg as params.
	 */
	default CriteriaQuery<T> param(Object arg) {
		return params(new Object[]{arg});
	}
	
	/**
	 * Sets a {@link ArrayParams} for jdbc placeholders in this CriteriaQuery.
	 */
	default CriteriaQuery<T> params(Object[] args) {
		return params(new ArrayParams(args));
	}
	
	/**
	 * Sets the order by expression in this CriteriaQuery.
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
	CriteriaQuery<T> orderBy(String expression);
	
	/**
	 * Sets to CriteriaQuery results limit to the given rows.
	 * 
	 * <p>
	 * Starts from 1.
	 */
	CriteriaQuery<T> limit(Integer rows);
	
	/**
	 * Sets to CriteriaQuery results in the range of the given start and end rows.
	 * 
	 * <p>
	 * Starts from 1.
	 */
	CriteriaQuery<T> limit(int startRows,int endRows);
	
	/**
	 * Sets to CriteriaQuery results in the range of the given page index and size.
	 * 
	 * @see Page
	 */
	CriteriaQuery<T> limit(Limit limit);
	
	/**
	 * Returns the primary entity mapping for this CriteriaQuery.
	 */
	EntityMapping getEntityMapping();
	
	/**
	 * Sets the selected fields(columns) in the CriteriaQuery result.
	 * 
	 * <p>
	 * Selects all columns if the given columns is null or empty.
	 */
	CriteriaQuery<T> select(String... fields);
	
	/**
	 * Sets the filter of selected fields.
	 * 
	 * <p>
	 * Returns <code>true</code> in the {@link Predicate#test(Object)} method if the field should be included in selected list. 
	 */
	CriteriaQuery<T> select(Predicate<FieldMapping> filter);

    /**
     * Returns the table alias of primary entity.
     */
    String alias();
	
	/**
	 * Sets the primary table's alias name in this CriteriaQuery.
	 * 
	 * <p>
	 * Default alias name is 't'.
	 */
	CriteriaQuery<T> alias(String alias);

    /**
     * Inner join the target entity by id columns.
     */
    CriteriaQuery<T> join(Class<?> entityClass, String alias);

    /**
     * Inner join the target entity by id columns.
     */
    CriteriaQuery<T> join(Class<?> entityClass, String relation, String alias);

    /**
     * Inner join the target entity by id columns.
     */
    CriteriaQuery<T> join(String entityName, String alias);

    /**
     * Inner join the target entity by id columns.
     */
    CriteriaQuery<T> join(String entityName, String relation, String alias);

    /**
     * Left join the target entity by id columns.
     */
    CriteriaQuery<T> leftJoin(Class<?> entityClass, String alias);

    /**
     * Left join the target entity by id columns.
     */
    CriteriaQuery<T> leftJoin(Class<?> entityClass, String relation, String alias);

    /**
     * Left join the target entity by id columns.
     */
    CriteriaQuery<T> leftJoin(String entityName, String alias);

    /**
     * Left join the target entity by id columns.
     */
    CriteriaQuery<T> leftJoin(String entityName, String relation, String alias);
	
	/**
	 * Creates a {@link CriteriaWhere} object for building where condition in this CriteriaQuery.
	 */
	CriteriaWhere<T> where();
	
	/**
	 * Sets the where expression to the lambda condition.
	 * 
	 * <p>
	 * Example : 
	 * <pre>
	 * 	where(m -> m.name == "Jim")
	 * </pre>
	 */
	CriteriaQuery<T> where(Condition<T> condition);

	/**
	 * Sets the where expression in this CriteriaQuery.
	 */
	CriteriaQuery<T> where(String expression);
	
	/**
	 * Sets the where expression in this CriteriaQuery.
	 */
	CriteriaQuery<T> where(String expression,Object... args);
	
	/**
	 * Sets the 'group by' sql expression in the generated sql.
	 */
	CriteriaQuery<T> groupBy(String expression);
	
	/**
	 * Sets the 'having' sql expression in the generated sql.
	 */
	CriteriaQuery<T> having(String expression);
	
	/**
	 * Sets the order by expression in ascending order of the primary key.
	 */
	CriteriaQuery<T> orderByIdAsc();
	
	/**
	 * Sets the order by expression in descending order of the primary key.
	 */
	CriteriaQuery<T> orderByIdDesc();
	
	/**
	 * Sets the order by expression in descending order of createdAt column.
	 */
	default CriteriaQuery<T> lastCreated() {
		return orderBy(getEntityMapping().getFieldMappingByMetaName(ReservedMetaFieldName.CREATED_AT).getFieldName() + " desc");
	}
	
	/**
	 * Sets the order by expression in descending order of createdAt column.
	 */
	default CriteriaQuery<T> lastCreated(Integer limit) {
		return lastCreated().limit(limit);
	}
	
	/**
	 * Sets the order by expression in descending order of updatedAt column.
	 */
	default CriteriaQuery<T> lastUpdated() {
		return orderBy(getEntityMapping().getFieldMappingByMetaName(ReservedMetaFieldName.UPDATED_AT).getFieldName() + " desc");
	}
	
	/**
	 * Sets the order by expression in descending order of updatedAt column.
	 */
	default CriteriaQuery<T> lastUpdated(Integer limit) {
		return lastUpdated().limit(limit);
	}
	
	/**
	 * Executes a delete operation on this CriteriaQuery.
	 */
	int delete();
	
	/**
	 * Executes an update operator on this CriteriaQuery.
	 * 
	 * @param fields the fields(columns) to be updated.
	 */
	int update(Map<String, Object> fields);
}