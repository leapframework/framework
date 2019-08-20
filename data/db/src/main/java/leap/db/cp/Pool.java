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

import leap.lang.Args;
import leap.lang.Classes;
import leap.lang.SimpleThreadFactory;
import leap.lang.Strings;
import leap.lang.jdbc.JDBC;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.logging.StackTraceStringBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer;

import static leap.db.cp.PooledConnection.*;

class Pool {
	private static final Log log = LogFactory.get(Pool.class);

    static final String FRAMEWORK_PACKAGE = Classes.getPackageName(PooledConnection.class) + ".";

    private static final AtomicInteger poolCounter = new AtomicInteger();
	
	private final PoolFactory                 factory;
	private final PoolConfig                  config;
	private final DataSource                  dataSource;
	private final PoolUtils                   utils;
    private final String                      initSQL;
	private final long                        maxWait;
	private final int                         defaultTransactionIsolationLevel;
	private final SyncPool                    syncPool;
	private final ScheduledThreadPoolExecutor scheduledExecutor;
	
	private volatile String  name;
	private volatile boolean closed = false;
	private boolean initializing = true;

	public Pool(PoolProperties props) throws SQLException {
		Args.notNull(props,"pool properties");
		props.validate();
		
		this.factory    = new PoolFactory(props);
		this.config     = factory.getPoolConfig();
		this.dataSource = factory.getDataSource();
		this.utils 		= new PoolUtils(this);
		this.syncPool   = new SyncPool();
		this.maxWait    = config.getMaxWait();
        this.initSQL    = Strings.trimToNull(props.getInitSQL());
		
		if(config.hasDefaultTransactionIsolation()) {
			this.defaultTransactionIsolationLevel = config.getDefaultTransactionIsolation().getValue();
		}else{
			this.defaultTransactionIsolationLevel = Integer.MIN_VALUE;
		}

        initMinIdleConnections();

        if(config.isHealthCheck()) {
			this.scheduledExecutor = new ScheduledThreadPoolExecutor(1, 
																	 new SimpleThreadFactory(getName() + "_health", true),
																	 new ThreadPoolExecutor.DiscardPolicy());

			this.scheduledExecutor.scheduleAtFixedRate(new HealthWorker(),
													   config.getHealthCheckIntervalMs(),
													   config.getHealthCheckIntervalMs(), TimeUnit.MILLISECONDS);
		}else{
			this.scheduledExecutor = null;
		}
	}
	
	public PoolUtils utils() {
		return utils;
	}
	
	public String getName() {
		if(null == name) {
			name = "CP" + poolCounter.getAndIncrement();
		}
		return name;
	}
	
	public PoolConfig getConfig() {
		return config;
	}
	
	/**
	 * Returns the wrapped {@link DataSource}.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

    public String getStateInfo() {
        State state = syncPool.state();

        StringBuilder s = new StringBuilder();
        s.append("total:").append(state.total)
                .append(", active:").append(state.active)
                .append(", idle:").append(state.idle);

        return s.toString();
    }
	
	/**
	 * Get a connection from the pool, or timeout after {@link #maxWait} milliseconds.
	 */
	public final Connection getConnection() throws SQLException {
		return getConnection(maxWait);
	}

	/**
	 * Get a connection from the pool, or timeout after the specified number of milliseconds.
	 *
	 * @param maxWait the maximum time to wait for a connection from the pool
	 */
	public final Connection getConnection(final long maxWait) throws SQLException {
		if(closed) {
			throw new SQLException("Connection Pool has been closed.");
		}
		
		log.trace("[{}] Borrowing connection...", getName());
		
		final long start = System.currentTimeMillis();

        SQLException se = null;
        PooledConnection conn = null;
		try{
			conn = syncPool.borrowConnection(maxWait);
			if(null != conn) {
				log.trace("[{}] A connection was borrowed from pool, setup and return.", getName());

				if(initializing) {
					if(config.isInitializationFailRetry()) {
						final long retryInterval = config.getInitializationFailRetryIntervalMs();
						int retries = 1;
						for (; ; ) {
							try {
								setupConnectionOnBorrow(conn);
								break;
							} catch (SQLException e) {
								log.error("Initializing failed '{}', retry {}", e.getMessage(), retries++);
								if(log.isDebugEnabled()) {
									log.debug(e);
								}
								Thread.sleep(retryInterval);
							}
						}
					}else {
						setupConnectionOnBorrow(conn);
					}
					initializing = false;
				}else {
					setupConnectionOnBorrow(conn);
				}
				return conn;
			}
		}catch(InterruptedException e) {
			se = new SQLException("Interrupted while connection borrowing");
		}catch(SQLException e) {
            log.warn("Setup connection error: {}", e.getMessage(), e);
            se = e;
        }catch (Throwable e) {
		    /*MySQL
                java.lang.IllegalMonitorStateException: null
                    at com.mysql.jdbc.StatementImpl.executeQuery(StatementImpl.java:1436)
		     */
			se = new SQLException("Setup connection error: " + e.getMessage(), e);
		}

        if(null != se) {
            if(null != conn) {
                log.info("Borrowing connection failed, return it to pool");
                returnConnectionFailed(conn);
            }
            throw se;
        }

		//Timeout
        log.error("[{}] Borrowing connection timeout. [{}]", getName(), getStateInfo());
        printConnections();
		throw new SQLTimeoutException("Timeout after " + (System.currentTimeMillis() - start) + "ms of borrowing a connection");
	}

