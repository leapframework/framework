/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tests.core.ds;

import leap.core.annotation.Inject;
import leap.core.ds.DataSourceConfig;
import leap.core.ds.DataSourceManager;
import leap.core.ds.management.MDataSource;
import leap.core.ds.management.MDataSourceProxy;
import leap.core.ds.management.MSlowSql;
import leap.core.junit.AppTestBase;
import leap.lang.Exceptions;
import org.junit.Test;
import tested.ds.MockDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceManagerTest extends AppTestBase {

    private @Inject DataSourceManager dsm;

	@Test
	public void testCreateDataSource() {
		DataSourceConfig p =
				DataSourceConfig.createBuilder()
								    .setDriverClassName("org.h2.Driver")
								    .setUrl("jdbc:h2:mem:test")
								    .build();
		
		DataSource ds;
		try {
			ds = dsm.createDataSource("test", p);
			assertNotNull(ds);
			
	        try(Connection connection = ds.getConnection()){
	        	
	        }
        } catch (SQLException e) {
        	throw Exceptions.wrap(e);
        }

        assertTrue(ds instanceof MDataSourceProxy);
		
		dsm.destroyDataSource(ds);

        assertNull(dsm.tryGetDataSource("test"));
	}

    @Test
    public void testDataSourceProxy() {
        assertTrue(dsm.getDefaultDataSource() instanceof MDataSourceProxy);
    }

    @Test
    public void testSlowSql() throws SQLException {
        DataSource     dataSource     = dsm.getDataSource("mock");
        MDataSource    mDataSource    = dsm.getManagedDataSource(dataSource);
        MockDataSource mockDataSource = dsm.getDataSource("mock").unwrap(MockDataSource.class);

        //warm up.
        dataSource.getConnection().createStatement().execute("sql1");

        //begin test

        mockDataSource.setStatementDurationMs(1);
        dataSource.getConnection().createStatement().execute("sql1");
        assertEquals(0, mDataSource.getSlowSqls().length);
        assertEquals(0, mDataSource.getVerySlowSqls().length);

        mockDataSource.setStatementDurationMs(20);
        dataSource.getConnection().createStatement().execute("sql1");
        assertEquals(1, mDataSource.getSlowSqls().length);
        assertEquals(0, mDataSource.getVerySlowSqls().length);

        mockDataSource.setStatementDurationMs(50);
        dataSource.getConnection().createStatement().execute("sql1");
        assertEquals(1, mDataSource.getSlowSqls().length);
        assertEquals(1, mDataSource.getVerySlowSqls().length);

        MSlowSql nss0 = mDataSource.getSlowSqls()[0];
        MSlowSql vss0 = mDataSource.getVerySlowSqls()[0];

        mockDataSource.setStatementDurationMs(20);
        for(int i=0;i<49;i++) {
            dataSource.getConnection().createStatement().execute("sql1");
        }
        assertEquals(50, mDataSource.getSlowSqls().length);
        assertSame(nss0, mDataSource.getSlowSqls()[0]);

        dataSource.getConnection().createStatement().execute("sql1");
        assertEquals(50, mDataSource.getSlowSqls().length);
        assertNotSame(nss0, mDataSource.getSlowSqls()[0]);

        mockDataSource.setStatementDurationMs(50);
        for(int i=0;i<49;i++) {
            dataSource.getConnection().createStatement().execute("sql1");
        }
        assertEquals(50, mDataSource.getVerySlowSqls().length);
        assertSame(vss0, mDataSource.getVerySlowSqls()[0]);

        dataSource.getConnection().createStatement().execute("sql1");
        assertEquals(50, mDataSource.getVerySlowSqls().length);
        assertNotSame(vss0, mDataSource.getVerySlowSqls()[0]);
    }

}
