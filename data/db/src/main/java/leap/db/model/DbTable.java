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

import java.sql.DatabaseMetaData;

import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.Assert;
import leap.lang.Strings;
import leap.lang.json.JsonStringable;
import leap.lang.json.JsonWriter;

public class DbTable extends DbSchemaObjectName implements JsonStringable {
	
	public static DbTable fromJson(String json) {
		DbTableBuilder b = new DbTableBuilder();
		b.parseJson(json);
		return b.build();
	}
	
	protected final String		   type;
	protected final String		   comment;
	protected final DbPrimaryKey   primaryKey;
	protected final DbColumn[]     primaryKeyColumns;
	protected final DbColumn[]     columns;
	protected final DbForeignKey[] foreignKeys;
	protected final DbIndex[]      indexes;

	public DbTable(String         catalog, 
				   String         schema,
				   String         name, 
				   String         type, 
				   String         comment, 
				   String		  primaryKeyName,
				   String[]       primaryKeyColumnNames, 
				   DbColumn[]     columns,
				   DbForeignKey[] foreignKeys,
				   DbIndex[]      indexes) {
		
	    super(catalog, schema, name);
	    
	    Args.notEmpty(type,"table type");
	    Args.notEmpty(columns,"columns' of table '" + name + "");
	    
	    Args.assertFalse(!Arrays2.isEmpty(primaryKeyColumnNames) && Strings.isEmpty(primaryKeyName),
	    				  "primaryKeyName must not be empty if primary key exists");
	    
	    this.type                  = type;
	    this.comment               = comment;
	    this.columns               = columns;
	    this.foreignKeys           = null == foreignKeys ? new DbForeignKey[]{} : foreignKeys;
	    this.indexes               = null == indexes     ? new DbIndex[]{}      : indexes;
	    this.primaryKey			   = Arrays2.isEmpty(primaryKeyColumnNames) ? null : new DbPrimaryKey(primaryKeyName, primaryKeyColumnNames);
	    this.primaryKeyColumns     = fromNames(primaryKeyColumnNames);
    }

	/**
	 * @see DatabaseMetaData#getTableTypes()
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * returns the comment of this table.
	 */
	public String getComment() {
		return comment;
	}
	
	/**
	 * returns truth if this table's type is view.
	 * 
	 * @see DbTableTypes#VIEW
	 * @see DatabaseMetaData#getTableTypes()
	 */
	public boolean isView(){
		return DbTableTypes.VIEW.equals(this.type);
	}
	
	/**
	 * returns true if this table has primary key(s).
	 */
	public boolean hasPrimaryKey(){
		return primaryKeyColumns.length > 0;
	}
	
	public DbPrimaryKey getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * Returns the constraint name of the primary key if exists.
	 * 
	 * <p>
	 * Returns <code>null</code> if no primary key in this table.
	 */
	public String getPrimaryKeyName() {
		return primaryKey == null ? null : primaryKey.getName();
	}

	/**
	 * returns the primary key {@link DbColumn} objects (order by physical position) of this table.
	 * 
	 * <p>
	 * 
	 * the array returned must not be null but may be empty.
	 */
	public DbColumn[] getPrimaryKeyColumns() {
		return primaryKeyColumns;
	}
	
	/**
	 * returns the primary key column names (order by physical position) of this table.
	 * 
	 * <p>
	 * 
	 * the array returned must not be null but may be empty.
	 */
	public String[] getPrimaryKeyColumnNames() {
		return null == primaryKey ? Arrays2.EMPTY_STRING_ARRAY : primaryKey.getColumnNames();
	}

	/**
	 * returns the {@link DbColumn} objects of this table.
	 * 
	 * <p>
	 * the array returned must not be null or empty. 
	 */
	public DbColumn[] getColumns() {
		return columns;
	}
	
	/**
	 * returns the {@link DbColumn} stored in the given position.
	 * 
	 * <p>
	 * index starts from 0.
	 * 
	 * @throws IndexOutOfBoundsException if index out of bounds.
	 */
	public DbColumn getColumn(int index) throws IndexOutOfBoundsException{
		return columns[index];
	}

	/**
	 * returns the {@link DbForeignKey} objects of this table.
	 * 
	 * <p>
	 * the array returned must be not null but may be empty.
	 */
	public DbForeignKey[] getForeignKeys() {
		return foreignKeys;
	}
	
	public boolean hasForeignKeys(){
		return foreignKeys.length > 0;
	}

	/**
	 * returns the {@link DbIndex} objects of this table.
	 * 
	 * <p>
	 * 
	 * the array returned must be not null but may be empty.
	 */
	public DbIndex[] getIndexes() {
		return indexes;
	}
	
	public boolean hasIndexes(){
		return indexes.length > 0;
	}
	
	/**
	 * returns the {@link DbColumn} object matched the given name (ignore case) in this table.
	 * 
	 * <p>
	 * returns <code>null</code> if no column match the given name.
	 */
	public DbColumn findColumn(String name){
		for(int i=0;i<columns.length;i++){
			DbColumn column = columns[i];
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
		for(int i=0;i<foreignKeys.length;i++){
			DbForeignKey fk = foreignKeys[i];
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
		for(int i=0;i<indexes.length;i++){
			DbIndex ix = indexes[i];
			if(Strings.equalsIgnoreCase(ix.getName(), name)){
				return ix;
			}
		}
		return null;
	}
	
	private DbColumn[] fromNames(String[] columnNames){
		DbColumn[] columns = new DbColumn[columnNames.length];
		
		for(int i=0;i<columns.length;i++){
			columns[i] = findColumn(columnNames[i]);
			Assert.isTrue(columns[i] != null);
		}
		
		return columns;
	}
	
    public String toString() {
        StringBuffer result = new StringBuffer();

        result.append("Table {name=");
        result.append(getName());
        result.append("; ");
        result.append(columns.length);
        result.append(" columns}");

        return result.toString();
    }

	@Override
    public void toJson(JsonWriter writer) {
		writer.startObject();
		
		writeName(writer);
		
		writer.property("type", type)
			  .propertyOptional("comment", comment)
			  .propertyOptional("primaryKeyName", primaryKey == null ? null : primaryKey.getName());
		
		writer.propertyJsonable("columns",columns);

		if(foreignKeys.length > 0){
			writer.propertyJsonable("foreignKeys",foreignKeys);
		}
		
		if(indexes.length > 0){
			writer.propertyJsonable("indexes",indexes);
		}
		
		writer.endObject();
    }

}