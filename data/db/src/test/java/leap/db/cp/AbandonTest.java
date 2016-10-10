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
package leap.db.cp;

import leap.lang.Threads;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class AbandonTest extends ConnPoolTestBase {

    @Test
    public void testAbandonIdleTimeout() throws SQLException {
        ds.setIdleTimeout(1);

        try(Connection conn = ds.getConnection()) {}

        assertEquals(1, ms.getNrOfOpenedConnections());
        assertEquals(0, ms.getNrOfClosedConnections());
        assertEquals(1, ms.getNrOfOpeningConnections());

        Threads.sleep(1100);
        assertEquals(1, ms.getNrOfOpenedConnections());
        assertEquals(1, ms.getNrOfClosedConnections());
        assertEquals(0, ms.getNrOfOpeningConnections());
    }

    @Test
    public void testMaxIdle() throws SQLException {
        ds.setMaxIdle(0);

        try(Connection conn = ds.getConnection()) {}

        assertEquals(1, ms.getNrOfOpeningConnections());
        Threads.sleep(1100);
        assertEquals(0, ms.getNrOfOpeningConnections());
    }

}
