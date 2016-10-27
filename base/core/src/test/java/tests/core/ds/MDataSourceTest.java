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

package tests.core.ds;

import leap.core.annotation.Inject;
import leap.core.ds.DataSourceManager;
import leap.core.ds.management.MDataSource;
import leap.core.junit.AppTestBase;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class MDataSourceTest extends AppTestBase {

    protected @Inject DataSource        ds;
    protected @Inject DataSourceManager dsm;

    private MDataSource mds;

    @Override
    protected void setUp() throws Exception {
        mds = dsm.getManagedDataSource(ds);
    }

    @Test
    public void testActiveConnections() throws SQLException {
        try(Connection conn = ds.getConnection()) {
            assertEquals(1, mds.getActiveConnections().length);

            try(Connection conn1 = ds.getConnection()) {
                assertEquals(2, mds.getActiveConnections().length);
            }
        }

        assertEquals(0, mds.getActiveConnections().length);
    }
}
