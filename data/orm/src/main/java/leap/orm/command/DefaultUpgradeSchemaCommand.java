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

import leap.db.DbCommand;
import leap.db.DbCommands;
import leap.db.DbExecution;
import leap.db.change.*;
import leap.db.command.*;
import leap.db.model.DbColumn;
import leap.db.model.DbColumnBuilder;
import leap.db.model.DbSchema;
import leap.db.model.DbTable;
import leap.lang.Collections2;
import leap.lang.Error;
import leap.lang.Strings;
import leap.lang.collection.WrappedCaseInsensitiveMap;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.dmo.Dmo;
import leap.orm.mapping.EntityMapping;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class DefaultUpgradeSchemaCommand extends AbstractDmoCommand implements UpgradeSchemaCommand,Predicate<DbCommand> {

	private static final Log log = LogFactory.get(DefaultUpgradeSchemaCommand.class);

    protected EntityMapping[] entityMappings;
    protected boolean         dropTableEnabled;
    protected boolean         dropTableObjectsEnabled;
    protected boolean         dropColumnEnabled;
    protected boolean         dropPrimaryKeyEnabled;
    protected boolean         dropForeignKeyEnabled;
    protected boolean         dropIndexEnabled;
    protected boolean         alterColumnEnabled;
    protected DbExecution     execution;

	public DefaultUpgradeSchemaCommand(Dmo dmo) {
	    super(dmo);
    }

    public DefaultUpgradeSchemaCommand(Dmo dmo, EntityMapping[] entityMappings) {
        super(dmo);
        this.entityMappings = entityMappings;
    }

	@Override
    public UpgradeSchemaCommand setDropTableObjectsEnabled(boolean dropTableObjectsEnabled) {
		this.dropTableObjectsEnabled = dropTableObjectsEnabled;
	    return this;
    }

	@Override
	public boolean isDropTableObjectsEnabled() {
		return dropTableObjectsEnabled;
	}

	@Override
	public boolean isDropColumnEnabled() {
		return dropColumnEnabled;
	}

	@Override
	public UpgradeSchemaCommand setDropColumnEnabled(boolean dropColumnEnabled) {
		this.dropColumnEnabled = dropColumnEnabled;
		return this;
	}

	@Override
	public boolean isDropTableEnabled() {
		return dropTableEnabled;
	}

	@Override
	public UpgradeSchemaCommand setDropTableEnabled(boolean dropTableEnabled) {
		this.dropTableEnabled = dropTableEnabled;
		return this;
	}

	@Override
	public boolean isDropPrimaryKeyEnabled() {
		return dropPrimaryKeyEnabled;
	}

	@Override
	public UpgradeSchemaCommand setDropPrimaryKeyEnabled(boolean dropPrimaryKeyEnabled) {
		this.dropPrimaryKeyEnabled = dropPrimaryKeyEnabled;
		return this;
	}

	@Override
	public boolean isDropForeignKeyEnabled() {
		return dropForeignKeyEnabled;
	}

	@Override
	public UpgradeSchemaCommand setDropForeignKeyEnabled(boolean dropForegignKeyEnabled) {
		this.dropForeignKeyEnabled = dropForegignKeyEnabled;
		return this;
	}

	@Override
	public boolean isDropIndexEnabled() {
		return dropIndexEnabled;
	}

	@Override
	public UpgradeSchemaCommand setDropIndexEnabled(boolean dropIndexEnabled) {
		this.dropIndexEnabled = dropIndexEnabled;
		return this;
	}

	@Override
	public boolean isAlterColumnEnabled() {
		return alterColumnEnabled;
	}

	@Override
	public UpgradeSchemaCommand setAlterColumnEnabled(boolean alterColumnEnabled) {
		this.alterColumnEnabled = alterColumnEnabled;
		return this;
	}

	@Override
    public boolean test(DbCommand command) {
		boolean apply = false;

		if(command instanceof DropTable){
			apply = isDropTableEnabled();

			if(!apply){
				log.info("Dropping table '{}' is disabled",((DropTable)command).getTableName());
			}

		}else if(command instanceof DropColumn){
			apply = isDropTableObjectsEnabled() || isDropColumnEnabled();

			if(!apply){
				log.info("Dropping column '{}.{}' is disabled",
						 ((DropColumn)command).getTableName(),((DropColumn)command).getColumnName());
			}
		}else if(command instanceof DropPrimaryKey){
			apply = isDropTableObjectsEnabled() || isDropPrimaryKeyEnabled();

			if(!apply){
				log.info("Dropping primary key of table '{}' is disabled",((DropPrimaryKey)command).getTableName());
			}
		}else if(command instanceof DropForeignKey){
			apply = isDropTableObjectsEnabled() || isDropForeignKeyEnabled();

			if(!apply){
				log.info("Dropping foreign key '{}' of table '{}' is disabled",
						 ((DropForeignKey)command).getForeignKeyName(),((DropForeignKey)command).getTableName());
			}
		}else if(command instanceof DropIndex){
			apply = isDropTableObjectsEnabled() || isDropIndexEnabled();

			if(!apply){
				log.info("Dropping index '{}' of table '{}' is disabled",
						((DropIndex)command).getIndexName(),((DropIndex)command).getTableName());
			}
		}else{
			apply = true;
		}

	    return apply;
    }

	@Override
    public List<? extends Error> errors() {
	    return execution.errors();
    }

    @Override
    public void printUpgradeScripts(PrintWriter out) {
        out.println();
        for(String s : getUpgradeScripts()) {
            out.println(s);
            out.println();
        }
    }

    @Override
    public void printUpgradeScripts(PrintStream out) {
        out.println();
        for(String s : getUpgradeScripts()) {
            out.println(s);
            out.println();
        }
    }

    @Override
    public List<String> getUpgradeScripts() {
        Predicate<SchemaChange> changePredicate = change -> {
            if (change instanceof ColumnDefinitionChange) {
                if (!isAlterColumnEnabled()) {

                    ColumnDefinitionChange cdc = (ColumnDefinitionChange) change;

                    log.info("Ignore the definition change of column '{}.{}'",
                            cdc.getTable().getName(), cdc.getOldColumn().getName());

                    return false;
                }
                return true;
            }

            if(change instanceof TablePropertyChange) {
                //todo: handle table property change.
                return false;
            }

            if(change instanceof IndexDefinitionChange) {
                //todo: handle index definition change
                return false;
            }

            if(change instanceof ForeignKeyDefinitionChange) {
                //todo: handle foreign key definition change.
                return false;
            }

            return true;
        };

        Function<SchemaChange, SchemaChange> changeProcessor = change -> {
            //make column not null -> null if no default value for safe add column.
            if(change instanceof AddColumnChange) {
                AddColumnChange acc = (AddColumnChange)change;
                DbColumn column = acc.getNewColumn();
                if(!column.isNullable() && Strings.isEmpty(column.getDefaultValue())) {
                    log.warn("Can't add not null column '{}.{}' without default value, change to null",
                            acc.getTable().getName(), column.getName());
                    DbColumnBuilder nullColumn = new DbColumnBuilder(column);
                    nullColumn.setNullable(true);
                    return new AddColumnChange(acc.getTable(), nullColumn.build());
                }
            }
            return null;
        };

        List<SchemaChanges> allChanges = compareChanges();

        List<String> scripts = new ArrayList<>();
        for(SchemaChanges changes : allChanges){

            DbCommands changeCommands = changes.filter(changePredicate)
                                               .process(changeProcessor)
                                               .getChangeCommands();

            Collections2.addAll(scripts, changeCommands.filter(this).getExecutionScripts());
        }

        return scripts;
    }

    @Override
    protected boolean doExecute() {
        execution = db.createExecution();
        execution.addAll(getUpgradeScripts());

		if(execution.numberOfStatements() == 0){
			log.info("Found 0 changes, no need to upgrade schemas on db '{}'",db.getDescription());
			return true;
		}else{
			log.info("Applying {} sql(s) for schema upgrade on db '{}'", execution.numberOfStatements() ,db.getDescription());
		    return execution.execute();
		}
    }

	protected List<SchemaChanges> compareChanges() {
        List<SchemaChanges> allChanges = new ArrayList<SchemaChanges>();

        if(null == entityMappings || entityMappings.length == 0) {
            List<DbSchema> schemas = dmo.getDbSchemas();
            log.info("Comparing {} schemas in db '{}'", schemas.size(), db.getDescription());

            for (DbSchema source : schemas) {
                DbSchema target = db.getMetadata().getSchema(source.getCatalog(), source.getName());
                allChanges.add(db.getComparator().compareSchema(source, target));
            }
        }else{
			Map<String, DbTable> tables = new WrappedCaseInsensitiveMap<>();
            for(EntityMapping em : entityMappings) {
            	if(em.isRemote() || em.isNarrowEntity()) {
            	    continue;
                }

            	addTable(tables, em.getTable());;
                if(em.hasSecondaryTable()) {
                	addTable(tables, em.getSecondaryTable());
                }
            }
            log.info("Comparing {} tables in db '{}'", tables.size(), db.getDescription());
            DbSchema target = db.getMetadata().getSchema();
            allChanges.add(db.getComparator().compareTables(tables.values().toArray(new DbTable[0]), target));
        }

        return allChanges;
	}

	protected void addTable(Map<String, DbTable> tables, DbTable table) {
		final String key = table.getQualifiedName();
		DbTable existence = tables.get(key);
		if(null != existence) {
			if(table.isSameColumnNamesWith(existence) || table.isSubColumnsNamesOf(existence)) {
				return;
			}
			if(!existence.isSubColumnsNamesOf(table)) {
				log.warn("Found duplicated table '" + table.getName() + "' with difference columns");
				return;
			}
		}
		tables.put(key, table);
	}
}