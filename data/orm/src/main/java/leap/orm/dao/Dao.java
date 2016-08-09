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
package leap.orm.dao;

import leap.core.AppContext;
import leap.core.exception.RecordNotFoundException;
import leap.core.jdbc.JdbcExecutor;
import leap.core.transaction.TransactionCallback;
import leap.core.transaction.TransactionCallbackWithResult;
import leap.core.validation.Errors;
import leap.core.value.Record;
import leap.orm.Orm;
import leap.orm.OrmContext;
import leap.orm.OrmMetadata;
import leap.orm.command.InsertCommand;
import leap.orm.command.UpdateCommand;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.MappingNotFoundException;
import leap.orm.query.CriteriaQuery;
import leap.orm.query.EntityQuery;
import leap.orm.query.Query;
import leap.orm.sql.SqlCommand;
import leap.orm.value.Entity;
import leap.orm.value.EntityBase;

import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Dao means Data Access Object.
 */
@SuppressWarnings("rawtypes")
public abstract class Dao implements JdbcExecutor {
	
	/**
	 * Returns the default {@link Dao} instance in current {@link AppContext}.
	 */
	public static Dao get(){
		return Orm.dao();
	}
	
	/**
	 * Returns the named {@link Dao} instance in current {@link AppContext}.
	 * 
	 * <p>
	 * The name of default {@link Dao} instance is {@link Orm#DEFAULT_NAME}
	 */
	public static Dao get(String name){
		return Orm.dao(name);
	}
	
	/**
	 * Returns the {@link OrmContext} of this dao.
	 */
	public abstract OrmContext getOrmContext();
	
	/**
	 * Returns the underlying {@link JdbcExecutor}.
	 */
	public abstract JdbcExecutor getJdbcExecutor();
	
	//----------------------------validate------------------------------
	
	/**
	 * Validates the given entity object and returns the {@link Errors} contains empty or validation errors.
	 * 
	 * <p>
	 * The given entity object must be a pojo or a {@link Entity} object.
	 */
	public abstract Errors validate(Object entity);
	
	/**
	 * Validates the given entity object and returns the {@link Errors} contains empty or validation errors.
	 * 
	 * <p>
	 * The given entity object must be a pojoo or a {@link Entity} object.
	 * 
	 * @param entity the entity object to be validated.
	 * @param maxErrors 0 means validates all errors, large than 0 means it will stop validating when the error's size reach the given maxErrors.
	 * 
	 */
	public abstract Errors validate(Object entity, int maxErrors);
	
	/**
	 * Validates the given entity object and returns the {@link Errors} contains empty or validation errors.
	 * 
	 * <p>
	 * The given entity object can be a pojo, a {@link Entity} object or a {@link Map} contains entity's attributes.
	 */
	public abstract Errors validate(EntityMapping em,Object entity);

    /**
     * Validates the given entity object and returns the {@link Errors} contains empty or validation errors.
     *
     * <p>
     * The given entity object can be a pojo, a {@link Entity} object or a {@link Map} contains entity's attributes.
     */
    public abstract Errors validate(EntityMapping em,Object entity, Iterable<String> fields);
	
	/**
	 * Validates the given entity object and returns the {@link Errors} contains empty or validation errors.
	 * 
	 * <p>
	 * The given entity object can be a pojo, a {@link Entity} object or a {@link Map} contains entity's attributes.
	 * 
	 * @param em {@link EntityMapping} mapping to the given entity object.
	 * @param entity the entity object to be validated.
	 * @param maxErrors 0 means validates all errors, large than 0 means it will stop validating when the error's size reach the given maxErrors.
	 * 
	 */
	public abstract Errors validate(EntityMapping em,Object entity, int maxErrors);

    /**
     * Validates the given entity object and returns the {@link Errors} contains empty or validation errors.
     *
     * <p>
     * The given entity object can be a pojo, a {@link Entity} object or a {@link Map} contains entity's attributes.
     *
     * @param em {@link EntityMapping} mapping to the given entity object.
     * @param entity the entity object to be validated.
     * @param maxErrors 0 means validates all errors, large than 0 means it will stop validating when the error's size reach the given maxErrors.
     *
     */
    public abstract Errors validate(EntityMapping em,Object entity, int maxErrors, Iterable<String> fields);
	
