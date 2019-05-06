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

public class GroupByParserTest extends TestBase {

    @Test
    public void testInvalidExpr() {
        assertInvalidExpr("app name id");
        assertInvalidExpr("app.name.id");
    }

    @Test
    public void testSingle() {
        GroupBy groupBy = GroupByParser.parse("name");
        assertSingleGroupBy(groupBy, "name", null, null);
        groupBy = GroupByParser.parse("name n");
        assertSingleGroupBy(groupBy, "name", "n", null);
        groupBy = GroupByParser.parse("t.name");
        assertSingleGroupBy(groupBy, "name", null, "t");
        groupBy = GroupByParser.parse("t.name n");
        assertSingleGroupBy(groupBy, "name", "n", "t");
    }

    @Test
    public void testMulti() {
        GroupBy groupBy = GroupByParser.parse("id,name");
        assertEquals(2, groupBy.items().length);
        assertGroupBy(groupBy.items()[0], "id", null, null);
        assertGroupBy(groupBy.items()[1], "name", null, null);
        groupBy = GroupByParser.parse("id i,name n");
        assertGroupBy(groupBy.items()[0], "id", "i", null);
        assertGroupBy(groupBy.items()[1], "name", "n", null);
        groupBy = GroupByParser.parse("t.id i,t.name n");
        assertGroupBy(groupBy.items()[0], "id", "i", "t");
        assertGroupBy(groupBy.items()[1], "name", "n", "t");
    }

    private static void assertInvalidExpr(String expr) {
        try{
            GroupByParser.parse(expr);
            fail("Should throw bad request");
        }catch (BadRequestException e) {

        }
    }

    private static void assertSingleGroupBy(GroupBy groupBy, String name, String alias, String joinAlias) {
        assertEquals(1, groupBy.items().length);
        assertGroupBy(groupBy.items()[0], name, alias, joinAlias);
    }

    private static void assertGroupBy(GroupBy.Item item, String name, String alias, String joinAlias) {
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
