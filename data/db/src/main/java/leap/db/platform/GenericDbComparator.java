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
package leap.db.platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import leap.db.Db;
import leap.db.DbAware;
import leap.db.DbComparator;
import leap.db.change.AddColumnChange;
import leap.db.change.AddForeignKeyChange;
import leap.db.change.AddIndexChange;
import leap.db.change.AddPrimaryKeyChange;
import leap.db.change.AddSequenceChange;
import leap.db.change.AddTableChange;
import leap.db.change.ColumnDefinitionChange;
import leap.db.change.ColumnPropertyChange;
import leap.db.change.ForeignKeyDefinitionChange;
import leap.db.change.ForeignKeyPropertyChange;
import leap.db.change.IndexDefinitionChange;
import leap.db.change.IndexPropertyChange;
import leap.db.change.PrimaryKeyDefinitionChange;
import leap.db.change.RemoveColumnChange;
import leap.db.change.RemoveForeignKeyChange;
import leap.db.change.RemoveIndexChange;
import leap.db.change.RemovePrimaryKeyChange;
import leap.db.change.RemoveSequenceChange;
import leap.db.change.RemoveTableChange;
import leap.db.change.SchemaChanges;
import leap.db.change.SequenceDefinitionChange;
import leap.db.change.SequencePropertyChange;
import leap.db.change.TablePropertyChange;
import leap.db.change.UnsupportedChangeException;
import leap.db.model.DbCascadeAction;
import leap.db.model.DbColumn;
import leap.db.model.DbForeignKey;
import leap.db.model.DbForeignKeyColumn;
import leap.db.model.DbIndex;
import leap.db.model.DbPrimaryKey;
import leap.db.model.DbSchema;
import leap.db.model.DbSequence;
import leap.db.model.DbTable;
import leap.lang.*;
import leap.lang.logging.Log;

public class GenericDbComparator implements DbComparator,DbAware {
	
	protected Log log;
	
	protected GenericDb        db;
	protected GenericDbDialect dialect;
	
	protected GenericDbComparator(){

	}

	@Override
    public void setDb(Db db) {
		this.db      = (GenericDb)db;
		this.dialect = (GenericDbDialect)db.getDialect();
		this.log     = this.db.getLog(this.getClass());
    }

	@Override
    public SchemaChanges compareTables(DbTable[] sourceTables, DbTable[] targetTables) throws UnsupportedChangeException {
		Args.notNull(sourceTables,"source tables");
		Args.notNull(targetTables,"target tables");
		
		GenericSchemaChanges changes = db.createSchemaChanges();
		
		compareTables(changes, sourceTables, targetTables);
		
	    return changes;
    }
	
	@Override
    public SchemaChanges compareTables(DbTable[] source, DbSchema target) throws UnsupportedChangeException {
	    List<DbTable> targetTables = new ArrayList<DbTable>();
	    for(DbTable table : source) {
	        DbTable targetTable = target.findTable(table);
	        if(null != targetTable) {
	            targetTables.add(targetTable);
	        }
	    }
        return compareTables(source, targetTables.toArray(new DbTable[targetTables.size()]));
    }

    @Override
    public SchemaChanges compareSchema(DbSchema source, DbSchema target) throws UnsupportedChangeException {
		Args.notNull(source,"source schema");
		Args.notNull(target,"target schema");
		
		log.debug("Comparing source schema '{}' to target schema '{}'",source,target);
		
		GenericSchemaChanges changes = db.createSchemaChanges();

		compareTables(changes, Arrays2.sort(source.getTables()), Arrays2.sort(target.getTables()));
		compareSequences(changes, Arrays2.sort(source.getSequences()), Arrays2.sort(target.getSequences()));
		
	    return changes;
    }

	@Override
    public SchemaChanges compareTable(DbTable source, DbTable target) throws UnsupportedChangeException {
		Args.notNull(source,"source table");
		Args.notNull(target,"target table");
		
		Assert.isTrue(Strings.equalsIgnoreCase(source.getName(),target.getName()),
					  "Can't compare two table which name are not equals : {0} -> {1}",source.getName(),target.getName());
		
		GenericSchemaChanges changes = db.createSchemaChanges();
		
		compareTable(changes, source, target);
		
	    return changes;
    }
	