	//----------------------------commands------------------------------
	/**
	 * Creates a new {@link InsertCommand} object for inserting a new entity into the underlying database later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 * 
	 * @throws MappingNotFoundException if the given entity not exists.
	 */
	public abstract InsertCommand cmdInsert(Class<?> entityClass) throws MappingNotFoundException;
	
	/**
	 * Creates a new {@link InsertCommand} object for inserting a new entity into the underlying database later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 * 
	 * @throws MappingNotFoundException if the given entity not exists.
	 */
	public abstract InsertCommand cmdInsert(String entityName) throws MappingNotFoundException;
	
	/**
	 * Creates a new {@link InsertCommand} object for inserting a new entity into the underlying database later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 */
	public abstract InsertCommand cmdInsert(EntityMapping em);	
	
	/**
	 * Creates an {@link UpdateCommand} command for the given entity class.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 * 
	 * @throws MappingNotFoundException if the given entity not exists.
	 */
	public abstract UpdateCommand cmdUpdate(Class<?> entityClass) throws MappingNotFoundException;
	
	/**
	 * Creates an {@link UpdateCommand} command for the given entity class.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 * 
	 * @throws MappingNotFoundException if the given entity not exists.
	 */
	public abstract UpdateCommand cmdUpdate(String entityName) throws MappingNotFoundException;
	
	/**
	 * Creates an {@link UpdateCommand} command for the given entity class.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 */
	public abstract UpdateCommand cmdUpdate(EntityMapping em);
	
	//----------------------------insert--------------------------------
	
	/**
	 * Inserts a new entity into the underlying database immediately.
	 * 
	 * <p>
	 * The given entity object must be a pojo or a {@link Entity} object.
	 * 
	 * @throws MappingNotFoundException if cannot resolve a {@link EntityMapping} from the given object.
	 * 
	 * @return The affected row(s).
	 */
	public abstract int insert(Object entity) throws MappingNotFoundException;
	
	//----------------------------update--------------------------------
	
	/**
	 * Updates the properties of the given entity into the underlying db.
	 * 
	 * @throws MappingNotFoundException if the given entity not exists.
	 * 
	 * @return The affected row(s).
	 */
	public abstract int update(Object entity) throws MappingNotFoundException;
	
	//----------------------------delete--------------------------------
	
	/**
	 * Deletes an entity by the given id immediately.
	 * 
	 * @throws MappingNotFoundException if {@link EntityMapping} not found for the given entity class.
	 * 
	 * @return The affected row(s).
	 */
	public abstract int delete(Class<?> entityClass,Object id) throws MappingNotFoundException;
	
	/**
	 * Deletes an entity by the given id immediately.
	 * 
	 * @throws MappingNotFoundException if {@link EntityMapping} not found for the given entity name.
	 * 
	 * @return Th affected row(s).
	 */
	public abstract int delete(String entityName,Object id) throws MappingNotFoundException;
	
	/**
	 * Deletes an entity by the given id immediately.
	 * 
	 * @return Th affected row(s).
	 */
	public abstract int delete(EntityMapping em,Object id);
	
	/**
	 * Deletes all the data of the given entity type.
	 * <p>
	 * 
	 * <font color="red"><strong>
	 * Be carefull : this method will clears all data in the table(s) mapping to the given entity class.
	 * </strong></font>
	 * 
	 * @return The affected row(s).
	 */
	public abstract int deleteAll(Class<?> entityClass);
	
	/**
	 * Deletes all the data of the given entity name.
	 * <p>
	 * 
	 * <font color="red"><strong>
	 * Be carefull : this method will clears all data in the table(s) mapping to the given entity name.
	 * </strong></font>
	 * 
	 * @return The affected row(s).
	 */
	public abstract int deleteAll(String entityName);
	
	/**
	 * Deletes all the data of the given entity mapping.
	 * <p>
	 * 
	 * <font color="red"><strong>
	 * Be carefull : this method will clears all data in the table(s) mapping to the entity.
	 * </strong></font>
	 * 
	 * @return The affected row(s).
	 */
	public abstract int deleteAll(EntityMapping em);
	
	//----------------------------find--------------------------------
	
