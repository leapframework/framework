/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package tests.cp.mock;

import leap.lang.Threads;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class MockDataSource implements DataSource {
	
	private final AtomicInteger nrOfOpenedConnections  = new AtomicInteger(0);
	private final AtomicInteger nrOfClosedConnections  = new AtomicInteger(0);
    private final AtomicInteger nrOfOpeningConnections = new AtomicInteger(0);
	
	private String  url                         = MockDriver.JDBC_URL;
	private boolean supportsJdbc4Validation     = true;
	private int	    defaultTransactionIsolation = Connection.TRANSACTION_NONE;
	private boolean defaultReadonly				= false;
	private boolean defaultAutoCommit			= true;
	private String  defaultCatalog				= null;

    private boolean openConnectionError;
    private int     maxOpenConnectionError;
    private AtomicInteger openConnectionErrorCount;
    private boolean setAutoCommitError;
    private AtomicInteger setAutoCommitErrorCount = new AtomicInteger(-1);
    private boolean validateConnectionError;
    private boolean returnSQLWarnings;
    private int     openConnectionWaitMs;

    public int getNrOfOpenedConnections() {
		return nrOfOpenedConnections.get();
	}

	public int getNrOfClosedConnections() {
		return nrOfClosedConnections.get();
	}

    public int getNrOfOpeningConnections() {
        return nrOfOpeningConnections.get();
    }

    public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public int getDefaultTransactionIsolation() {
		return defaultTransactionIsolation;
	}

	public void setDefaultTransactionIsolation(int defaultTransactionIsolation) {
		this.defaultTransactionIsolation = defaultTransactionIsolation;
	}
	
	public String getDefaultCatalog() {
		return defaultCatalog;
	}

	public void setDefaultCatalog(String defaultCatalog) {
		this.defaultCatalog = defaultCatalog;
	}

	public boolean isDefaultReadonly() {
		return defaultReadonly;
	}

	public void setDefaultReadonly(boolean defaultReadonly) {
		this.defaultReadonly = defaultReadonly;
	}

	public boolean isDefaultAutoCommit() {
		return defaultAutoCommit;
	}

	public void setDefaultAutoCommit(boolean defaultAutoCommit) {
		this.defaultAutoCommit = defaultAutoCommit;
	}

	public boolean isSupportsJdbc4Validation() {
		return supportsJdbc4Validation;
	}

	public void setSupportsJdbc4Validation(boolean supportsJdbc4Validation) {
		this.supportsJdbc4Validation = supportsJdbc4Validation;
	}

    public int getOpenConnectionWaitMs() {
        return openConnectionWaitMs;
    }

    public void setOpenConnectionWaitMs(int openConnectionWaitMs) {
        this.openConnectionWaitMs = openConnectionWaitMs;
    }

    @Override
	public Connection getConnection() throws SQLException {
		return getConnection(null, null);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
        if(openConnectionWaitMs > 0) {
            Threads.sleep(openConnectionWaitMs);
        }

        if(openConnectionError) {
            if(maxOpenConnectionError <= 0 || openConnectionErrorCount.incrementAndGet() <= maxOpenConnectionError) {
                throw new SQLException("Open Connection Error!");
            }
        }

		MockConnection connection = new MockConnection(this, username, password);
		
		nrOfOpenedConnections.incrementAndGet();
        nrOfOpeningConnections.incrementAndGet();
		
		return connection;
	}
	
	protected void closeConnection(MockConnection connection) {
		nrOfClosedConnections.incrementAndGet();
        nrOfOpeningConnections.decrementAndGet();
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {

	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {

	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

    public boolean isOpenConnectionError() {
        return openConnectionError;
    }

    public void setOpenConnectionError(boolean openConnectionError) {
        setOpenConnectionError(openConnectionError, 0);
    }

    public void setOpenConnectionError(boolean openConnectionError, int max) {
        this.openConnectionError = openConnectionError;
        this.maxOpenConnectionError = max;
        this.openConnectionErrorCount = new AtomicInteger(0);
    }

    public boolean isSetAutoCommitError() {
        return setAutoCommitError;
    }

    public void setSetAutoCommitError(boolean setAutoCommitError) {
        this.setAutoCommitError = setAutoCommitError;
    }

    public AtomicInteger getSetAutoCommitErrorCount() {
        return setAutoCommitErrorCount;
    }

    public boolean isValidateConnectionError() {
        return validateConnectionError;
    }

    public void setValidateConnectionError(boolean validateConnectionError) {
        this.validateConnectionError = validateConnectionError;
    }

    public boolean isReturnSQLWarnings() {
        return returnSQLWarnings;
    }

    public void setReturnSQLWarnings(boolean returnSQLWarnings) {
        this.returnSQLWarnings = returnSQLWarnings;
    }
}