	protected void compareTables(GenericSchemaChanges changes,DbTable[] sourceTables,DbTable[] targetTables) {
		//check for remove
		for(DbTable targetTable : targetTables){
			if(null == findTable(sourceTables, targetTable.getName())){
				changes.add(new RemoveTableChange(targetTable));
			}
		}
		
		//check for add or modify
		for(DbTable sourceTable : sourceTables){
			DbTable targetTable = findTable(targetTables,sourceTable.getName());
			
			if(null == targetTable){
				changes.add(new AddTableChange(sourceTable));
			}else{
				compareTable(changes,sourceTable,targetTable);
			}
		}
	}
	
	protected void compareSequences(GenericSchemaChanges changes,DbSequence[] sourceSequences,DbSequence[] targetSequences){
		//check for remove
		for(DbSequence targetSequence : targetSequences){
			if(null == findSequence(sourceSequences, targetSequence.getName())){
				changes.add(new RemoveSequenceChange(targetSequence));
			}
		}
		
		//check for add or modify
		for(DbSequence sourceSequence : sourceSequences){
			DbSequence targetSequence = findSequence(targetSequences, sourceSequence.getName());
			
			if(null == targetSequence){
				changes.add(new AddSequenceChange(sourceSequence));
			}else{
				checkForSequenceDefinitionChange(changes, sourceSequence, targetSequence);
			}
		}
	}
	
	protected void compareTable(GenericSchemaChanges changes, DbTable source, DbTable target) {
		checkForPropertyChanges(changes, source, target);
		checkForPrimaryKeyChanges(changes, source, target);
		checkForColumnChanges(changes, source, target);
		checkForForeignKeyChanges(changes, source, target);
		checkForIndexChanges(changes, source, target);
	}
	
	protected void checkForPropertyChanges(GenericSchemaChanges changes, DbTable source, DbTable target){
		if(!Strings.equals(Strings.trimToNull(source.getComment()),Strings.trimToNull(target.getComment()))){
			log.debug("Comment changed in table '{}'",source.getName());
			changes.add(new TablePropertyChange(target, TablePropertyChange.COMMENT, target.getComment(), source.getComment()));
		}
	}
	
	protected void checkForPrimaryKeyChanges(GenericSchemaChanges changes, DbTable source, DbTable target){
		DbPrimaryKey sourcePrimaryKey = source.getPrimaryKey();
		DbPrimaryKey targetPrimaryKey = target.getPrimaryKey();
		
		//No primary key
		if(null == sourcePrimaryKey && null == targetPrimaryKey){
			return;
		}
		
		//add
		if(null != sourcePrimaryKey && null == targetPrimaryKey){
			log.debug("Primary key needs to be added to table '{}'",source.getName());
			changes.add(new AddPrimaryKeyChange(target, sourcePrimaryKey));
			return;
		}
		
		//remove
		if(null == sourcePrimaryKey && null != targetPrimaryKey){
			log.debug("Primary key needs to be removed from table '{}'",source.getName());
			changes.add(new RemovePrimaryKeyChange(target, targetPrimaryKey));
			return;
		}
		
		//definition change
		if(!Arrays2.equals(sourcePrimaryKey.getColumnNames(), targetPrimaryKey.getColumnNames(), true)){
			log.debug("Primary key's definition changed in table '{}'",source.getName());
			changes.add(new PrimaryKeyDefinitionChange(target, targetPrimaryKey, sourcePrimaryKey));
			return;
		}
	}
	
	protected void checkForColumnChanges(GenericSchemaChanges changes, DbTable sourceTable, DbTable targetTable){
		//check for remove
		for(DbColumn targetColumn : targetTable.getColumns()){
			DbColumn sourceColumn = sourceTable.findColumn(targetColumn.getName());
			
			if(null == sourceColumn){
				log.debug("Column '{}' needs to be removed from table '{}'",targetColumn.getName(),targetTable.getName());
				changes.add(new RemoveColumnChange(targetTable, targetColumn));
			}
		}
		
		//check for add or modify
		for(DbColumn sourceColumn : sourceTable.getColumns()){
			DbColumn targetColumn = targetTable.findColumn(sourceColumn.getName());
			
			if(null == targetColumn){
				log.debug("Column '{}' needs to be added to table '{}'",sourceColumn.getName(),sourceTable.getName());
				changes.add(new AddColumnChange(targetTable, sourceColumn));
			}else{
				checkForColumnDefinitionChanges(changes, sourceTable, sourceColumn, targetTable, targetColumn);
			}
		}
	}
	
