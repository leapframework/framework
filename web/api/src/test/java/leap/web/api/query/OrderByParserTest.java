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

public class OrderByParserTest extends TestBase {

    @Test
    public void testInvalidExpr() {
        assertInvalidExpr("name noDesc");
        assertInvalidExpr("name%");
    }

    @Test
    public void testSingle() {
        OrderBy orderBy = OrderByParser.parse("name");
        assertSingleOrderBy(orderBy, "name", null, true);
        orderBy = OrderByParser.parse("t.name");
        assertSingleOrderBy(orderBy, "name", "t", true);
        orderBy = OrderByParser.parse("t.name desc");
        assertSingleOrderBy(orderBy, "name", "t", false);
    }

    @Test
    public void testMulti() {
        OrderBy orderBy = OrderByParser.parse("id,name");
        assertEquals(2, orderBy.items().length);
        assertOrderBy(orderBy.items()[0], "id", null, true);
        assertOrderBy(orderBy.items()[1], "name", null, true);
        orderBy = OrderByParser.parse("id desc,name");
        assertOrderBy(orderBy.items()[0], "id", null, false);
        assertOrderBy(orderBy.items()[1], "name", null, true);
        orderBy = OrderByParser.parse("t.id desc,t.name asc");
        assertOrderBy(orderBy.items()[0], "id", "t", false);
        assertOrderBy(orderBy.items()[1], "name", "t", true);
    }

    private static void assertInvalidExpr(String expr) {
        try{
            OrderByParser.parse(expr);
            fail("Should throw bad request");
        }catch (BadRequestException e) {

        }
    }

    private static void assertSingleOrderBy(OrderBy orderBy, String name, String alias, boolean ascending) {
        assertEquals(1, orderBy.items().length);
        assertOrderBy(orderBy.items()[0], name, alias, ascending);
    }

    private static void assertOrderBy(OrderBy.Item item, String name, String alias, boolean ascending) {
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
        assertEquals(ascending, item.isAscending());
    }

}
