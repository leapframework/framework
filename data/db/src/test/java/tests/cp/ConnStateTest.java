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

import leap.db.cp.PooledConnection;
import leap.lang.jdbc.TransactionIsolation;

import org.junit.Test;

public class ConnStateTest extends PoolTestBase {

    @Test
    public void testStackTrace() throws SQLException{

        try(Connection conn = ds.getConnection()) {
            PooledConnection pc = conn.unwrap(PooledConnection.class);

            assertTrue(pc.hasStackTraceOnOpen());
            assertNotNull(pc.getStackTraceOnOpen());
        }

    }
	
	@Test
	public void testDefaultState() throws Exception {
		ds.setMinIdle(1);

		try(Connection conn = getConnection()) {
			assertTrue(conn.getAutoCommit());
			assertFalse(conn.isReadOnly());
			assertEquals(ms.getDefaultTransactionIsolation(),conn.getTransactionIsolation());
			assertNull(conn.getCatalog());
		}
		
		initDefaultDataSource();
		ds.setMinIdle(1);
		ds.setHealthCheck(false);
		ms.setDefaultAutoCommit(false);
		ms.setDefaultReadonly(true);
		ms.setDefaultTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
		ms.setDefaultCatalog("test");
		
		try(Connection conn = getConnection()) {
			assertTrue(conn.getAutoCommit());
			assertFalse(conn.isReadOnly());
			assertEquals(Connection.TRANSACTION_REPEATABLE_READ,conn.getTransactionIsolation());
			assertEquals("test",conn.getCatalog());
		}
		
		try(Connection conn = getConnection()) {
			assertTrue(conn.getAutoCommit());
			assertFalse(conn.isReadOnly());
			assertEquals(Connection.TRANSACTION_REPEATABLE_READ,conn.getTransactionIsolation());
			assertEquals("test",conn.getCatalog());
		}
	}
	
	@Test
	public void testConfigState() throws Exception {
		ds.setMinIdle(1);
		ds.setDefaultAutoCommit(false);
		ds.setDefaultReadonly(true);
		ds.setDefaultTransactionIsolation(TransactionIsolation.READ_COMMITTED);
		ds.setDefaultCatalog("test1");

		//First time
		try(Connection conn = getConnection()) {
			assertFalse(conn.getAutoCommit());
			assertTrue(conn.isReadOnly());
			assertEquals(Connection.TRANSACTION_READ_COMMITTED,conn.getTransactionIsolation());
			assertEquals("test1",conn.getCatalog());
		}
		
		//Try again (the same connection)
		try(Connection conn = getConnection()) {
			assertFalse(conn.getAutoCommit());
			assertTrue(conn.isReadOnly());
			assertEquals(Connection.TRANSACTION_READ_COMMITTED,conn.getTransactionIsolation());
			assertEquals("test1",conn.getCatalog());
		}
	}
	
	@Test
	public void testSetState() throws Exception {
		ds.setMinIdle(1);
		
		PooledConnection conn = getConnection();
		
		conn.setAutoCommit(false);
		conn.commit();
		conn.setCatalog("test2");
		conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		conn.setReadOnly(true);
		
		Connection real = conn.wrapped();
		assertFalse(real.getAutoCommit());
		assertTrue(real.isReadOnly());
		assertEquals("test2",real.getCatalog());
		assertEquals(Connection.TRANSACTION_SERIALIZABLE, real.getTransactionIsolation());
		
		conn.close();

		assertNull(real.getCatalog());
		assertEquals(ms.getDefaultTransactionIsolation(), real.getTransactionIsolation());
		
		try(Connection conn1 = getConnection()) {
			assertTrue(conn1.getAutoCommit());
			assertFalse(conn1.isReadOnly());
			assertEquals(ms.getDefaultTransactionIsolation(),conn1.getTransactionIsolation());
			assertNull(conn1.getCatalog());
		}
	}

}