    protected void printConnections() {
        if(log.isInfoEnabled()) {
            int maxPrint = 20;
            int count = 0;
            for (PooledConnection conn : syncPool.connections()) {
                count++;
                if (count == maxPrint) {
                    break;
                }
                if (conn.isActive()) {
                    log.info("Connection busy duration {}seconds\n{}",
                            conn.getBusyDurationMs() / 1000,
                            new StackTraceStringBuilder(conn.getStackTraceOnOpen()).toString(FRAMEWORK_PACKAGE));
                }
            }
        }
    }

	public boolean isClose() {
		return closed;
	}

	/**
	 * Close this pool, release all the underlying resources.
	 */
	public void close() {
		if(null != scheduledExecutor && !scheduledExecutor.isShutdown()) {
			try {
	            scheduledExecutor.shutdownNow();
            } catch (Throwable e) {
            	log.info("[{}] Error shutdown the scheduled executor, {}",getName(), e.getMessage(), e);
            }
		}
		
		if(closed) {
			log.warn("[{}] Connection Pool has been closed, cannot close again!",getName());
			return;
		}
		
		closed = true;
		syncPool.close();
		log.info("[{}] Connection pool closed!",getName());
	}
	
	public void returnConnection(PooledConnection conn) throws SQLException {
		try{
			setupConnectionOnReturn(conn);
		}finally{
			try{
				conn.setupOnReturn();
			}finally {
				syncPool.returnConnection(conn);
				log.trace("A connection was returned to pool");
			}
		}
	}

    protected void returnConnectionFailed(PooledConnection conn) throws SQLException {
        try {
            conn.closeReal();
        } finally {
            syncPool.returnConnection(conn);
            log.trace("A connection was returned to pool");
        }
    }
	
	@SuppressWarnings("resource")
    protected void setupConnectionOnBorrow(PooledConnection conn) throws SQLException {
		conn.setupBeforeOnBorrow();
		
		Connection wrapped = conn.wrapped();
		if(null == wrapped) {
			//Connection not created yet ( or was abandoned ).
			log.trace("Real Connection not created yet, Create it");
			wrapped = createNewConnectionOnBorrow(conn);

            //todo : test the connection by validation query?.
		}else if(config.isTestOnBorrow() && !conn.isValid()) {
			log.info("Real Connection is invalid, Abandon it and Create a new one");
			conn.abandonReal();
			wrapped = createNewConnectionOnBorrow(conn);
		}else{
			conn.setNewCreatedConnection(false);
		}
		
		setupConnectionStateOnBorrow(conn, wrapped, 1);
		
		conn.setupAfterOnBorrow();
	}
	
	protected Connection createNewConnectionOnBorrow(PooledConnection conn) throws SQLException {
		Connection wrapped = factory.getConnection();

        conn.setWrapped(wrapped);
		conn.setNewCreatedConnection(true);

        if(null != initSQL) {
            log.info("Execute initSQL '{}' on new connection", initSQL);
            try(Statement stmt = wrapped.createStatement()){
                stmt.execute(initSQL);
            }
        }

		return wrapped;
	}
	
	protected void setupConnectionStateOnBorrow(PooledConnection conn, Connection wrapped, int count) throws SQLException {
		try {
	        //Set auto commit.
	        if(wrapped.getAutoCommit() != config.isDefaultAutoCommit()) {
	        	wrapped.setAutoCommit(config.isDefaultAutoCommit());
	        }
	        
	        //Set transaction isolation level.
	        if(config.hasDefaultTransactionIsolation()) {
		        if(defaultTransactionIsolationLevel != wrapped.getTransactionIsolation()) {
		        	wrapped.setTransactionIsolation(defaultTransactionIsolationLevel);
		        }
	        }else {
	        	conn.setRealTransactionIsolation(wrapped.getTransactionIsolation());
	        }
	        
	        //Set default catalog.
	        if(config.hasDefaultCatalog()) {
		        if(!config.getDefaultCatalog().equals(wrapped.getCatalog())) {
		        	wrapped.setCatalog(config.getDefaultCatalog());
		        }
	        }else {
	        	conn.setRealCatalog(wrapped.getCatalog());
	        }

	        //Set default readonly
	        if(config.isDefaultReadOnly() != wrapped.isReadOnly()) {
	        	wrapped.setReadOnly(config.isDefaultReadOnly());
	        }
        } catch (SQLException e) {
            conn.abandonReal();

        	//if the connection is new, throw the exception.
        	//if the connection is old, abandon it and create a new one.
        	if(count > 1 || conn.isNewCreatedConnection()) {
        		log.info("Setup new connection error : {}", e.getMessage(), e);
        		throw e;
        	}else{
        		log.info("Reset old connection error ({}) abandon it and create a new one", e.getMessage(), e);
        		wrapped = createNewConnectionOnBorrow(conn);
        		setupConnectionStateOnBorrow(conn, wrapped, count++);
        	}
        }
	}
	
