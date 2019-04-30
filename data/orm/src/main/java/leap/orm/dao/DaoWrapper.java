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

package leap.orm.dao;

import leap.core.jdbc.BatchPreparedStatementHandler;
import leap.core.jdbc.JdbcExecutor;
import leap.core.jdbc.PreparedStatementHandler;
import leap.core.jdbc.ResultSetReader;
import leap.core.transaction.TransactionCallback;
import leap.core.transaction.TransactionCallbackWithResult;
import leap.core.transaction.TransactionDefinition;
import leap.core.validation.Errors;
import leap.core.value.Record;
import leap.lang.exception.NestedSQLException;
import leap.lang.jdbc.ConnectionCallback;
import leap.lang.jdbc.ConnectionCallbackWithResult;
import leap.orm.OrmContext;
import leap.orm.command.DeleteCommand;
import leap.orm.command.InsertCommand;
import leap.orm.command.UpdateCommand;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.MappingNotFoundException;
import leap.orm.query.CriteriaQuery;
import leap.orm.query.EntityQuery;
import leap.orm.query.Query;
import leap.orm.sql.SqlCommand;
import leap.orm.value.Entity;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class DaoWrapper extends Dao {

    protected abstract Dao dao();

    @Override
    public OrmContext getOrmContext() {
        return dao().getOrmContext();
    }

    @Override
    public JdbcExecutor getJdbcExecutor() {
        return dao().getJdbcExecutor();
    }

    @Override
    public Errors validate(Object entity) {
        return dao().validate(entity);
    }

    @Override
    public Errors validate(Object entity, int maxErrors) {
        return dao().validate(entity, maxErrors);
    }

    @Override
    public Errors validate(EntityMapping em, Object entity) {
        return dao().validate(em, entity);
    }

    @Override
    public Errors validate(EntityMapping em, Object entity, Iterable<String> fields) {
        return dao().validate(em, entity, fields);
    }

    @Override
    public Errors validate(EntityMapping em, Object entity, int maxErrors) {
        return dao().validate(em, entity, maxErrors);
    }

    @Override
    public Errors validate(EntityMapping em, Object entity, int maxErrors, Iterable<String> fields) {
        return dao().validate(em, entity, maxErrors, fields);
    }

    @Override
    public InsertCommand cmdInsert(Class<?> entityClass) throws MappingNotFoundException {
        return dao().cmdInsert(entityClass);
    }

    @Override
    public InsertCommand cmdInsert(String entityName) throws MappingNotFoundException {
        return dao().cmdInsert(entityName);
    }

    @Override
    public InsertCommand cmdInsert(EntityMapping em) {
        return dao().cmdInsert(em);
    }

    @Override
    public UpdateCommand cmdUpdate(Class<?> entityClass) throws MappingNotFoundException {
        return dao().cmdUpdate(entityClass);
    }

    @Override
    public UpdateCommand cmdUpdate(String entityName) throws MappingNotFoundException {
        return dao().cmdUpdate(entityName);
    }

    @Override
    public UpdateCommand cmdUpdate(EntityMapping em) {
        return dao().cmdUpdate(em);
    }

    @Override
    public DeleteCommand cmdDelete(EntityMapping em, Object id) {
        return dao().cmdDelete(em, id);
    }

    @Override
    public int insert(Object entity) throws MappingNotFoundException {
        return dao().insert(entity);
    }

    @Override
    public int insert(String entityName, Object entity) {
        return dao().insert(entityName, entity);
    }

    @Override
    public int insert(Class<?> entityClass, Object entity) throws MappingNotFoundException {
        return dao().insert(entityClass, entity);
    }

    @Override
    public int insert(EntityMapping em, Object entity, Object id) {
        return dao().insert(em, entity, id);
    }

    @Override
    public int update(Object entity) throws MappingNotFoundException {
        return dao().update(entity);
    }

    @Override
    public int update(Class<?> entityClass, Object entity) throws MappingNotFoundException {
        return dao().update(entityClass, entity);
    }

    @Override
    public int update(String entityName, Object entity) throws MappingNotFoundException {
        return dao().update(entityName, entity);
    }

    @Override
    public int update(EntityMapping em, Object entity) throws MappingNotFoundException {
        return dao().update(em, entity);
    }

    @Override
    public int update(Object entity, Map<String, Object> fields) throws MappingNotFoundException {
        return dao().update(entity, fields);
    }

    @Override
    public int update(Class<?> entityClass, Object id, Map<String, Object> fields) throws MappingNotFoundException {
        return dao().update(entityClass, id, fields);
    }

    @Override
    public int update(String entityName, Object id, Map<String, Object> fields) throws MappingNotFoundException {
        return dao().update(entityName, id, fields);
    }

    @Override
    public int update(EntityMapping em, Object id, Map<String, Object> fields) throws MappingNotFoundException {
        return dao().update(em, id, fields);
    }

    @Override
    public int delete(Object entity) {
        return dao().delete(entity);
    }

    @Override
    public int delete(Class<?> entityClass, Object id) throws MappingNotFoundException {
        return dao().delete(entityClass, id);
    }

    @Override
    public int delete(String entityName, Object id) throws MappingNotFoundException {
        return dao().delete(entityName, id);
    }

    @Override
    public int delete(EntityMapping em, Object id) {
        return dao().delete(em, id);
    }

    @Override
    public boolean cascadeDelete(Class<?> entityClass, Object id) throws MappingNotFoundException {
        return dao().cascadeDelete(entityClass, id);
    }

    @Override
    public boolean cascadeDelete(String entityName, Object id) throws MappingNotFoundException {
        return dao().cascadeDelete(entityName, id);
    }

    @Override
    public boolean cascadeDelete(EntityMapping em, Object id) {
        return dao().cascadeDelete(em, id);
    }

    @Override
    public int deleteAll(Class<?> entityClass) {
        return dao().deleteAll(entityClass);
    }

    @Override
    public int deleteAll(String entityName) {
        return dao().deleteAll(entityName);
    }

    @Override
    public int deleteAll(EntityMapping em) {
        return dao().deleteAll(em);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object id) {
        return dao().find(entityClass, id);
    }

    @Override
    public Record find(String entityName, Object id) {
        return dao().find(entityName, id);
    }

    @Override
    public Record find(EntityMapping em, Object id) {
        return dao().find(em, id);
    }

    @Override
    public <T> T find(Class<?> entityClass, Class<T> resultClass, Object id) {
        return dao().find(entityClass, resultClass, id);
    }

    @Override
    public <T> T find(String entityName, Class<T> resultClass, Object id) {
        return dao().find(entityName, resultClass, id);
    }

    @Override
    public <T> T find(EntityMapping em, Class<T> resultClass, Object id) {
        return dao().find(em, resultClass, id);
    }

    @Override
    public <T> T findOrNull(Class<T> entityClass, Object id) {
        return dao().findOrNull(entityClass, id);
    }

    @Override
    public Record findOrNull(String entityName, Object id) {
        return dao().findOrNull(entityName, id);
    }

    @Override
    public Record findOrNull(EntityMapping em, Object id) {
        return dao().findOrNull(em, id);
    }

    @Override
    public <T> T findOrNull(Class<?> entityClass, Class<T> resultClass, Object id) {
        return dao().findOrNull(entityClass, resultClass, id);
    }

    @Override
    public <T> T findOrNull(String entityName, Class<T> resultClass, Object id) {
        return dao().findOrNull(entityName, resultClass, id);
    }

    @Override
    public <T> T findOrNull(EntityMapping em, Class<T> resultClass, Object id) {
        return dao().findOrNull(em, resultClass, id);
    }

    @Override
    public <T> List<T> findList(Class<T> entityClass, Object[] ids) {
        return dao().findList(entityClass, ids);
    }

    @Override
    public List<Entity> findList(String entityName, Object[] ids) {
        return dao().findList(entityName, ids);
    }

    @Override
    public <T> List<T> findList(String entityName, Class<T> resultClass, Object[] ids) {
        return dao().findList(entityName, resultClass, ids);
    }

    @Override
    public <T> List<T> findList(EntityMapping em, Class<T> resultClass, Object[] ids) {
        return dao().findList(em, resultClass, ids);
    }

    @Override
    public <T> List<T> findListIfExists(Class<T> entityClass, Object[] ids) {
        return dao().findListIfExists(entityClass, ids);
    }

    @Override
    public List<Record> findListIfExists(String entityName, Object[] ids) {
        return dao().findListIfExists(entityName, ids);
    }

    @Override
    public <T> List<T> findListIfExists(String entityName, Class<T> resultClass, Object[] ids) {
        return dao().findListIfExists(entityName, resultClass, ids);
    }

    @Override
    public <T> List<T> findListIfExists(EntityMapping em, Class<T> resultClass, Object[] ids) {
        return dao().findListIfExists(em, resultClass, ids);
    }

    @Override
    public <T> List<T> findAll(Class<T> entityClass) {
        return dao().findAll(entityClass);
    }

    @Override
    public <T> List<T> findAll(String entityName, Class<T> resultClass) {
        return dao().findAll(entityName, resultClass);
    }

    @Override
    public boolean exists(Class<?> entityClass, Object id) throws MappingNotFoundException {
        return dao().exists(entityClass, id);
    }

    @Override
    public boolean exists(String entityName, Object id) throws MappingNotFoundException {
        return dao().exists(entityName, id);
    }

    @Override
    public long count(Class<?> entityClass) {
        return dao().count(entityClass);
    }

    @Override
    public long count(String entityName) {
        return dao().count(entityName);
    }

    @Override
    public long count(EntityMapping em) {
        return dao().count(em);
    }

    @Override
    public int executeUpdate(SqlCommand command, Object[] args) {
        return dao().executeUpdate(command, args);
    }

    @Override
    public int executeUpdate(SqlCommand command, Object bean) {
        return dao().executeUpdate(command, bean);
    }

    @Override
    public int executeUpdate(SqlCommand command, Map params) {
        return dao().executeUpdate(command, params);
    }

    @Override
    public int executeUpdate(String sql, Object bean) {
        return dao().executeUpdate(sql, bean);
    }

    @Override
    public int executeUpdate(String sql, Map params) {
        return dao().executeUpdate(sql, params);
    }

    @Override
    public int executeNamedUpdate(String sqlKey, Object[] args) {
        return dao().executeNamedUpdate(sqlKey, args);
    }

    @Override
    public int executeNamedUpdate(String sqlKey, Object bean) {
        return dao().executeNamedUpdate(sqlKey, bean);
    }

    @Override
    public int executeNamedUpdate(String sqlKey, Map<String, Object> params) {
        return dao().executeNamedUpdate(sqlKey, params);
    }

    @Override
    public Query<Record> createQuery(SqlCommand command) {
        return dao().createQuery(command);
    }

    @Override
    public <T> Query<T> createQuery(Class<T> resultClass, SqlCommand command) {
        return dao().createQuery(resultClass, command);
    }

    @Override
    public Query<Record> createSqlQuery(String sql) {
        return dao().createSqlQuery(sql);
    }

    @Override
    public Query<Record> createSqlQuery(String sql, Object... args) {
        return dao().createSqlQuery(sql, args);
    }

    @Override
    public <T> Query<T> createSqlQuery(Class<T> resultClass, String sql) {
        return dao().createSqlQuery(resultClass, sql);
    }

    @Override
    public EntityQuery<Record> createSqlQuery(EntityMapping em, String sql) {
        return dao().createSqlQuery(em, sql);
    }

    @Override
    public <T> EntityQuery<T> createSqlQuery(EntityMapping em, Class<T> resultClass, String sql) {
        return dao().createSqlQuery(em, resultClass, sql);
    }

    @Override
    public <T> CriteriaQuery<T> createCriteriaQuery(Class<T> entityClass) {
        return dao().createCriteriaQuery(entityClass);
    }

    @Override
    public CriteriaQuery<Record> createCriteriaQuery(String entityName) {
        return dao().createCriteriaQuery(entityName);
    }

    @Override
    public CriteriaQuery<Record> createCriteriaQuery(EntityMapping em) {
        return dao().createCriteriaQuery(em);
    }

    @Override
    public <T> CriteriaQuery<T> createCriteriaQuery(EntityMapping em, Class<T> resultClass) {
        return dao().createCriteriaQuery(em, resultClass);
    }

    @Override
    public <T> CriteriaQuery<T> createCriteriaQuery(Class<?> entityClass, Class<T> resultClass) {
        return dao().createCriteriaQuery(entityClass, resultClass);
    }

    @Override
    public Query<Record> createNamedQuery(String queryName) {
        return dao().createNamedQuery(queryName);
    }

    @Override
    public <T> Query<T> createNamedQuery(String queryName, Class<T> resultClass) {
        return dao().createNamedQuery(queryName, resultClass);
    }

    @Override
    public <T> EntityQuery<T> createNamedQuery(Class<T> entityClass, String queryName) {
        return dao().createNamedQuery(entityClass, queryName);
    }

    @Override
    public EntityQuery<Record> createNamedQuery(String entityName, String queryName) {
        return dao().createNamedQuery(entityName, queryName);
    }

    @Override
    public <T> EntityQuery<T> createNamedQuery(String entityName, Class<T> resultClass, String queryName) {
        return dao().createNamedQuery(entityName, resultClass, queryName);
    }

    @Override
    public <T> EntityQuery<T> createNamedQuery(EntityMapping em, Class<T> resultClass, String queryName) {
        return dao().createNamedQuery(em, resultClass, queryName);
    }

    @Override
    public int[] batchInsert(List<?> entities) {
        return dao().batchInsert(entities);
    }

    @Override
    public int[] batchInsert(Object[] entities) {
        return dao().batchInsert(entities);
    }

    @Override
    public int[] batchInsert(String entityName, List<?> records) {
        return dao().batchInsert(entityName, records);
    }

    @Override
    public int[] batchInsert(String entityName, Object[] records) {
        return dao().batchInsert(entityName, records);
    }

    @Override
    public int[] batchInsert(Class<?> entityClass, List<?> records) {
        return dao().batchInsert(entityClass, records);
    }

    @Override
    public int[] batchInsert(Class<?> entityClass, Object[] records) {
        return dao().batchInsert(entityClass, records);
    }

    @Override
    public int[] batchInsert(EntityMapping em, List<?> records) {
        return dao().batchInsert(em, records);
    }

    @Override
    public int[] batchInsert(EntityMapping em, Object[] records) {
        return dao().batchInsert(em, records);
    }

    @Override
    public int[] batchUpdate(List<?> entities) {
        return dao().batchInsert(entities);
    }

    @Override
    public int[] batchUpdate(Object[] entities) {
        return dao().batchUpdate(entities);
    }

    @Override
    public int[] batchUpdate(String entityName, List<?> records) {
        return dao().batchUpdate(entityName, records);
    }

    @Override
    public int[] batchUpdate(String entityName, Object[] records) {
        return dao().batchUpdate(entityName, records);
    }

    @Override
    public int[] batchUpdate(Class<?> entityClass, List<?> records) {
        return dao().batchUpdate(entityClass, records);
    }

    @Override
    public int[] batchUpdate(Class<?> entityClass, Object[] records) {
        return dao().batchUpdate(entityClass, records);
    }

    @Override
    public int[] batchUpdate(EntityMapping em, List<?> records) {
        return dao().batchUpdate(em, records);
    }

    @Override
    public int[] batchUpdate(EntityMapping em, Object[] records) {
        return dao().batchUpdate(em, records);
    }

    @Override
    public int[] batchDelete(String entityName, List<?> ids) {
        return dao().batchDelete(entityName, ids);
    }

    @Override
    public int[] batchDelete(String entityName, Object[] ids) {
        return dao().batchDelete(entityName, ids);
    }

    @Override
    public int[] batchDelete(Class<?> entityClass, List<?> ids) {
        return dao().batchDelete(entityClass, ids);
    }

    @Override
    public int[] batchDelete(Class<?> entityClass, Object[] ids) {
        return dao().batchDelete(entityClass, ids);
    }

    @Override
    public int[] batchDelete(EntityMapping em, List<?> ids) {
        return dao().batchDelete(em, ids);
    }

    @Override
    public int[] batchDelete(EntityMapping em, Object[] ids) {
        return dao().batchDelete(em, ids);
    }

    @Override
    public void doTransaction(TransactionCallback callback) {
        dao().doTransaction(callback);
    }

    @Override
    public <T> T doTransaction(TransactionCallbackWithResult<T> callback) {
        return dao().doTransaction(callback);
    }

    @Override
    public void doTransaction(TransactionCallback callback, boolean requiresNew) {
        dao().doTransaction(callback, requiresNew);
    }

    @Override
    public void doTransaction(TransactionCallback callback, TransactionDefinition definition) {
        dao().doTransaction(callback, definition);
    }

    @Override
    public <T> T doTransaction(TransactionCallbackWithResult<T> callback, boolean requiresNew) {
        return dao().doTransaction(callback, requiresNew);
    }

    @Override
    public void withDataSource(DataSource dataSource, Runnable runnable) {
        dao().withDataSource(dataSource, runnable);
    }

    @Override
    public void execute(ConnectionCallback callback) throws NestedSQLException {
        dao().execute(callback);
    }

    @Override
    public <T> T executeWithResult(ConnectionCallbackWithResult<T> callback) throws NestedSQLException {
        return dao().executeWithResult(callback);
    }

    @Override
    public int executeUpdate(String sql) throws NestedSQLException {
        return dao().executeUpdate(sql);
    }

    @Override
    public int executeUpdate(String sql, Object[] args) throws NestedSQLException {
        return dao().executeUpdate(sql, args);
    }

    @Override
    public int executeUpdate(String sql, Object[] args, int[] types) throws NestedSQLException {
        return dao().executeUpdate(sql, args, types);
    }

    @Override
    public int executeUpdate(String sql, Object[] args, int[] types, PreparedStatementHandler<?> handler) throws NestedSQLException {
        return dao().executeUpdate(sql, args, types, handler);
    }

    @Override
    public int[] executeBatchUpdate(String... sqls) throws NestedSQLException {
        return dao().executeBatchUpdate(sqls);
    }

    @Override
    public int[] executeBatchUpdate(String sql, Object[][] batchArgs) throws NestedSQLException {
        return dao().executeBatchUpdate(sql, batchArgs);
    }

    @Override
    public int[] executeBatchUpdate(String sql, Object[][] batchArgs, int[] types) throws NestedSQLException {
        return dao().executeBatchUpdate(sql, batchArgs, types);
    }

    @Override
    public int[] executeBatchUpdate(String sql, Object[][] batchArgs, int[] types, BatchPreparedStatementHandler<?> handler) throws NestedSQLException {
        return dao().executeBatchUpdate(sql, batchArgs, types, handler);
    }

    @Override
    public <T> T executeQuery(String sql, ResultSetReader<T> reader) throws NestedSQLException {
        return dao().executeQuery(sql, reader);
    }

    @Override
    public <T> T executeQuery(String sql, Object[] args, ResultSetReader<T> reader) throws NestedSQLException {
        return dao().executeQuery(sql, args, reader);
    }

    @Override
    public <T> T executeQuery(String sql, Object[] args, int[] types, ResultSetReader<T> reader) throws NestedSQLException {
        return dao().executeQuery(sql, args, types, reader);
    }

    @Override
    public void withEvents(Runnable func) {
        dao().withEvents(func);
    }

    @Override
    public <T> T withEvents(Supplier<T> func) {
        return dao().withEvents(func);
    }

    @Override
    public void withoutEvents(Runnable func) {
        dao().withEvents(func);
    }

    @Override
    public <T> T withoutEvents(Supplier<T> func) {
        return dao().withoutEvents(func);
    }

    @Override
    public boolean isWithEvents() {
        return dao().isWithEvents();
    }
}
