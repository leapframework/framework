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
package leap.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import leap.db.model.DbSchema;
import leap.db.model.DbSchemaName;
import leap.lang.exception.NestedSQLException;

public interface DbMetadataReader {

	/**
	 * reads all the schema names associated with the given connection.
	 * 
	 * @see DatabaseMetaData#getSchemas()
	 * 
	 * @throws NestedSQLException if a {@link SQLException} error occurs.
	 */
	DbSchemaName[] readSchemaNames(Connection connection) throws NestedSQLException;
	
	/**
	 * reads the {@link DbSchema} object in the underlying database associated with the given connection.
	 * 
	 * <p>
	 * returns <code>null</code> if the given schema name not exists in the underlying database.
	 * 
	 * @throws NestedSQLException if a {@link SQLException} error occurs.
	 */
	DbSchema readSchema(Connection connection,String catalog,String schema) throws NestedSQLException;
	
}