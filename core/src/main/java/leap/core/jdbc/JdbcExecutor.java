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
package leap.core.jdbc;

import leap.lang.Arrays2;
import leap.lang.exception.NestedSQLException;
import leap.lang.jdbc.ConnectionCallback;
import leap.lang.jdbc.ConnectionCallbackWithResult;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public interface JdbcExecutor {
	
	/**
	 * Executes the given {@link ConnectionCallback}.
	 * 
	 * @throws NestedSQLException if an {@link SQLException} throwed.
	 */
	void execute(ConnectionCallback callback) throws NestedSQLException;
	
	/**
	 * Executes the given {@link ConnectionCallbackWithResult} and returns the result.
	 * 
	 * @throws NestedSQLException if an {@link SQLException} throwed.
	 */
	<T> T executeWithResult(ConnectionCallbackWithResult<T> callback) throws NestedSQLException;

	/**
	 * Execute an update use the given sql statement.
	 */
	int executeUpdate(String sql) throws NestedSQLException;
	
	/**
	 * Executes an update use the given sql statement.
	 * 
	 * @see PreparedStatement#executeUpdate()
	 */
	int executeUpdate(String sql, Object[] args) throws NestedSQLException;
	
	/**
	 * Executes an update use the given sql statement.
	 * 
	 * @see PreparedStatement#executeUpdate()
	 */
	int executeUpdate(String sql, Object[] args,int[] types) throws NestedSQLException;
	
	/**
	 * Executes an update use the given sql statement.
	 * 
	 * @see PreparedStatement#executeUpdate()
	 */
	int executeUpdate(String sql, Object[] args,int[] types,PreparedStatementHandler<?> handler) throws NestedSQLException;
	
	/**
	 * Executes a batch update use the given sql statements.
	 * 
	 * @see PreparedStatement#executeBatch()
	 */
	int[] executeBatchUpdate(String... sqls) throws NestedSQLException;
	
	/**
	 * Executes a batch update use the given sql statement and data rows.
	 * 
	 * @see PreparedStatement#executeBatch()
	 */
	int[] executeBatchUpdate(String sql,Object[][] batchArgs) throws NestedSQLException;
	
	/**
	 * Executes a batch update use the given sql statement and data rows.
	 * 
	 * @see PreparedStatement#executeBatch()
	 */
	int[] executeBatchUpdate(String sql,Object[][] batchArgs,int[] types) throws NestedSQLException;
	
	/**
	 * Executes a batch update use the given sql statement and data rows.
	 * 
	 * @see PreparedStatement#executeBatch()
	 */
	int[] executeBatchUpdate(String sql,Object[][] batchArgs,int[] types,BatchPreparedStatementHandler<?> handler) throws NestedSQLException;
	
	/**
	 * Executes a query and returns the result readed by the given {@link ResultSetReader}.
	 * 
	 * @see Statement#executeQuery(String)
	 */
	<T> T executeQuery(String sql, ResultSetReader<T> reader) throws NestedSQLException;
	
	/**
	 * Executes a query and returns the result readed by the given {@link ResultSetReader}.
	 * 
	 * @see Statement#executeQuery(String)
	 */
	<T> T executeQuery(String sql, Object[] args, ResultSetReader<T> reader) throws NestedSQLException;
	
	/**
	 * Executes a query and returns the result readed by the given {@link ResultSetReader}.
	 * 
	 * @see Statement#executeQuery(String)
	 */
	<T> T executeQuery(String sql, Object[] args, int[] types, ResultSetReader<T> reader) throws NestedSQLException;
	
	/**
	 * Executes a query and returns the scalar value for the given type.
	 */
	default <T> T queryForScalar(Class<T> type, String sql) throws NestedSQLException {
		return queryForScalar(type, sql, Arrays2.EMPTY_OBJECT_ARRAY,Arrays2.EMPTY_INT_ARRAY);
	}
	
	/**
	 * Executes a query and returns the scalar value for the given type.
	 */
	default <T> T queryForScalar(Class<T> type, String sql,Object[] args) throws NestedSQLException {
		return queryForScalar(type, sql, args,Arrays2.EMPTY_INT_ARRAY);
	}
	
	/**
	 * Executes a query and returns the scalar value for the given type. 
	 */
	default <T> T queryForScalar(Class<T> type, String sql,Object[] args, int[] types) throws NestedSQLException {
		return executeQuery(sql, args, types, new RawScalarReader<T>(type,false));
	}
	
	default String queryForString(String sql) throws NestedSQLException {
		return queryForScalar(String.class, sql);
	}
	
	default String queryForString(String sql,Object[] args) throws NestedSQLException {
		return queryForScalar(String.class, sql, args);
	}
	
	default Integer queryForInteger(String sql) throws NestedSQLException {
		return queryForScalar(Integer.class, sql);
	}
	
	default Integer queryForInteger(String sql,Object[] args) throws NestedSQLException {
		return queryForScalar(Integer.class, sql, args);
	}
	
	default Long queryForLong(String sql) throws NestedSQLException {
		return queryForScalar(Long.class, sql);
	}
	
	default Long queryForLong(String sql,Object[] args) throws NestedSQLException {
		return queryForScalar(Long.class, sql, args);
	}
}