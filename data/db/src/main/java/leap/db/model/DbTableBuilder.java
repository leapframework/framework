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

import java.util.ArrayList;
import java.util.List;

public class DbTableBuilder implements Buildable<DbTable>,JsonParsable {
	
	protected String catalog;
	protected String schema;
	protected String name;
	protected String type = DbTableTypes.TABLE;
    protected boolean quoted;
	protected String comment;
	protected String primaryKeyName;

    protected List<String>              primaryKeyColumnNames = new ArrayList<String>();
    protected List<DbColumnBuilder>     columns               = new ArrayList<DbColumnBuilder>();
    protected List<DbForeignKeyBuilder> foreignKeys           = new ArrayList<DbForeignKeyBuilder>();
    protected List<DbIndexBuilder>      indexes               = new ArrayList<DbIndexBuilder>();

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
            addColumn(new DbColumnBuilder(column));
        }

        for(DbForeignKey fk : table.getForeignKeys()) {
            addForeignKey(new DbForeignKeyBuilder(fk));
        }

        for(DbIndex ix : table.getIndexes()) {
            addIndex(new DbIndexBuilder(ix));
        }
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

    public boolean isQuoted() {
        return quoted;
    }

    public void setQuoted(boolean quoted) {
        this.quoted = quoted;
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

        indexes.forEach(index -> {
            if(Strings.containsIgnoreCase(index.getName(), name)) {

                String newIndexName = Strings.replaceIgnoreCase(index.getName(), name, newName);
                index.setName(newIndexName);
            }

        });

        foreignKeys.forEach(fk -> {
            if(Strings.containsIgnoreCase(fk.getName(), name)) {
                String newFkName = Strings.replaceIgnoreCase(fk.getName(), name , newName);
                fk.setName(newFkName);
            }
        });

        this.name = newName;
        return this;
    }

    public List<DbColumnBuilder> getColumns() {
        return columns;
    }

    public DbTableBuilder addPrimaryKey(DbColumnBuilder column){
        addColumn(column);
        addPrimaryKeyColumnName(column.getName());
        column.setPrimaryKey(true);
        column.setNullable(false);
        return this;
    }

    public DbTableBuilder addColumn(DbColumnBuilder column) {
        return addColumn(column,-1);
    }

    public DbTableBuilder addColumn(DbColumn column) {
        return addColumn(new DbColumnBuilder(column));
    }

    public DbTableBuilder addColumn(DbColumnBuilder column,int index) {
        Args.notNull(column);

        if(index < 0){
            columns.add(column);
        }else{
            columns.add(index,column);
        }

        if(column.isPrimaryKey() && !primaryKeyColumnNames.contains(column.getName())){
            addPrimaryKeyColumnName(column.getName());
        }

        return this;
    }

    public List<DbForeignKeyBuilder> getForeignKeys() {
        return foreignKeys;
    }

    public DbTableBuilder addForeignKey(DbForeignKeyBuilder fk) {
        foreignKeys.add(fk);
        return this;
    }

    public DbTableBuilder addForeignKey(DbForeignKey fk) {
        return addForeignKey(new DbForeignKeyBuilder(fk));
    }

    public List<DbIndexBuilder> getIndexes() {
        return indexes;
    }

    public DbTableBuilder addIndex(DbIndexBuilder ix) {
        indexes.add(ix);
        return this;
    }

    public DbTableBuilder addIndex(DbIndex ix) {
        return addIndex(new DbIndexBuilder(ix));
    }

    public List<String> getPrimaryKeyColumnNames() {
        return primaryKeyColumnNames;
    }

    public DbTableBuilder addPrimaryKeyColumnName(String columnName){
        return addPrimaryKeyColumnName(columnName, -1);
    }

    public DbTableBuilder addPrimaryKeyColumnName(String columnName, int index){
        Args.notEmpty(columnName);
        Assert.isTrue(findColumn(columnName) != null);

        if(primaryKeyColumnNames.contains(columnName)) {
            if(index == -1) {
                return this;
            }
            throw new IllegalStateException("Duplicated primary key '" + columnName + "'");
        }

        if(index < 0){
            primaryKeyColumnNames.add(columnName);
        }else{
            primaryKeyColumnNames.add(index, columnName);
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
    public void parseJson(JsonValue value) {
        JsonObject o = value.asJsonObject();

        this.catalog = o.getString("catalog");
        this.schema  = o.getString("schema");
        this.name    = o.getString("name");
        this.type    = o.getString("type",DbTableTypes.TABLE);
        this.quoted  = o.getBoolean("quoted", false);
        this.comment = o.getString("comment");
        this.primaryKeyName = o.getString("primaryKeyName");

        JsonArray columns = o.getArray("columns");
        if(null != columns){
            for(JsonValue v : columns){
                DbColumnBuilder cb = new DbColumnBuilder();
                cb.parseJson(v);
                addColumn(cb);
            }
        }

        JsonArray fks = o.getArray("foreignKeys");
        if(null != fks){
            for(JsonValue v : fks){
                DbForeignKeyBuilder fb = new DbForeignKeyBuilder();
                fb.parseJson(v);
                addForeignKey(fb);
            }
        }

        JsonArray indexes = o.getArray("indexes");
        if(null != indexes){
            for(JsonValue v : indexes){
                DbIndexBuilder ib = new DbIndexBuilder();
                ib.parseJson(v);
                addIndex(ib);
            }
        }
    }

    @Override
    public DbTable build() {
        if(!primaryKeyColumnNames.isEmpty() && Strings.isEmpty(primaryKeyName)){
            primaryKeyName = "PK_" + name;
        }

        return new DbTable(catalog, schema, name, type, quoted, comment,
                primaryKeyName,
                primaryKeyColumnNames.toArray(new String[primaryKeyColumnNames.size()]),
                Builders.buildArray(columns, new DbColumn[columns.size()]),
                Builders.buildArray(foreignKeys,new DbForeignKey[foreignKeys.size()]),
                Builders.buildArray(indexes, new DbIndex[indexes.size()]));
    }
}
