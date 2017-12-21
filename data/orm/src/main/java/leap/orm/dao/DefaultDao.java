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

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.exception.EmptyRecordsException;
import leap.core.exception.TooManyRecordsException;
import leap.core.ioc.PreInjectBean;
import leap.core.jdbc.JdbcExecutor;
import leap.core.validation.Errors;
import leap.core.validation.Validation;
import leap.core.validation.ValidationManager;
import leap.core.value.Record;
import leap.lang.*;
import leap.lang.params.ArrayParams;
import leap.lang.tostring.ToStringBuilder;
import leap.orm.Orm;
import leap.orm.OrmContext;
import leap.orm.OrmRegistry;
import leap.orm.command.InsertCommand;
import leap.orm.command.UpdateCommand;
import leap.orm.enums.RemoteType;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.EntityNotFoundException;
import leap.orm.mapping.MappingNotFoundException;
import leap.orm.mapping.Mappings;
import leap.orm.model.Model;
import leap.orm.query.CriteriaQuery;
import leap.orm.query.EntityQuery;
import leap.orm.query.Query;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlContext;
import leap.orm.sql.SqlNotFoundException;
import leap.orm.validation.EntityValidator;
import leap.orm.value.Entity;
import leap.orm.value.EntityBase;
import leap.orm.value.EntityWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DefaultDao extends DaoBase implements PreInjectBean {

	private final Readonly _readonly = new Readonly("this dao aleady inited,can not change the internal state");

	protected @Inject @M EntityValidator   entityValidator;
	protected @Inject @M ValidationManager validationManager;

	protected SqlContext simpleSqlContext = new SimpleSqlContext();

	public DefaultDao(){

	}

	public DefaultDao(String name){
		this.name = name;
	}

    public DefaultDao(OrmContext context){
        this.name = context.getName();
        this.ormContext = context;
    }

	//-------------------- validate -----------------------------------
	@Override
    public Errors validate(Object entity) {
	    return validate(emForObject(entity),entity,0);
    }

	@Override
    public Errors validate(Object entity, int maxErrors) {
		return validate(emForObject(entity),entity,maxErrors);
    }

	@Override
    public Errors validate(EntityMapping em, Object entity) {
	    return validate(em,entity,0);
    }

    @Override
    public Errors validate(EntityMapping em, Object entity, Iterable<String> fields) {
        return validate(em, entity, 0, fields);
    }

    @Override
    public Errors validate(EntityMapping em, Object entity, int maxErrors) {
        return validate(em, entity, maxErrors, null);
    }

    @Override
    public Errors validate(EntityMapping em, Object entity, int maxErrors, Iterable<String> fields) {
        Validation validation = validationManager.createValidation();

        entityValidator.validate(EntityWrapper.wrap(em, entity), validation, maxErrors, fields);

        return validation.errors();
    }

    //--------------------- insert ------------------------------------

	@Override
    public int insert(Object entity) {
		Args.notNull(entity,"entity");

		EntityMapping em = emForObject(entity);

		return runInWrapperContext(em, (context)->{
			return commandFactory().newInsertCommand(context.getDao(), context.getEntityMapping()).from(entity).execute();
		});
    }

    @Override
    public int insert(String entityName, Object entity) {
        return insert(em(entityName), entity, null);
    }

    @Override
    public int insert(Class<?> entityClass, Object entity) throws MappingNotFoundException {
        Args.notNull(entityClass, "entity class");
        Args.notNull(entity, "entity");
        return insert(em(entityClass), entity, null);
    }

    @Override
    public int insert(EntityMapping em, Object entity, Object id) {
    	return runInWrapperContext(em, (context)->{
	        InsertCommand insert =
	                commandFactory().newInsertCommand(context.getDao(), context.getEntityMapping()).from(entity);

	        if(null != id) {
	            insert.withId(id);
	        }

	        return insert.execute();
    	});
    }

    @Override
    public InsertCommand cmdInsert(Class<?> entityClass) {
		Args.notNull(entityClass,"entity class");
		return runInWrapperContext(em(entityClass), (context)->{
			return commandFactory().newInsertCommand(context.getDao(), context.getEntityMapping());
		});
    }

	@Override
    public InsertCommand cmdInsert(String entityName) {
		Args.notEmpty(entityName,"entity name");
		return runInWrapperContext(em(entityName), (context)->{
			return commandFactory().newInsertCommand(context.getDao(), context.getEntityMapping());
		});
    }

	@Override
    public InsertCommand cmdInsert(EntityMapping em) {
		Args.notEmpty(em,"entity mapping");
		return runInWrapperContext(em, (context)->{
			return commandFactory().newInsertCommand(context.getDao(), context.getEntityMapping());
		});
    }

	//--------------------- update ------------------------------------

	@Override
    public int update(Object entity) throws MappingNotFoundException {
		Args.notNull(entity,"entity");

		EntityMapping em = emForObject(entity);

		return runInWrapperContext(em, (context)->{
			return commandFactory().newUpdateCommand(context.getDao(), context.getEntityMapping()).from(entity).execute();
		});
    }

    @Override
    public int update(Class<?> entityClass, Object entity) throws MappingNotFoundException {
        return cmdUpdate(entityClass).from(entity).execute();
    }

    @Override
    public int update(String entityName, Object entity) throws MappingNotFoundException {
        return cmdUpdate(entityName).from(entity).execute();
    }

    @Override
    public int update(EntityMapping em, Object entity) throws MappingNotFoundException {
        return cmdUpdate(em).from(entity).execute();
    }

    @Override
    public int update(Object entity, Map<String, Object> fields) throws MappingNotFoundException {
        return cmdUpdate(entity.getClass()).withId(entity).setAll(fields).execute();
    }

    @Override
    public int update(Class<?> entityClass, Object id, Map<String, Object> fields) throws MappingNotFoundException {
        return cmdUpdate(entityClass).withId(id).setAll(fields).execute();
    }

    @Override
    public int update(String entityName, Object id, Map<String, Object> fields) throws MappingNotFoundException {
        return cmdUpdate(entityName).withId(id).setAll(fields).execute();
    }

    @Override
    public int update(EntityMapping em, Object id, Map<String, Object> fields) throws MappingNotFoundException {
        return cmdUpdate(em).withId(id).setAll(fields).execute();
    }

    @Override
    public UpdateCommand cmdUpdate(Class<?> entityClass) throws MappingNotFoundException {
		Args.notNull(entityClass,"entity class");
		return runInWrapperContext(em(entityClass), (context)->{
			return commandFactory().newUpdateCommand(context.getDao(),context.getEntityMapping());
		});
    }

	@Override
    public UpdateCommand cmdUpdate(String entityName) throws MappingNotFoundException {
		Args.notEmpty(entityName,"entity name");
		return runInWrapperContext(em(entityName), (context)->{
			return commandFactory().newUpdateCommand(context.getDao(),context.getEntityMapping());
		});
    }

	@Override
    public UpdateCommand cmdUpdate(EntityMapping em) throws MappingNotFoundException {
		Args.notNull(em,"entity mapping");
		return runInWrapperContext(em, (context)->{
			return commandFactory().newUpdateCommand(context.getDao(),context.getEntityMapping());
		});
    }

	//--------------------- delete ------------------------------------

    @Override
    public int delete(Object entity) {
        Args.notNull(entity, "entity");
        EntityMapping em = em(entity.getClass());
        return delete(em, Mappings.getId(em, entity));
    }

    @Override
    public int delete(Class<?> entityClass, Object id) {
		Args.notNull(entityClass,"entityClass");
		Args.notNull(id,"id");

		return runInWrapperContext(em(entityClass), (context)->{
			return commandFactory().newDeleteCommand(context.getDao(),context.getEntityMapping(),id).execute();
		});
    }

	@Override
    public int delete(String entityName, Object id) throws MappingNotFoundException {
		Args.notNull(entityName,"entityName");
		Args.notNull(id,"id");

		return runInWrapperContext(em(entityName), (context)->{
			return commandFactory().newDeleteCommand(context.getDao(),context.getEntityMapping(),id).execute();
		});
    }

	@Override
    public int delete(EntityMapping em, Object id) {
		Args.notNull(em);
		Args.notNull(id);

		return runInWrapperContext(em, (context)->{
			return commandFactory().newDeleteCommand(context.getDao(),context.getEntityMapping(), id).execute();
		});
    }

    @Override
    public boolean cascadeDelete(Class<?> entityClass, Object id) throws MappingNotFoundException {
        return cascadeDelete(em(entityClass), id);
    }

    @Override
    public boolean cascadeDelete(String entityName, Object id) throws MappingNotFoundException {
        return cascadeDelete(em(entityName), id);
    }

    @Override
    public boolean cascadeDelete(EntityMapping em, Object id) {

    	return runInWrapperContext(em, (context)->{
    		return commandFactory().newCascadeDeleteCommand(context.getDao(),context.getEntityMapping(), id).execute();
    	});
    }

    public int deleteAll(Class<?> entityClass) {
		Args.notNull(entityClass,"entity class");

		EntityMapping em = em(entityClass);

		return runInWrapperContext(em, (context)->{
			return commandFactory().newDeleteAllCommand(context.getDao(),context.getEntityMapping()).execute();
		});
    }

	@Override
    public int deleteAll(String entityName) {
		Args.notNull(entityName);

		EntityMapping em = em(entityName);

		return runInWrapperContext(em, (context)->{
			return commandFactory().newDeleteAllCommand(context.getDao(),context.getEntityMapping()).execute();
		});
    }

	@Override
    public int deleteAll(EntityMapping em) {
		Args.notNull(em);

		return runInWrapperContext(em, (context)->{
			return commandFactory().newDeleteAllCommand(context.getDao(),context.getEntityMapping()).execute();
		});
    }

	//--------------------- find ------------------------------------
	@Override
    public <T> T find(Class<T> entityClass, Object id) {
		Args.notNull(entityClass,"entity class");
		Args.notNull(id,"id");

		return runInWrapperContext(em(entityClass), (context)->{
			return commandFactory().newFindCommand(context.getDao(),context.getEntityMapping(), id, entityClass, true).execute();
		});
    }

	@Override
    public Record find(String entityName, Object id) {
		Args.notNull(entityName,"entity name");
		Args.notNull(id,"id");

		return runInWrapperContext(em(entityName), (context)->{
			return commandFactory().newFindCommand(context.getDao(),context.getEntityMapping(), id, Record.class, true).execute();
		});
    }

    @Override
    public Record find(EntityMapping em, Object id) {
        Args.notNull(em, "entity mapping");
        Args.notNull(id, "id");

        return runInWrapperContext(em, (context)->{
        	return commandFactory().newFindCommand(context.getDao(),context.getEntityMapping(), id, Record.class, true).execute();
        });
    }

    @Override
    public <T> T find(Class<?> entityClass, Class<T> resultClass, Object id) {
        Args.notNull(entityClass, "entity class");
        return find(em(entityClass), resultClass, id);
    }

    @Override
    public <T> T find(String entityName, Class<T> resultClass, Object id) throws EmptyRecordsException, TooManyRecordsException {
		Args.notNull(entityName,"entity name");
		Args.notNull(resultClass,"result class");
		Args.notNull(id,"id");

		return runInWrapperContext(em(entityName), (context)->{
			return commandFactory().newFindCommand(context.getDao(),context.getEntityMapping(), id, resultClass, true).execute();
		});
    }

	@Override
    public <T> T find(EntityMapping em, Class<T> resultClass, Object id) throws TooManyRecordsException {
		Args.notNull(em,"entity mapping");
		Args.notNull(resultClass,"result class");
		Args.notNull(id,"id");

		return runInWrapperContext(em, (context)->{
			return commandFactory().newFindCommand(context.getDao(),context.getEntityMapping(), id, resultClass, true).execute();
		});
    }

    @Override
    public <T> T findOrNull(Class<T> entityClass, Object id) {
        Args.notNull(entityClass,"entity class");
        Args.notNull(id,"id");

        return runInWrapperContext(em(entityClass), (context)->{
        	return commandFactory().newFindCommand(context.getDao(),context.getEntityMapping(), id, entityClass, false).execute();
        });
    }

    @Override
    public Record findOrNull(String entityName, Object id) {
        Args.notNull(entityName,"entity name");
        Args.notNull(id,"id");

        return runInWrapperContext(em(entityName), (context)->{
        	return commandFactory().newFindCommand(context.getDao(),context.getEntityMapping(), id, Record.class, false).execute();
        });
    }

    @Override
    public Record findOrNull(EntityMapping em, Object id) {
    	return runInWrapperContext(em, (context)->{
    		return commandFactory().newFindCommand(context.getDao(),context.getEntityMapping(), id, Record.class, false).execute();
    	});
    }

    @Override
    public <T> T findOrNull(Class<?> entityClass, Class<T> resultClass, Object id) {
        Args.notNull(entityClass, "entity class");
        return findOrNull(em(entityClass), resultClass, id);
    }

    @Override
    public <T> T findOrNull(String entityName, Class<T> resultClass, Object id) throws EmptyRecordsException, TooManyRecordsException {
        Args.notNull(entityName,"entity name");
        Args.notNull(resultClass,"result class");
        Args.notNull(id,"id");

        return runInWrapperContext(em(entityName), (context)->{
        	return commandFactory().newFindCommand(context.getDao(),context.getEntityMapping(), id, resultClass, false).execute();
        });
    }

    @Override
    public <T> T findOrNull(EntityMapping em, Class<T> resultClass, Object id) throws TooManyRecordsException {
        Args.notNull(em,"entity mapping");
        Args.notNull(resultClass,"result class");
        Args.notNull(id,"id");

        return runInWrapperContext(em, (context)->{
        	return commandFactory().newFindCommand(context.getDao(),context.getEntityMapping(), id, resultClass, false).execute();
        });
    }

    @Override
    public <T> List<T> findList(Class<T> entityClass, Object[] ids) {
        Args.notNull(entityClass,"entity class");
        return findList(em(entityClass), entityClass, ids);
    }

    @Override
    public List<Entity> findList(String entityName, Object[] ids) {
        Args.notEmpty(entityName, "entity name");
        return findList(em(entityName), Entity.class, ids);
    }

    @Override
    public <T> List<T> findList(String entityName, Class<T> resultClass, Object[] ids) {
        Args.notEmpty(entityName, "entity name");
        return findList(em(entityName), resultClass, ids);
    }

    @Override
    public <T> List<T> findList(EntityMapping em, Class<T> resultClass, Object[] ids) throws TooManyRecordsException {
		Args.notNull(em,"entity mapping");
		Args.notNull(resultClass,"result class");
		Args.notNull(ids,"ids");

		if(ids.length == 0) {
			return new ArrayList<T>();
		}

		return runInWrapperContext(em, (context)->{
			 return commandFactory().newFindListCommand(context.getDao(),context.getEntityMapping(), ids, resultClass, resultClass, true).execute();
		});
    }

	@Override
    public <T> List<T> findListIfExists(Class<T> entityClass, Object[] ids) {
	    Args.notNull(entityClass, "entity class");

	    return findListIfExists(em(entityClass), entityClass, ids);
    }

    @Override
    public List<Record> findListIfExists(String entityName, Object[] ids) {
        Args.notEmpty(entityName, "entity name");
        return findListIfExists(em(entityName), Record.class, ids);
    }

    @Override
    public <T> List<T> findListIfExists(String entityName, Class<T> resultClass, Object[] ids) {
        Args.notEmpty(entityName, "entity name");
        return findListIfExists(em(entityName), resultClass, ids);
    }

    @Override
    public <T> List<T> findListIfExists(EntityMapping em, Class<T> resultClass, Object[] ids) throws TooManyRecordsException {
        Args.notNull(em,"entity mapping");
        Args.notNull(resultClass,"result class");
        Args.notNull(ids,"ids");

        if(ids.length == 0) {
            return new ArrayList<T>();
        }
        return runInWrapperContext(em, (context)->{
        	return commandFactory().newFindListCommand(context.getDao(),context.getEntityMapping(), ids, resultClass, resultClass, false).execute();
        });
    }

    @Override
    public <T> List<T> findAll(Class<T> entityClass) {
		Args.notNull(entityClass,"entity class");
		return runInWrapperContext(em(entityClass), (context)->{
			return commandFactory().newFindAllCommand(context.getDao(),context.getEntityMapping(), entityClass, entityClass).execute();
		});
	}

	@Override
    public <T> List<T> findAll(String entityName, Class<T> resultClass) {
		Args.notNull(entityName,"entity name");
		Args.notNull(resultClass,"result class");
		return runInWrapperContext(em(entityName), (context)->{
			return commandFactory().newFindAllCommand(context.getDao(),context.getEntityMapping(), resultClass, resultClass).execute();
		});
    }

	//----------------------------count and exists---------------------

	@Override
    public boolean exists(Class<?> entityClass, Object id) {
		Args.notNull(entityClass,"entity class");
		Args.notNull(id,"id");
		return runInWrapperContext(em(entityClass), (context)->{
			return commandFactory().newCheckEntityExistsCommand(context.getDao(),context.getEntityMapping(), id).execute();
		});
    }

	@Override
    public long count(Class<?> entityClass) {
		Args.notNull(entityClass,"entity class");
		return commandFactory().newCountEntityCommand(this, em(entityClass)).execute();
    }

	//-------------------- execute -----------------------------------
	public int executeUpdate(SqlCommand command, Object[] args) {
	    return command.executeUpdate(simpleSqlContext, args);
	}

	@Override
    public int executeUpdate(SqlCommand command, Object bean) {
        return command.executeUpdate(simpleSqlContext, bean);
    }

    public int executeUpdate(SqlCommand command, Map params) {
	    return command.executeUpdate(simpleSqlContext, params);
	}

    @Override
    public int executeUpdate(String sql, Object bean) {
        return executeUpdate(sqlFactory().createSqlCommand(ormContext, sql), bean);
    }

    @Override
    public int executeUpdate(String sql, Map params) {
        return executeUpdate(sqlFactory().createSqlCommand(ormContext, sql), params);
    }

	@Override
    public int executeNamedUpdate(String sqlKey, Object[] args) {
		return ensureGetSqlCommand(sqlKey).executeUpdate(simpleSqlContext, new ArrayParams(args));
    }

	@Override
    public int executeNamedUpdate(String sqlKey, Map<String, Object> params) {
	    return ensureGetSqlCommand(sqlKey).executeUpdate(simpleSqlContext, params);
    }

	@Override
    public int executeNamedUpdate(String sqlKey, Object bean) {
        return ensureGetSqlCommand(sqlKey).executeUpdate(simpleSqlContext, Beans.toMap(bean));
    }

    protected SqlCommand ensureGetSqlCommand(String key) {
		Args.notEmpty(key, "sql key");

		SqlCommand command = metadata().tryGetSqlCommand(key);
		if(null == command){
			throw new SqlNotFoundException("Sql command '" + key + "' not found");
		}

		return command;
	}

	//--------------------- query ------------------------------------
	@Override
	public Query<Record> createQuery(SqlCommand command) {
		return queryFactory().createQuery(this, Record.class, command);
	}

	public <T> Query<T> createQuery(Class<T> resultClass, SqlCommand command) {
	    return queryFactory().createQuery(this, resultClass, command);
	}

    @Override
    public Query<Record> createSqlQuery(String sql) {
	    return (Query)createSqlQuery(Record.class, sql);
    }

    @Override
    public Query<Record> createSqlQuery(String sql, Object... args) {
        return createSqlQuery(sql).params(args);
    }

    @Override
    public <T> Query<T> createSqlQuery(Class<T> resultClass, String sql) {
		Args.notNull(resultClass,"resultClass");
		Args.notEmpty(sql,"sql");

		if(isEntityClass(resultClass)){
			return this.createSqlQuery(metadata().getEntityMapping(resultClass),resultClass,sql);
		}

		return queryFactory().createQuery(this, resultClass, sql);
    }

    @Override
    public EntityQuery<Record> createSqlQuery(EntityMapping em, String sql) {
	    return (EntityQuery)createSqlQuery(em, Record.class, sql);
    }

	@Override
    public <T> EntityQuery<T> createSqlQuery(EntityMapping em, Class<T> resultClass, String sql) {
		Args.notNull(em,"entityMapping");
		Args.notNull(resultClass,"resultClass");
		Args.notEmpty(sql,"sql");
		return runInWrapperContext(em, (context)->{
			return queryFactory().createEntityQuery(context.getDao(),context.getEntityMapping(), resultClass, sql);
		});
    }

	@Override
    public <T> CriteriaQuery<T> createCriteriaQuery(Class<T> entityClass) {
		Args.notNull(entityClass,"entity class");
		return runInWrapperContext(em(entityClass), (context)->{
			return queryFactory().createCriteriaQuery(context.getDao(),context.getEntityMapping(), entityClass);
		});
    }

    @Override
    public CriteriaQuery<Record> createCriteriaQuery(String entityName) {
        Args.notNull(entityName, "entityName");
        return runInWrapperContext(em(entityName), (context)->{
        	return queryFactory().createCriteriaQuery(context.getDao(),context.getEntityMapping(), Record.class);
        });
    }

    @Override
    public CriteriaQuery<Record> createCriteriaQuery(EntityMapping em) {
        Args.notNull(em, "entity mapping");
        return runInWrapperContext(em, (context)->{
        	return queryFactory().createCriteriaQuery(context.getDao(),context.getEntityMapping(), Record.class);
        });
    }

    @Override
    public <T> CriteriaQuery<T> createCriteriaQuery(EntityMapping em, Class<T> resultClass) {
		Args.notNull(em,"entity mapping");
		return runInWrapperContext(em, (context)->{
			return queryFactory().createCriteriaQuery(context.getDao(),context.getEntityMapping(), resultClass);
		});
    }

    @Override
    public <T> CriteriaQuery<T> createCriteriaQuery(Class<?> entityClass, Class<T> resultClass) {
        Args.notNull(entityClass, "entity class");
        Args.notNull(resultClass, "result class");

        return runInWrapperContext(em(entityClass), (context)->{
        	return queryFactory().createCriteriaQuery(context.getDao(),context.getEntityMapping() , resultClass);
        });
    }

    @Override
    public Query<Record> createNamedQuery(String queryName) {
		Args.notEmpty(queryName,"query name");

		SqlCommand command = metadata().tryGetSqlCommand(queryName);
		if(null == command){
			throw new SqlNotFoundException("Query '" + queryName + "' not found");
		}

	    return (Query)queryFactory().createQuery(this, Record.class, command);
    }

	@Override
    public <T> Query<T> createNamedQuery(String queryName, Class<T> resultClass) {
		Args.notEmpty(queryName,"query name");
		Args.notNull(resultClass,"result class");

		if(isEntityClass(resultClass)){
			return this.createNamedQuery(resultClass,queryName);
		}

		SqlCommand command = metadata().tryGetSqlCommand(queryName);
		if(null == command){
			throw new SqlNotFoundException("Query '" + queryName + "' not found");
		}

	    return queryFactory().createQuery(this, resultClass, command);
    }

	@Override
    public <T> EntityQuery<T> createNamedQuery(Class<T> entityClass,String queryName) {
		Args.notNull(entityClass,"entity class");
		Args.notEmpty(queryName,"query name");

		EntityMapping em = metadata().getEntityMapping(entityClass);

		SqlCommand command = metadata().tryGetSqlCommand(queryName);

		if(null == command) {
			command = metadata().tryGetSqlCommand(em.getEntityName(),queryName);
		}

		if(null == command){
			throw new SqlNotFoundException("Query '" + queryName + "' not found for entity class '" + entityClass.getName() + "'");
		}

		final SqlCommand c=command;
		return runInWrapperContext(em, (context)->{
			return queryFactory().createEntityQuery(context.getDao(), context.getEntityMapping(), entityClass, c);
		});
	}

	@Override
    public EntityQuery<Record> createNamedQuery(String entityName, String queryName) {
	    return createNamedQuery(entityName, Record.class, queryName);
    }

	@Override
    public <T> EntityQuery<T> createNamedQuery(String entityName, Class<T> resultClass, String queryName) {
		Args.notEmpty(entityName,"entity name");
		Args.notEmpty(queryName,"query name");
		Args.notNull(resultClass,"result class");

		EntityMapping em = metadata().getEntityMapping(entityName);

		SqlCommand command = metadata().tryGetSqlCommand(queryName);

		if(null == command) {
			command = metadata().tryGetSqlCommand(em.getEntityName(), queryName);
		}

		if(null == command){
			throw new SqlNotFoundException("Query '" + queryName + "' not found for entity '" + entityName + "'");
		}

		final SqlCommand c=command;
		return runInWrapperContext(em, (context)->{
			return queryFactory().createEntityQuery(context.getDao(), context.getEntityMapping(), resultClass, c);
		});
    }

	@Override
    public <T> EntityQuery<T> createNamedQuery(EntityMapping em, Class<T> resultClass, String queryName) {
		Args.notEmpty(em,"entity mapping");
		Args.notNull(resultClass,"result class");
		Args.notEmpty(queryName,"query name");

		SqlCommand command = metadata().tryGetSqlCommand(queryName);

		if(null == command) {
			command = metadata().tryGetSqlCommand(em.getEntityName(), queryName);
		}

		if(null == command){
			throw new SqlNotFoundException("Query '" + queryName + "' not found for entity '" + em.getEntityName() + "'");
		}

		final SqlCommand c=command;
		return runInWrapperContext(em, (context)->{
			return queryFactory().createEntityQuery(context.getDao(), context.getEntityMapping(), resultClass, c);
		});
    }

	//--------------------- batch ------------------------------------
	@Override
    public int[] batchInsert(List<?> entities) {
		if(null == entities || entities.isEmpty()){
			return Arrays2.EMPTY_INT_ARRAY;
		}
	    return doBatchInsert(emForObject(entities.get(0)),entities.toArray());
    }

	@Override
    public int[] batchInsert(Object[] entities) {
		if(null == entities || entities.length == 0){
			return Arrays2.EMPTY_INT_ARRAY;
		}
	    return doBatchInsert(emForObject(entities[0]), entities);
    }

	@Override
	public int[] batchInsert(String entityName, List<?> records) {
		if(null == records || records.size() == 0) {
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchInsert(em(entityName), records.toArray());
	}

	@Override
	public int[] batchInsert(String entityName, Object[] records) {
		if(null == records || records.length == 0) {
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchInsert(em(entityName), records);
	}

	@Override
	public int[] batchInsert(Class<?> entityClass, List<?> records) {
		if(null == records || records.size() == 0) {
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchInsert(em(entityClass), records.toArray());
	}

	@Override
	public int[] batchInsert(Class<?> entityClass, Object[] records) {
		if(null == records || records.length == 0) {
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchInsert(em(entityClass), records);
	}

	@Override
	public int[] batchInsert(EntityMapping em, List<?> records) {
		if(null == records || records.isEmpty()){
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchInsert(em, records.toArray());
	}

	@Override
	public int[] batchInsert(EntityMapping em, Object[] records) {
		Args.notNull(em,"entity mapping");

		if(null == records || records.length == 0) {
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchInsert(em, records);
	}

	@Override
	public int[] batchUpdate(List<?> entities) {
		if(null == entities || entities.size() == 0){
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchUpdate(emForObject(entities.get(0)), entities.toArray());
	}

	@Override
	public int[] batchUpdate(Object[] entities) {
		if(null == entities || entities.length == 0) {
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchUpdate(emForObject(entities[0]), entities);
	}

	@Override
	public int[] batchUpdate(String entityName, List<?> records) {
		if(null == records || records.size() == 0){
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchUpdate(em(entityName), records.toArray());
	}

	@Override
	public int[] batchUpdate(String entityName, Object[] records) {
		if(null == records || records.length == 0) {
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchUpdate(em(entityName), records);
	}

	@Override
	public int[] batchUpdate(Class<?> entityClass, List<?> records) {
		if(null == records || records.size() == 0) {
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchUpdate(em(entityClass), records.toArray());
	}

	@Override
	public int[] batchUpdate(Class<?> entityClass, Object[] records) {
		if(null == records || records.length == 0) {
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchUpdate(em(entityClass), records);
	}

	@Override
	public int[] batchUpdate(EntityMapping em, List<?> records) {
		Args.notNull(em,"entity mapping");

		if(null == records || records.size() == 0) {
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchUpdate(em, records.toArray());
	}

	@Override
	public int[] batchUpdate(EntityMapping em, Object[] records) {
		Args.notNull(em,"entity mapping");

		if(null == records || records.length == 0) {
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchUpdate(em, records);
	}

	@Override
	public int[] batchDelete(String entityName, List<?> ids) {
		if(null == ids || ids.size() == 0) {
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchDelete(em(entityName), ids.toArray());
	}

	@Override
	public int[] batchDelete(String entityName, Object[] ids) {
		if(null == ids || ids.length == 0) {
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchDelete(em(entityName), ids);
	}

	@Override
	public int[] batchDelete(Class<?> entityClass, List<?> ids) {
		if(null == ids || ids.size() == 0) {
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchDelete(em(entityClass), ids.toArray());
	}

	@Override
	public int[] batchDelete(Class<?> entityClass, Object[] ids) {
		if(null == ids || ids.length == 0) {
			return Arrays2.EMPTY_INT_ARRAY;
		}
		return doBatchDelete(em(entityClass), ids);
	}

	@Override
	public int[] batchDelete(EntityMapping em, List<?> ids) {
		Args.notNull(em,"entity mapping");

		if(null == ids || ids.size() == 0){
			return Arrays2.EMPTY_INT_ARRAY;
		}

		return doBatchDelete(em, ids.toArray());
	}

	@Override
    public int[] batchDelete(EntityMapping em, Object[] ids) {
		Args.notNull(em,"entity mapping");

		if(null == ids || ids.length == 0){
			return Arrays2.EMPTY_INT_ARRAY;
		}

		return doBatchDelete(em, ids);
    }

	protected int[] doBatchInsert(EntityMapping em, Object[] records) {
		return runInWrapperContext(em, (context)->{
			return commandFactory().newBatchInsertCommand(context.getDao(), context.getEntityMapping(), records).execute();
		});
	}

	protected int[] doBatchUpdate(EntityMapping em, Object[] records) {
		return runInWrapperContext(em, (context)->{
			return commandFactory().newBatchUpdateCommand(context.getDao(), context.getEntityMapping(), records).execute();
		});
	}

	protected int[] doBatchDelete(EntityMapping em, Object[] ids) {
		return runInWrapperContext(em, (context)->{
			return commandFactory().newBatchDeleteCommand(context.getDao(), context.getEntityMapping(), ids).execute();
		});
	}

	//--------------------- other ------------------------------------
	protected EntityMapping emForObject(Object object) throws MappingNotFoundException {
		if(object instanceof EntityBase){
			return em(((EntityBase) object).getEntityName());
		}
		return em(object.getClass());
	}

	protected EntityMapping em(String name) throws MappingNotFoundException {
		return ormContext.getMetadata().getEntityMapping(name);
	}

	protected EntityMapping em(Class<?> type) throws MappingNotFoundException {
		EntityMapping em = ormContext.getMetadata().tryGetEntityMapping(type);
        if(null == em && !Model.class.isAssignableFrom(type) && !ormContext.getMappingStrategy().isExplicitEntity(ormContext, type)) {
            em = ormContext.getMetadata().tryGetEntityMapping(type.getSimpleName());
        }
        if(null == em) {
            throw new EntityNotFoundException("No entity mapping to '" + type.getName() + " or '" + type.getSimpleName() + "'");
        }
        return em;
	}

	@Override
    public void preInject(BeanFactory factory) {
		_readonly.check();
		if(null == ormContext){
		    if(Strings.equals(name, Orm.DEFAULT_NAME)){
		    	ormContext = factory.tryGetBean(OrmContext.class);
		    }else{
		    	ormContext = factory.tryGetBean(OrmContext.class,name);
		    }
		}
    }

	@Override
    public String toString() {
		ToStringBuilder tsb = new ToStringBuilder(this);

		if(null != ormContext){
			tsb.append("dataSource",ormContext.getDataSource());
		}

		return tsb.toString();
	}

	protected class SimpleSqlContext implements SqlContext {
		@Override
        public OrmContext getOrmContext() {
	        return DefaultDao.this.getOrmContext();
        }

		@Override
        public JdbcExecutor getJdbcExecutor() {
	        return DefaultDao.this;
        }

		@Override
        public EntityMapping getPrimaryEntityMapping() {
	        return null;
        }
	}

	protected boolean isEntityClass(Class<?> clzz){
		return metadata().tryGetEntityMapping(clzz)!=null;
	}

	protected class WrapperContext{
		private Dao dao;
		private EntityMapping entityMapping;
		public WrapperContext(){

		}
		public WrapperContext(Dao dao,EntityMapping em){
			this.dao=dao;
			this.setEntityMapping(em);
		}
		public Dao getDao() {
			return dao;
		}
		public void setDao(Dao dao) {
			this.dao = dao;
		}
		public EntityMapping getEntityMapping() {
			return entityMapping;
		}
		public void setEntityMapping(EntityMapping entityMapping) {
			this.entityMapping = entityMapping;
		}
	}

	/**
	 * 对远程实体进行检查，屏蔽Rest实体的dao操作，对远程db实体，动态切换dao
	 * @param originalEm
	 * @param func
	 * @return
	 */
	private <T> T runInWrapperContext(EntityMapping originalEm,Function<WrapperContext,T> func){
		WrapperContext context=new WrapperContext(this,originalEm);
		if(!originalEm.isRemote()){
			return func.apply(context);
		}
		if(RemoteType.rest.equals(originalEm.getRemoteSettings().getRemoteType())){
			throw new RuntimeException("remote rest entity isn't supported.");
		}
		String remoteDs=originalEm.getRemoteSettings().getDataSource();
		OrmContext targetOrmContext= Orm.context(remoteDs);
		if(targetOrmContext==null){
			throw new RuntimeException("remote orm context can't be found.");
		}
		EntityMapping targetEm=targetOrmContext.getMetadata().tryGetEntityMapping(originalEm.getEntityName());
		if(targetEm==null){
			throw new RuntimeException("remote entity mapping can't be found.");
		}
		context.setDao(targetOrmContext.getDao());
		context.setEntityMapping(targetEm);
		return func.apply(context);
	}

}