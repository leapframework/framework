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

import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.Objects2;
import leap.lang.Strings;
import leap.orm.query.QueryContext;

public class JdbcSqlClause extends AbstractSqlClause implements SqlClause {
	
	protected final boolean query;
	protected final String  sql;
	
	private String countSql;
	
	public JdbcSqlClause(String sql){
		Args.notEmpty(sql,"sql");
		this.sql   = sql.trim();
		this.query = Strings.startsWith(sql, "select ",true);
	}
	
	@Override
    public SqlStatement createUpdateStatement(SqlContext context, Object params) {
	    return doCreateStatement(context, sql, params);
    }
	
	@Override
    public SqlStatement createQueryStatement(QueryContext context, Object params) {
		//TODO : dynamic page query in jdbc clause
		if(null != context.getLimit()){
			throw new UnsupportedOperationException("Dyanmic page query not implemented by " + this.getClass().getSimpleName());
		}
		
		//TODO : dynamic order by in jdbc clause
		if(!Strings.isEmpty(context.getOrderBy())){
			throw new UnsupportedOperationException("Dynamic order by not implemented by " + this.getClass().getSimpleName());
		}
		
		return doCreateStatement(context, sql, params);
    }
	
	@Override
    public SqlStatement createCountStatement(QueryContext context, Object params) {
		if(null == countSql) {
			countSql = "select count(*) from (" + sql + ") t";
		}
	    return doCreateStatement(context, countSql, params);
    }

	@Override
    public BatchSqlStatement createBatchStatement(SqlContext context, Object[] params) {
		Object[][] batchArgs = new Object[params.length][];
		
		for(int i=0;i<params.length;i++){
			Object param = params[i];
			Object[] args;
			if(null != param){
				if(param.getClass().isArray()){
					args = Objects2.toObjectArray(param);
				}else{
					args = new Object[]{param};
				}
			}else{
				args = Arrays2.EMPTY_OBJECT_ARRAY;;
			}
			batchArgs[i] = args;
		}
		
		return new DefaultSqlStatement(context, sql, batchArgs, null);
    }

	protected SqlStatement doCreateStatement(SqlContext context, String sql, Object params){
		return new DefaultSqlStatement(context, sql, resolveArgs(params), null);
	}
	
	protected Object[] resolveArgs(Object params) {
		Object[] args;
		if(null != params){
			if(params.getClass().isArray()){
				args = Objects2.toObjectArray(params);
			}else{
				args = new Object[]{params};
			}
		}else{
			args = Arrays2.EMPTY_OBJECT_ARRAY;;
		}
		return args;
	}

	@Override
    public String toString() {
	    return sql;
    }
}