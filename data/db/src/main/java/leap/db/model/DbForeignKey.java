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

import leap.lang.Args;
import leap.lang.annotation.Nullable;
import leap.lang.json.JsonStringable;
import leap.lang.json.JsonWriter;

public class DbForeignKey extends DbNamedObject implements JsonStringable {

	protected final DbSchemaObjectName   foreignTable;
	protected final DbForeignKeyColumn[] columns;
	protected final DbCascadeAction      onUpdate;
	protected final DbCascadeAction      onDelete;
	
	public DbForeignKey(String name,DbSchemaObjectName foreignTable,DbForeignKeyColumn[] columns,DbCascadeAction onUpdate,DbCascadeAction onDelete) {
	    super(name);
	    
	    Args.notNull(foreignTable,"the foreign table name");
	    Args.notEmpty(columns,"the foreign key columns");
	    
	    this.foreignTable = foreignTable;
	    this.columns      = columns;
	    this.onUpdate     = onUpdate;
	    this.onDelete     = onDelete;
	}

	public DbSchemaObjectName getForeignTable() {
		return foreignTable;
	}

	public DbForeignKeyColumn[] getColumns() {
		return columns;
	}

	public @Nullable DbCascadeAction getOnUpdate() {
		return onUpdate;
	}

	public @Nullable DbCascadeAction getOnDelete() {
		return onDelete;
	}

	@Override
    public void toJson(JsonWriter writer) {
		writer.startObject();
		
		writer.property("name", name)
			  .property("foreignKeyName", foreignTable);
		
		if(null != onUpdate){
			writer.property("onUpdate", onUpdate.name());
		}
		
		if(null != onDelete){
			writer.property("onDelete", onDelete.name());
		}
		
		writer.propertyJsonable("columns",columns);
		
		writer.endObject();
	}
}