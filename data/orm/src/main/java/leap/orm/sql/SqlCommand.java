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
package leap.orm.sql;

import leap.core.jdbc.BatchPreparedStatementHandler;
import leap.core.jdbc.PreparedStatementHandler;
import leap.core.jdbc.ResultSetReader;
import leap.db.Db;
import leap.lang.Sourced;
import leap.lang.annotation.Nullable;
import leap.lang.exception.NestedSQLException;
import leap.orm.metadata.MetadataContext;
import leap.orm.query.QueryContext;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface SqlCommand extends Sourced {
	
	String INSERT_COMMAND_NAME     = "insert";
	String UPDATE_COMMAND_NAME     = "update";
	String DELETE_COMMAND_NAME     = "delete";
	String DELETE_ALL_COMMAND_NAME = "deleteAll";
	String FIND_COMMAND_NAME       = "find";
	String FIND_LIST_COMMAND_NAME  = "findList";
	String FIND_ALL_COMMAND_NAME   = "findAll";
	String EXISTS_COMMAND          = "exists";
	String COUNT_COMMAND           = "count";

    /**
     * Called after all sql commands was loaded, for preparing execution, such as parsing the sql content.
     */
    void prepare(MetadataContext context);
	
	/**
	 * Returns the name of db platform or <code>null</code> if this command supports all db platforms.
	 */
	String getDbType();
	
	/**
	 * Returns the unique <code>SqlClause</code>.
	 *
	 * @return the sql clause of this command
	 * @throws IllegalStateException if this command contains two or more sql clauses.
     */
	SqlClause getClause() throws IllegalStateException;

	/**
	 * Executes update and returns the affected rows.
	 * 
	 * @throws IllegalStateException if this command is not an update command.
	 * @throws NestedSQLException if a {@link SQLException} throwed.
	 * 
	 * @see PreparedStatement#executeUpdate()
	 */
	int executeUpdate(SqlContext context,@Nullable Object params) throws IllegalStateException, NestedSQLException;
	
	/**
	 * Executes update and returns the affected rows.
	 * 
	 * @throws IllegalStateException if this command is not an update command.
	 * @throws NestedSQLException if a {@link SQLException} throwed.
	 * 
	 * @see PreparedStatement#executeUpdate()
	 */
	int executeUpdate(SqlContext context,Object params,PreparedStatementHandler<Db> preparedStatementHandler) throws IllegalStateException,NestedSQLException;
	
	/**
	 * Executes query and returns the result readed by the given {@link ResultSetReader}.
	 * 
	 * @throws IllegalStateException if this command is not a query command.
	 * @throws IllegalStateException if a {@link SQLException} throwed.
	 * 
	 * @see PreparedStatement#executeQuery()
	 */
	<T> T executeQuery(QueryContext context, Object params, ResultSetReader<T> reader) throws IllegalStateException, NestedSQLException;
	
	/**
	 * Executes a count(*) query on this command and returns the total records.
	 */
	long executeCount(QueryContext context,Object params);
	
	/**
	 * Executes batch update and returns the affected rows.
	 * 
	 * @see PreparedStatement#executeBatch()
	 */
	int[] executeBatchUpdate(SqlContext context,Object[] batchParams) throws IllegalStateException, NestedSQLException;
	
	/**
	 * Executes batch update and returns the affected rows.
	 * 
	 * @see PreparedStatement#executeBatch()
	 */
	int[] executeBatchUpdate(SqlContext context,Object[] batchParams,BatchPreparedStatementHandler<Db> preparedStatementHandler) throws IllegalStateException, NestedSQLException;

}