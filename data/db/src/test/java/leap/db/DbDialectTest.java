/*
 *  Copyright 2020 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package leap.db;

import leap.junit.contexual.Contextual;
import org.junit.Test;

public class DbDialectTest extends DbTestCase {

    @Test
    @Contextual("h2")
    public void testH2DialectProperties() {
        assertNotEmpty(dialect.getTimestampAddMsExpr());
        assertEquals("v3_1", dialect.getProperty("p1"));
    }

    @Test
    @Contextual("mysql")
    public void testMySQLDialectProperties() {
        assertNotEmpty(dialect.getTimestampAddMsExpr());
    }
}
