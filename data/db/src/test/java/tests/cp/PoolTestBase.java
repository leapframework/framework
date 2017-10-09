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
package tests.cp;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import leap.core.junit.AppTestBase;
import leap.db.cp.*;
import tests.cp.mock.MockConnection;
import tests.cp.mock.MockDataSource;
import tests.cp.mock.MockStatement;

public abstract class PoolTestBase extends AppTestBase {
	
	PooledDataSource ds;
	MockDataSource   ms;
	
	@Override
    protected void setUp() throws Exception {
		initDefaultDataSource();
    }
	
	@Override
    protected void tearDown() throws Exception {
		if(null != ds && !ds.isClose()) {
			ds.close();
		}
	}

	protected void initDefaultDataSource() {
		if(null != ds && !ds.isClose()) {
			ds.close();
		}
		ms = new MockDataSource();
		ds = new PooledDataSource(ms);
        ds.setMaxWait(5000);
        ds.setHealthCheckIntervalMs(100);
	}
	
	PooledConnection getConnection() throws SQLException {
		return (PooledConnection) ds.getConnection();
	}
	
	protected static MockConnection real(Connection conn) {
		return (MockConnection)((PooledConnection)conn).wrapped();
	}

}
