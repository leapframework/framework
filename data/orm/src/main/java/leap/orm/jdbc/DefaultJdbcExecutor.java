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
package leap.orm.jdbc;

import leap.core.jdbc.BatchPreparedStatementHandler;
import leap.core.jdbc.JdbcExecutor;
import leap.core.jdbc.PreparedStatementHandler;
import leap.core.jdbc.ResultSetReader;
import leap.db.Db;
import leap.lang.exception.NestedSQLException;
import leap.lang.jdbc.ConnectionCallback;
import leap.lang.jdbc.ConnectionCallbackWithResult;
import leap.orm.OrmContext;

import javax.sql.DataSource;

public class DefaultJdbcExecutor implements JdbcExecutor {

	protected final OrmContext context;
	protected final Db         db;

	public DefaultJdbcExecutor(OrmContext ormContext) {
		this.context = ormContext;
	    this.db      = ormContext.getDb();
    }
	
	@Override
    public void execute(ConnectionCallback callback) throws NestedSQLException {
		db.execute(callback);
    }

    @Override
    public void withDataSource(DataSource dataSource, Runnable runnable) {
        db.withDataSource(dataSource, runnable);
    }

    @Override
    public <T> T executeWithResult(ConnectionCallbackWithResult<T> callback) throws NestedSQLException {
	    return db.executeWithResult(callback);
    }

	@Override
    public int executeUpdate(String sql) throws NestedSQLException {
	    return db.executeUpdate(sql);
    }

	@Override
    public int executeUpdate(String sql, Object[] args) throws NestedSQLException {
	    return db.executeUpdate(sql, args);
    }

	@Override
    public int executeUpdate(String sql, Object[] args, int[] types) throws NestedSQLException {
	    return db.executeUpdate(sql, args, types);
    }

	@Override
    public int executeUpdate(String sql, Object[] args, int[] types, PreparedStatementHandler<?> handler) throws NestedSQLException {
	    return db.executeUpdate(sql, args, types, handler);
    }

	@Override
    public int[] executeBatchUpdate(String... sqls) throws NestedSQLException {
	    return db.executeBatchUpdate(sqls);
    }

	@Override
    public int[] executeBatchUpdate(String sql, Object[][] batchArgs) throws NestedSQLException {
	    return db.executeBatchUpdate(sql, batchArgs);
    }

	@Override
    public int[] executeBatchUpdate(String sql, Object[][] batchArgs, int[] types) throws NestedSQLException {
	    return db.executeBatchUpdate(sql, batchArgs, types);
    }
	
	@Override
    public int[] executeBatchUpdate(String sql, Object[][] batchArgs, int[] types, BatchPreparedStatementHandler<?> handler)
            throws NestedSQLException {
	    return db.executeBatchUpdate(sql,batchArgs,types,handler);
    }

	@Override
    public <T> T executeQuery(String sql, ResultSetReader<T> reader) throws NestedSQLException {
	    return db.executeQuery(sql, reader);
    }

	@Override
    public <T> T executeQuery(String sql, Object[] args, ResultSetReader<T> reader) throws NestedSQLException {
	    return db.executeQuery(sql, reader);
    }

	@Override
    public <T> T executeQuery(String sql, Object[] args, int[] types, ResultSetReader<T> reader) throws NestedSQLException {
	    return db.executeQuery(sql, args, reader);
    }
}
