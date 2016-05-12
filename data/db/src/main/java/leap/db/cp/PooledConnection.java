/*
 * Copyright 2015 the original author or authors.
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
package leap.db.cp;

import leap.lang.jdbc.ConnectionWrapper;
import leap.lang.jdbc.JDBC;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.logging.StackTraceStringBuilder;

import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PooledConnection extends ConnectionWrapper implements Connection {
	private static final Log log = LogFactory.get(PooledConnection.class);
	
	static final int STATE_IDLE = 0;
	static final int STATE_BUSY = 1;
	static final int STATE_CLEANUP = 2;
    static final int STATE_ABANDON = 3;
	
	private static final int TRANSACTION_STATE_INIT     = 0;
	private static final int TRANSACTION_STATE_COMMIT   = 1;
	private static final int TRANSACTION_STATE_ROLLBACK = 1;
	
	private final Pool			 pool;
	private final PoolConfig     poolConfig;
	private final AtomicInteger  state;
	private final PoolUtils 	 utils;
	private final StatementList	 statements = new StatementList();
	
	private boolean   			  newCreatedConnection;
	private int		  			  transactionState = TRANSACTION_STATE_INIT;
	private long      			  lastBusyTime;
	private long	  			  lastIdleTime;
	private Thread				  threadOnBorrow;
	
	private String realCatalog;
	private int	   realTransactionIsolation;
	
	PooledConnection(Pool pool) {
		super(null);
		this.pool       = pool;
		this.poolConfig = pool.getConfig();
		this.state      = new AtomicInteger(STATE_IDLE);
		this.utils      = pool.utils();
	}
	
	void setupBeforeOnBorrow() {
		//stackTraceExceptionOnBorrow = new IllegalStateException("");
		threadOnBorrow   = Thread.currentThread();
		transactionState = TRANSACTION_STATE_INIT;
	}
	
	void setupAfterOnBorrow() {
		lastBusyTime = System.currentTimeMillis();
	}
	
	void setupOnReturn() {
		if(null != conn) {
			lastIdleTime = System.currentTimeMillis();
		}
		statements.reset();
	}
	
	long getBusyDurationMs() {
		return System.currentTimeMillis() - lastBusyTime;
	}
	
	boolean isActive() {
		return state.get() == STATE_BUSY && null != conn;
	}
	
	boolean isIdle() {
		return null != conn && state.get() == STATE_IDLE;
	}
	
	boolean isLeakTimeout() {
		if(state.get() != STATE_BUSY ) {
			return false;
		}

		if(poolConfig.getConnectionLeakTimeoutMs() > 0 && lastBusyTime > 0) {
			if(getBusyDurationMs() >= poolConfig.getConnectionLeakTimeoutMs()) {
				return true;
			}
		}
		
		return false;
	}
	
	boolean isIdleTimeout() {
		if(null == conn || state.get() != STATE_IDLE) {
			return false;
		}
		
		if(lastIdleTime > 0) {
			if(System.currentTimeMillis() - lastIdleTime >= poolConfig.getIdleTimeoutMs()) {
				return true;
			}
		}
		
		return false;
	}
	
	public Thread getThreadOnBorrow() {
		return threadOnBorrow;
	}
	
	public StackTraceElement[] getStackTraceOfThreadOnBorrow() {
		return threadOnBorrow.getStackTrace();
	}

	AtomicInteger getState() {
		return state;
	}
	
	Connection getReal() {
		return conn;
	}
	
	void setReal(Connection conn) {
		this.conn = conn;
	}

	public boolean isNewCreatedConnection() {
		return newCreatedConnection;
	}

	public void setNewCreatedConnection(boolean newCreatedConnection) {
		this.newCreatedConnection = newCreatedConnection;
	}

	public boolean isValid() {
		if(null == conn) {
			return false;
		}
		
		final int validationTimeout = poolConfig.getValidationTimeout();
		
        if(utils.supportsJdbc4Validation(conn)) {
    		try {
    			return conn.isValid(validationTimeout);
            } catch (SQLException e) {
            	log.info("Error validating connection use Connection.isValid(), {}", e.getMessage(), e);
            	return false;
            }
        }
        
        if(poolConfig.hasValidationQuery()) {
        	
        	 final int originalTimeout = utils.getAndSetNetworkTimeout(conn, poolConfig.getValidationTimeout());

             try {
				try (Statement statement = conn.createStatement()) {
					utils.setQueryTimeout(statement, validationTimeout);
					statement.executeQuery(poolConfig.getValidationQuery());
				}
            } catch (SQLException e) {
            	log.info("Error validating connection use validationQuery : {} , {}", poolConfig.getValidationQuery(), e.getMessage(), e);
            	return false;
            }

            utils.setNetworkTimeout(conn, originalTimeout);
        }else{
        	log.warn("Cannot validate the connection, 'validationQuery' not specified");
        }
		
		return true;
	}
	
	public String getRealCatalog() {
		return realCatalog;
	}

	public void setRealCatalog(String realCatalog) {
		this.realCatalog = realCatalog;
	}

	public int getRealTransactionIsolation() {
		return realTransactionIsolation;
	}

	public void setRealTransactionIsolation(int realTransactionCatalog) {
		this.realTransactionIsolation = realTransactionCatalog;
	}

	boolean compareStateAndSet(int expectState, int updateState) {
		return state.compareAndSet(expectState, updateState);
	}

    /**
     * Sets the state to {@link #STATE_ABANDON}.
     */
    void markAbandon() {
        state.set(STATE_ABANDON);
    }

	void abandonReal() {
		if(null != conn) {
			log.debug("Abandon the real connection");
			JDBC.closeConnection(conn);
			conn = null;
		}
	}
	
	void closeReal() {
		if(null != conn) {
			log.debug("Close the real connection");
			JDBC.closeConnection(conn);
			conn = null;
		}
	}
	
	void checkDisconnectAndAbandon(SQLException e) {
		if(utils.db().getDialect().isDisconnectSQLState(e.getSQLState())){
			log.info("Connection was disconnect on statement close, abandon it");
			abandonReal();
		}
	}
	
	void closeStatement(Statement proxy,Statement real) throws SQLException {
        if(((ProxyStatement)proxy).isClosed()) {
            log.warn("Invalid state, the proxy statement already closed", new Exception("stack"));
            return;
        }

		try{
			real.close();	
			successCloseStatement(proxy);
		}catch(SQLException e) {
			errorCloseStatement(proxy, e);
			throw e;
		}finally{
			if(!statements.remove(proxy)){
				log.error("Invalid state, No open statement found for the closed statement",new Exception("stack"));
			}
		}
	}
	
	void closeStatementOnly(Statement proxy,Statement real) throws SQLException {
        if(((ProxyStatement)proxy).isClosed()) {
            log.warn("Invalid state, the proxy statement already closed", new Exception("stack"));
            return;
        }

		try{
			real.close();	
			successCloseStatement(proxy);
		}catch(SQLException e) {
			errorCloseStatement(proxy, e);
			throw e;
		}
	}
	
	private void successCloseStatement(Statement stmt) {
		//do nothing.
	}
	
	private void errorCloseStatement(Statement stmt, SQLException e) {
		checkDisconnectAndAbandon(e);
	}
	
	@Override
	public void close() throws SQLException {
		pool.returnConnection(this);
	}
	
	@Override
	public boolean isClosed() throws SQLException {
		return state.get() == STATE_IDLE;
	}
	
	public boolean isCommitOrRollback() {
		return transactionState > TRANSACTION_STATE_INIT;
	}
	
	@Override
    public void rollback() throws SQLException {
		transactionState = TRANSACTION_STATE_ROLLBACK;
	    super.rollback();
    }

	@Override
    public void rollback(Savepoint savepoint) throws SQLException {
		transactionState = TRANSACTION_STATE_ROLLBACK;
	    super.rollback(savepoint);
    }

	@Override
    public void commit() throws SQLException {
		transactionState = TRANSACTION_STATE_COMMIT;
	    super.commit();
    }

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return new ProxyDatabaseMetadata(this, conn.getMetaData());
	}
	
	@Override
	public Statement createStatement() throws SQLException {
		return createStatementProxy(conn.createStatement());
	}
	
	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		return createStatementProxy(conn.createStatement(resultSetType, resultSetConcurrency));
	}
	
	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return createStatementProxy(conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return createPreparedStatementProxy(conn.prepareStatement(sql), sql);
	}
	
	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		return createPreparedStatementProxy(conn.prepareStatement(sql, autoGeneratedKeys), sql);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		return createPreparedStatementProxy(conn.prepareStatement(sql, columnIndexes), sql);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		return createPreparedStatementProxy(conn.prepareStatement(sql, columnNames), sql);
	}
	
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return createPreparedStatementProxy(conn.prepareStatement(sql, resultSetType, resultSetConcurrency), sql);
	}
	
	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return createPreparedStatementProxy(conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql);
	}
	
	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		return createCallableStatementProxy(conn.prepareCall(sql), sql);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		return createCallableStatementProxy(conn.prepareCall(sql, resultSetType, resultSetConcurrency), sql);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		return createCallableStatementProxy(conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql);
	}
	
	ProxyStatement createProxy(Statement stmt) {
		if(stmt instanceof PreparedStatement) {
			return createPreparedStatementProxy((PreparedStatement)stmt, null);
		}
		
		if(stmt instanceof CallableStatement) {
			return createCallableStatementProxy((CallableStatement)stmt, null);
		}
		
		return createStatementProxy(stmt);
	}
	
	private void setupStatement(Statement stmt) {
		if(poolConfig.getStatementTimeout() > 0) {
			utils.setQueryTimeout(stmt, poolConfig.getStatementTimeout());
		}
	}
	
	private ProxyStatement createStatementProxy(Statement stmt) {
		setupStatement(stmt);
		ProxyStatement proxy = new ProxyStatement(this, stmt);
		statements.add(proxy);
		return proxy;
	}
	
	private ProxyPreparedStatement createPreparedStatementProxy(PreparedStatement ps, String sql) {
		setupStatement(ps);
		ProxyPreparedStatement proxy = new ProxyPreparedStatement(this, ps, sql);
		statements.add(proxy);
		return proxy;
	}
	
	private ProxyCallableStatement createCallableStatementProxy(CallableStatement cs, String sql) {
		setupStatement(cs);
		ProxyCallableStatement proxy = new ProxyCallableStatement(this, cs, sql);
		statements.add(proxy);
		return proxy;
	}
	
	private final class StatementList {
		
		static final int DEFAULT_CAPACITY = 16; 
		static final int MAX_CAPACITY     = DEFAULT_CAPACITY * 2;
		
		private ProxyStatement[] array;
		private int				 length;
		
		public StatementList() {
			this(DEFAULT_CAPACITY);
		}
		
		public StatementList(int capacity) {
			array = new ProxyStatement[capacity];
		}
		
		public void add(ProxyStatement stmt) {
			try {
	            array[length++] = stmt;
            } catch (ArrayIndexOutOfBoundsException e) {
            	int oldLength = array.length;
            	int newLength = oldLength + DEFAULT_CAPACITY;
            	
            	final ProxyStatement[] newArray = new ProxyStatement[newLength];
                System.arraycopy(array, 0, newArray, 0, oldLength);
                newArray[length - 1] = stmt;
                array = newArray;
            }
		}
		
		public boolean remove(Statement stmt) {
			for (int index = length - 1; index >= 0; index--) {
				if (stmt == array[index]) {
					final int numMoved = length - index - 1;
					if (numMoved > 0) {
						System.arraycopy(array, index + 1, array, index, numMoved);
					}
					array[--length] = null;
					return true;
				}
			}
			return false;
		}
		
		public void reset() {
			for(int i=0;i<length;i++) {
				ProxyStatement stmt = array[i];
				if(null != stmt) {
					try {
						log.warn("A potential statement leak detected, force to close it, last executed sql : \n\n{}\n{}",
								stmt.getLastExecutingSql(),
								new StackTraceStringBuilder(null, stmt.getStackTraceOfThreadOnCreate()).toString());
	                    closeStatementOnly(stmt, stmt.getReal());
                    } catch (SQLException e) {
                    	;
                    } finally {
                    	array[i] = null;
                    }
				}
			}
			
			if(array.length > MAX_CAPACITY) {
				array = new ProxyStatement[DEFAULT_CAPACITY];
			}
			
			this.length = 0;
		}
	}
	
}