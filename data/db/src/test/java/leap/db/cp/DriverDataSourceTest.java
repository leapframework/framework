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

import leap.db.cp.mock.MockDriver;
import leap.junit.TestBase;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DriverDataSourceTest extends TestBase {

	@Test
	public void testDriverOk() throws SQLException {
		
		try(PooledDataSource ds = new PooledDataSource()){
			ds.setDriverClassName(MockDriver.CLASS_NAME);
			ds.setJdbcUrl(MockDriver.JDBC_URL);
		
			ds.init();
			
			assertTrue(ds.isWrapperFor(DriverDataSource.class));
			
			DataSource unwrap = ds.unwrap(DriverDataSource.class);
			assertNotNull(unwrap);
			
			try(Connection conn = ds.getConnection()) {
				
			}
		}
	}
	
	@Test
	public void testDriverError() throws SQLException {
		
		try(PooledDataSource ds = new PooledDataSource()){
			ds.setDriverClassName("com.example.UnknownDriver");
			ds.setJdbcUrl("jdbc:err:db");
		
	        try {
	            ds.init();
	            fail("Should throw exception");
            } catch (Exception e) {
            	assertContains(e.getMessage(), "Unable to get driver");
            }
	        
		}
	}
	
}
