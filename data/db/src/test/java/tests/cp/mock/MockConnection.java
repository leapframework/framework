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

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import leap.lang.jdbc.ConnectionAdapter;

public class MockConnection extends ConnectionAdapter {
	
	private final MockDataSource dataSource;
	private final String         username;
	private final String		 password;
	
	private boolean		  autoCommit;
	private boolean		  readOnly;
	private int			  transactionIsolation;
	private String		  catalog;
	
	private boolean       closed;
	private MockStatement lastStatement;
	private boolean		  validMethodSuccessCalled;

    private SQLWarning    warning;
	
	public MockConnection(MockDataSource dataSource) {
		this(dataSource, null, null);
	}

	public MockConnection(MockDataSource dataSource,String username, String password) {
		this.dataSource = dataSource;
		this.username   = username;
		this.password   = password;
		
		this.transactionIsolation = dataSource.getDefaultTransactionIsolation();
		this.autoCommit			  = dataSource.isDefaultAutoCommit();
		this.readOnly			  = dataSource.isDefaultReadonly();
		this.catalog			  = dataSource.getDefaultCatalog();
	}
	
	public MockDataSource getDataSource() {
		return dataSource;
	}
	
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	public MockStatement getLastStatement() {
		return lastStatement;
	}
	
	public boolean isValidMethodSuccessCalled() {
		return validMethodSuccessCalled;
	}

	@Override
    public void close() throws SQLException {
		this.closed = true;
		dataSource.closeConnection(this);
	}
	
	public boolean isClosed() {
		return closed;
	}
	
	@Override
    public boolean isValid(int timeout) throws SQLException {

        if(dataSource.isValidateConnectionError()) {
            throw new SQLException("Validate Connection Error!");
        }

		if(!dataSource.isSupportsJdbc4Validation()) {
			throw new SQLException("Not supported");
		}else{
			this.validMethodSuccessCalled = true;
			return true;
		}
    }

    public SQLWarning warning() {
        return warning;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        if(dataSource.isReturnSQLWarnings()) {
            if(null == warning) {
                warning = new SQLWarning("test", "test");
            }
            return warning;
        }
        return super.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        warning = null;
    }

    @Override
    public Statement createStatement() throws SQLException {
		lastStatement = new MockStatement(this);
		return lastStatement;
    }

	@Override
    public DatabaseMetaData getMetaData() throws SQLException {
		return new MockDatabaseMetaData(this);
	}

	@Override
	public String getCatalog() {
		return catalog;
	}

	@Override
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	@Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if(dataSource.isSetAutoCommitError()) {
            AtomicInteger count = dataSource.getSetAutoCommitErrorCount();
            if(count.get() < 0 || count.get() > 0) {
                if(count.get() > 0){
                    count.decrementAndGet();
                }
                throw new SQLException("Set AutoCommit Error!");
            }
        }
		this.autoCommit = autoCommit;
	}

	@Override
    public boolean getAutoCommit() throws SQLException {
		return autoCommit;
	}
	
	@Override
    public void setReadOnly(boolean readOnly) throws SQLException {
		this.readOnly = readOnly;
	}

	@Override
    public boolean isReadOnly() throws SQLException {
		return readOnly;
	}

	@Override
    public void setTransactionIsolation(int level) throws SQLException {
		this.transactionIsolation = level;
	}

	@Override
    public int getTransactionIsolation() throws SQLException {
		return transactionIsolation;
	}
	
}