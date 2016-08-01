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
import leap.core.jdbc.JdbcExecutor;
import leap.core.jdbc.PreparedStatementHandler;
import leap.core.jdbc.ResultSetReader;
import leap.db.Db;
import leap.lang.annotation.Nullable;
import leap.lang.exception.NestedSQLException;
import leap.orm.OrmContext;
import leap.orm.mapping.EntityMapping;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DefaultSqlStatement implements SqlStatement,BatchSqlStatement,SqlExecutionContext {
	
	protected final SqlContext context;
    protected final Sql        sql;
	protected final String     sqlString;
	protected final Object[]   args;
	protected final Object[][] batchArgs;
	protected final int[]      argTypes;
	
	public DefaultSqlStatement(SqlContext context, Sql sql, String sqlString, Object[] args, int[] argTypes){
		this.context   = context;
        this.sql       = sql;
		this.sqlString = sqlString;
		this.args      = args;
		this.argTypes  = argTypes;
		this.batchArgs = null;
	}
	
	public DefaultSqlStatement(SqlContext context, Sql sql, String sqlString, Object[][] batchArgs, int[] argTypes){
		this.context   = context;
        this.sql       = sql;
		this.sqlString = sqlString;
		this.args      = null;
		this.argTypes  = argTypes;
		this.batchArgs = batchArgs;
	}

    @Override
    public OrmContext getOrmContext() {
        return context.getOrmContext();
    }

    @Override
    public JdbcExecutor getJdbcExecutor() {
        return context.getJdbcExecutor();
    }

    @Override
    public EntityMapping getPrimaryEntityMapping() {
        return context.getPrimaryEntityMapping();
    }

    @Override
    public Sql sql() {
        return sql;
    }

    @Override
    public int executeUpdate() throws NestedSQLException {
		return context.getJdbcExecutor().executeUpdate(sqlString, args, argTypes);
    }
	
	@Override
    public int executeUpdate(@Nullable PreparedStatementHandler<Db> psHandler) throws NestedSQLException {
	    return context.getJdbcExecutor().executeUpdate(sqlString, args, argTypes, psHandler);
    }

	@Override
    public int[] executeBatchUpdate() throws NestedSQLException {
	    return context.getJdbcExecutor().executeBatchUpdate(sqlString, batchArgs, argTypes);
    }

	@Override
    public int[] executeBatchUpdate(BatchPreparedStatementHandler<Db> psHandler) throws NestedSQLException {
	    return context.getJdbcExecutor().executeBatchUpdate(sqlString, batchArgs, argTypes, psHandler);
    }

	@Override
    public <T> T executeQuery(ResultSetReader<T> reader) throws NestedSQLException {
		return context.getJdbcExecutor().executeQuery(sqlString, args, argTypes, wrap(reader));
    }

    private <T> ResultSetReader<T> wrap(ResultSetReader<T> reader) {
        if(reader instanceof SqlResultSetReader) {
            return rs -> ((SqlResultSetReader<T>) reader).read(this, rs);
        }else{
            return reader;
        }
    }
}