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

import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import leap.db.Db;
import leap.db.DbFactory;
import leap.db.cp.Pool.SimpleThreadFactory;
import leap.db.cp.Pool.SynchronousExecutor;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

final class PoolUtils {
	
	private static final Log log = LogFactory.get(PoolUtils.class);
	
	private Pool     pool;
	private Db		 db;
	private Executor networkTimeoutExecutor;
	private Boolean  supportsJdbc4Validation;
	private boolean  supportsNetworkTimeout = true;
	private boolean  supportsQueryTimeout   = true;
	
	public PoolUtils(Pool pool) {
		this.pool = pool;
	}
	
	public Db db() {
		if(null == db) {
			db = DbFactory.createInstance(pool.getDataSource());
		}
		return db;
	}
	
	public boolean supportsJdbc4Validation(Connection connection) {
		if(null == supportsJdbc4Validation) {
			try {
				connection.isValid(5);
				supportsJdbc4Validation = true;
			} catch (Throwable e) {
				supportsJdbc4Validation = false;
				log.debug("JDBC4 Connection.isValid() not supported");
			}
		}
		return supportsJdbc4Validation;
	}
	
	public int getAndSetNetworkTimeout(final Connection connection, final long timeout) {
		if (supportsNetworkTimeout) {
			try {
				final int networkTimeout = connection.getNetworkTimeout();
				connection.setNetworkTimeout(getNetworkTimeoutExecutor(), (int) timeout);
				return networkTimeout;
			} catch (Throwable e) {
				supportsNetworkTimeout = false;
				log.debug("Connection.setNetworkTimeout() not supported");
			}
		}
		return 0;
	}

	public void setNetworkTimeout(final Connection connection, final long timeoutMs) {
		if (supportsNetworkTimeout) {
			try {
				connection.setNetworkTimeout(getNetworkTimeoutExecutor(), (int) timeoutMs);
			} catch (Throwable e) {
				log.debug("Unable to reset network timeout for connection {}", connection.toString(), e);
			}
		}
	}
	
	public void setQueryTimeout(final Statement statement, final int timeoutSec) {
		if (supportsQueryTimeout) {
			try {
				statement.setQueryTimeout(timeoutSec);
			} catch (Throwable e) {
				supportsQueryTimeout = false;
				log.debug("Statement.setQueryTimeout() not supported");
			}
		}
	}
	
	private Executor getNetworkTimeoutExecutor() {
		if(null == networkTimeoutExecutor) {
			createNetworkTimeoutExecutor();
		}
		return networkTimeoutExecutor;
	}
	
	// from HikariCP
	// Temporary hack for MySQL issue: http://bugs.mysql.com/bug.php?id=75615
	private void createNetworkTimeoutExecutor() {
		boolean isMySql = false;
		
		try{
			isMySql = db().isMySql();
		}catch(Throwable e) {
			;
		}
		
		if(isMySql) {
			networkTimeoutExecutor = new SynchronousExecutor();
		}else{
			networkTimeoutExecutor = Executors.newCachedThreadPool(new SimpleThreadFactory("CP - Connection Timeout Executor", true));
			((ThreadPoolExecutor) networkTimeoutExecutor).allowCoreThreadTimeOut(true);
		}
	}
}