	protected void checkForForeignKeyChanges(GenericSchemaChanges changes, DbTable sourceTable, DbTable targetTable) {
		//check for remove
		for(DbForeignKey targetForeignKey : targetTable.getForeignKeys()){
			DbForeignKey sourceForeignKey = sourceTable.findForeignKey(targetForeignKey.getName());
			
			if(null == sourceForeignKey){
				log.debug("Foreign key '{}' needs to be removed from table '{}'",targetForeignKey.getName(),targetTable.getName());
				changes.add(new RemoveForeignKeyChange(targetTable, targetForeignKey));
			}
		}
		
		//check for add or modify
		for(DbForeignKey sourceForeignKey : sourceTable.getForeignKeys()){
			DbForeignKey targetForeignKey = targetTable.findForeignKey(sourceForeignKey.getName());
			
			if(null == targetForeignKey){
				log.debug("Foreign key '{}' needs to be added to table '{}'",sourceForeignKey.getName(),sourceTable.getName());
				changes.add(new AddForeignKeyChange(targetTable, sourceForeignKey));
			}else{
				checkForForeignKeyDefinitionChange(changes, sourceTable, sourceForeignKey, targetTable, targetForeignKey);
			}
		}
	}
	
	protected void checkForIndexChanges(GenericSchemaChanges changes, DbTable sourceTable, DbTable targetTable) {
		//check for remove
		for(DbIndex targetIndex : targetTable.getIndexes()){
			DbIndex sourceIndex = sourceTable.findIndex(targetIndex.getName());
			
			if(null == sourceIndex){
				log.debug("Index '{}' needs to be removed from table '{}'",targetIndex.getName(),targetTable.getName());
				changes.add(new RemoveIndexChange(targetTable, targetIndex));
			}
		}
		
		//check for add or modify
		for(DbIndex sourceIndex : sourceTable.getIndexes()){
			DbIndex targetIndex = targetTable.findIndex(sourceIndex.getName());
			
			if(null == targetIndex){
				log.debug("Index '{}' needs to be added to table '{}'",sourceIndex.getName(),sourceTable.getName());
				changes.add(new AddIndexChange(targetTable, sourceIndex));
			}else{
				checkForIndexDefinitionChange(changes, sourceTable, sourceIndex, targetTable, targetIndex);
			}
		}
	}
	
	protected void checkForColumnDefinitionChanges(GenericSchemaChanges changes, DbTable sourceTable, DbColumn sourceColumn, DbTable targetTable, DbColumn targetColumn){
		List<ColumnPropertyChange> propertyChanges = new ArrayList<ColumnPropertyChange>();
		
		//check for type definition changed
		checkForColumnTypeDefinitionChange(propertyChanges, sourceTable, sourceColumn, targetTable, targetColumn);
		
		//check for default changed
		checkForColumnDefaultValueChange(propertyChanges, sourceTable, sourceColumn, targetTable, targetColumn);
		
		//check for nullable changed
		checkForColumnNullableChange(propertyChanges, sourceTable, sourceColumn, targetTable, targetColumn);
		
		//check for comment changed
		checkForColumnCommentChange(propertyChanges, sourceTable, sourceColumn, targetTable, targetColumn);
		
		//check for unique changed
		checkForColumnUniqueChange(propertyChanges, sourceTable, sourceColumn, targetTable, targetColumn);
		
		if(!propertyChanges.isEmpty()){
			changes.add(new ColumnDefinitionChange(targetTable, targetColumn, sourceColumn, propertyChanges));
		}
	}	
	
	protected void checkForColumnTypeDefinitionChange(List<ColumnPropertyChange> changes, DbTable sourceTable, DbColumn sourceColumn, DbTable targetTable, DbColumn targetColumn){
		String sourceTypeDef = dialect.getColumnTypeDefinition(sourceColumn);
		String targetTypeDef = dialect.getColumnTypeDefinition(targetColumn);
		
		if(!compareColumnTypeDefinition(sourceColumn, sourceTypeDef, targetColumn, targetTypeDef)){
			
			log.debug("Type definition changed in column '{}.{}', [{}] -> [{}]",
					  sourceTable.getName(),
					  sourceColumn.getName(),
					  targetTypeDef,
					  sourceTypeDef);
			
			String[] sourceTypeAndSize = splitTypeAndSize(sourceTypeDef);
			String[] targetTypeAndSize = splitTypeAndSize(targetTypeDef);
			
			//check for type changed
			if(!Strings.equals(sourceTypeAndSize[0],targetTypeAndSize[0])){
				changes.add(new ColumnPropertyChange(targetColumn, ColumnPropertyChange.TYPE, targetTypeAndSize[0], sourceTypeAndSize[0]).setTable(sourceTable));
			}
			
			//check for size changed
			if(!Strings.equals(sourceTypeAndSize[1],targetTypeAndSize[1])){
				changes.add(new ColumnPropertyChange(targetColumn, ColumnPropertyChange.SIZE, targetTypeAndSize[1], sourceTypeAndSize[1]).setTable(sourceTable));
			}
		}
	}
	
