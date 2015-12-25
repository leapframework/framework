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
import leap.lang.annotation.Nullable;
import leap.lang.exception.NestedSQLException;

public class DefaultSqlStatement implements SqlStatement,BatchSqlStatement {
	
	protected final SqlContext context;
	protected final String	   sql;
	protected final Object[]   args;
	protected final Object[][] batchArgs;
	protected final int[]	   argTypes;
	
	public DefaultSqlStatement(SqlContext context,String sql,Object[] args,int[] argTypes){
		this.context   = context;
		this.sql       = sql;
		this.args      = args;
		this.argTypes  = argTypes;
		this.batchArgs = null;
	}
	
	public DefaultSqlStatement(SqlContext context,String sql,Object[][] batchArgs,int[] argTypes){
		this.context   = context;
		this.sql       = sql;
		this.args      = null;
		this.argTypes  = argTypes;
		this.batchArgs = batchArgs;
	}

	@Override
    public int executeUpdate() throws NestedSQLException {
		return context.getJdbcExecutor().executeUpdate(sql, args, argTypes);
    }
	
	@Override
    public int executeUpdate(@Nullable PreparedStatementHandler<Db> psHandler) throws NestedSQLException {
	    return context.getJdbcExecutor().executeUpdate(sql, args, argTypes, psHandler);
    }

	@Override
    public int[] executeBatchUpdate() throws NestedSQLException {
	    return context.getJdbcExecutor().executeBatchUpdate(sql, batchArgs, argTypes);
    }

	@Override
    public int[] executeBatchUpdate(BatchPreparedStatementHandler<Db> psHandler) throws NestedSQLException {
	    return context.getJdbcExecutor().executeBatchUpdate(sql, batchArgs, argTypes, psHandler);
    }

	@Override
    public <T> T executeQuery(ResultSetReader<T> reader) throws NestedSQLException {
		return context.getJdbcExecutor().executeQuery(sql, args, argTypes, reader);
    }
}