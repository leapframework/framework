/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package leap.core.ds;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.tostring.ToStringBuilder;

/**
 * A very simple implementation of {@link DataSource}, just used for test or debug only.
 */
public class UnPooledDataSource implements DataSource {

	private Properties	driverProperties;
	private boolean	    driverInitialized;

	private String	    driverClassName;
	private String	    jdbcUrl;
	private String	    username;
	private String	    password;

	private Boolean	    defaultAutoCommit;
	private Integer	    defaultTransactionIsolation;

	static {
		DriverManager.getDrivers();
	}

	public UnPooledDataSource() {
		
	}

	public UnPooledDataSource(String driverClassName, String url, String username, String password) {
		this.driverClassName = driverClassName;
		this.jdbcUrl         = url;
		this.username        = username;
		this.password        = password;
	}

	public UnPooledDataSource(String driverClassName, String url, Properties driverProperties) {
		this.driverClassName  = driverClassName;
		this.jdbcUrl              = url;
		this.driverProperties = driverProperties;
	}

	public Connection getConnection() throws SQLException {
		return doGetConnection(username, password);
	}

	public Connection getConnection(String username, String password) throws SQLException {
		return doGetConnection(username, password);
	}

	public void setLoginTimeout(int loginTimeout) throws SQLException {
		DriverManager.setLoginTimeout(loginTimeout);
	}

	public int getLoginTimeout() throws SQLException {
		return DriverManager.getLoginTimeout();
	}

	public void setLogWriter(PrintWriter logWriter) throws SQLException {
		DriverManager.setLogWriter(logWriter);
	}

	public PrintWriter getLogWriter() throws SQLException {
		return DriverManager.getLogWriter();
	}

	public Properties getDriverProperties() {
		return driverProperties;
	}

	public void setDriverProperties(Properties driverProperties) {
		this.driverProperties = driverProperties;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
		synchronized (this) {
			driverInitialized = false;
        }
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String url) {
		this.jdbcUrl = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean isAutoCommit() {
		return defaultAutoCommit;
	}

	public void setDefaultAutoCommit(Boolean autoCommit) {
		this.defaultAutoCommit = autoCommit;
	}

	public Integer getDefaultTransactionIsolation() {
		return defaultTransactionIsolation;
	}

	public void setDefaultTransactionIsolation(Integer defaultTransactionIsolationLevel) {
		this.defaultTransactionIsolation = defaultTransactionIsolationLevel;
	}

	private Connection doGetConnection(String username, String password) throws SQLException {
		Properties props = new Properties(driverProperties);
		if (username != null) {
			props.setProperty("user", username);
		}
		if (password != null) {
			props.setProperty("password", password);
		}
		return doGetConnection(props);
	}

	private Connection doGetConnection(Properties properties) throws SQLException {
		initializeDriver();
		Connection connection = DriverManager.getConnection(jdbcUrl, properties);
		configureConnection(connection);
		return connection;
	}

	private synchronized void initializeDriver() throws SQLException {
		if (!driverInitialized) {
			
			if(Strings.isEmpty(driverClassName)){
				throw new SQLException("'driverClassName' must not be empty");
			}
			
			try {
				Classes.forName(driverClassName);
			} catch (Exception e) {
				throw new SQLException("Error setting driver on UnpooledDataSource. Cause: " + e);
			}
			
			driverInitialized = true;
		}
	}

	private void configureConnection(Connection conn) throws SQLException {
		if (defaultAutoCommit != null && defaultAutoCommit != conn.getAutoCommit()) {
			conn.setAutoCommit(defaultAutoCommit);
		}
		if (defaultTransactionIsolation != null) {
			conn.setTransactionIsolation(defaultTransactionIsolation);
		}
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLException(getClass().getName() + " is not a wrapper.");
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	public Logger getParentLogger() {
		return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); // requires JDK version 1.6
	}

	@Override
    public String toString() {
	    return new ToStringBuilder(this)
	    			.append("driverClassName", driverClassName)
	    			.append("url",jdbcUrl)
	    			.toString();
    }
}
