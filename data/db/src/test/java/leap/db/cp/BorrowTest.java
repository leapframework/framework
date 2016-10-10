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
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

import leap.db.cp.mock.MockConnection;
import leap.db.cp.mock.MockStatement;

import org.junit.Test;

public class BorrowTest extends ConnPoolTestBase {

	@Test
	public void testWaitTimeout() throws SQLException {
		final int maxWait = 1000;
		
		ds.setMaxActive(1);
		ds.setMaxWait(maxWait); //1 second
		
		try(Connection conn1 = ds.getConnection()) {
			long start = System.currentTimeMillis();
			try{
				ds.getConnection();
				fail("Should throw a SQLTimeoutException");
			}catch(SQLException e) {
				long wait = System.currentTimeMillis() - start;
				boolean inrange = wait - maxWait <= 1000;
				assertTrue("Connection should have been borrowed within +/- 1 second", inrange);
				assertTrue(e instanceof SQLTimeoutException);
			}
		}
		
	}
	
	@Test
	public void testValidationQueryOnBorrow() throws SQLException {
		ds.setTestOnBorrow(true);
		ds.setValidationQuery("select 1");
		
		ms.setSupportsJdbc4Validation(false);
		
		//force create an underlying connection.
		try(Connection conn = ds.getConnection()){}
		
		//should validate 
		try(Connection conn = ds.getConnection()) {
			MockConnection mconn = real(conn);
			MockStatement  mstmt = mconn.getLastStatement();
			
			assertFalse(mconn.isValidMethodSuccessCalled());
			assertEquals(ds.getValidationQuery(), mstmt.getLastQuery());
		}
	}
	
	@Test
	public void testJdbc4ValidationOnBorrow() throws SQLException {
		ds.setTestOnBorrow(true);
		ds.setValidationQuery("select 1");
		
		ms.setSupportsJdbc4Validation(true);
		
		//force create an underlying connection.
		try(Connection conn = ds.getConnection()){}
		
		//should validate 
		try(Connection conn = ds.getConnection()) {
			MockConnection mconn = real(conn);
			MockStatement  mstmt = mconn.getLastStatement();
			assertNull(mstmt);
			assertTrue(mconn.isValidMethodSuccessCalled());
		}
	}
	
}
