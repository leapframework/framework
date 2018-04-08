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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

import tests.cp.mock.MockConnection;
import tests.cp.mock.MockStatement;

import org.junit.Test;

public class BorrowTest extends PoolTestBase {

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
			MockConnection mconn = conn.unwrap(MockConnection.class);
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

    @Test
    public void testInvalidConnectionOnBorrowOld() throws SQLException {
        ds.setMaxWait(1);
        ds.setTestOnBorrow(true);
        ms.setSupportsJdbc4Validation(true);

        Connection wrapped;
        try(Connection conn = ds.getConnection()){
            wrapped = conn.unwrap(MockConnection.class);
        }

        try(Connection conn = ds.getConnection()){
            assertSame(wrapped, conn.unwrap(MockConnection.class));
        }

        ms.setValidateConnectionError(true);
        try(Connection conn = ds.getConnection()){
            assertNotSame(wrapped, conn.unwrap(MockConnection.class));
        }

        ms.setValidateConnectionError(false);
        try(Connection conn = ds.getConnection()) {}
    }

    @Test
    public void testOpenConnectionError() throws SQLException {
        ds.setMaxActive(1);
        ms.setOpenConnectionError(true);

        try {
            try(Connection conn = ds.getConnection()){}
            fail("should throw SQLException");
        }catch (SQLException e) {

        }

        ms.setOpenConnectionError(false);
        try(Connection conn = ds.getConnection()) {}
    }

    @Test
    public void testSetupConnectionErrorOnBorrowNew() throws SQLException {
        ds.setMaxActive(1);
        ds.setDefaultAutoCommit(false);
        ms.setSetAutoCommitError(true);

        try {
            try(Connection conn = ds.getConnection()){}
            fail("should throw SQLException");
        }catch (SQLException e) {
            assertContains(e.getMessage(), "Set AutoCommit Error");
        }

        ms.setSetAutoCommitError(false);
        try(Connection conn = ds.getConnection()) {}
    }

    @Test
    public void testSetupConnectionErrorOnBorrowOld() throws SQLException {
        ds.setMaxActive(1);
        ds.setDefaultAutoCommit(false);
        try(Connection conn = ds.getConnection()){
            conn.setAutoCommit(true);
        }

        ms.setSetAutoCommitError(true);
        try {
            try(Connection conn = ds.getConnection()){}
            fail("should throw SQLException");
        }catch (SQLException e) {
            assertContains(e.getMessage(), "Set AutoCommit Error");
        }

        //test abandon old one, creates a new.
        ms.setSetAutoCommitError(false);
        try(Connection conn = ds.getConnection()){
            conn.setAutoCommit(true);
        }
        ms.setSetAutoCommitError(true);
        ms.getSetAutoCommitErrorCount().set(1);
        try(Connection conn = ds.getConnection()){}

        //test abandon old one, throw exception.
        ms.setSetAutoCommitError(false);
        try(Connection conn = ds.getConnection()){
            conn.setAutoCommit(true);
        }
        ms.setSetAutoCommitError(true);
        ms.getSetAutoCommitErrorCount().set(2);
        try {
            try(Connection conn = ds.getConnection()){}
            fail("should throw SQLException");
        }catch (SQLException e) {
            assertContains(e.getMessage(), "Set AutoCommit Error");
        }

        ms.setSetAutoCommitError(false);
        try(Connection conn = ds.getConnection()) {}
    }
}
