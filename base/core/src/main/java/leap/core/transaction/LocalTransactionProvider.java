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
package leap.core.transaction;

import leap.core.ioc.AbstractReadonlyBean;
import leap.core.transaction.TransactionDefinition.Isolation;
import leap.core.transaction.TransactionDefinition.Propagation;
import leap.lang.Args;
import leap.lang.Exceptions;
import leap.lang.exception.NestedSQLException;
import leap.lang.jdbc.ConnectionCallback;
import leap.lang.jdbc.ConnectionCallbackWithResult;
import leap.lang.jdbc.JDBC;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Stack;

public class LocalTransactionProvider extends AbstractReadonlyBean implements TransactionProvider {
	
	private static final Log log = LogFactory.get(LocalTransactionProvider.class);
	
	private final ThreadLocal<Stack<LocalTransaction>> currentTransactions = new ThreadLocal<>();

    protected DataSource  dataSource;
	protected Propagation defaultPropagation = Propagation.REQUIRED;
	protected Isolation   defaultIsolation   = Isolation.DEFAULT;

    private SimpleTransactionDefinition requiredDefinition    = null;
    private SimpleTransactionDefinition requiresNewDefinition = null;

	public LocalTransactionProvider(DataSource dataSource){
		this.dataSource = dataSource;
	}

    @Override
    public Transaction beginTransaction() {
        return getTransaction(getRequiredDefinition()).begin();
    }

    @Override
    public Transaction beginTransaction(TransactionDefinition td) {
        return getTransaction(null == td ? getRequiredDefinition() : td).begin();
    }

    @Override
    public void execute(ConnectionCallback callback) {
        Connection connection = null;
        try{
            connection = getConnection();
            callback.execute(connection);
        }catch(SQLException e){
            Exceptions.wrapAndThrow(e);
        }finally{
            closeConnection(connection);
        }
    }

    @Override
    public <T> T executeWithResult(ConnectionCallbackWithResult<T> callback) {
        Connection connection = null;
        try{
            connection = getConnection();
            return callback.execute(connection);
        }catch(SQLException e){
            Exceptions.wrapAndThrow(e);
            return null;
        }finally{
            closeConnection(connection);
        }
    }

    @Override
    public void doTransaction(TransactionCallback callback) {
		getTransaction(false).execute(callback);
    }

	@Override
    public <T> T doTransaction(TransactionCallbackWithResult<T> callback) {
	    return getTransaction(false).executeWithResult(callback);
    }
	
	@Override
    public void doTransaction(TransactionCallback callback,boolean requiresNew) {
		getTransaction(requiresNew).execute(callback);
    }

	@Override
    public <T> T doTransaction(TransactionCallbackWithResult<T> callback, boolean requiresNew) {
	    return getTransaction(requiresNew).executeWithResult(callback);
    }
	
	@Override
    public void doTransaction(TransactionCallback callback, TransactionDefinition td) {
		Args.notNull(td,"transaction definition");
		getTransaction(td).execute(callback);	    
    }

	@Override
    public <T> T doTransaction(TransactionCallbackWithResult<T> callback, TransactionDefinition td) {
		Args.notNull(td,"transaction definition");
		return getTransaction(td).executeWithResult(callback);
    }

    public void setDefaultPropagation(Propagation defaultPropagation) {
        this.defaultPropagation = defaultPropagation;
    }

    public void setDefaultIsolation(Isolation defaultIsolation) {
        this.defaultIsolation = defaultIsolation;
    }

    protected TransactionDefinition getRequiredDefinition() {
		if(null == requiredDefinition) {
			requiredDefinition = new SimpleTransactionDefinition();
			requiredDefinition.setPropagation(Propagation.REQUIRED);
			requiredDefinition.setIsolation(this.defaultIsolation);
		}
		return requiredDefinition;
	}
	
	protected TransactionDefinition getRequiresNewDefinition() {
		if(null == requiresNewDefinition) {
			requiresNewDefinition = new SimpleTransactionDefinition();
			requiresNewDefinition.setPropagation(Propagation.REQUIRES_NEW);
			requiresNewDefinition.setIsolation(this.defaultIsolation);
		}
		return requiresNewDefinition;
	}

	/**
	 * Return a currently active transaction or create a new one.
	 */
    protected LocalTransaction getTransaction(boolean requiresNew) {
    	return requiresNew ? getTransaction(getRequiresNewDefinition()) : getTransaction(getRequiredDefinition());
    }
    
	/**
	 * Return a currently active transaction or create a new one.
	 */
    protected LocalTransaction getTransaction(TransactionDefinition td) {
    	if(td.getPropagation() == Propagation.REQUIRES_NEW) {
    		log.debug("Force to Create a new Transaction");
    		LocalTransaction trans = new LocalTransaction(this,td);
    		pushActiveTransaction(trans);
    		return trans;
    	}else{
    		LocalTransaction trans = peekActiveTransaction();
    		if(null == trans){
    			log.debug("No Active Transaction, Creates a new one");
    			trans = new LocalTransaction(this,td);
    			pushActiveTransaction(trans);
    		}else{
    			log.debug("Returns an Nested Transaction");
    		}
            return trans;
    	}
    }

    protected Connection getConnection() throws NestedSQLException {
		LocalTransaction transaction = peekActiveTransaction();
		
        if(transaction == null){
            log.debug("Fetching JDBC Connection from DataSource Non Transactional");
            return fetchConnectionFromDataSource();
        }
        
        if(!transaction.hasConnection()){
        	log.debug("Fetching JDBC Connection from DataSource Transactional");
        	transaction.setConnection(fetchConnectionFromDataSource());
        }
        
        return transaction.getConnection();
    }

    protected boolean closeConnection(Connection connection) {
		if(null == connection){
			return false;
		}
		
		LocalTransaction transaction = peekActiveTransaction();
		if(null != transaction && connectionEquals(transaction, connection)){
			return false;
		}
		
		log.debug("Returning JDBC Connection to DataSource");
		returnConnectionToDataSource(connection);
		return true;
    }
	
	protected LocalTransaction peekActiveTransaction() {
		Stack<LocalTransaction> trans = currentTransactions.get();
		if(null == trans || trans.empty()) {
			return null;
		}else{
			return trans.peek();
		}
	}
	
	protected void pushActiveTransaction(LocalTransaction tran) {
		Stack<LocalTransaction> trans = currentTransactions.get();
		if(null == trans) {
			trans = new Stack<>();
			currentTransactions.set(trans);
		}
		trans.push(tran);
	}
	
	protected LocalTransaction removeActiveTransaction() {
		Stack<LocalTransaction> trans = currentTransactions.get();
		if(null == trans || trans.empty()) {
			throw new IllegalStateException("No active transaction, cannot remove it");
		}
		return trans.pop();
	}
	
	protected Connection fetchConnectionFromDataSource() {
		try {
	        return dataSource.getConnection();
        } catch (SQLException e) {
        	throw new NestedSQLException(e);
        }
	}
	
	protected void returnConnectionToDataSource(Connection connection){
		JDBC.closeConnection(connection);
	}
	
	protected boolean connectionEquals(LocalTransaction transaction, Connection passedInConn){
		Connection holdedConn = transaction.getConnection();
		
		if(null == holdedConn){
			return false;
		}
	
		// Explicitly check for identity too: for Connection handles that do not implement
		// "equals" properly, such as the ones Commons DBCP exposes).		
		return holdedConn == passedInConn || holdedConn.equals(passedInConn);
	}

}