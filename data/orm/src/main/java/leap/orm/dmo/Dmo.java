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
package leap.orm.dmo;

import leap.db.model.DbSchema;
import leap.lang.annotation.Dangerous;
import leap.orm.Orm;
import leap.orm.OrmContext;
import leap.orm.command.CreateEntityCommand;
import leap.orm.command.CreateTableCommand;
import leap.orm.command.DropTableCommand;
import leap.orm.command.UpgradeSchemaCommand;
import leap.orm.df.DataFactory;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.EntityNotFoundException;
import leap.orm.mapping.MappingExistsException;
import leap.orm.mapping.MappingNotFoundException;

import java.util.List;

/**
 * Dmo means Data Management Object.
 */
public abstract class Dmo {
	
	public static Dmo get() {
		return Orm.dmo();
	}
	
	public static Dmo get(String name){
		return Orm.dmo(name);
	}
	
	/**
	 * Returns the {@link OrmContext} instance.
	 */
	public abstract OrmContext getOrmContext();
	
	/**
	 * Returns a {@link List} contains all the {@link DbSchema} in current datasource.
	 */
	public abstract List<DbSchema> getDbSchemas();
	
	/**
	 * Returns the {@link DataFactory} instance.
	 */
	public abstract DataFactory getDataFactory();
	
	/**
	 * Truncates all the data in the table(s) mapping to the given entity immediately.
	 * 
	 * <p>
	 * <font color="red"><strong>
	 * Be careful : this method will clear all data in the table(s) mapping to the given entity type.
	 * </strong></font>
	 * 
	 * @throws MappingNotFoundException if the given entity not exists.
	 */
	@Dangerous(askForConfirm=true)
	public abstract void truncate(Class<?> entityClass);
	
	/**
	 * Creates the table of the entity if not exists.
	 * 
	 * <p>
	 * Returns <code>true</code> if the creation of table was executed.
	 * 
	 * <p>
	 * Returns <code>false</code> if do nothing.
	 * 
	 * <p>
	 * This operation do not compares the table's schema, just create the table or do nothing.
	 */
	public abstract boolean createTableIfNotExists(Class<?> entityClass); 
	
	/**
	 * Creates a new {@link CreateEntityCommand} command for creating entity of the given entity class later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 * 
	 * @throws MappingExistsException if the given entity aleady exists.
	 */
	public abstract CreateEntityCommand cmdCreateEntity(Class<?> entityClass);

	/**
	 * Creates a new {@link CreateTableCommand} for creating table of entity.
     */
	public abstract CreateTableCommand cmdCreateTable(Class<?> entityClass);

    /**
     * Creates a new {@link CreateTableCommand} for creating table of entity.
     */
    public abstract CreateTableCommand cmdCreateTable(EntityMapping em);

	/**
	 * Creates a new {@link DropTableCommand} command for dropping the table of entity.
     */
	@Dangerous(askForConfirm = true)
	public abstract DropTableCommand cmdDropTable(Class<?> entityClass);
	
	/**
	 * Creates an {@link UpgradeSchemaCommand} for the entity class.
	 * 
	 * @throws EntityNotFoundException if no entity mapping exists of the entity class.
	 */
	public abstract UpgradeSchemaCommand cmdUpgradeSchema(Class<?> entityClass) throws EntityNotFoundException;
	
    /**
     * Creates an {@link UpgradeSchemaCommand} for the entity mapping.
     */
    public abstract UpgradeSchemaCommand cmdUpgradeSchema(EntityMapping em);	
	
	/**
	 * Creates a new {@link UpgradeSchemaCommand} command for applying schema changes to the underlying database later.
	 * 
	 * <p>
	 * <strong>Note :</strong>
	 * 
	 * <p>
	 * <strong>
	 * You must invoke the <code>execute()</code> method in the returned command to perform this operation.
	 * </strong>
	 */
	public abstract UpgradeSchemaCommand cmdUpgradeSchema();
	
}