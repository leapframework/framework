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
import leap.core.transaction.TransactionDefinition.IsolationLevel;
import leap.core.transaction.TransactionDefinition.PropagationBehaviour;
import leap.core.validation.annotations.NotNull;
import leap.lang.Args;
import leap.lang.exception.NestedSQLException;
import leap.lang.jdbc.JDBC;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Stack;

public class DefaultTransactionManager extends AbstractReadonlyBean implements TransactionManager {
	
	private static final Log log = LogFactory.get(DefaultTransactionManager.class);
	
	private final ThreadLocal<Stack<DefaultTransaction>> currentTransactions = new ThreadLocal<>();
	
	protected @NotNull DataSource  dataSource;
	protected PropagationBehaviour defaultPropagationBehaviour = PropagationBehaviour.REQUIRED;
	protected IsolationLevel       defaultIsolationLevel       = IsolationLevel.DEFAULT;
	
	public DefaultTransactionManager(){
		
	}
	
	public DefaultTransactionManager(DataSource dataSource){
		this.dataSource = dataSource;
	}
	
	public void setDataSource(DataSource dataSource) {
		checkReadonly();
		this.dataSource = dataSource;
	}
	
	public void setDefaultPropagationBehaviour(PropagationBehaviour defaultPropagationBehaviour) {
		this.defaultPropagationBehaviour = defaultPropagationBehaviour;
	}

	public void setDefaultIsolationLevel(IsolationLevel defaultIsolationLevel) {
		this.defaultIsolationLevel = defaultIsolationLevel;
	}

	@Override
    public void doTransaction(TransactionCallback callback) {
		getTransaction(false).execute(callback);
    }

	@Override
    public <T> T doTransaction(TransactionCallbackWithResult<T> callback) {
	    return getTransaction(false).execute(callback);
    }
	
	@Override
    public void doTransaction(TransactionCallback callback,boolean requiresNew) {
		getTransaction(requiresNew).execute(callback);
    }

	@Override
    public <T> T doTransaction(TransactionCallbackWithResult<T> callback, boolean requiresNew) {
	    return getTransaction(requiresNew).execute(callback);
    }
	
	@Override
    public void doTransaction(TransactionCallback callback, TransactionDefinition td) {
		Args.notNull(td,"transaction definition");
		getTransaction(td).execute(callback);	    
    }

	@Override
    public <T> T doTransaction(TransactionCallbackWithResult<T> callback, TransactionDefinition td) {
		Args.notNull(td,"transaction definition");
		return getTransaction(td).execute(callback);
    }
	
	private DefaultTransactionDefinition requiredDefinition = null;
	private DefaultTransactionDefinition requiresNewDefinition = null;
	
	protected TransactionDefinition getRequiredDefinition() {
		if(null == requiredDefinition) {
			requiredDefinition = new DefaultTransactionDefinition();
			requiredDefinition.setPropagationBehavior(PropagationBehaviour.REQUIRED);
			requiredDefinition.setIsolationLevel(this.defaultIsolationLevel);
		}
		return requiredDefinition;
	}
	
	protected TransactionDefinition getRequiresNewDefinition() {
		if(null == requiresNewDefinition) {
			requiresNewDefinition = new DefaultTransactionDefinition();
			requiresNewDefinition.setPropagationBehavior(PropagationBehaviour.REQUIRES_NEW);
			requiresNewDefinition.setIsolationLevel(this.defaultIsolationLevel);
		}
		return requiresNewDefinition;
	}

	/**
	 * Return a currently active transaction or create a new one.
	 */
    protected Transaction getTransaction(boolean requiresNew) {
    	return requiresNew ? getTransaction(getRequiresNewDefinition()) : getTransaction(getRequiredDefinition());
    }
    
	/**
	 * Return a currently active transaction or create a new one.
	 */
    protected Transaction getTransaction(TransactionDefinition td) {
    	if(td.getPropagationBehavior() == PropagationBehaviour.REQUIRES_NEW) {
    		log.debug("Force to Create a new Transaction");
    		DefaultTransaction trans = new DefaultTransaction(td);
    		pushActiveTransaction(trans);
    		return trans;
    	}else{
    		DefaultTransaction trans = peekActiveTransaction();
    		if(null == trans){
    			log.debug("No Active Transaction, Creates a new one");
    			trans = new DefaultTransaction(td);
    			pushActiveTransaction(trans);
    		}else{
    			log.debug("Returns an Active Transaction");
    		}
    		return trans;
    	}
    }