	/**
	 * Returns the record for the id.
	 *
	 * @throws RecordNotFoundException if the record not exists.
	 */
	public abstract <T> T find(Class<T> entityClass,Object id);
	
    /**
     * Returns the record for the id.
     *
     * @throws RecordNotFoundException if the record not exists.
     */
	public abstract Entity find(String entityName,Object id);
	
    /**
     * Returns the record for the id.
     *
     * @throws RecordNotFoundException if the record not exists.
     */
    public abstract <T> T find(String entityName,Class<T> resultClass,Object id);
    
    /**
     * Returns the record for the id.
     *
     * @throws RecordNotFoundException if the record not exists.
     */
    public abstract <T> T find(EntityMapping em,Class<T> resultClass,Object id);
	
	/**
     * Returns the record for the id.
     *
     * <p>
     * Returns <code>null</code> if record not exists.
     */
    public abstract <T> T findOrNull(Class<T> entityClass,Object id);
	
    /**
     * Returns the record for the id.
     *
     * <p>
     * Returns <code>null</code> if record not exists.
     */
	public abstract Entity findOrNull(String entityName,Object id);
	
    /**
     * Returns the record for the id.
     *
     * <p>
     * Returns <code>null</code> if record not exists.
     */
    public abstract <T> T findOrNull(String entityName,Class<T> resultClass,Object id);	
    
    /**
     * Returns the record for the id.
     *
     * <p>
     * Returns <code>null</code> if record not exists.
     */
    public abstract <T> T findOrNull(EntityMapping em,Class<T> resultClass,Object id);
    
    /**
     * Returns the entity list by the given id array.
     * 
     * @throws RecordNotFoundException if the returned size less than the id array's size.
     */
    public abstract <T> List<T> findList(Class<T> entityClass,Object[] ids);    
    
    /**
     * Returns the entity list by the given id array.
     * 
     * @throws RecordNotFoundException if the returned size less than the id array's size.
     */
    public abstract List<Entity> findList(String entityName,Object[] ids);
    
    /**
     * Returns the entity list by the given id array.
     * 
     * @throws RecordNotFoundException if the returned size less than the id array's size.
     */
    public abstract <T> List<T> findList(String entityName, Class<T> resultClass,Object[] ids);       
	
	/**
	 * Returns the entity list by the given id array.
	 * 
	 * @throws RecordNotFoundException if the returned size less than the id array's size.
	 */
	public abstract <T> List<T> findList(EntityMapping em,Class<T> resultClass,Object[] ids);
	
    /**
     * Returns the entity list by the given id array.
     * 
     * <p>
     * Do not throws {@link RecordNotFoundException} if the some of record(s) not exists.
     */
    public abstract <T> List<T> findListIfExists(Class<T> entityClass,Object[] ids);
    
    /**
     * Returns the entity list by the given id array.
     * 
     * <p>
     * Do not throws {@link RecordNotFoundException} if the some of record(s) not exists.
     */
    public abstract List<Entity> findListIfExists(String entityName,Object[] ids);  
    
    /**
     * Returns the entity list by the given id array.
     * 
     * <p>
     * Do not throws {@link RecordNotFoundException} if the some of record(s) not exists.
     */
    public abstract <T> List<T> findListIfExists(String entityName, Class<T> resultClass,Object[] ids);    
	
	/**
     * Returns the entity list by the given id array.
     * 
     * <p>
     * Do not throws {@link RecordNotFoundException} if the some of record(s) not exists.
     */
    public abstract <T> List<T> findListIfExists(EntityMapping em,Class<T> resultClass,Object[] ids);
	
	/**
	 * Finds all the entities of the given entity class.
	 */
	public abstract <T> List<T> findAll(Class<T> entityClass);
	
	/**
	 * Finds all the entitties of the given entity name.
	 */
	public abstract <T> List<T> findAll(String entityName,Class<T> resultClass);
	
	//----------------------------count and exists---------------------
	
	/**
	 * Checks is an entity of the given id exists in the underlying database.
	 * 
	 * @throws MappingNotFoundException if the given entity not exists.
	 */
	public abstract boolean exists(Class<?> entityClass,Object id) throws MappingNotFoundException;

