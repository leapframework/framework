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

import leap.db.change.SchemaChanges;
import leap.db.change.UnsupportedChangeException;
import leap.db.model.DbSchema;
import leap.db.model.DbTable;

public interface DbComparator {

	/**
	 * Returns all the schema changes compare the source table to the target table.
	 * 
	 * @throws UnsupportedChangeException if some changes not supported by the underlying implementation.
	 */
	SchemaChanges compareTable(DbTable source,DbTable target) throws UnsupportedChangeException;
	
	/**
	 * Returns all the schema changes compare the source tables to the target tables.
	 * 
	 * @throws UnsupportedChangeException if some changes not supported by the underlying implementation.
	 */
	SchemaChanges compareTables(DbTable[] source,DbTable[] target) throws UnsupportedChangeException;
	
	/**
	 * Returns all the schema changes compare the source table to the tables in the target schema. 
	 */
	SchemaChanges compareTables(DbTable[] source,DbSchema target) throws UnsupportedChangeException;
	
	/**
	 * Returns all the schema changes compare the source schema to the target schema.
	 * 
	 * @throws UnsupportedChangeException if some changes not suppported by the underlying implementation.
	 */
	SchemaChanges compareSchema(DbSchema source,DbSchema target) throws UnsupportedChangeException;
	
}