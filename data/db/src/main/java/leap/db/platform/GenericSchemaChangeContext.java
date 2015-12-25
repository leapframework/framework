/*
 * Copyright 2014 the original author or authors.
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
package leap.db.platform;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import leap.db.change.SchemaChangeContext;
import leap.db.model.DbSchemaObjectName;

public class GenericSchemaChangeContext implements SchemaChangeContext {

	private final Map<String, Boolean> emptyCache = new HashMap<String, Boolean>();
	
	protected final GenericDb  db;
	protected final Connection connection;
	
	public GenericSchemaChangeContext(GenericDb db) {
		this.db 		= db;
		this.connection = null;
	}
	
	public GenericSchemaChangeContext(GenericDb db, Connection connection) {
		this.db 		= db;
		this.connection = connection;
	}

	@Override
	public boolean isEmptyTable(DbSchemaObjectName tableName) {
		Boolean b = emptyCache.get(tableName.getQualifiedName());
		
		if(null == b){
			
			if(null == connection) {
				b = db.queryForScalar(Long.class, db.getDialect().getCountTableSql(tableName)) == 0;	
			}else{
				b = db.queryForScalar(Long.class, connection, db.getDialect().getCountTableSql(tableName)) == 0;
			}
			
			emptyCache.put(tableName.getQualifiedName(), b);
		}
		
		return b;
	}

}
