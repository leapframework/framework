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
package leap.core.ds;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.sql.DataSource;

import leap.core.BeanFactory;
import leap.core.annotation.R;
import leap.core.ioc.PostCreateBean;
import leap.lang.beans.BeanCreationException;
import leap.lang.jndi.JndiLocator;

public class JndiDataSource implements DataSource, PostCreateBean {
	
    private @R String  jndiName;
    private boolean    resourceRef;
    private DataSource jndiDataSource;
	
	public boolean isResourceRef() {
		return resourceRef;
	}

	public void setResourceRef(boolean resourceRef) {
		this.resourceRef = resourceRef;
	}

	public String getJndiName() {
		return jndiName;
	}

	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}
	
	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
		try {
	        this.jndiDataSource = new JndiLocator(resourceRef).lookup(jndiName, DataSource.class);
        } catch (NamingException e) {
        	throw new BeanCreationException("Cannot loolup jndi DataSource '" + jndiName + "', " + e.getMessage(), e);
        }
    }

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return jndiDataSource.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		jndiDataSource.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		jndiDataSource.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return jndiDataSource.getLoginTimeout();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return jndiDataSource.getParentLogger();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return jndiDataSource.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return jndiDataSource.isWrapperFor(iface);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return jndiDataSource.getConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return jndiDataSource.getConnection();
	}
}
