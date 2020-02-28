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

import leap.core.metamodel.ReservedMetaFieldName;
import leap.lang.annotation.Internal;
import leap.lang.annotation.Nullable;
import leap.lang.beans.DynaBean;
import leap.lang.params.ArrayParams;
import leap.lang.params.Params;
import leap.lang.value.Limit;
import leap.lang.value.Page;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.mapping.RelationMapping;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public interface CriteriaQuery<T> extends Query<T> {

    String DEFAULT_ALIAS_NAME = "t";

    /**
     * Returns the selection fields or null.
     */
    String[] getSelects();

    /**
     * Returns the where expression or null.
     */
    String getWhere();

    /**
     * Returns is distinct query or not.
     */
    boolean isDistinct();

    /**
     * Returns the group by expression or null.
     */
    String getGroupBy();

    /**
     * Returns the having expression or null.
     */
    String getHaving();

    /**
     * Returns the sql view or null.
     */
    String getSqlView();

    /**
     * Sets the sql of from.
     *
     * <p/>
     * select * from (sql view) t.
     */
    CriteriaQuery<T> fromSqlView(String sql);

    /**
     * Wraps the sql query with the wrapper.
     */
    CriteriaQuery<T> wrapSqlQuery(Function<String, String> wrapper);

    /**
     * Sets the given name and value as CriteriaQuery parameter.
     */
    CriteriaQuery<T> param(String name, Object value);

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
     * <p>
     * <p>
     * An order by expression is a standand sql order by clause without "order by" prefix..
     * <p>
     * <p>
     * For example :
     * <pre>
     * 	orderBy("col1 asc,col2 desc")
     * </pre>
     */
    CriteriaQuery<T> orderBy(String expression);

    /**
     * Sets to CriteriaQuery results limit to the given rows.
     * <p>
     * <p>
     * Starts from 1.
     */
    CriteriaQuery<T> limit(Integer rows);

    /**
     * Sets to CriteriaQuery results in the range of the given start and end rows.
     * <p>
     * <p>
     * Starts from 1.
     */
    CriteriaQuery<T> limit(int startRows, int endRows);

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
     * Select distinct ...
     */
    CriteriaQuery<T> distinct();

    /**
     * Sets the selected fields(columns) in the CriteriaQuery result.
     * <p>
     * <p>
     * Selects all columns if the given columns is null or empty.
     */
    CriteriaQuery<T> select(String... fields);

    /**
     * Removes the fields(columns) in the CriteriaQuery result.
     */
    CriteriaQuery<T> selectExclude(String... fields);

    /**
     * Sets the filter of selected fields.
     * <p>
     * <p>
     * Returns <code>true</code> in the {@link Predicate#test(Object)} method if the field should be included in selected list.
     */
    CriteriaQuery<T> select(Predicate<FieldMapping> filter);

    /**
     * Appends a select item.
     */
    CriteriaQuery<T> addSelectItem(String fieldOrExpr);

    /**
     * Appends a select field, skip if exists.
     */
    CriteriaQuery<T> addSelectField(String field);

    /**
     * Appends a select fields, skip if exists.
     */
    default CriteriaQuery<T> addSelectFields(String... fields) {
        if(null == fields || fields.length == 0) {
            return this;
        }
        for(String field : fields) {
            addSelectField(field);
        }
        return this;
    }

    /**
     * Returns the table alias of primary entity.
     */
    String alias();

    /**
     * Sets the primary table's alias name in this CriteriaQuery.
     * <p>
     * <p>
     * Default alias name is 't'.
     */
    CriteriaQuery<T> alias(String alias);

    /**
     * Add a customer join builder
     */
    CriteriaQuery<T> join(JoinBuilder builder);

    /**
     * Inner join the target entity by id columns.
     */
    CriteriaQuery<T> join(Class<?> targetEntityClass, String alias);

    /**
     * Inner join the target entity by id columns.
     */
    CriteriaQuery<T> join(Class<?> targetEntityClass, String localRelation, String alias);

    /**
     * Inner join the target entity by id columns.
     */
    CriteriaQuery<T> join(String targetEntityName, String alias);

    /**
     * Inner join the target entity by id columns.
     */
    CriteriaQuery<T> join(String targetEntityName, String localRelation, String alias);

    /**
     * Inner join the target entity by id columns and with the id of the target entity.
     */
    CriteriaQuery<T> joinById(Class<?> targetEntityClass, String alias, Object id);

    /**
     * Inner join the target entity by id columns and with the id of the target entity.
     */
    CriteriaQuery<T> joinById(String targetEntityName, String alias, Object id);

    /**
     * Inner join the target entity by id columns and with the id of the target entity.
     */
    CriteriaQuery<T> joinById(Class<?> targetEntityName, String localRelation, String alias, Object id);

    /**
     * Inner join the target entity by id columns and with the id of the target entity.
     */
    CriteriaQuery<T> joinById(String targetEntityName, String localRelation, String alias, Object id);

    /**
     * Inner join the target entity by id columns and with a where condition.
     */
    @Internal
    CriteriaQuery<T> joinWithWhere(Class<?> targetEntityClass, String alias, Appendable where, Consumer<FieldMapping> idCallback);

    /**
     * Inner join the target entity by id columns and with a where condition.
     */
    @Internal
    CriteriaQuery<T> joinWithWhere(Class<?> targetEntityClass, String localRelation, String alias, Appendable where, Consumer<FieldMapping> idCallback);

    /**
     * Inner join the target entity by id columns and with a where condition.
     */
    @Internal
    CriteriaQuery<T> joinWithWhere(String targetEntityName, String alias, Appendable where, Consumer<FieldMapping> idCallback);

    /**
     * Inner join the target entity by id columns and with a where condition.
     */
    @Internal
    CriteriaQuery<T> joinWithWhere(String targetEntityName, String localRelation, String alias, Appendable where, Consumer<FieldMapping> idCallback);

    /**
     * Left join the target entity by id columns.
     */
    CriteriaQuery<T> leftJoin(Class<?> targetEntityClass, String alias);

    /**
     * Left join the target entity by id columns.
     */
    CriteriaQuery<T> leftJoin(Class<?> targetEntityClass, String localRelation, String alias);

    /**
     * Left join the target entity by id columns.
     */
    CriteriaQuery<T> leftJoin(String targetEntityName, String alias);

    /**
     * Left join the target entity by id columns.
     */
    CriteriaQuery<T> leftJoin(String targetEntityName, String localRelation, String alias);

    /**
     * Left join the target entity by id columns and with the id of the target entity.
     */
    CriteriaQuery<T> LeftJoinById(Class<?> targetEntityClass, String alias, Object id);

    /**
     * Left join the target entity by id columns and with the id of the target entity.
     */
    CriteriaQuery<T> LeftJoinById(String targetEntityName, String alias, Object id);

    /**
     * Left join the target entity by id columns and with the id of the target entity.
     */
    CriteriaQuery<T> LeftJoinById(Class<?> targetEntityClass, String localRelation, String alias, Object id);

    /**
     * Left join the target entity by id columns and with the id of the target entity.
     */
    CriteriaQuery<T> LeftJoinById(String targetEntityName, String localRelation, String alias, Object id);

    /**
     * Left join the target entity by id columns and with a where condition.
     */
    @Internal
    CriteriaQuery<T> leftJoinWithWhere(Class<?> targetEntityClass, String alias, Appendable where, Consumer<FieldMapping> idCallback);

    /**
     * Left join the target entity by id columns and with a where condition.
     */
    @Internal
    CriteriaQuery<T> leftJoinWithWhere(Class<?> targetEntityClass, String localRelation, String alias, Appendable where, Consumer<FieldMapping> idCallback);

    /**
     * Left join the target entity by id columns and with a where condition.
     */
    @Internal
    CriteriaQuery<T> leftJoinWithWhere(String targetEntityName, String alias, Appendable where, Consumer<FieldMapping> idCallback);

    /**
     * Left join the target entity by id columns and with a where condition.
     */
    @Internal
    CriteriaQuery<T> leftJoinWithWhere(String targetEntityName, String localRelation, String alias, Appendable where, Consumer<FieldMapping> idCallback);

    /**
     * Just like where("id = ?", id).
     */
    CriteriaQuery<T> whereById(Object id);

    /**
     * Just like where("parentId = ?", parentId).
     *
     * <p/>
     * The relation must be many-to-one.
     */
    @Internal
    CriteriaQuery<T> whereByReference(Class<?> refEntityClass, Object refToId);

    /**
     * Just like where("parentId = ?", parentId).
     *
     * <p/>
     * The relation must be many-to-one.
     */
    @Internal
    CriteriaQuery<T> whereByReference(String refEntityName, Object refToId);

    /**
     * Just like where("parentId = ?", parentId).
     *
     * <p/>
     * The relation must be many-to-one.
     */
    @Internal
    CriteriaQuery<T> whereByReference(RelationMapping refRelation, Object refToId);

    /**
     * Creates a {@link CriteriaWhere} object for building where condition in this CriteriaQuery.
     */
    CriteriaWhere<T> where();

    /**
     * Sets the where expression by the given field names and values.
     */
    CriteriaQuery<T> where(Map<String, Object> fields);

    /**
     * Sets the where expression in this CriteriaQuery.
     */
    CriteriaQuery<T> where(String expression);

    /**
     * Sets the where expression in this CriteriaQuery.
     */
    CriteriaQuery<T> where(String expression, Object... args);

    /**
     * Where -> (where) and (expr)
     */
    CriteriaQuery<T> whereAnd(String expr);

    /**
     * Where -> (where) and (expr)
     */
    CriteriaQuery<T> whereAnd(String expr, Object... args);

    /**
     * Where -> (where) or (expr)
     */
    CriteriaQuery<T> whereOr(String expr);

    /**
     * Where -> (where) or (expr)
     */
    CriteriaQuery<T> whereOr(String expr, Object... args);

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