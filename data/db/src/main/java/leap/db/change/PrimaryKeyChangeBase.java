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

import leap.db.model.DbPrimaryKey;
import leap.db.model.DbTable;
import leap.lang.Args;

public abstract class PrimaryKeyChangeBase extends SchemaChangeBase implements PrimaryKeyChange {
	
	protected final DbTable      table;
	protected final DbPrimaryKey oldPrimaryKey;
	protected final DbPrimaryKey newPrimaryKey;
	
	protected PrimaryKeyChangeBase(DbTable table,DbPrimaryKey oldPrimaryKey,DbPrimaryKey newPrimaryKey){
		Args.notNull(table,"table");
		this.table         = table;
		this.oldPrimaryKey = oldPrimaryKey;
		this.newPrimaryKey = newPrimaryKey;
	}
	
	@Override
    public SchemaObjectType getObjectType() {
	    return SchemaObjectType.PRIMARY_KEY;
    }

	public DbTable getTable() {
		return table;
	}

	public DbPrimaryKey getOldPrimaryKey() {
		return oldPrimaryKey;
	}

	public DbPrimaryKey getNewPrimaryKey() {
		return newPrimaryKey;
	}
}