	/**
	 * Returns <code>true</code> if source type definition equals to target type definition.
	 */
	protected boolean compareColumnTypeDefinition(DbColumn sourceColumn, String sourceTypeDef, DbColumn targetColumn, String targetTypeDef){
		return Strings.equals(sourceTypeDef, targetTypeDef);
	}
	
	protected void checkForColumnDefaultValueChange(List<ColumnPropertyChange> changes, DbTable sourceTable, DbColumn sourceColumn, DbTable targetTable, DbColumn targetColumn){
		if(!Strings.equals(sourceColumn.getDefaultValue(), targetColumn.getDefaultValue())){
			
			log.debug("Default value changed in column '{}.{}', [{}] -> [{}]",
					  sourceTable.getName(),
					  sourceColumn.getName(),
					  targetColumn.getDefaultValue(),
					  sourceColumn.getDefaultValue());
			
			changes.add(new ColumnPropertyChange(targetColumn, ColumnPropertyChange.DEFAULT, targetColumn.getDefaultValue(), sourceColumn.getDefaultValue()).setTable(sourceTable));
		}
	}
	
	protected void checkForColumnNullableChange(List<ColumnPropertyChange> changes, DbTable sourceTable, DbColumn sourceColumn, DbTable targetTable, DbColumn targetColumn){
		if(sourceColumn.isNullable() != targetColumn.isNullable()){
			
			log.debug("Nullable changed in column '{}.{}',  [{}] -> [{}]",
					  sourceTable.getName(),
					  sourceColumn.getName(),
					  targetColumn.isNullable() ? "null" : "not null",
					  sourceColumn.isNullable() ? "null" : "not null");
			
			changes.add(new ColumnPropertyChange(targetColumn, ColumnPropertyChange.NULLABLE, targetColumn.isNullable(), sourceColumn.isNullable()).setTable(sourceTable));
		}
	}
	
	protected void checkForColumnCommentChange(List<ColumnPropertyChange> changes, DbTable sourceTable, DbColumn sourceColumn, DbTable targetTable, DbColumn targetColumn){
		if(!Strings.equals(sourceColumn.getComment(), targetColumn.getComment())){
			
			log.debug("Comment changed in column '{}.{}', [{}] -> [{}] ",
					  sourceTable.getName(),
					  sourceColumn.getName(),
					  targetColumn.getComment(),
					  sourceColumn.getComment());
			
			changes.add(new ColumnPropertyChange(targetColumn,ColumnPropertyChange.COMMENT, targetColumn.getComment(), sourceColumn.getComment()).setTable(sourceTable));
		}
	}
	
	protected void checkForColumnUniqueChange(List<ColumnPropertyChange> changes, DbTable sourceTable, DbColumn sourceColumn, DbTable targetTable, DbColumn targetColumn){
		if(sourceColumn.isUnique() != targetColumn.isUnique()){
			
			log.debug("Unique changed in column '{}.{}',  [{}] -> [{}]",
					  sourceTable.getName(),
					  sourceColumn.getName(),
					  targetColumn.isUnique() ? "unique" : "",
					  sourceColumn.isUnique() ? "unique" : "");
			
			changes.add(new ColumnPropertyChange(targetColumn, ColumnPropertyChange.UNIQUE, targetColumn.isUnique(), sourceColumn.isUnique()).setTable(sourceTable));
		}
	}
	