	@Override
    public Connection getConnection() throws NestedSQLException {
		DefaultTransaction transaction = peekActiveTransaction();
		
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

	@Override
    public boolean closeConnection(Connection connection) {
		if(null == connection){
			return false;
		}
		
		DefaultTransaction transaction = peekActiveTransaction();
		if(null != transaction && connectionEquals(transaction, connection)){
			return false;
		}
		
		log.debug("Returning JDBC Connection to DataSource");
		returnConnectionToDataSource(connection);
		return true;
    }
	
	protected DefaultTransaction peekActiveTransaction() {
		Stack<DefaultTransaction> trans = currentTransactions.get();
		if(null == trans || trans.empty()) {
			return null;
		}else{
			return trans.peek();
		}
	}
	
	protected void pushActiveTransaction(DefaultTransaction tran) {
		Stack<DefaultTransaction> trans = currentTransactions.get();
		if(null == trans) {
			trans = new Stack<>();
			currentTransactions.set(trans);
		}
		trans.push(tran);
	}
	
	protected DefaultTransaction removeActiveTransaction() {
		Stack<DefaultTransaction> trans = currentTransactions.get();
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
	
	protected boolean connectionEquals(DefaultTransaction transaction,Connection parssedInConn){
		Connection holdedConn = transaction.getConnection();
		
		if(null == holdedConn){
			return false;
		}
	
		// Explicitly check for identity too: for Connection handles that do not implement
		// "equals" properly, such as the ones Commons DBCP exposes).		
		return holdedConn == parssedInConn || holdedConn.equals(parssedInConn);
	}
	
	protected class DefaultTransaction implements Transaction,TransactionStatus {

		private int 	   referenceCount = 0;
		private Connection connection;
		private boolean    rollbackOnly;
		
		private boolean	   originalAutoCommit;
		private int		   originalIsolationLevel;
		
		private final int isolationLevel;
		
		protected DefaultTransaction() {
	        this.isolationLevel = DefaultTransactionManager.this.defaultIsolationLevel.getValue();
        }
		
		protected DefaultTransaction(TransactionDefinition td) {
	        this.isolationLevel = td.getIsolationLevel() == null ? 
	        						DefaultTransactionManager.this.defaultIsolationLevel.getValue() :
	        						td.getIsolationLevel().getValue();
        }
		
		public int getIsolationLevel() {
			return isolationLevel;
		}

		@Override
        public boolean isNewTransaction() {
	        return referenceCount <= 1;
        }

		@Override
        public void setRollbackOnly() {
			log.debug("Transaction set to rollback-only");
	        rollbackOnly = true;
        }

		@Override
        public boolean isRollbackOnly() {
	        return rollbackOnly;
        }

		@Override
        public boolean isCompleted() {
	        return connection == null;
        }

		public boolean hasConnection(){
			return null != connection;
		}
		
		public Connection getConnection() {
			return connection;
		}

		protected void setConnection(Connection connection) {
			try {
				this.originalAutoCommit     = connection.getAutoCommit();
            	connection.setAutoCommit(false);
            } catch (SQLException e) {
            	returnConnectionToDataSource(connection);
            	throw new NestedSQLException("Error setting connection's 'autoCommit'" + e.getMessage(), e);
            }		
			
			if(isolationLevel != IsolationLevel.DEFAULT.getValue()) {
				try{
					this.originalIsolationLevel = connection.getTransactionIsolation();
	            	if(this.isolationLevel != this.originalIsolationLevel) {
	            		connection.setTransactionIsolation(this.isolationLevel);
	            	}
				}catch(SQLException e){
	            	returnConnectionToDataSource(connection);
	            	throw new NestedSQLException("Error setting connection's 'transactionIsolaction'" + e.getMessage(), e);
				}
			}
			
			this.connection = connection;
		}

		@Override
        public void execute(TransactionCallback callback) {
			begin();

			try{
				callback.doInTransaction(this);
			}catch(Throwable e) {
				setRollbackOnly();
				log.warn("Error executing transaction, auto rollback", e);
				
				if(e instanceof RuntimeException) {
					throw (RuntimeException)e;
				}else{
					throw new TransactionException("Error executing transaction, " + e.getMessage(), e);
				}
			}finally{
				complete();				
			}
        }

		@Override
        public <T> T execute(TransactionCallbackWithResult<T> callback) {
			begin();

			try{
				return callback.doInTransaction(this);
			}catch(Throwable e) {
				setRollbackOnly();
				log.warn("Error executing transaction, auto rollback", e);
				
				if(e instanceof RuntimeException) {
					throw (RuntimeException)e;
				}else{
					throw new TransactionException("Error executing transaction, " + e.getMessage(), e);
				}
			}finally{
				complete();	
			}
        }
		
		protected void begin() {
			increase();
			
			if(log.isDebugEnabled()){
				if(isNewTransaction()){
					log.debug("Begin a new transaction, referencedCount={}",referenceCount);	
				}else{
					log.debug("Join a exists transaction, referencedCount={}",referenceCount);
				}
			}
		}

		protected void complete() {
			decrease();
			
			if(referenceCount == 0) {
				try{
					//Connection may be null if no database access in transaction.
					if(null != connection){
						if(rollbackOnly){
							try {
								log.debug("Rollback transaction, referencedCount={}",referenceCount);
				                connection.rollback();
				                connection.setAutoCommit(originalAutoCommit); //Notice: must do the rollback before setAutoCommit
			                } catch (SQLException e) {
			                	log.warn("Error rollback transaction, " + e.getMessage(), e);
			                }
						}else{
							try {
								log.debug("Commit transaction, referencedCount={}",referenceCount);
			                    connection.commit();
			                    connection.setAutoCommit(originalAutoCommit);
			                    
			                    if(isolationLevel != IsolationLevel.DEFAULT.getValue() && 
			                       isolationLevel != originalIsolationLevel) {
			                    	connection.setTransactionIsolation(isolationLevel);
			                    }
		                    } catch (SQLException e) {
		                    	throw new TransactionException("Error commit transaction, " + e.getMessage(), e);
		                    } 
						}
					}
				}finally{
					try{
						removeActiveTransaction();
					}finally{
						closeConnection(connection);	
					}
				}
			}else{
				log.debug("Exit transaction(no rollback or commit), referencedCount={}, rollbackOnly={}", referenceCount,rollbackOnly);
			}
		}
		
		/**
		 * Increase the reference count by one because the connection has been requested
		 */
		protected void increase() {
			this.referenceCount++;
		}
		
		/**
		 * Decrease the reference count by one because the connection has been released
		 */
		protected void decrease() {
			this.referenceCount--;
		}
	
		public int getReferenceCount() {
			return referenceCount;
		}
	}
}