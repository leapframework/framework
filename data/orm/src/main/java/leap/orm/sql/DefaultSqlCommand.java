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
import leap.lang.Strings;
import leap.lang.exception.NestedSQLException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.metadata.MetadataContext;
import leap.orm.query.QueryContext;
import leap.orm.reader.ResultSetReaders;

public class DefaultSqlCommand extends AbstractSqlCommand {

    private static final Log log = LogFactory.get(DefaultSqlCommand.class);

    protected Boolean filterColumnEnabled;
    protected Boolean queryFilterEnabled;

    public DefaultSqlCommand(SqlInfo info) {
        super(info);
    }

    public DefaultSqlCommand clone() {
        DefaultSqlCommand cloned = new DefaultSqlCommand(info);
        cloned.filterColumnEnabled = filterColumnEnabled;
        cloned.queryFilterEnabled = queryFilterEnabled;
        return cloned;
    }

    @Override
    public Boolean getFilterColumnEnabled() {
        return filterColumnEnabled;
    }

    @Override
    public Boolean getQueryFilterEnabled() {
        return queryFilterEnabled;
    }

    public void setFilterColumnEnabled(Boolean filterColumnEnabled) {
        this.filterColumnEnabled = filterColumnEnabled;
    }

    public void setQueryFilterEnabled(Boolean queryFilterEnabled) {
        this.queryFilterEnabled = queryFilterEnabled;
    }

    @Override
    public int executeUpdate(SqlContext context, Object params, PreparedStatementHandler<Db> psHandler) throws IllegalStateException, NestedSQLException {
        mustPrepare(context);

        log.info("Executing sql update: '{}'", desc());

        if(clauses.length == 1){
            return clauses[0].createUpdateStatement(context, params).executeUpdate(psHandler);
        }else{
            //todo : check it
            return context.getOrmContext().getDao().doTransaction((s) -> {
                int result = 0;

                for(SqlClause clause : clauses) {
                    result += clause.createUpdateStatement(context, params).executeUpdate(psHandler);
                }

                return result;
            });

            //throw new IllegalStateException("Two or more sql statements in a sql command not supported now");
        }
    }

	@Override
    public <T> T executeQuery(QueryContext context, Object params,ResultSetReader<T> reader) throws NestedSQLException {
		//Assert.isTrue(null != queryClause,"This command is not a query, cannot execute query");
        log.info("Executing sql query: '{}'", desc());
        mustPrepare(context);

		if(clauses.length == 1){
            return clauses[0].createQueryStatement(context, params).executeQuery(reader);
		}else{
			throw new IllegalStateException("Two or more sql statements in a sql command not supported now");
		}
    }
	
	@Override
    public long executeCount(QueryContext context, Object params) {
        log.info("Executing sql count: '{}'", desc());
        mustPrepare(context);

		if(clauses.length == 1){
			return clauses[0].createCountStatement(context, params).executeQuery(ResultSetReaders.forScalarValue(Long.class, false));
		}else{
			throw new IllegalStateException("Two or more sql statements in a sql command not supported now");
		}
    }
	
	@Override
    public int[] executeBatchUpdate(SqlContext context, 
    								Object[] batchParams, 
    								BatchPreparedStatementHandler<Db> handler) throws IllegalStateException, NestedSQLException {

        log.info("Executing sql batch update: '{}'", desc());
        mustPrepare(context);

		if(clauses.length == 1){
			return clauses[0].createBatchStatement(context, batchParams).executeBatchUpdate(handler);
		}else{
			throw new IllegalStateException("Two or more sql statements in a sql command not supported now");
		}
	}

}