	protected void setupConnectionOnReturn(PooledConnection conn)  throws SQLException {
		Connection wrapped = conn.wrapped();
		if(null == wrapped) {
			//If pool has been closed, just ignore it.
			if(!closed) {
				log.error("Invalid state, the pooled connection has no underlying connection on return");
			}
			return;
		}
		
		//Reset transaction isolation level
		if(!config.hasDefaultTransactionIsolation() && conn.getRealTransactionIsolation() != wrapped.getTransactionIsolation()) {
			wrapped.setTransactionIsolation(conn.getRealTransactionIsolation());
		}
		
		//Reset catalog
		if(!config.hasDefaultCatalog() && !Strings.equals(conn.getRealCatalog(), wrapped.getCatalog())) {
			conn.setCatalog(conn.getRealCatalog());
		}
		
		if(config.isDefaultAutoCommit() && !conn.isCommitOrRollback() && !conn.getAutoCommit()) {
			
			if(config.isRollbackPendingTransaction()) {
				try{
					log.warn("A potential pending transaction detected, force rollback");
					conn.rollback();
				}catch(SQLException e) {
					log.warn("A SQLException was threw when rollback on return, {}", e.getMessage(), e);
				}
			}
			
			if(config.isThrowPendingTransactionException()) {
				throw new SQLException("A potential pending transaction detected");
			}
		}
		
		try {
			if(null != conn.getWarnings()) {
				conn.clearWarnings();	
			}
        } catch (SQLException e) {
        	log.info("A SQLException was threw on clear the warnings, {}",e.getMessage(),e);
        	conn.checkDisconnectAndAbandon(e);
        }
	}

	//from http://bugs.mysql.com/bug.php?id=75615
	final static class SynchronousExecutor implements Executor {
		public void execute(Runnable command) {
			try {
				command.run();
			} catch (Throwable t) {
				log.warn("Exception executing {}", command.toString(), t);
			}
		}
	}
	
	final static class SimpleThreadFactory implements ThreadFactory {

		private String	threadName;
		private boolean	daemon;

