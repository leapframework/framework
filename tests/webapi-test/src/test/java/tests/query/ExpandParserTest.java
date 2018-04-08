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

package tests.query;

import leap.junit.TestBase;
import leap.web.api.query.Expand;
import leap.web.api.query.ExpandParser;
import org.junit.Test;

public class ExpandParserTest extends TestBase {

    @Test
    public void testEmpty() {
        assertEquals(0, ExpandParser.parse(null).length);
        assertEquals(0, ExpandParser.parse("").length);
    }

    @Test
    public void testNoSelect() {
        Expand[] expands = ExpandParser.parse("a");
        assertEquals(1, expands.length);
        assertEquals("a", expands[0].getName());
        assertNull(expands[0].getSelect());

        expands = ExpandParser.parse("a , b");
        assertEquals(2, expands.length);
        assertEquals("a", expands[0].getName());
        assertNull(expands[0].getSelect());
    }

    @Test
    public void testWithSelect() {
        Expand[] expands = ExpandParser.parse("a(id, name)");
        assertEquals(1, expands.length);

        Expand expand = expands[0];
        assertEquals("a", expand.getName());
        assertEquals("id, name", expand.getSelect());

        expands = ExpandParser.parse("a(id), b(name)");
        assertEquals(2, expands.length);
        expand = expands[0];
        assertEquals("a", expand.getName());
        assertEquals("id", expand.getSelect());

        expand = expands[1];
        assertEquals("b", expand.getName());
        assertEquals("name", expand.getSelect());
    }

    @Test
    public void testCombineWithAndWithoutSelect() {
        Expand[] expands = ExpandParser.parse("a, b(name), c, d(id)");
        assertEquals(4, expands.length);
        Expand expand = expands[0];
        assertEquals("a", expand.getName());
        assertNull(expand.getSelect());

        expand = expands[1];
        assertEquals("b", expand.getName());
        assertEquals("name", expand.getSelect());

        expand = expands[2];
        assertEquals("c", expand.getName());
        assertNull(expand.getSelect());

        expand = expands[3];
        assertEquals("d", expand.getName());
        assertEquals("id", expand.getSelect());
    }
}