	protected void checkForForeignKeyDefinitionChange(GenericSchemaChanges changes,
													  DbTable sourceTable,DbForeignKey sourceForeignKey,
													  DbTable targetTable,DbForeignKey targetForeignKey){
		
		List<ForeignKeyPropertyChange> propertyChanges = new ArrayList<ForeignKeyPropertyChange>();
		
		//check for foreign table change (ignore catalog and schema )
		if(!sourceForeignKey.getForeignTable().getName().equalsIgnoreCase(targetForeignKey.getForeignTable().getName())){
			
			log.debug("Foreign table changed in foreign key '{}.{}', [{}] -> [{}]",
					  sourceTable.getName(),
					  sourceForeignKey.getName(),
					  sourceForeignKey.getForeignTable().getName(),
					  targetForeignKey.getForeignTable().getName());
			
			propertyChanges.add(new ForeignKeyPropertyChange(targetForeignKey, 
															 ForeignKeyPropertyChange.FOREIGN_TABLE, 
															 targetForeignKey.getForeignTable(), 
															 sourceForeignKey.getForeignTable()));		
		}

		//check for columns change
		if(sourceForeignKey.getColumns().length != targetForeignKey.getColumns().length){
			log.debug("Columns changed in foreign key '{}.{}', [{} columns] -> [{} columns]",
					  sourceTable.getName(),
					  sourceForeignKey.getName(),
					  sourceForeignKey.getColumns().length,
					  targetForeignKey.getColumns().length);			
			
			propertyChanges.add(new ForeignKeyPropertyChange(targetForeignKey, 
															 ForeignKeyPropertyChange.COLUMNS,
															 targetForeignKey.getColumns(), 
															 sourceForeignKey.getColumns()));
		}else{
			for(int i=0;i<sourceForeignKey.getColumns().length;i++){
				DbForeignKeyColumn sourceColumn = sourceForeignKey.getColumns()[i];
				DbForeignKeyColumn targetColumn = targetForeignKey.getColumns()[i];
				
				if(!sourceColumn.equalsIgnoreCase(targetColumn)){
					log.debug("Column changed in foreign key '{}.{}', [{}] -> [{}]",
							  sourceTable.getName(),
							  sourceForeignKey.getName(),
							  sourceColumn.toString(),
							  targetColumn.toString());					
					
					propertyChanges.add(new ForeignKeyPropertyChange(targetForeignKey, 
																     ForeignKeyPropertyChange.COLUMNS, 
																     targetForeignKey.getColumns(), 
																     sourceForeignKey.getColumns()));
					break;
				}
			}
		}
		
		//check for onUpdate change
		DbCascadeAction sourceOnUpdate = sourceForeignKey.getOnUpdate();
		DbCascadeAction targetOnUpdate = targetForeignKey.getOnUpdate();
		if(null == sourceOnUpdate){
			sourceOnUpdate = db.getDialect().getForeignKeyDefaultOnUpdate();
		}
		if(null == targetOnUpdate){
			targetOnUpdate = db.getDialect().getForeignKeyDefaultOnUpdate();
		}
		if(!Objects.equals(sourceOnUpdate,targetOnUpdate)){
			
			log.debug("OnUpdate changed in foreign key '{}.{}', [{}] -> [{}]",
					  sourceTable.getName(),
					  sourceForeignKey.getName(),
					  sourceOnUpdate,
					  targetOnUpdate);	
			
			propertyChanges.add(new ForeignKeyPropertyChange(targetForeignKey, 
															 ForeignKeyPropertyChange.ON_UPDATE,
															 targetForeignKey.getOnUpdate(), 
															 sourceForeignKey.getOnUpdate()));
		}
		
		//check for onDelete change
		DbCascadeAction sourceOnDelete = sourceForeignKey.getOnDelete();
		DbCascadeAction targetOnDelete = targetForeignKey.getOnDelete();
		if(null == sourceOnDelete){
			sourceOnDelete = db.getDialect().getForeignKeyDefaultOnDelete();
		}
		if(null == targetOnDelete){
			targetOnDelete = db.getDialect().getForeignKeyDefaultOnDelete();
		}
		if(!Objects.equals(sourceOnDelete,targetOnDelete)){
			log.debug("OnDelete changed in foreign key '{}.{}', [{}] -> [{}]",
					  sourceTable.getName(),
					  sourceForeignKey.getName(),
					  sourceOnDelete,
					  targetOnDelete);	
			
			propertyChanges.add(new ForeignKeyPropertyChange(targetForeignKey, 
															 ForeignKeyPropertyChange.ON_DELETE,
															 targetForeignKey.getOnDelete(), 
															 sourceForeignKey.getOnDelete()));
		}		
		
		if(!propertyChanges.isEmpty()){
			changes.add(new ForeignKeyDefinitionChange(targetTable, targetForeignKey, sourceForeignKey, propertyChanges));
		}
		
	}
	
