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

import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;
import leap.orm.domain.Domains;
import leap.orm.domain.Domain;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.SequenceMapping;
import leap.orm.sql.SqlCommand;
import leap.orm.sql.SqlFragment;

import java.util.List;

public interface OrmMetadata {
	
	/**
	 * Returns the {@link Domains} contains all the {@link Domain} in this metadata.
	 */
	Domains domains();
	
	/**
	 * Returns a new {@link List} contains all the {@link EntityMapping} objects in this metadata.
	 * 
	 * <p>
	 * The underlying changes in this metadata will not affect the returned {@link List}, and vice-versa.
	 */
	List<EntityMapping> getEntityMappingSnapshotList();
	
	/**
	 * Returns a new {@link List} contains all the {@link SequenceMapping} objects in this metadata.
	 * 
	 * <p>
	 * The underlying changes in this metadata will not affect the returned {@link List}, and vice-versa.
	 */
	List<SequenceMapping> getSequenceMappingSnapshotList();
	
	/**
	 * Returns a new snapshot {@link List} contains all the {@link SqlCommand} objects in this metadata at the moment calling this method.
	 * 
	 * <p>
	 * The underlying changes in this metadata will not affect the returned {@link List}, and vice-versa.
	 */
	List<SqlCommand> getSqlCommandSnapshotList();
	
	/**
	 * Returns the size of {@link EntityMapping} in this metadata.
	 */
	int getEntityMappingSize();
	
	/**
	 * Returns the size of {@link SqlCommand} in this metadata.
	 */
	int getSqlCommandSize();
	
	/**
	 * Returns the size of {@link SequenceMapping} in this metadata.
	 */
	int getSequenceMappingSize();
	
	/**
	 * Returns the {@link EntityMapping} for the given <code>class</code> reprenents a entity type.
	 * 
	 * 
	 * @throws ObjectNotFoundException if no mapping exists for the given java type.
	 */
	EntityMapping getEntityMapping(Class<?> entityClass) throws ObjectNotFoundException;
	
	/**
	 * Returns the {@link EntityMapping} for the given entity name (ignore case).
	 * 
	 * @throws ObjectNotFoundException if no mapping exists for the given entity name.
	 */
	EntityMapping getEntityMapping(String entityName) throws ObjectNotFoundException;
	
	/**
	 * Returns the {@link SequenceMapping} for the given sequence name (ignore case).
	 * 
	 * @throws ObjectNotFoundException if not sequence definition exists for the given name.
	 */
	SequenceMapping getSequenceMapping(String sequenceName) throws ObjectNotFoundException;
	
	/**
	 * Returns the {@link SqlCommand} for the given command key.
	 *
	 * @throws ObjectNotFoundException if sql command does not exists.
	 */
	SqlCommand getSqlCommand(String key) throws ObjectNotFoundException;

    /**
     * Returns the {@link SqlCommand} for the given entity name and the command name.
     *
     * @throws ObjectNotFoundException if sql command does not exists.
     */
    SqlCommand getSqlCommand(String entityName,String commandName) throws ObjectNotFoundException;

    /**
	 * Returns the {@link SqlFragment} for the given command key.
	 *
	 * @throws ObjectNotFoundException if sql fragment does not exists.
	 */
	SqlFragment getSqlFragment(String key) throws ObjectNotFoundException;

	/**
	 * Returns the {@link EntityMapping} for the given <code>class</code> represents a entity type.
	 * 
	 * <p>
	 * Returns <code>null</code> if no mapping exists for the given entity class.
	 */
	EntityMapping tryGetEntityMapping(Class<?> entityClass);
	
	/**
	 * Returns the {@link EntityMapping} for the given entity name (ignore case).
	 * 
	 * <p>
	 * Returns <code>null</code> if no mapping exists for the given entity name.
	 */
	EntityMapping tryGetEntityMapping(String entityName);
	
	/**
	 * Returns the {@link EntityMapping} for given table name (ignore case).
	 * 
	 * <p>
	 * Returns <code>null</code> if no mapping exists for the given table name.
	 */
	EntityMapping tryGetEntityMappingByTableName(String tableName);

    /**
     * Returns the {@link EntityMapping} for given secondary table name (ignore case).
     */
    EntityMapping tryGetEntityMappingBySecondaryTableName(String tableName);

	/**
	 * Returns the {@link SequenceMapping} for the given name (ignore case).
	 * 
	 * <p>
	 * Returns <code>null</code> if no sequence definition exists for the name.
	 */
	SequenceMapping tryGetSequenceMapping(String sequenceName);
	
	/**
	 * Returns the {@link SqlCommand} for the given command key.
	 * 
	 * <p>
	 * Returns <code>null</code> if sql command does not exists.
	 */
	SqlCommand tryGetSqlCommand(String key);

	/**
	 * Returns the {@link SqlFragment} for the given fragment key.
	 *
	 * <p>
	 * Returns <code>null</code> if sql fragment does not exists.
	 */
	SqlFragment tryGetSqlFragment(String key);
	
	/**
	 * Returns the {@link SqlCommand} for the given entity name and the command name.
	 * 
	 * <p>
	 * Returns <code>null</code> if sql command does not exists.
	 */
	SqlCommand tryGetSqlCommand(String entityName,String commandName);
	
	/**
	 * Adds a new {@link EntityMapping} object into this metadata.
	 */
	void addEntityMapping(EntityMapping em) throws ObjectExistsException;
	
	/**
	 * Removes an exists {@link EntityMapping} object from this metadata.
	 */
	boolean removeEntityMapping(EntityMapping em);
	
	/**
	 * Removes an exists {@link EntityMapping} object from this metadata.
	 */
	EntityMapping removeEntityMapping(String entityName);
	
	/**
	 * Adds a new {@link SequenceMapping} object into this metadata.
	 */
	void addSequenceMapping(SequenceMapping seq) throws ObjectExistsException;
	
	/**
	 * Removes an exists {@link SequenceMapping} object from this metadata.
	 */
	SequenceMapping removeSequenceMapping(String sequenceName);
	
	/**
	 * Adds a new {@link SqlCommand} object to this metadata.
	 * 
	 * <p>
	 * Throws {@link ObjectExistsException} if the key exists.
	 */
	void addSqlCommand(String key,SqlCommand cmd) throws ObjectExistsException;

    /**
     * Adds a new {@link SqlCommand} objects to this metadata.
     */
    void addSqlCommand(EntityMapping em, String name, SqlCommand cmd) throws ObjectExistsException;

	/**
	 * Removes an exists {@link SqlCommand} object from this metadata.
	 * 
	 * <p>
	 * Returns the removed {@link SqlCommand} if the key mapped to an exists {@link SqlCommand}.
	 * 
	 * <p>
	 * Returns <code>null<code> if there was no mapping for the key.
	 */
	SqlCommand removeSqlCommand(String key);
	
}