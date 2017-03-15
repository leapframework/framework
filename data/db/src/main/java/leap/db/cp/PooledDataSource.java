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

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Wrapper;
import java.util.Map;
import java.util.logging.Logger;

public class PooledDataSource extends PoolProperties implements DataSource, Closeable {

	private Pool pool;
	
	public PooledDataSource() {
		
	}
	
	public PooledDataSource(Map<String, String> properties){
		Args.notNull(properties,"pool properties");
		properties.forEach((k, v) -> dataSourceProperties.put(k,v));
	}
	
	public PooledDataSource(PoolProperties props) {
		Args.notNull(props,"pool properties");
		this.setProperties(props);
	}
	
	public PooledDataSource(DataSource real) {
		Args.notNull(real, "real datasource");
		this.dataSource = real;
	}

	private Pool pool() throws SQLException {
		if(null == pool) {
			synchronized (this) {
				if(null == pool) {
					pool = new Pool(this);	
				}
            }
		}
		return pool;
	}

    /**
     * Opens the pool and initial connections.
     */
	public void open() throws SQLException {
		if(null != pool) {
			throw new IllegalStateException("Pool already opened!");
		}
		pool();
	}

    /**
     * Close the pool and close all the underlying opened connections.
     */
    @Override
    public void close() {
        if(null != pool) {
            pool.close();
        }
    }

    public boolean isOpen() {
        return null != pool && !pool.isClose();
    }
	
	public boolean isClose() {
		return null != pool && pool.isClose();
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		return pool().getConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		throw new SQLFeatureNotSupportedException("'getConnection(username,password)' not supported");
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return pool().getDataSource() == null ? null : pool.getDataSource().getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		if (pool().getDataSource() != null) {
			pool.getDataSource().setLogWriter(out);
		}
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		if (pool.getDataSource() != null) {
			pool.getDataSource().setLoginTimeout(seconds);
		}
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return pool().getDataSource() == null ? 0 : pool.getDataSource().getLoginTimeout();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException("'getParentLogger()' not supported");
	}

    /**
     * Returns the wrapped {@link DataSource}.
     */
    public <T extends DataSource> T wrapped() {
        return (T)pool.getDataSource();
    }

    @Override
    @SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		if (iface.isInstance(this)) {
			return (T) this;
		}

		if (pool != null) {
			if (iface.isInstance(pool.getDataSource())) {
				return (T) pool.getDataSource();
			}

			if (pool.getDataSource() instanceof Wrapper) {
				return (T) pool.getDataSource().unwrap(iface);
			}
		}

		throw new SQLException("Wrapped DataSource is not an instance of " + iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		if (iface.isInstance(this)) {
			return true;
		}

		if (pool != null) {
			if (iface.isInstance(pool.getDataSource())) {
				return true;
			}

			if (pool.getDataSource() instanceof Wrapper) {
				return pool.getDataSource().isWrapperFor(iface);
			}
		}

		return false;
	}
}