		public SimpleThreadFactory(String threadName, boolean daemon) {
			this.threadName = threadName;
			this.daemon = daemon;
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r, threadName);
			thread.setDaemon(daemon);
			return thread;
		}
	}

    final class State {
        public int total;
        public int active;
        public int idle;
    }

    /**
     * The underlying pool holds all the created connections and sync state.
     */
	final class SyncPool {
		
		private final CopyOnWriteArrayList<PooledConnection> list;
		private final AbstractQueuedLongSynchronizer         synchronizer;
		private final AtomicLong 							 syncState;

		SyncPool() {
			this.list 		  = new CopyOnWriteArrayList<>();
			this.synchronizer = new Synchronizer();
			this.syncState    = new AtomicLong(1); 
			this.init();
		}
		
		List<PooledConnection> connections() {
			return list;
		}
		
        State state() {
            State state = new State();

            for(final PooledConnection conn : list) {
                if(conn.isActive()) {
                    state.total++;
                    state.active++;
                    continue;
                }

                if(conn.isCreated()) {
                    state.total++;
                }
            }

            state.idle = state.total - state.active;
            return state;
        }
		
		int getIdleCount() {

			int count = 0;
			
			for(final PooledConnection conn : list) {
				if(conn.isIdle()) {
					count++;
				}
			}
			
			return count;
		}
		
		/**
		 * Borrow a connection from pool.
		 */
		public PooledConnection borrowConnection(long maxWait) throws InterruptedException{
			
			long timeout = maxWait;
			final long start = System.currentTimeMillis();
			
			do{
		        long waitingState;

		        do{
		        	waitingState = syncState.get();

		        	for (final PooledConnection conn : list) {
		               if (conn.compareStateAndSet(STATE_IDLE, STATE_BUSY)){
		                  return conn;
		               }
		            }
		        	
		        	//decrease the timeout
		        	timeout = maxWait - (System.currentTimeMillis() - start);
		        	if(timeout <= 0L) {
		        		//time out.
		        		return null;
		        	}
		        	
		        	//The sync state will increase if connection returned.
		        	final long newState = syncState.get();
		        	if(waitingState < newState || (waitingState > 0 && newState < 0)) {
		        		continue;
		        	}else{
		        		break;
		        	}
		        }while(true);
				
		        //wait for synchronizer's notification.
				if(!synchronizer.tryAcquireSharedNanos(waitingState, TimeUnit.MILLISECONDS.toNanos(timeout))){
					//time out
					return null;
				}
				
			}while(timeout > 0L);
			
			return null;
		}

		/**
		 * Returns the connection to pool.
		 */
		public void returnConnection(PooledConnection conn) {
			updateToIdleState(conn, STATE_BUSY);
		}
		
		public boolean updateToNotIdleState(PooledConnection conn, int toState) {
			return conn.compareStateAndSet(STATE_IDLE, toState);
		}
		
		public void updateToIdleState(PooledConnection conn, int fromState) {
			if(conn.compareStateAndSet(fromState, STATE_IDLE)) {
				//increase the state,see PoolSynchronizer
				synchronizer.releaseShared(syncState.incrementAndGet());
			}else{
				log.error("Failed to update connection's to 'IDLE', expected {}, but {}",fromState,conn.getState().get());
			}
		}
		
		void close() {
			for(final PooledConnection conn : list) {
				try {
		            conn.closeReal();
	            } catch (Throwable e) {
	            	log.warn("Error closing wrapped connection, {}", e.getMessage(), e);
	            }
			}
		}
		
		private void init() {
			for(int i=0;i<config.getMaxActive();i++) {
				list.add(new PooledConnection(Pool.this));
			}
		}
		
		private final class Synchronizer extends AbstractQueuedLongSynchronizer {

			private static final long serialVersionUID = -5101833107186489746L;

			@Override
	        protected long tryAcquireShared(long state) {
				final long newState = getState();
		        return (newState > state || (state > 0 && newState < 0)) && !hasQueuedPredecessors() ? 1L : -1L;
	        }

			@Override
	        protected boolean tryReleaseShared(long newState) {
				setState(newState);
				return true;
			}
			
		}
	}

    private void initMinIdleConnections() throws SQLException {
        if(config.getMinIdle() <= 0){
            return;
        }

        List<Connection> list = new ArrayList<>();
        try{
            for(int i=0;i<config.getMinIdle();i++) {
                list.add(getConnection());
            }
        }finally{
            list.forEach(JDBC::closeConnection);
        }
    }
	
	final class HealthWorker implements Runnable {

		@Override
        public void run() {

            log.trace("Health check");
			
			for(final PooledConnection conn : syncPool.connections()) {
				
				//cleanup idle timeout connection.
				if(conn.isIdleTimeout() && syncPool.updateToNotIdleState(conn, STATE_CLEANUP)) {
					log.debug("Abandon a connection is idle timeout");
					conn.abandonReal();
					syncPool.updateToIdleState(conn, STATE_CLEANUP);
					continue;
				}
				
				//cleanup leak timeout connection.
				if(conn.isLeakTimeout() && conn.compareStateAndSet(STATE_BUSY, STATE_CLEANUP)) {
					log.error("A potential connection leak detected (busy {}ms)\n{}",
							  conn.getBusyDurationMs(), 
							  new StackTraceStringBuilder(conn.getStackTraceOnOpen()).toString(FRAMEWORK_PACKAGE));
                    conn.markLeak();
					continue;
				}

                //todo : test the underlying connection is valid?
			}
			
			//check max idle and decrease the idle connections.
			if(config.hasMaxIdle() && config.getMaxActive() > config.getMaxIdle() ) {
				int diff = syncPool.getIdleCount() - config.getMaxIdle();
				if(diff > 0) {
					for(int i=0;i<diff;i++) {
						if(syncPool.getIdleCount() - config.getMaxIdle() <= 0) {
							break;
						}
						for(PooledConnection conn : syncPool.connections()) {
							if(syncPool.updateToNotIdleState(conn, STATE_CLEANUP)) {
								log.debug("Close a connection for maxIdle");
								conn.closeReal();
								syncPool.updateToIdleState(conn, STATE_CLEANUP);
								break;
							}
						}
					}
				}
			}
			
			//check min idle and increase the idle connections.
			int idles = syncPool.getIdleCount();
			int diff = config.getMinIdle() - idles;
			if(diff > 0) {
                List<Connection> list = new ArrayList<>();
                try{
                    for(int i=0;i<diff;i++) {
                        if(config.getMinIdle() - syncPool.getIdleCount() <= 0) {
                            break;
                        }
                        try{
                            list.add(getConnection());
                        }catch(SQLException e) {
                            return;
                        }
                    }
                }finally{
                    list.forEach(JDBC::closeConnection);
                }
			}
        }
		
	}
}
