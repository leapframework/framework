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
package leap.orm;

import leap.core.ioc.AbstractReadonlyBean;
import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;
import leap.orm.domain.Domains;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.EntityNotFoundException;
import leap.orm.mapping.SequenceMapping;
import leap.orm.metadata.SqlRegistry;
import leap.orm.model.Model;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlFragment;
import leap.orm.sql.SqlNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultOrmMetadata extends AbstractReadonlyBean implements OrmMetadata {
	
	public static final String SQL_COMMAND_KEY_ENTITY_NAME_TEMPLATE  = "orm.entity({0}).command({1})";
	public static final String SQL_COMMAND_KEY_ENTITY_CLASS_TEMPLATE = "orm.class({0}).command({1})";
	
	private final Object _entityLock = new Object();
	private final Object _sqlLock    = new Object();
	
	protected final Map<Class<?>,EntityMapping> classToEntityMappings  = new ConcurrentHashMap<>();
	protected final Map<Class<?>,EntityMapping> modelToEntityMappings  = new ConcurrentHashMap<>();
	protected final Map<String,EntityMapping>   nameToEntityMappings   = new ConcurrentHashMap<>();
	protected final Map<String,EntityMapping>   tableToEntityMappings  = new ConcurrentHashMap<>();
	protected final Map<String,SqlCommand>      keyToSqlCommands       = new ConcurrentHashMap<>();
	protected final Map<String,SequenceMapping> nameToSequenceMappings = new ConcurrentHashMap<>();
    protected final Map<String,EntityMapping>   shardingEntityMappings = new ConcurrentHashMap<>();

    protected Domains     domains;
    protected SqlRegistry sqlRegistry;

	@Override
	public Domains domains() {
		return domains;
	}

    public void setDomains(Domains domains) {
        this.domains = domains;
    }

    public void setSqlRegistry(SqlRegistry sqlRegistry) {
        this.sqlRegistry = sqlRegistry;
    }

    @Override
    public int getEntityMappingSize() {
	    return nameToEntityMappings.size();
    }

	@Override
    public int getSqlCommandSize() {
	    return keyToSqlCommands.size();
    }

	@Override
    public int getSequenceMappingSize() {
	    return nameToSequenceMappings.size();
    }

	@Override
    public List<EntityMapping> getEntityMappingSnapshotList() {
	    return new ArrayList<>(nameToEntityMappings.values());
    }

	@Override
    public List<SequenceMapping> getSequenceMappingSnapshotList() {
	    return new ArrayList<>(nameToSequenceMappings.values());
    }

	@Override
    public List<SqlCommand> getSqlCommandSnapshotList() {
	    return new ArrayList<SqlCommand>(keyToSqlCommands.values());
    }

	@Override
    public EntityMapping getEntityMapping(Class<?> entityClass) {
		EntityMapping em = tryGetEntityMapping(entityClass);
		
		if(null == em){
			throwEntityNotFound(entityClass);
		}
		
	    return em;
    }
	
	@Override
    public EntityMapping getEntityMapping(String entityName) throws ObjectNotFoundException {
		EntityMapping em = tryGetEntityMapping(entityName);
		
		if(null == em){
			throwEntityNotFound(entityName);
		}
		
	    return em;
    }
	
	@Override
    public SequenceMapping getSequenceMapping(String sequenceName) throws ObjectNotFoundException {
		SequenceMapping seq = tryGetSequenceMapping(sequenceName);
		
		if(null == seq){
			throw new ObjectNotFoundException("sequence '" + sequenceName + "' not found");
		}
		
	    return seq;
    }

	@Override
    public SqlCommand getSqlCommand(String key) throws ObjectNotFoundException {
		SqlCommand cmd = tryGetSqlCommand(key);
		
		if(null == cmd){
			throwSqlNotFound(key);
		}
		
	    return cmd;
    }

	@Override
	public SqlFragment getSqlFragment(String key) throws ObjectNotFoundException {
		SqlFragment fragment = tryGetSqlFragment(key);
		if(null == fragment){
			throwFragmentNotFound(key);
		}
		return fragment;
	}

	@Override
    public SqlCommand getSqlCommand(String entityName, String commandName) throws ObjectNotFoundException {
		SqlCommand cmd = tryGetSqlCommand(entityName, commandName);
		
		if(null == cmd){
			throwSqlNotFound(entityName, commandName);
		}
		
	    return cmd;
    }

	@Override
    public EntityMapping tryGetEntityMapping(Class<?> entityClass) {
		Args.notNull(entityClass,"entity class");
	    EntityMapping em = classToEntityMappings.get(entityClass);
	    return null == em && Model.class.isAssignableFrom(entityClass) ? modelToEntityMappings.get(entityClass) : em;
    }
	
	@Override
    public EntityMapping tryGetEntityMapping(String entityName) {
		Args.notNull(entityName,"entity name");
	    return nameToEntityMappings.get(entityName.toLowerCase());
    }
	
	@Override
    public EntityMapping tryGetEntityMappingByTableName(String tableName) {
		Args.notNull(tableName,"table name");
	    return tableToEntityMappings.get(tableName.toLowerCase());
    }

    @Override
    public EntityMapping tryGetEntityMappingByShardingTableName(String tableName) {
        for(EntityMapping em : shardingEntityMappings.values()) {

            if(em.isShardingTable(tableName)) {
                return em;
            }

        }

        return null;
    }

    @Override
    public SequenceMapping tryGetSequenceMapping(String sequenceName) {
		Args.notEmpty(sequenceName,"sequence name");
	    return nameToSequenceMappings.get(sequenceName.toLowerCase());
    }

	@Override
    public SqlCommand tryGetSqlCommand(String key) {
		Args.notEmpty(key,"command key");
	    return keyToSqlCommands.get(key);
    }

	@Override
	public SqlFragment tryGetSqlFragment(String key) {
		return sqlRegistry.tryGetSqlFragment(key);
	}

	@Override
    public SqlCommand tryGetSqlCommand(String entityName, String commandName) {
		Args.notEmpty(entityName,"entity name");
		Args.notEmpty(commandName,"command name");
		
	    return keyToSqlCommands.get(formatSqlCommandKey(entityName, commandName));
    }
	
	@Override
    public void addEntityMapping(EntityMapping em) throws ObjectExistsException {
		Args.notNull(em);
		
		synchronized (_entityLock) {
			checkNotExist(em);
			
			if(null != em.getEntityClass()){
				classToEntityMappings.put(em.getEntityClass(),em);	
			}
			
			if(null != em.getModelClass()){
				modelToEntityMappings.put(em.getModelClass(), em);
			}
			
			nameToEntityMappings.put(em.getEntityName().toLowerCase(), em);
			tableToEntityMappings.put(em.getTableName().toLowerCase(), em);

            if(em.isSharding()) {
                shardingEntityMappings.put(em.getEntityName().toLowerCase(), em);
            }
        }
    }
	
	@Override
    public boolean removeEntityMapping(EntityMapping em) {
		Args.notNull(em);
		
		synchronized (_entityLock) {
			String   entityNameKey = em.getEntityName().toLowerCase();
			Class<?> entityClass   = em.getEntityClass();
			
			if(null == nameToEntityMappings.remove(entityNameKey)){
				return false;
			}
			
			nameToEntityMappings.remove(entityNameKey);
            shardingEntityMappings.remove(entityNameKey);
			
			if(null != entityClass){
				classToEntityMappings.remove(entityClass);
			}
			
			if(null != em.getModelClass()){
				modelToEntityMappings.remove(em.getModelClass());
			}
			
			tableToEntityMappings.remove(em.getTableName().toLowerCase());
			
			return true;
        }
	}
	
	@Override
    public EntityMapping removeEntityMapping(String entityName) {
		Args.notEmpty(entityName,"entity name");
		
		EntityMapping em = tryGetEntityMapping(entityName);
		
		if(null != em){
			removeEntityMapping(em);
		}
		
		return em;
    }
	
	@Override
    public void addSequenceMapping(SequenceMapping seq) throws ObjectExistsException {
		Args.notNull(seq);
		
		String key = seq.getName().toLowerCase();
		
		if(nameToSequenceMappings.containsKey(key)){
			throw new ObjectExistsException("sequence '" + seq.getName() + "' aleady exists");
		}
		nameToSequenceMappings.put(key, seq);
    }

	@Override
    public SequenceMapping removeSequenceMapping(String sequenceName) {
		return nameToSequenceMappings.remove(sequenceName.toLowerCase());
    }

	@Override
    public void addSqlCommand(String key,SqlCommand cmd) throws ObjectExistsException {
		Args.notEmpty(key,"command key");
		Args.notNull(cmd,"sql command");
		synchronized (_sqlLock) {
			if(keyToSqlCommands.containsKey(key)){
				throw new ObjectExistsException("sql command '" + key + "' aleady exists");
			}
			keyToSqlCommands.put(key, cmd);
        }
    }

	@Override
    public void addSqlCommand(EntityMapping entityMapping, String commandName, SqlCommand cmd) throws ObjectExistsException {
		Args.notNull(entityMapping,"entity mapping");
		Args.notEmpty(commandName,"command name");
		Args.notNull(cmd,"sql command");
		
		synchronized (_sqlLock) {
			if(null != entityMapping.getEntityClass()){
				String key = formatSqlCommandKey(entityMapping.getEntityClass(), commandName);
				
				if(keyToSqlCommands.containsKey(key)){
					throw new ObjectExistsException("sql command '" + commandName + "' of entity class '" + entityMapping.getEntityClass().getName() + "' aleady exists");
				}
				keyToSqlCommands.put(key, cmd);
			}
			
			String key = formatSqlCommandKey(entityMapping.getEntityName(), commandName);
			if(keyToSqlCommands.containsKey(key)){
				throw new ObjectExistsException("sql command '" + commandName + "' of entity '" + entityMapping.getEntityName() + "' aleady exists");
			}
			keyToSqlCommands.put(key, cmd);
        }
    }

	@Override
    public SqlCommand removeSqlCommand(String key) {
		Args.notEmpty(key,"command key");
		return keyToSqlCommands.remove(key);
    }
	
	protected static String formatSqlCommandKey(Class<?> entityClass,String commandName){
		return Strings.format(SQL_COMMAND_KEY_ENTITY_CLASS_TEMPLATE,entityClass.getName(),commandName.toLowerCase());
	}
	
	protected static String formatSqlCommandKey(String entityName,String commandName){
		return Strings.format(SQL_COMMAND_KEY_ENTITY_NAME_TEMPLATE,entityName.toLowerCase(),commandName.toLowerCase());
	}
	
	protected static void throwEntityNotFound(Class<?> entityType){
		throw new EntityNotFoundException("No entity mapping associated with the java class '" + entityType.getName() + "'");
	}
	
	protected static void throwEntityNotFound(String entityName){
		throw new EntityNotFoundException("No entity mapping associated with the entity name '" + entityName + "'");
	}
	
	protected static void throwSqlNotFound(String key) {
		throw new SqlNotFoundException("Sql commmand '" + key + "' not found");
	}

	protected static void throwFragmentNotFound(String key) {
		throw new SqlNotFoundException("Sql fragment '" + key + "' not found");
	}

	protected static void throwSqlNotFound(String entityName,String commandName) {
		throw new SqlNotFoundException("Sql commmand '" + commandName + "' not defined for entity '" + entityName + "'");
	}
	
	protected static void throwSqlNotFound(Class<?> entityClass,String commandName) {
		throw new SqlNotFoundException("Sql commmand '" + commandName + "' not defined for entity class '" + entityClass.getName() + "'");
	}
	
	protected void checkNotExist(EntityMapping em) throws ObjectExistsException {
		checkNotExist(em.getEntityName());
		checkNotExist(em.getEntityClass());
	}
	
	protected void checkNotExist(String entityName) throws ObjectExistsException {
		if(nameToEntityMappings.containsKey(entityName.toLowerCase())){
			throw new ObjectExistsException("Entity '" + entityName + "' aleady exists");
		}
	}
	
	protected void checkNotExist(Class<?> entityClass) throws ObjectExistsException {
		if(null != entityClass){
			if(classToEntityMappings.containsKey(entityClass)){
				throw new ObjectExistsException("Entity class '" + entityClass.getName() + "' aleady exists");
			}
		}
	}
}