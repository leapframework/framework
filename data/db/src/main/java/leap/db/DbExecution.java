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
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import leap.lang.annotation.Immutable;
import leap.lang.exception.NestedSQLException;

public interface DbExecution {
	
	/**
	 * Default is <code>true</code>
	 */
	DbExecution setThrowExceptionOnExecuting(boolean throwExceptionOnExecuting);
	
	/**
	 * adds a sql statement to this execution.
	 */
	DbExecution add(String sql);
	
	/**
	 * adds a sql statements array to this execution.
	 */
	DbExecution addAll(String... sqls);
	
	/**
	 * adds a sql statements collection to this execution.
	 */
	DbExecution addAll(Collection<String> sqls);
	
	/**
	 * set to <code>true</code> will continue executing if error occured.
	 * 
	 * <p>
	 * the default value is <code>false</code>.
	 */
	DbExecution setContinueOnError(boolean continueOnError);
	
	/**
	 * Sets to <code>true</code> will refresh the schema after execution.
	 * 
	 * <p>
	 * Defaults is <code>true</code>
	 * 
	 * @see DbMetadata#refresh()
	 */
	DbExecution setRefreshSchema(boolean refreshSchema);
	
	/**
	 * returns all the sql statements in this execution.
	 * 
	 * <p>
	 * the returned statements list is immutable.
	 */
	@Immutable List<? extends DbStatement> statements();
	
	/**
	 * Returns the number of sql statements to be executed.
	 * 
	 * <p>
	 * Returns 0 if no sql statements.
	 */
	int numberOfStatements();
	
	/**
	 * returns the number of sql statements executed successfully.
	 * 
	 * <p>
	 * returns -1 if this execution not executed.
	 */
	int numberOfSuccesses();
	
	/**
	 * returns the number of sql statements executed errored.
	 * 
	 * <p>
	 * returns -1 if this execution not executed.
	 */
	int numberOfErrors();
	
	/**
	 * returns the number of sql statemetns have been executed.
	 * 
	 * <p>
	 * returns -1 if this execution not executed.
	 * 
	 * <p>
	 * {@link #numberOfExecuted()} = {@link #numberOfSuccesses()} + {@link #numberOfErrors()}
	 */
	int numberOfExecuted();
	
	/**
	 * returns <code>true</code> if this execution executed successfully.
	 * that means all statements have been executed and no errors occured.
	 * 
	 * @throws IllegalStateException if this execution not executed.
	 */
	boolean success() throws IllegalStateException;
	
	/**
	 * returns <code>true</code> if this execution have been executed.
	 */
	boolean executed();
	
	/**
	 * returns a list of execution errors.
	 * 
	 * <p>
	 * every time invoking this method will creates a new {@link List} contains the errors.
	 * 
	 * @throws IllegalStateException if this execution not executed.
	 */
	List<DbError> errors() throws IllegalStateException;
	
	/**
	 * Returns a list contains all the sql scripts to be executed.
	 */
	List<String> sqls();
	
	/**
	 * returns <code>true<code> if no errors after execution.
	 * 
	 * <p>
	 * returns <code>false</code> if errors occured during execution.
	 * 
	 * @throws NestedSQLException if {@link SQLException} occured while not executing statement.
	 * @throws IllegalStateException if this execution aleady executed. 
	 */
	boolean execute() throws NestedSQLException,IllegalStateException;
	
	/**
	 * returns <code>true<code> if no errors after execution.
	 * 
	 * <p>
	 * returns <code>false</code> if errors occured during execution.
	 * 
	 * <p>
	 * the given connection must be closed after executing this execution.
	 * 
	 * @throws NestedSQLException if {@link SQLException} occured while not executing any statement.
	 * @throws IllegalStateException if this execution aleady executed.
	 */
	boolean execute(Connection connection) throws NestedSQLException,IllegalStateException;
}