	protected void checkForIndexDefinitionChange(GenericSchemaChanges changes,
											     DbTable sourceTable,DbIndex sourceIndex,
												 DbTable targetTable,DbIndex targetIndex){

		List<IndexPropertyChange> propertyChanges = New.arrayList();

		//check for unique change
		if(sourceIndex.isUnique() != targetIndex.isUnique()){
			propertyChanges.add(new IndexPropertyChange(targetIndex, 
														IndexPropertyChange.UNIQUE, 
														targetIndex.isUnique(), 
														sourceIndex.isUnique()));
			
		}
		
		if(sourceIndex.getColumnNames().length != targetIndex.getColumnNames().length){
			propertyChanges.add(new IndexPropertyChange(targetIndex, 
														IndexPropertyChange.COLUMNS, 
														targetIndex.getColumnNames(),
														sourceIndex.getColumnNames()));
		}else{
			for(int i=0;i<sourceIndex.getColumnNames().length;i++){
				String sourceColumnName = sourceIndex.getColumnNames()[i];
				String targetColumnName = targetIndex.getColumnNames()[i];
				
				if(!Strings.equalsIgnoreCase(sourceColumnName, targetColumnName)){
					propertyChanges.add(new IndexPropertyChange(targetIndex, 
																IndexPropertyChange.COLUMNS, 
																targetIndex.getColumnNames(),
																sourceIndex.getColumnNames()));	
					break;
				}
			}
		}
		
		if(!propertyChanges.isEmpty()){
			changes.add(new IndexDefinitionChange(targetTable, targetIndex, sourceIndex, propertyChanges));
		}
	}
	
	protected void checkForSequenceDefinitionChange(GenericSchemaChanges changes,DbSequence source,DbSequence target){
		List<SequencePropertyChange> propertyChanges = New.arrayList();
		
		if(null != source.getMinValue() && source.getMinValue() != target.getMinValue()){
			propertyChanges.add(new SequencePropertyChange(target, SequencePropertyChange.MIN_VALUE, target.getMinValue(), source.getMinValue()));
		}
		
		if(null != source.getMaxValue() && source.getMaxValue() != target.getMaxValue()){
			propertyChanges.add(new SequencePropertyChange(target, SequencePropertyChange.MAX_VALUE, target.getMaxValue(), source.getMaxValue()));
		}
		
		if(null != source.getIncrement() && source.getIncrement() != target.getIncrement()){
			propertyChanges.add(new SequencePropertyChange(target, SequencePropertyChange.INCREMENT, target.getIncrement(), source.getIncrement()));
		}
		
		if(null != source.getStart() && source.getStart() != target.getStart()){
			propertyChanges.add(new SequencePropertyChange(target, SequencePropertyChange.START, target.getStart(), source.getStart()));
		}
		
		if(null != source.getCache() && source.getCache() != target.getCache()){
			propertyChanges.add(new SequencePropertyChange(target, SequencePropertyChange.CACHE, target.getCache(), source.getCache()));
		}
		
		if(null != source.getCycle() && source.getCycle() != target.getCycle()){
			propertyChanges.add(new SequencePropertyChange(target, SequencePropertyChange.CYCLE, target.getCycle(), source.getCycle()));
		}
		
		if(!propertyChanges.isEmpty()){
			changes.add(new SequenceDefinitionChange(target, source, propertyChanges));
		}
	}	
	
	protected boolean isSizeChanged(DbColumn sourceColumn, DbColumn targetColumn) {
		if (sourceColumn.getLength() > 0 && targetColumn.getLength() > 0 && sourceColumn.getLength() != targetColumn.getLength()) {
			return true;
		}

		if (sourceColumn.getPrecision() > 0 && targetColumn.getPrecision() > 0 && sourceColumn.getPrecision() != targetColumn.getPrecision()) {
			return true;
		}

		if (sourceColumn.getScale() > 0 && targetColumn.getScale() > 0 && sourceColumn.getScale() != targetColumn.getScale()) {
			return true;
		}

		return false;
	}	
	
	protected String[] splitTypeAndSize(String typeDef){
		int index = typeDef.indexOf('(');
		
		if(index < 0){
			return new String[]{typeDef,""};
		}else{
			return new String[]{typeDef.substring(0,index),typeDef.substring(index+1,typeDef.length()-1)};
		}
	}
	
	protected DbTable findTable(DbTable[] tables,String name){
		for(int i=0;i<tables.length;i++){
			DbTable table = tables[i];
			if(Strings.equalsIgnoreCase(name, table.getName())){
				return table;
			}
		}
		return null;
	}
	
	protected DbSequence findSequence(DbSequence[] sequences,String name){
		for(int i=0;i<sequences.length;i++){
			DbSequence sequence = sequences[i];
			if(Strings.equalsIgnoreCase(name, sequence.getName())){
				return sequence;
			}
		}
		return null;
	}
}
