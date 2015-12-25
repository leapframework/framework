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

import leap.lang.Buildable;
import leap.lang.Strings;
import leap.lang.json.JsonArray;
import leap.lang.json.JsonObject;
import leap.lang.json.JsonParsable;
import leap.lang.json.JsonValue;

public class DbForeignKeyBuilder implements Buildable<DbForeignKey>,JsonParsable {
	
	protected String                   name;
	protected DbSchemaObjectName	   foreignTable;
	protected List<DbForeignKeyColumn> columns = new ArrayList<DbForeignKeyColumn>();
	protected DbCascadeAction          onUpdate;
	protected DbCascadeAction          onDelete;
	
	public String getName() {
		return name;
	}

	public DbForeignKeyBuilder setName(String name) {
		this.name = name;
		return this;
	}
	
	public DbSchemaObjectName getForeignTable() {
		return foreignTable;
	}

	public DbForeignKeyBuilder setForeignTable(DbSchemaObjectName foreignTable) {
		this.foreignTable = foreignTable;
		return this;
	}

	public List<DbForeignKeyColumn> getColumns() {
		return columns;
	}
	
	public DbForeignKeyBuilder addColumn(DbForeignKeyColumn column){
		return addColumn(column,-1);
	}
	
	public DbForeignKeyBuilder addColumn(DbForeignKeyColumn column,int index){
		if(index < 0){
			columns.add(column);
		}else{
			columns.add(index,column);
		}
		return this;
	}
	
	public DbCascadeAction getOnUpdate() {
		return onUpdate;
	}

	public DbForeignKeyBuilder setOnUpdate(DbCascadeAction onUpdate) {
		this.onUpdate = onUpdate;
		return this;
	}

	public DbCascadeAction getOnDelete() {
		return onDelete;
	}

	public DbForeignKeyBuilder setOnDelete(DbCascadeAction onDelete) {
		this.onDelete = onDelete;
		return this;
	}

	@Override
    public DbForeignKey build() {
	    return new DbForeignKey(name, foreignTable, columns.toArray(new DbForeignKeyColumn[columns.size()]), onUpdate, onDelete);
    }

	@Override
    public void parseJson(JsonValue value) {
		JsonObject o = value.asJsonObject();
		
		this.name = o.getString("name");
		
		JsonObject foreignTable = o.getObject("foreignTable");
		if(null != foreignTable){
			DbSchemaObjectNameBuilder nb = new DbSchemaObjectNameBuilder();
			nb.parseJson(foreignTable);
		}
		
		JsonArray columns = o.getArray("columns");
		if(null != columns){
			for(JsonValue v : columns){
				JsonObject column = v.asJsonObject();
				addColumn(new DbForeignKeyColumn(column.getString("localColumnName"), column.getString("foreignColumnName")));
			}
		}
		
		String onUpdate = o.getString("onUpdate");
		if(!Strings.isEmpty(onUpdate)){
			this.onUpdate = DbCascadeAction.valueOf(onUpdate);
		}
		
		String onDelete = o.getString("onDelete");
		if(!Strings.isEmpty(onDelete)){
			this.onDelete = DbCascadeAction.valueOf(onDelete);
		}
    }
}
