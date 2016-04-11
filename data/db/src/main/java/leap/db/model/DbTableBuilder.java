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
package leap.db.model;

import leap.lang.*;
import leap.lang.json.JsonArray;
import leap.lang.json.JsonObject;
import leap.lang.json.JsonParsable;
import leap.lang.json.JsonValue;
import leap.lang.value.SimpleEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DbTableBuilder implements Buildable<DbTable>,JsonParsable {
	
	protected String catalog;
	protected String schema;
	protected String name;
	protected String type = DbTableTypes.TABLE;
	protected String comment;
	protected String primaryKeyName;
	
	protected List<String>       primaryKeyColumnNames = new ArrayList<>();
	protected List<DbColumn>     columns               = new ArrayList<>();
	protected List<DbForeignKey> foreignKeys           = new ArrayList<>();
	protected List<DbIndex>      indexes               = new ArrayList<>();
	
	public DbTableBuilder(){
		
	}

	public DbTableBuilder(String name){
		this.name = name;
	}
	
	public DbTableBuilder(String catalog,String schema,String name){
		this.catalog = catalog;
		this.schema  = schema;
		this.name    = name;
	}

    public DbTableBuilder(DbTable table) {
        this.catalog = table.getCatalog();
        this.schema  = table.getSchema();
        this.name    = table.getName();
        this.type    = table.getType();
        this.comment = table.getComment();
        this.primaryKeyName = table.getPrimaryKeyName();

        for(DbColumn column : table.getColumns()) {
            addColumn(column);
        }

        Collections2.addAll(foreignKeys, table.getForeignKeys());
        Collections2.addAll(indexes, table.getIndexes());
    }
	
	public DbSchemaObjectName getTableName(){
		return new DbSchemaObjectName(catalog, schema, name);
	}
	
	public String getCatalog() {
		return catalog;
	}

	public DbTableBuilder setCatalog(String catalog) {
		this.catalog = catalog;
		return this;
	}
	
	public String getSchema() {
		return schema;
	}

	public DbTableBuilder setSchema(String schema){
		this.schema = schema;
		return this;
	}
	
	public String getName() {
		return name;
	}

	public DbTableBuilder setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getType() {
		return type;
	}

	public DbTableBuilder setType(String type) {
		Args.notEmpty(type);
		this.type = type;
		return this;
	}

	public String getComment() {
		return comment;
	}

	public DbTableBuilder setComment(String comment) {
		this.comment = comment;
		return this;
	}
	
	public String getPrimaryKeyName() {
		return primaryKeyName;
	}

	public DbTableBuilder setPrimaryKeyName(String primaryKeyName) {
		this.primaryKeyName = primaryKeyName;
		return this;
	}

    public DbTableBuilder updateTableName(String newName) {

        List<Map.Entry<DbIndex,DbIndex>> updatedIndexes = new ArrayList<>();
        for(DbIndex index : indexes) {

            if(Strings.containsIgnoreCase(index.getName(), name)) {

                String newIndexName = Strings.replaceIgnoreCase(index.getName(), name, newName);

                DbIndex newIndex = new DbIndexBuilder(index).setName(newIndexName).build();

                updatedIndexes.add(new SimpleEntry<>(index, newIndex));
            }

        }
        for(Map.Entry<DbIndex,DbIndex> entry : updatedIndexes) {
            indexes.set(indexes.indexOf(entry.getKey()), entry.getValue());
        }

        List<Map.Entry<DbForeignKey,DbForeignKey>> updatedFks = new ArrayList<>();
        for(DbForeignKey fk : foreignKeys) {
            if(Strings.containsIgnoreCase(fk.getName(), name)) {

                String newFkName = Strings.replaceIgnoreCase(fk.getName(), name , newName);

                DbForeignKey newFk = new DbForeignKeyBuilder(fk).setName(newFkName).build();

                updatedFks.add(new SimpleEntry<>(fk, newFk));
            }
        }

        for(Map.Entry<DbForeignKey,DbForeignKey> entry : updatedFks) {
            foreignKeys.set(foreignKeys.indexOf(entry.getKey()), entry.getValue());
        }

        this.name = newName;
        return this;
    }

	public List<DbColumn> getColumns() {
		return columns;
	}
	
	public DbTableBuilder addColumn(DbColumn column) {
		return addColumn(column,-1);
	}
	
	public DbTableBuilder addColumn(DbColumn column,int index) {
		Args.notNull(column);
		
		if(index < 0){
			columns.add(column);
		}else{
			columns.add(index,column);
		}
		
		if(column.isPrimaryKey()){
			addPrimaryKeyColumnName(column.getName());
		}
		
		return this;
	}
	
	public List<DbForeignKey> getForeignKeys() {
		return foreignKeys;
	}

	public DbTableBuilder addForeignKey(DbForeignKey fk) {
		foreignKeys.add(fk);
		return this;
	}
	
	public List<DbIndex> getIndexes() {
		return indexes;
	}

	public DbTableBuilder addIndex(DbIndex ix) {
		indexes.add(ix);
		return this;
	}
	
	public List<String> getPrimaryKeyColumnNames() {
		return primaryKeyColumnNames;
	}
	
	/*
	public DbTableBuilder updatePrimaryKey(DbColumn theAddedColumn){
		Args.checkNotNull(theAddedColumn);
		Assert.isValid(primaryKeyColumnNames.isEmpty(),"primary key columns must be empty");
		
		int index = columns.indexOf(theAddedColumn);
		if(index < 0){
			throw new IllegalStateException("column '" + theAddedColumn.getName() + "' not exists");
		}
		
		columns.set(index, new DbColumnBuilder(theAddedColumn).setPrimaryKey(true).build());
		primaryKeyColumnNames.add(theAddedColumn.getName());
		
		return this;
	}
	*/

	/**
	 * returns the {@link DbColumn} object matched the given name (ignore case) in this table.
	 * 
	 * <p>
	 * returns <code>null</code> if no column match the given name.
	 */
	public DbColumn findColumn(String name){
		for(int i=0;i<columns.size();i++){
			DbColumn column = columns.get(i);
			if(Strings.equalsIgnoreCase(column.getName(), name)){
				return column;
			}
		}
		return null;
	}
	
	/**
	 * returns the {@link DbForeignKey} object matched the given name (ignore case) in this table.
	 * 
	 * <p>
	 * returns <code>null</code> if no foreign key match the given name.
	 */
	public DbForeignKey findForeignKey(String name){
		for(int i=0;i<foreignKeys.size();i++){
			DbForeignKey fk = foreignKeys.get(i);
			if(Strings.equalsIgnoreCase(fk.getName(), name)){
				return fk;
			}
		}
		return null;
	}
	
	/**
	 * returns the {@link DbIndex} object matched the given name (ignore case) in this table.
	 * 
	 * <p>
	 * returns <code>null</code> if no index match the given name.
	 */
	public DbIndex findIndex(String name){
		for(int i=0;i<indexes.size();i++){
			DbIndex ix = indexes.get(i);
			if(Strings.equalsIgnoreCase(ix.getName(), name)){
				return ix;
			}
		}
		return null;
	}
	
	@Override
    public DbTable build() {
		if(!primaryKeyColumnNames.isEmpty() && Strings.isEmpty(primaryKeyName)){
			primaryKeyName = "PK_" + name;
		}
		
	    return new DbTable(catalog, schema, name, type, comment, 
	    				   primaryKeyName,
	    				   primaryKeyColumnNames.toArray(new String[primaryKeyColumnNames.size()]),
	    				   columns.toArray(new DbColumn[columns.size()]), 
	    				   foreignKeys.toArray(new DbForeignKey[foreignKeys.size()]),
	    				   indexes.toArray(new DbIndex[indexes.size()]));
    }
	
	@Override
    public void parseJson(JsonValue value) {
		JsonObject o = value.asJsonObject();
		
		this.catalog = o.getString("catalog");
		this.schema  = o.getString("schema");
		this.name    = o.getString("name");
		this.type    = o.getString("type",DbTableTypes.TABLE);
		this.comment = o.getString("comment");
		this.primaryKeyName = o.getString("primaryKeyName");
		
		JsonArray columns = o.getArray("columns");
		if(null != columns){
			for(JsonValue v : columns){
				DbColumnBuilder cb = new DbColumnBuilder();
				cb.parseJson(v);
				addColumn(cb.build());
			}
		}
		
		JsonArray fks = o.getArray("foreignKeys");
		if(null != fks){
			for(JsonValue v : fks){
				DbForeignKeyBuilder fb = new DbForeignKeyBuilder();
				fb.parseJson(v);
				addForeignKey(fb.build());
			}
		}
		
		JsonArray indexes = o.getArray("indexes");
		if(null != indexes){
			for(JsonValue v : indexes){
				DbIndexBuilder ib = new DbIndexBuilder();
				ib.parseJson(v);
				addIndex(ib.build());
			}
		}
    }

	protected DbTableBuilder addPrimaryKeyColumnName(String columName){
		return addPrimaryKeyColumnName(columName, -1);
	}
	
	protected DbTableBuilder addPrimaryKeyColumnName(String columName,int index){
		Args.notEmpty(columName);
		Assert.isTrue(findColumn(columName) != null);
		
		if(index < 0){
			primaryKeyColumnNames.add(columName);
		}else{
			primaryKeyColumnNames.add(index,columName);	
		}
		
		return this;
	}
	
	
}
