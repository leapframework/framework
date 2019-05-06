/*
 *  Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package leap.web.api.query;

import leap.junit.TestBase;
import leap.lang.Strings;
import leap.web.exception.BadRequestException;
import org.junit.Test;

public class SelectParserTest extends TestBase {

    @Test
    public void testInvalidExpr() {
        assertInvalidExpr("app name id");
        assertInvalidExpr("app.name.id");
    }

    @Test
    public void testSingle() {
        Select select = SelectParser.parse("name");
        assertSingleSelect(select, "name", null, null);
        select = SelectParser.parse("name n");
        assertSingleSelect(select, "name", "n", null);
        select = SelectParser.parse("t.name");
        assertSingleSelect(select, "name", null, "t");
        select = SelectParser.parse("t.name n");
        assertSingleSelect(select, "name", "n", "t");
    }

    @Test
    public void testMulti() {
        Select select = SelectParser.parse("id,name");
        assertEquals(2, select.items().length);
        assertSelect(select.items()[0], "id", null, null);
        assertSelect(select.items()[1], "name", null, null);
        select = SelectParser.parse("id i,name n");
        assertSelect(select.items()[0], "id", "i", null);
        assertSelect(select.items()[1], "name", "n", null);
        select = SelectParser.parse("t.id i,t.name n");
        assertSelect(select.items()[0], "id", "i", "t");
        assertSelect(select.items()[1], "name", "n", "t");
    }

    private static void assertInvalidExpr(String expr) {
        try{
            SelectParser.parse(expr);
            fail("Should throw bad request");
        }catch (BadRequestException e) {

        }
    }

    private static void assertSingleSelect(Select select, String name, String alias, String joinAlias) {
        assertEquals(1, select.items().length);
        assertSelect(select.items()[0], name, alias, joinAlias);
    }

    private static void assertSelect(Select.Item item, String name, String alias, String joinAlias) {
        if (Strings.isEmpty(name)) {
            assertNull(item.name());
        } else {
            assertEquals(name, item.name());
        }
        if (Strings.isEmpty(alias)) {
            assertNull(item.alias());
        } else {
            assertEquals(alias, item.alias());
        }
        if (Strings.isEmpty(joinAlias)) {
            assertNull(item.joinAlias());
        } else {
            assertEquals(joinAlias, item.joinAlias());
        }
    }

}
