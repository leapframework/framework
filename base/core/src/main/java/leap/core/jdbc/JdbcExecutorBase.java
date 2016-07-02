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

import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.exception.NestedSQLException;

public abstract class JdbcExecutorBase implements JdbcExecutor {
	
	@Override
	public final int executeUpdate(String sql) throws NestedSQLException {
		Args.notEmpty(sql,"sql");
		return doExecuteUpdate(sql,Arrays2.EMPTY_OBJECT_ARRAY,Arrays2.EMPTY_INT_ARRAY);
	}

	@Override
	public final int executeUpdate(String sql, Object[] args) throws NestedSQLException {
		Args.notEmpty(sql,"sql");
		Args.notNull(args,"args");
		return doExecuteUpdate(sql, args, Arrays2.EMPTY_INT_ARRAY);
	}

	@Override
	public final int executeUpdate(String sql, Object[] args, int[] types) throws NestedSQLException {
		Args.notEmpty(sql,"sql");
		Args.notNull(args,"args");
		Args.notNull(types,"types");
		return doExecuteUpdate(sql, args, types);
	}

	@Override
	public final int[] executeBatchUpdate(String... sqls) throws NestedSQLException {
		Args.notEmpty(sqls,"sqls");
		return doExecuteBatchUpdate(sqls);
	}

	@Override
	public final int[] executeBatchUpdate(String sql, Object[][] batchArgs) throws NestedSQLException {
		Args.notEmpty(sql,"sql");
		Args.notNull(batchArgs,"args");
		return doExecuteBatchUpdate(sql, batchArgs, null);
	}

	@Override
	public final int[] executeBatchUpdate(String sql, Object[][] batchArgs, int[] types) throws NestedSQLException {
		Args.notEmpty(sql,"sql");
		Args.notNull(batchArgs,"args");
		Args.notNull(types,"types");
		return doExecuteBatchUpdate(sql, batchArgs, types);
	}

	@Override
	public final <T> T executeQuery(String sql, ResultSetReader<T> reader) throws NestedSQLException {
		Args.notEmpty(sql,"sql");
		Args.notNull(reader,"reader");
		return doExecuteQuery(sql, Arrays2.EMPTY_OBJECT_ARRAY, Arrays2.EMPTY_INT_ARRAY, reader);
	}

	@Override
	public final <T> T executeQuery(String sql, Object[] args, ResultSetReader<T> reader) throws NestedSQLException {
		Args.notEmpty(sql,"sql");
		Args.notNull(args,"args");
		Args.notNull(reader,"reader");
		return doExecuteQuery(sql, args, Arrays2.EMPTY_INT_ARRAY, reader);
	}

	@Override
	public final <T> T executeQuery(String sql, Object[] args, int[] types, ResultSetReader<T> reader) throws NestedSQLException {
		Args.notEmpty(sql,"sql");
		Args.notNull(args,"args");
		Args.notNull(types,"types");
		Args.notNull(reader,"reader");
		return doExecuteQuery(sql, args, types, reader);
	}
	
	protected abstract int doExecuteUpdate(String sql, Object[] args, int[] types) throws NestedSQLException;
	
	protected abstract int[] doExecuteBatchUpdate(String[] sqls) throws NestedSQLException;
	
	protected abstract int[] doExecuteBatchUpdate(String sql, Object[][] args, int[] types) throws NestedSQLException;
	
	protected abstract <T> T doExecuteQuery(String sql, Object[] args, int[] types, ResultSetReader<T> reader) throws NestedSQLException;
}