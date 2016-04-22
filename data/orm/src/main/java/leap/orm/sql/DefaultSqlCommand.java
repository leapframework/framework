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

import java.util.List;

import leap.core.jdbc.BatchPreparedStatementHandler;
import leap.core.jdbc.PreparedStatementHandler;
import leap.core.jdbc.ResultSetReader;
import leap.db.Db;
import leap.lang.Args;
import leap.lang.Assert;
import leap.lang.exception.NestedSQLException;
import leap.orm.query.QueryContext;
import leap.orm.reader.ResultSetReaders;

public class DefaultSqlCommand implements SqlCommand {
	
	protected final Object 	    source;
	protected final String	    dbType;
	protected final SqlClause[] clauses;
	protected final SqlClause   queryClause;
	
	public DefaultSqlCommand(Object source,List<SqlClause> clauses){
		this(source,null,clauses);
	}
	
	public DefaultSqlCommand(Object source,String dbType,List<SqlClause> clauses){
		Args.notEmpty(clauses,"clauses");
		this.source  = source;
		this.dbType  = dbType;
		this.clauses = clauses.toArray(new SqlClause[clauses.size()]);
		this.queryClause = checkQuery();
	}
	
	public DefaultSqlCommand(String source,SqlClause clause){
		this(source,null,clause);
	}
	
	public DefaultSqlCommand(String source,String dbType, SqlClause clause){
		Args.notNull(clause,"clause");
		this.source  = source;
		this.dbType  = dbType;
		this.clauses = new SqlClause[]{clause};
		this.queryClause = checkQuery();
	}
	
	@Override
    public Object getSource() {
	    return source;
    }
	
	@Override
    public String getDbType() {
	    return dbType;
    }

	@Override
    public boolean isQuery() {
	    return null != queryClause;
    }

	@Override
	public SqlClause getQueryClause() throws SqlClauseException {
		if(isQuery()){
			return queryClause;
		}
		throw new SqlClauseException("this command is not a query command!");
	}

	@Override
    public int executeUpdate(SqlContext context, Object params) throws NestedSQLException {
		return doExecuteUpdate(context, params, null);
    }
	
	@Override
    public int executeUpdate(SqlContext context, Object params, PreparedStatementHandler<Db> psHandler) throws IllegalStateException, NestedSQLException {
	    return doExecuteUpdate(context, params, psHandler);
    }

	@Override
    public <T> T executeQuery(QueryContext context, Object params,ResultSetReader<T> reader) throws NestedSQLException {
		Assert.isTrue(null != queryClause,"This command is not a query, cannot execute query");
		
		if(clauses.length == 1){
			return queryClause.createQueryStatement(context, params).executeQuery(reader);
		}else{
			throw new IllegalStateException("Two or more sql statements in a sql command not supported now");
		}
    }
	
	@Override
    public long executeCount(QueryContext context, Object params) {
		Assert.isTrue(null != queryClause,"This command is not a query, cannot execute count(*) query");
		
		if(clauses.length == 1){
			return queryClause.createCountStatement(context, params).executeQuery(ResultSetReaders.forScalarValue(Long.class, false));
		}else{
			throw new IllegalStateException("Two or more sql statements in a sql command not supported now");
		}
    }
	
	@Override
    public int[] executeBatchUpdate(SqlContext context, Object[] batchParams) throws IllegalStateException, NestedSQLException {
	    return doExecuteBatchUpdate(context, batchParams, null);
    }
	
	@Override
    public int[] executeBatchUpdate(SqlContext context, 
    								Object[] batchParams, 
    								BatchPreparedStatementHandler<Db> preparedStatementHandler) throws IllegalStateException, NestedSQLException {
	    return doExecuteBatchUpdate(context, batchParams, preparedStatementHandler);
    }

	protected int doExecuteUpdate(SqlContext context, Object params, PreparedStatementHandler<Db> psHandler) {
		Assert.isTrue(null == queryClause,"This command is a query, cannot execute update");
		
		if(clauses.length == 1){
			return clauses[0].createUpdateStatement(context, params).executeUpdate(psHandler);
		}else{
			throw new IllegalStateException("Two or more sql statements in a sql command not supported now");
		}
	}
	
	protected int[] doExecuteBatchUpdate(SqlContext context, Object[] batchParams, BatchPreparedStatementHandler<Db> psHandler) {
		Assert.isTrue(null == queryClause,"This command is a query, cannot execute batch update");
		
		if(clauses.length == 1){
			return clauses[0].createBatchStatement(context, batchParams).executeBatchUpdate(psHandler);
		}else{
			throw new IllegalStateException("Two or more sql statements in a sql command not supported now");
		}
	}
	
	protected SqlClause checkQuery(){
		SqlClause queryClause = null;
		
		for(SqlClause clause : clauses){
			if(clause.isQuery()){
				if(null != queryClause){
					throw new SqlConfigException("Two or more query clauses in a command not supported, source : " + source);
				}
				queryClause = clause;
			}
		}
		return queryClause;
	}

	@Override
    public String toString() {
		return this.getClass().getSimpleName() + "[" + source + "]";
    }
	
}