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

import leap.db.model.DbTable;


public abstract class TableChangeBase extends SchemaChangeBase implements TableChange {

	protected final DbTable oldTable;
	protected final DbTable newTable;
	
	protected TableChangeBase(DbTable oldTable,DbTable newTable){
		this.oldTable = oldTable;
		this.newTable = newTable;
	}
	
	@Override
    public SchemaObjectType getObjectType() {
	    return SchemaObjectType.TABLE;
    }

	@Override
    public DbTable getOldTable() {
	    return oldTable;
    }

	@Override
    public DbTable getNewTable() {
	    return newTable;
    }

}