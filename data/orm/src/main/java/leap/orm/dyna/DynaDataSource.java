/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.orm.dyna;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class DynaDataSource implements DataSource {

    private final ThreadLocal<DataSource> current = new InheritableThreadLocal<>();
    private final DataSource defaultDataSource;

    public DynaDataSource() {
        this(null);
    }

    public DynaDataSource(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    public DataSource getDefaultDataSource() {
        return defaultDataSource;
    }

    public DataSource getCurrentDataSource() {
        return current.get();
    }

    public void exec(DataSource ds, Runnable action) {
        try {
            current.set(ds);
            action.run();
        }finally {
            current.remove();
        }
    }

    public <T> T execWithResult(DataSource ds, Supplier<T> func) {
        try {
            current.set(ds);
            return func.get();
        }finally {
            current.remove();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return ds().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return ds().getConnection(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return ds().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return ds().isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return ds().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        ds().setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        ds().setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return ds().getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return ds().getParentLogger();
    }

    protected DataSource ds() {
        DataSource ds = current.get();
        return null == ds ? defaultDataSource : ds;
    }
}