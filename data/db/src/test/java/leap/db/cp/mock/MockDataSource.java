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
package leap.db.cp.mock;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.sql.DataSource;

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

	@Override
	public Connection getConnection() throws SQLException {
		return getConnection(null, null);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
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
}
