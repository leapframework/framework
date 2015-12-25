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
package leap.orm.dao;

import leap.core.BeanFactory;
import leap.core.ioc.PostCreateBean;
import leap.core.ioc.PostInjectBean;
import leap.core.jdbc.BatchPreparedStatementHandler;
import leap.core.jdbc.JdbcExecutor;
import leap.core.jdbc.PreparedStatementHandler;
import leap.core.jdbc.ResultSetReader;
import leap.core.transaction.TransactionCallback;
import leap.core.transaction.TransactionCallbackWithResult;
import leap.core.transaction.TransactionManager;
import leap.core.transaction.Transactions;
import leap.core.validation.annotations.NotEmpty;
import leap.core.validation.annotations.NotNull;
import leap.lang.Readonly;
import leap.lang.exception.NestedSQLException;
import leap.lang.jdbc.ConnectionCallback;
import leap.lang.jdbc.ConnectionCallbackWithResult;
import leap.orm.OrmContext;
import leap.orm.OrmMetadata;
import leap.orm.command.CommandFactory;
import leap.orm.jdbc.JdbcExecutorFactory;
import leap.orm.query.QueryFactory;
import leap.orm.sql.SqlFactory;

public abstract class DaoBase extends Dao implements PostCreateBean,PostInjectBean {
	protected final Readonly _readonly = new Readonly(this);
	
	protected @NotEmpty String      	   name;
	protected @NotNull  OrmContext   	   ormContext;
	protected @NotNull  JdbcExecutor 	   jdbcExecutor;
	protected @NotNull  TransactionManager transactionManager;
	
	@Override
    public OrmContext getOrmContext() {
	    return ormContext;
    }
	
	public JdbcExecutor getJdbcExecutor() {
		return jdbcExecutor;
	}

	public void setJdbcExecutor(JdbcExecutor jdbcExecutor) {
		_readonly.check();
		this.jdbcExecutor = jdbcExecutor;
	}
	
	//---------------------jdbc executor---------------------------------------
	
	@Override
    public void execute(ConnectionCallback callback) throws NestedSQLException {
		jdbcExecutor.execute(callback);
    }

	@Override
    public <T> T execute(ConnectionCallbackWithResult<T> callback) throws NestedSQLException {
	    return jdbcExecutor.execute(callback);
    }

	@Override
    public int executeUpdate(String sql) throws NestedSQLException {
	    return jdbcExecutor.executeUpdate(sql);
    }

	@Override
    public int executeUpdate(String sql, Object[] args) throws NestedSQLException {
	    return jdbcExecutor.executeUpdate(sql, args);
    }

	@Override
    public int executeUpdate(String sql, Object[] args, int[] types) throws NestedSQLException {
	    return jdbcExecutor.executeUpdate(sql, args, types);
    }

	@Override
    public int executeUpdate(String sql, Object[] args, int[] types, PreparedStatementHandler<?> handler) throws NestedSQLException {
	    return jdbcExecutor.executeUpdate(sql, args, types, handler);
    }

	@Override
    public int[] executeBatchUpdate(String... sqls) throws NestedSQLException {
	    return jdbcExecutor.executeBatchUpdate(sqls);
    }

	@Override
    public int[] executeBatchUpdate(String sql, Object[][] batchArgs) throws NestedSQLException {
	    return jdbcExecutor.executeBatchUpdate(sql, batchArgs);
    }

	@Override
    public int[] executeBatchUpdate(String sql, Object[][] batchArgs, int[] types) throws NestedSQLException {
	    return jdbcExecutor.executeBatchUpdate(sql, batchArgs, types);
    }

	@Override
    public int[] executeBatchUpdate(String sql, Object[][] batchArgs, int[] types, BatchPreparedStatementHandler<?> handler)
            throws NestedSQLException {
	    return jdbcExecutor.executeBatchUpdate(sql, batchArgs, types, handler);
    }

	@Override
    public <T> T executeQuery(String sql, ResultSetReader<T> reader) throws NestedSQLException {
	    return jdbcExecutor.executeQuery(sql, reader);
    }

	@Override
    public <T> T executeQuery(String sql, Object[] args, ResultSetReader<T> reader) throws NestedSQLException {
	    return jdbcExecutor.executeQuery(sql, args, reader);
    }

	@Override
    public <T> T executeQuery(String sql, Object[] args, int[] types, ResultSetReader<T> reader) throws NestedSQLException {
	    return jdbcExecutor.executeQuery(sql, args, types, reader);
    }

	@Override
    public void doTransaction(TransactionCallback callback) {
		transactionManager.doTransaction(callback);
    }
	
	@Override
    public <T> T doTransaction(TransactionCallbackWithResult<T> callback) {
	    return transactionManager.doTransaction(callback);
    }
	
	@Override
    public void doTransaction(TransactionCallback callback, boolean requiresNew) {
		transactionManager.doTransaction(callback,requiresNew);
    }

	@Override
    public <T> T doTransaction(TransactionCallbackWithResult<T> callback, boolean requiresNew) {
	    return transactionManager.doTransaction(callback, requiresNew);
    }

	@Override
    public void postInject(BeanFactory factory) {
		_readonly.check();
		
		if(null == jdbcExecutor){
			jdbcExecutor = factory.getBean(JdbcExecutorFactory.class).createJdbcExecutor(ormContext);
		}
		
		if(null == transactionManager) {
			transactionManager = Transactions.getTransactionManager(ormContext.getDataSource());
		}
    }
	
	@Override
    public void postCreate(BeanFactory beanFactory) throws Exception {
	    _readonly.check().enable();
	    this.doInit();	    
    }
	
	protected OrmMetadata metadata(){
		return ormContext.getMetadata();
	}
	
	protected CommandFactory commandFactory() {
		return ormContext.getCommandFactory();
	}
	
    protected SqlFactory sqlFactory() {
        return ormContext.getSqlFactory();
    }

	protected QueryFactory queryFactory(){
		return ormContext.getQueryFactory();
	}
	
	protected void doInit() throws Exception {
		
	}
}