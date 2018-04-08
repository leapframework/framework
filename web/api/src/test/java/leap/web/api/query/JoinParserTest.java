/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.api.query;

import leap.junit.TestBase;
import org.junit.Test;

public class JoinParserTest extends TestBase {

    @Test
    public void testErrorWithoutAlias() {
        try{
            JoinParser.parse("a");
            fail("Should thrown exception");
        }catch (Exception e) {

        }
    }

    @Test
    public void testSingleJoin() {
        Join[] joins = JoinParser.parse("User u");
        assertEquals(1, joins.length);
        assertEquals("User", joins[0].getRelation());
        assertEquals("u", joins[0].getAlias());

        joins = JoinParser.parse("User  u ");
        assertEquals(1, joins.length);
        assertEquals("User", joins[0].getRelation());
        assertEquals("u", joins[0].getAlias());

        joins = JoinParser.parse("  User  u ");
        assertEquals(1, joins.length);
        assertEquals("User", joins[0].getRelation());
        assertEquals("u", joins[0].getAlias());
    }

    @Test
    public void testMultiJoins() {
        Join[] joins = JoinParser.parse("User1 u1, User2 u2");
        assertEquals(2, joins.length);
        assertEquals("User1", joins[0].getRelation());
        assertEquals("u1", joins[0].getAlias());
        assertEquals("User2", joins[1].getRelation());
        assertEquals("u2", joins[1].getAlias());
    }
}
