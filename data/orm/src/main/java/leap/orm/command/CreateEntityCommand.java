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
package leap.orm.command;



public interface CreateEntityCommand extends DmoCommand {

	/**
	 * Set createTable to <code>true</code> will create db table when execute, Defaults to <code>false</code>
	 */
	CreateEntityCommand setCreateTable(boolean createTable);
	
	/**
	 * Sets to <code>true</code> will upgrade the schema of table if exists.
	 * 
	 * <p>
	 * The table will be created if not exists. 
	 */
	CreateEntityCommand setUpgradeTable(boolean upgrade);
	
	/**
	 * Be careful. Sets to <code>true</code> will drop the table if exists.
	 */
	CreateEntityCommand setDropTableIfExists(boolean dropTableIfExists);
	
	/**
	 * Sets the table name of entity.
	 */
	CreateEntityCommand setTableName(String tableName);
	
	/**
	 * Execute this command.
	 * 
	 * <p>
	 * Return <code>true</code> if no error(s).
	 */
	boolean execute(boolean createTable);
}