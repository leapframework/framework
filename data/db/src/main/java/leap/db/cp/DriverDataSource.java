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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;

import javax.sql.DataSource;

import leap.lang.Strings;
import leap.lang.exception.NestedSQLException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

public final class DriverDataSource implements DataSource {

    private static final Log log = LogFactory.get(DriverDataSource.class);

	public static final String USERNAME_PROPERTY = "user";
	public static final String PASSWORD_PROPERTY = "password";
	
	public static final String SUB_PROTOCOL_ORACLE = "oracle";
	
	private final String	 jdbcUrl;
	private final String 	 driverClassName;
	private final Properties driverProperties;
	private final Driver	 driver;
	
	public DriverDataSource(String driverClassName, String jdbcUrl, String username, String password) {
		this(driverClassName,jdbcUrl,username,password,null);
	}

	public DriverDataSource(String driverClassName, String jdbcUrl, String username, String password, Properties properties) {
		try {
			this.jdbcUrl = jdbcUrl;
			this.driverProperties = new Properties();

			if(null != properties) {
				for (Entry<Object, Object> entry : properties.entrySet()) {
					driverProperties.setProperty(entry.getKey().toString(), entry.getValue().toString());
				}
			}

			if (username != null) {
				driverProperties.put(USERNAME_PROPERTY, driverProperties.getProperty(USERNAME_PROPERTY, username));
			}
			
			if (password != null) {
				driverProperties.put(PASSWORD_PROPERTY, driverProperties.getProperty(PASSWORD_PROPERTY, password));
			}
			this.driverClassName = driverClassName;
			preGetDriver();
			if (driverClassName != null) {
				try {
					Class.forName(driverClassName);
				} catch (ClassNotFoundException e) {
					throw new SQLException("Driver class name " + driverClassName + " not found " + e);
				}
				
				Driver matched = DriverManager.getDriver(jdbcUrl);
				if(null == matched) {
					Enumeration<Driver> drivers = DriverManager.getDrivers();
					while (drivers.hasMoreElements()) {
						Driver d = drivers.nextElement();
						if (d.getClass().getName().equals(driverClassName)) {
							matched = d;
							break;
						}
					}
				}

				if (matched != null) {
					driver = matched;
				} else {
					throw new IllegalArgumentException("Driver class name " + driverClassName + " was not found in registered drivers");
				}
			} else {
				driver = DriverManager.getDriver(jdbcUrl);
			}
		} catch (SQLException e) {
			throw new NestedSQLException("Unable to get driver for JDBC URL " + jdbcUrl, e);
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
        log.debug("{} : Connecting...", jdbcUrl);
        Connection conn = driver.connect(jdbcUrl, driverProperties);
        log.debug("{} : Connected!!!", jdbcUrl);
		return conn;
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return getConnection();
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setLogWriter(PrintWriter logWriter) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		DriverManager.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return DriverManager.getLoginTimeout();
	}

	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return driver.getParentLogger();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}
	
	protected String getSubProtocol(){
		if(Strings.isEmpty(this.jdbcUrl)){
			return null;
		}
		String protocol = "jdbc:";
		int index = this.jdbcUrl.indexOf(protocol);
		if(index < 0){
			return null;
		}
		String uri = this.jdbcUrl.substring(protocol.length());
		index = uri.indexOf(":");
		if(index < 0){
			return null;
		}
		String subProtocol = uri.substring(0,index);
		return subProtocol;
	}
	
	protected boolean isOracleUrl(){
		String subProtocol = getSubProtocol();
		return SUB_PROTOCOL_ORACLE.equals(subProtocol);
	}
	
	protected void preGetDriver(){
		if(isOracleUrl()){
			String remarksReporting = driverProperties.getProperty("remarksReporting");
			if(remarksReporting == null){
				driverProperties.setProperty("remarksReporting","true");
			}
		}
	}
	
}