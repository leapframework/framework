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

import leap.db.model.DbForeignKey;
import leap.lang.Strings;

public class ForeignKeyPropertyChange extends PropertyChange<DbForeignKey> {
	
	public static final String FOREIGN_TABLE = "foreignTable";
	public static final String COLUMNS       = "columns";
	public static final String ON_UPDATE     = "onUpdate";
	public static final String ON_DELETE     = "onDelete";

	public ForeignKeyPropertyChange(DbForeignKey object, String property, Object oldValue, Object newValue) {
		super(object, property, oldValue, newValue);
	}

	@Override
	public SchemaObjectType getObjectType() {
		return SchemaObjectType.FOREIGN_KEY;
	}
	
	public boolean isForeignTableChanged(){
		return Strings.equals(this.getProperty(), FOREIGN_TABLE);
	}
	
	public boolean isColumnsChanged(){
		return Strings.equals(this.getProperty(), COLUMNS);
	}
	
	public boolean isOnUpdateChanged(){
		return Strings.equals(this.getProperty(), ON_UPDATE);
	}
	
	public boolean isOnDeleteChanged(){
		return Strings.equals(this.getProperty(), ON_DELETE);
	}
}
