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
import java.util.List;

import leap.lang.Ordered;
import leap.lang.exception.NestedSQLException;

public interface DbCommand extends Ordered {
	
	/**
	 * Default is <code>true</code>
	 * 
	 * @see DbExecution#setThrowExceptionOnExecuting(boolean)
	 */
	DbCommand setThrowExceptionOnExecuting(boolean throwExceptionOnExecuting);
	
	/**
	 * Returns a {@link List} contains all the sql scripts to be executed in this command.
	 */
	List<String> sqls();

	/**
	 * Executes this command and returns the {@link DbExecution} object as result.
	 */
	DbExecution execute() throws NestedSQLException,IllegalStateException;
	
	/**
	 * Executes this command and returns the {@link DbExecution} object as result.
	 */
	DbExecution execute(Connection connection) throws NestedSQLException,IllegalStateException;
}