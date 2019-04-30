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
package leap.db.change;

import leap.db.model.DbColumn;
import leap.db.model.DbTable;

public class ColumnPropertyChange extends PropertyChange<DbColumn> {
	
	public static final String TYPE      = "type";
	public static final String SIZE      = "size";
	public static final String NULLABLE  = "nullable";
	public static final String UNIQUE    = "unique";
	public static final String DEFAULT   = "default";
	public static final String COMMENT   = "comment";

	private DbTable table;
	
	public ColumnPropertyChange(DbColumn object, String property, Object oldValue, Object newValue) {
	    super(object, property, oldValue, newValue);
    }

	@Override
    public SchemaObjectType getObjectType() {
	    return SchemaObjectType.COLUMN;
    }
	
	public boolean isUnique(){
		return UNIQUE.equalsIgnoreCase(property);
	}
	
	public boolean isNullable() {
		return NULLABLE.equalsIgnoreCase(property);
	}
	
	public boolean isType(){
		return TYPE.equalsIgnoreCase(property);
	}
	
	public boolean isSize() {
		return SIZE.equalsIgnoreCase(property);
	}
	
	public boolean isDefault() {
		return DEFAULT.equalsIgnoreCase(property);
	}
	
	public boolean isComment() {
		return COMMENT.equalsIgnoreCase(property);
	}

	public DbTable getTable() {
		return table;
	}

	public ColumnPropertyChange setTable(DbTable table) {
		this.table = table;
		return this;
	}
}