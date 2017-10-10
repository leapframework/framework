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

import leap.lang.Threads;
import org.junit.Test;
import tests.cp.mock.MockConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PoolTest extends PoolTestBase {

    @Test
    public void testClosedPool() throws SQLException {
        ds.open();
        ds.close();

        try {
            ds.getConnection();

            fail("Should throw exception");
        }catch(SQLException e) {
            assertTrue(e.getMessage().contains("Connection Pool has been closed."));
        }

        //close again.
        ds.close();
    }

    @Test
    public void testMinIdle() throws SQLException {
        ds.setMinIdle(1);
        ds.setIdleTimeoutMs(100);
        ds.setHealthCheckIntervalMs(1000);

        ds.open();
        assertEquals(1, ms.getNrOfOpeningConnections());
        assertEquals(0, ms.getNrOfClosedConnections());

        Threads.sleep(1500L);
        assertEquals(1, ms.getNrOfOpeningConnections());
        assertEquals(1, ms.getNrOfClosedConnections());

        for(int i=0;i<1;i++) {
            try(Connection conn = ds.getConnection()) {
                assertFalse(conn.unwrap(MockConnection.class).isClosed());
            }
        }

        ds.close();
        assertEquals(0, ms.getNrOfOpeningConnections());
    }
}
