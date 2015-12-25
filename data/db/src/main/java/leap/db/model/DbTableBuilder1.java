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

import java.util.ArrayList;
import java.util.List;

import leap.lang.Args;
import leap.lang.Assert;
import leap.lang.Buildable;
import leap.lang.Builders;
import leap.lang.Strings;

public class DbTableBuilder1 implements Buildable<DbTable> {
	
	protected String catalog;
	protected String schema;
	protected String name;
	protected String type = DbTableTypes.TABLE;
	protected String comment;
	protected String primaryKeyName;
	
	protected List<String>              primaryKeyColumnNames = new ArrayList<String>();
	protected List<DbColumnBuilder>     columns               = new ArrayList<DbColumnBuilder>();
	protected List<DbForeignKeyBuilder> foreignKeys           = new ArrayList<DbForeignKeyBuilder>();
	protected List<DbIndexBuilder>      indexes               = new ArrayList<DbIndexBuilder>();
	
	public DbTableBuilder1(){
		
	}

	public DbTableBuilder1(String name){
		this.name = name;
	}
	
	public DbTableBuilder1(String catalog,String schema,String name){
		this.catalog    = catalog;
		this.schema = schema;
		this.name       = name;
	}
	
	public DbSchemaObjectName getTableName(){
		return new DbSchemaObjectName(catalog, schema, name);
	}
	
	public String getCatalog() {
		return catalog;
	}

	public DbTableBuilder1 setCatalog(String catalog) {
		this.catalog = catalog;
		return this;
	}
	
	public String getSchema() {
		return schema;
	}

	public DbTableBuilder1 setSchema(String schemaName){
		this.schema = schemaName;
		return this;
	}
	
	public String getName() {
		return name;
	}

	public DbTableBuilder1 setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getType() {
		return type;
	}

	public DbTableBuilder1 setType(String type) {
		Args.notEmpty(type);
		this.type = type;
		return this;
	}

	public String getComment() {
		return comment;
	}

	public DbTableBuilder1 setComment(String comment) {
		this.comment = comment;
		return this;
	}
	
	public String getPrimaryKeyName() {
		return primaryKeyName;
	}

	public DbTableBuilder1 setPrimaryKeyName(String primaryKeyName) {
		this.primaryKeyName = primaryKeyName;
		return this;
	}

	public List<DbColumnBuilder> getColumns() {
		return columns;
	}
	
	public DbTableBuilder1 addPrimaryKey(DbColumnBuilder column){
		addColumn(column).addPrimaryKeyColumnName(column.getName());
		column.setPrimaryKey(true);
		column.setNullable(false);
		return this;
	}
	
	public DbTableBuilder1 addColumn(DbColumnBuilder column) {
		return addColumn(column,-1);
	}
	
	public DbTableBuilder1 addColumn(DbColumnBuilder column,int index) {
		Args.notNull(column);
		
		if(index < 0){
			columns.add(column);
		}else{
			columns.add(index,column);
		}
		return this;
	}
	
	public List<DbForeignKeyBuilder> getForeignKeys() {
		return foreignKeys;
	}

	public DbTableBuilder1 addForeignKey(DbForeignKeyBuilder fk) {
		foreignKeys.add(fk);
		return this;
	}
	
	public List<DbIndexBuilder> getIndexes() {
		return indexes;
	}

	public DbTableBuilder1 addIndex(DbIndexBuilder ix) {
		indexes.add(ix);
		return this;
	}
	
	public List<String> getPrimaryKeyColumnNames() {
		return primaryKeyColumnNames;
	}
	
	public DbTableBuilder1 addPrimaryKeyColumnName(String columName){
		return addPrimaryKeyColumnName(columName, -1);
	}
	
	public DbTableBuilder1 addPrimaryKeyColumnName(String columName,int index){
		Args.notEmpty(columName);
		Assert.isTrue(findColumn(columName) != null);
		
		if(index < 0){
			primaryKeyColumnNames.add(columName);
		}else{
			primaryKeyColumnNames.add(index,columName);	
		}
		
		return this;
	}

	/**
	 * returns the {@link DbColumnBuilder} object matched the given name (ignore case) in this table.
	 * 
	 * <p>
	 * returns <code>null</code> if no column match the given name.
	 */
	public DbColumnBuilder findColumn(String name){
		for(int i=0;i<columns.size();i++){
			DbColumnBuilder column = columns.get(i);
			if(Strings.equalsIgnoreCase(column.getName(), name)){
				return column;
			}
		}
		return null;
	}
	
	/**
	 * returns the {@link DbForeignKeyBuilder} object matched the given name (ignore case) in this table.
	 * 
	 * <p>
	 * returns <code>null</code> if no foreign key match the given name.
	 */
	public DbForeignKeyBuilder findForeignKey(String name){
		for(int i=0;i<foreignKeys.size();i++){
			DbForeignKeyBuilder fk = foreignKeys.get(i);
			if(Strings.equalsIgnoreCase(fk.getName(), name)){
				return fk;
			}
		}
		return null;
	}
	
	/**
	 * returns the {@link DbIndexBuilder} object matched the given name (ignore case) in this table.
	 * 
	 * <p>
	 * returns <code>null</code> if no index match the given name.
	 */
	public DbIndexBuilder findIndex(String name){
		for(int i=0;i<indexes.size();i++){
			DbIndexBuilder ix = indexes.get(i);
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
	    				   Builders.buildArray(columns, new DbColumn[columns.size()]), 
	    				   Builders.buildArray(foreignKeys,new DbForeignKey[foreignKeys.size()]),
	    				   Builders.buildArray(indexes, new DbIndex[indexes.size()]));
    }
}