	/**
	 * Returns total rows in the underlying db of the given entity.
	 */
	public abstract long count(Class<?> entityClass);
	
	//----------------------------execute-------------------------------
	public abstract int executeUpdate(SqlCommand command, Object[] args);
	
	public abstract int executeUpdate(SqlCommand command, Object bean);
	
    public abstract int executeUpdate(SqlCommand command, Map params);
    
    public abstract int executeUpdate(String sql, Object bean);
    
    public abstract int executeUpdate(String sql, Map params);
	
	/**
	 * Executes a named sql and returns the affected number of records.
	 *
	 * @param sqlKey the key of sql.
	 * @param args the jdbc args.
	 * 
	 * @see Statement#executeUpdate(String)
	 */
	public abstract int executeNamedUpdate(String sqlKey, Object[] args);
	
    /**
     * Executes a named sql and returns the affected number of records.
     * 
     * @see Statement#executeUpdate(String)
     */
	public abstract int executeNamedUpdate(String sqlKey, Object bean);
	
	/**
	 * Executes a named sql and returns th affected number of records.
	 * 
	 * @see Statement#executeUpdate(String)
	 */
	public abstract int executeNamedUpdate(String sqlKey, Map<String, Object> params);
	
	//----------------------------query--------------------------------
	public abstract Query<Record> createQuery(SqlCommand command);

	/**
	 * Creates a new {@link Query} for executing the given sql command.
	 */
	public abstract <T> Query<T> createQuery(Class<T> resultClass, SqlCommand command);
	
	/**
	 * Creates a new {@link Query} object for executing the given query sql. 
	 */
	public abstract Query<Record> createSqlQuery(String sql);
	
	/**
	 * Creates a new {@link Query} object for executing the given query sql.
	 */
	public abstract <T> Query<T> createSqlQuery(Class<T> resultClass, String sql);
	
	/**
	 * Creates a new {@link EntityQuery} object for executing the given query sql.
	 */
	public abstract EntityQuery<EntityBase> createSqlQuery(EntityMapping em,String sql);
	
	/**
	 * Creates a new {@link EntityQuery} object for executing the given query sql.
	 */
	public abstract <T> EntityQuery<T> createSqlQuery(EntityMapping em, Class<T> resultClass, String sql);
	
	/**
	 * Creates a new {@link CriteriaQuery} for querying the records of the given entity.
	 */
	public abstract <T> CriteriaQuery<T> createCriteriaQuery(Class<T> entityClass); 

	/**
	 * Creates a new {@link CriteriaQuery} for querying the records of the given entity.
	 */
	public abstract <T> CriteriaQuery<T> createCriteriaQuery(EntityMapping em, Class<T> resultClass); 
	
	/**
	 * Creates a new {@link Query} object for querying data later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>result(),first(),...,list()</code> methods in the returned {@link Query} object to execute the query.
	 * </strong>
	 * 
	 */
	public abstract Query<Record> createNamedQuery(String queryName);
	
	/**
	 * Creates a new {@link Query} object for querying data later.
	 */
	public abstract <T> Query<T> createNamedQuery(String queryName,Class<T> resultClass);
	
	/**
	 * Creates a new {@link Query} object for querying data later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>result(),first(),...,list()</code> methods in the returned {@link Query} object to execute the query.
	 * </strong>
	 * 
	 * <p>
	 * Example : 
	 * 
	 * <pre>
	 * 
	 * //find user by email address
	 * User user = dao.query(User.class,"findByEmail").param("email","user@example.com").singleOrNull();
	 * 
	 * //find all users who's last name equals to 'Jim'
	 * List&lt;User&gt; users = dao.query(User.class","findByLastName").param("lastName","Jim").list();
	 * </pre>
	 * 
	 * @param entityClass an entity class to be query.
	 * 
	 * @param queryName a unique key or command name use to get a {@link SqlCommand} from {@link OrmMetadata}.
	 * 
	 * @see OrmMetadata#getSqlCommand(String)
	 * @see OrmMetadata#getSqlCommand(Class, String)
	 */
	public abstract <T> EntityQuery<T> createNamedQuery(Class<T> entityClass,String queryName);
	
