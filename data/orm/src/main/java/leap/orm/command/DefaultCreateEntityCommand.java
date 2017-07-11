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
package leap.orm.command;

import java.util.ArrayList;
import java.util.List;

import leap.db.DbExecution;
import leap.db.change.SchemaChanges;
import leap.db.model.DbTable;
import leap.lang.Args;
import leap.lang.Error;
import leap.orm.dmo.Dmo;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.EntityMappingBuilder;
import leap.orm.mapping.MappingExistsException;

public class DefaultCreateEntityCommand extends AbstractDmoCommand implements CreateEntityCommand {
	
	protected final Class<?>             entityClass;
	protected final EntityMappingBuilder emb;
	
	protected boolean	  dropTableIfExists;
	protected DbExecution dropTableExecution;
	protected boolean 	  createTable;
	protected DbExecution createTableExecution;
	protected boolean     upgradeTable;
	protected DbExecution upgradeTableExecution;
	
	public DefaultCreateEntityCommand(Dmo dmo,Class<?> entityClass){
		super(dmo);
		this.entityClass = entityClass;
		
		if(null != metadata.tryGetEntityMapping(entityClass)){
			throw new MappingExistsException("The entity mapping for class '" + entityClass.getName() + "' aleady exists");
		}

		emb = dmo.getOrmContext().getMappingStrategy().createEntityClassMapping(context,entityClass);

		if(null != metadata.tryGetEntityMapping(emb.getEntityName())){
			throw new MappingExistsException("Entity named '" + emb.getEntityName() + "' aleady exists, check the class '" + entityClass.getName() + "'");
		}
	}

	@Override
    public CreateEntityCommand setCreateTable(boolean createTable) {
		this.createTable = createTable;
	    return this;
    }
	
	@Override
    public CreateEntityCommand setUpgradeTable(boolean upgrade) {
	    this.upgradeTable = upgrade;
        return this;
    }

    @Override
    public CreateEntityCommand setDropTableIfExists(boolean dropTableIfExists) {
		this.dropTableIfExists = dropTableIfExists;
	    return this;
    }
	
	@Override
    public CreateEntityCommand setTableName(String tableName) {
	    Args.notEmpty(tableName, "table name");
	    emb.setTableName(tableName);
        return this;
    }

    public boolean isDropTableIfExists() {
		return dropTableIfExists;
	}

	public boolean isCreateTable() {
		return createTable;
	}

	public DbExecution getCreateTableExecution() {
		return createTableExecution;
	}

	@Override
    public boolean execute(boolean createTable) {
	    return setCreateTable(createTable).execute();
    }
	
	@Override
    public List<? extends Error> errors() {
		List<Error> faults = new ArrayList<Error>();
		
		if(null != dropTableExecution && !dropTableExecution.success()) {
			faults.addAll(dropTableExecution.errors());
		}
		
		if(null != createTableExecution && !createTableExecution.success()){
			faults.addAll(createTableExecution.errors());
		}
		
	    return faults;
    }
	
	@Override
    protected boolean doExecute() {
	    EntityMapping em = emb.build();
	    
	    if(upgradeTable) {
	        DbTable oldTable = db.getMetadata().tryGetTable(em.getTable());
	        if(null == oldTable) {
                if(!createTable(em)) {
                    return false;
                }
	        }else{
	            if(!upgradeTable(em, oldTable)) {
	                return false;
	            }
	        }
	    }else if(createTable){
			if(dropTableIfExists && db.checkTableExists(em.getTable())){
				dropTableExecution = db.cmdDropTable(em.getTable()).execute();
				if(!dropTableExecution.success()){
					return false;
				}
			}
			
			if(!createTable(em)) {
			    return false;
			}
		}
		metadataManager.createEntity(context, em);
		return true;
    }
	
	protected boolean createTable(EntityMapping em) {
        createTableExecution = db.cmdCreateTable(em.getTable()).execute();
        return createTableExecution.success(); 
	}
	
	protected boolean upgradeTable(EntityMapping em, DbTable exists) {
	    SchemaChanges changes = 
	            db.getComparator().compareTable(em.getTable(), exists)
	              .addOnly();

	    if(changes.isEmpty()) {
	        return true;
	    }
	    
	    upgradeTableExecution = changes.applyChanges();
	    return upgradeTableExecution.success();
	}
}