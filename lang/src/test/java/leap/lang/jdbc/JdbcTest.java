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

package leap.lang.jdbc;

import leap.junit.TestBase;
import org.junit.Test;

public class JdbcTest extends TestBase {

    @Test
    public void testExtractServerString() {
        /*
            jdbc:microsoft:sqlserver://<server_name>:<port> -> port is optional
            jdbc:sqlserver://<server_name>:<port>
            jdbc:oracle:thin:@//<host>:<port>/ServiceName
            jdbc:oracle:thin:@<host>:<port>:<SID>
            jdbc:db2://<host>[:<port>]/<database_name>
            jdbc:mysql://<host>:<port>/<database_name>
            jdbc:postgresql://<host>:<port>/<database_name>
            jdbc:h2:tcp://<server>[:<port>]/[<path>]<databaseName>
            jdbc:h2:[file:][<path>]<databaseName>
            jdbc:h2:<url>[;USER=<username>][;PASSWORD=<value>]

         */
        assertEquals("127.0.0.1",      JDBC.tryExtractServerString("jdbc:microsoft:sqlserver://127.0.0.1"));
        assertEquals("127.0.0.1:1234", JDBC.tryExtractServerString("jdbc:microsoft:sqlserver://127.0.0.1:1234"));
        assertEquals("127.0.0.1",      JDBC.tryExtractServerString("jdbc:sqlserver://127.0.0.1"));
        assertEquals("127.0.0.1:1234", JDBC.tryExtractServerString("jdbc:sqlserver://127.0.0.1:1234"));
        assertEquals("127.0.0.1:1234", JDBC.tryExtractServerString("jdbc:oracle:thin:@127.0.0.1:1234:db"));
        assertEquals("127.0.0.1:1234", JDBC.tryExtractServerString("jdbc:oracle:thin:@//127.0.0.1:1234/db"));
        assertEquals("127.0.0.1:1234", JDBC.tryExtractServerString("jdbc:db2://127.0.0.1:1234/db"));
        assertEquals("127.0.0.1",      JDBC.tryExtractServerString("jdbc:mysql://127.0.0.1"));
        assertEquals("127.0.0.1:1234", JDBC.tryExtractServerString("jdbc:mysql://127.0.0.1:1234"));
        assertEquals("127.0.0.1:1234", JDBC.tryExtractServerString("jdbc:mysql://127.0.0.1:1234?a=b&c=d"));
        assertEquals("127.0.0.1",      JDBC.tryExtractServerString("jdbc:h2:tcp://127.0.0.1/path/db"));
        assertEquals("127.0.0.1",      JDBC.tryExtractServerString("jdbc:h2:tcp://127.0.0.1/path/db;a=b;c=d"));
        assertEquals("127.0.0.1:1234", JDBC.tryExtractServerString("jdbc:h2:tcp://127.0.0.1:1234/path/db"));
        assertEquals("file:a.db",      JDBC.tryExtractServerString("jdbc:h2:file:a.db"));

        assertNull(JDBC.tryExtractServerString("jdbc:h2:./target/test"));
        assertNull(JDBC.tryExtractServerString("abcd"));
    }
}