	/**
	 * Creates a new {@link Query} object for querying data later.
	 */
	public abstract EntityQuery<EntityBase> createNamedQuery(String entityName,String queryName);
	
	/**
	 * Creates a new {@link Query} object for querying data later.
	 */
	public abstract <T> EntityQuery<T> createNamedQuery(String entityName,Class<T> resultClass, String queryName);
	
	/**
	 * Creates a new {@link EntityQuery} object for executing the given named query.
	 */
	public abstract <T> EntityQuery<T> createNamedQuery(EntityMapping em, Class<T> resultClass, String queryName);
	
	
	//----------------------------batch-------------------------------------
	
	/**
	 * Batch inserts all the entities
	 */
	public abstract int[] batchInsert(List<?> entities);
	
	/**
	 * Batch inserts all the entities
	 */
	public abstract int[] batchInsert(Object[] entities);

	/**
	 * Batch inserts all the records.
	 */
	public abstract int[] batchInsert(String entityName,List<?> records);

	/**
	 * Batch inserts all the records.
	 */
	public abstract int[] batchInsert(String entityName,Object[] records);

	/**
	 * Batch inserts all the records.
	 */
	public abstract int[] batchInsert(Class<?> entityClass,List<?> records);

	/**
	 * Batch inserts all the records.
	 */
	public abstract int[] batchInsert(Class<?> entityClass,Object[] records);

	/**
	 * Batch inserts all the records.
	 */
	public abstract int[] batchInsert(EntityMapping em,List<?> records);

	/**
	 * Batch inserts all the records.
	 */
	public abstract int[] batchInsert(EntityMapping em,Object[] records);
	
	/**
	 * Batch updates all the entities
	 */
	public abstract int[] batchUpdate(List<?> entities);

	/**
	 * Batch updates all the entities
	 */
	public abstract int[] batchUpdate(Object[] entities);

	/**
	 * Batch updates all the records.
	 */
	public abstract int[] batchUpdate(String entityName, List<?> records);

	/**
	 * Batch updates all the records.
	 */
	public abstract int[] batchUpdate(String entityName, Object[] records);

	/**
	 * Batch updates all the records.
	 */
	public abstract int[] batchUpdate(Class<?> entityClass, List<?> records);

	/**
	 * Batch updates all the records.
	 */
	public abstract int[] batchUpdate(Class<?> entityClass, Object[] records);

	/**
	 * Batch updates all the records.
	 */
	public abstract int[] batchUpdate(EntityMapping em, List<?> records);

	/**
	 * Batch updates all the records.
	 */
	public abstract int[] batchUpdate(EntityMapping em, Object[] records);

	/**
	 * Batch deletes all the records which id in the given arrays of the given entity.
	 */
	public abstract int[] batchDelete(String entityName,List<?> ids);

	/**
	 * Batch deletes all the records which id in the given arrays of the given entity.
	 */
	public abstract int[] batchDelete(String entityName,Object[] ids);

	/**
	 * Batch deletes all the records which id in the given arrays of the given entity.
	 */
	public abstract int[] batchDelete(Class<?> entityClass,List<?> ids);

	/**
	 * Batch deletes all the records which id in the given arrays of the given entity.
	 */
	public abstract int[] batchDelete(Class<?> entityClass,Object[] ids);

	/**
	 * Batch deletes all the records which id in the given arrays of the given entity mapping.
	 */
	public abstract int[] batchDelete(EntityMapping em,List<?> ids);

	/**
	 * Batch deletes all the records which id in the given arrays of the given entity mapping.
	 */
	public abstract int[] batchDelete(EntityMapping em,Object[] ids);

	//----------------------------transaction--------------------------------
	
	/**
	 * Executes the given callback transactional.
	 */
	public abstract void doTransaction(TransactionCallback callback);
	
	/**
	 * Executes the given callback transactional.
	 */
	public abstract <T> T doTransaction(TransactionCallbackWithResult<T> callback);
	
	/**
	 * Executes the given callback transactional.
	 */
	public abstract void doTransaction(TransactionCallback callback, boolean requiresNew);
	
	/**
	 * Executes the given callback transactional.
	 */
	public abstract <T> T doTransaction(TransactionCallbackWithResult<T> callback, boolean requiresNew);

}