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
import leap.lang.text.scel.ScelNode;
import leap.web.api.query.*;
import org.junit.Test;

import java.io.File;

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
        Expand expand = expands[0];
        assertEquals("a", expand.getName());
        assertNull(expand.getSelect());
        assertNull(expand.getFilters());
        assertNull(expand.getOrderBy());

        expands = ExpandParser.parse("a , b");
        assertEquals(2, expands.length);
        expand = expands[0];
        assertEquals("a", expand.getName());
        assertNull(expand.getSelect());
        assertNull(expand.getFilters());
        assertNull(expand.getOrderBy());
        expand = expands[1];
        assertEquals("b", expand.getName());
        assertNull(expand.getSelect());
        assertNull(expand.getFilters());
        assertNull(expand.getOrderBy());
    }

    @Test
    public void testWithSelect() {
        Expand[] expands = ExpandParser.parse("a(id, name)");
        assertEquals(1, expands.length);

        Expand expand = expands[0];
        assertEquals("a", expand.getName());
        Select.Item[] selectItems = SelectParser.parse(expand.getSelect()).items();
        assertEquals(2, selectItems.length);
        assertEquals("id", selectItems[0].name());
        assertNull(selectItems[0].alias());
        assertNull(selectItems[0].joinAlias());
        assertEquals("name", selectItems[1].name());
        assertNull(selectItems[1].alias());
        assertNull(selectItems[1].joinAlias());
        assertNull(expand.getFilters());
        assertNull(expand.getOrderBy());

        expands = ExpandParser.parse("a(id), b(name)");
        assertEquals(2, expands.length);
        expand = expands[0];
        assertEquals("a", expand.getName());
        selectItems = SelectParser.parse(expand.getSelect()).items();
        assertEquals(1, selectItems.length);
        assertEquals("id", selectItems[0].name());

        expand = expands[1];
        assertEquals("b", expand.getName());
        selectItems = SelectParser.parse(expand.getSelect()).items();
        assertEquals(1, selectItems.length);
        assertEquals("name", selectItems[0].name());

        expands = ExpandParser.parse("a(select:name)");
        expand = expands[0];
        assertNull(expand.getFilters());
        assertNull(expand.getOrderBy());
        Select.Item[] selectsItems = SelectParser.parse(expand.getSelect()).items();
        assertEquals(1, selectsItems.length);
        Select.Item selectItem = selectsItems[0];
        assertEquals("name", selectItem.name());
        assertNull(selectItem.joinAlias());
        assertNull(selectItem.alias());
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
        assertEquals("name", SelectParser.parse(expand.getSelect()).items()[0].name());

        expand = expands[2];
        assertEquals("c", expand.getName());
        assertNull(expand.getSelect());

        expand = expands[3];
        assertEquals("d", expand.getName());
        assertEquals("id", SelectParser.parse(expand.getSelect()).items()[0].name());
    }

    @Test
    public void testWithFilters() {
        Expand[] expands = ExpandParser.parse("a(filters:id eq bingo and name is null),b,c(filters:name eq bingo)");
        assertEquals(3, expands.length);

        Expand expand = expands[0];
        assertEquals("a", expand.getName());
        assertNull(expand.getSelect());
        ScelNode[] filtersItems = FiltersParser.parse(expand.getFilters()).nodes();
        assertEquals(7, filtersItems.length);
        assertNull(expand.getOrderBy());

        expand = expands[1];
        assertEquals("b", expand.getName());
        assertNull(expand.getSelect());
        assertNull(expand.getFilters());
        assertNull(expand.getOrderBy());

        expand = expands[2];
        assertEquals("c", expand.getName());
        assertNull(expand.getSelect());
        filtersItems = FiltersParser.parse(expand.getFilters()).nodes();
        assertEquals(3, filtersItems.length);
        assertNull(expand.getOrderBy());
    }

    @Test
    public void testWithOrder() {
        Expand[] expands = ExpandParser.parse("a(orderby:name desc)");
        assertEquals(1, expands.length);

        Expand expand = expands[0];
        assertEquals("a", expand.getName());
        assertNull(expand.getSelect());
        assertNull(expand.getFilters());
        OrderBy.Item[] items = OrderByParser.parse(expand.getOrderBy()).items();
        assertEquals(1, items.length);
        OrderBy.Item item = items[0];
        assertFalse(item.hasAlias());
        assertEquals("name", item.name());
        assertFalse(item.isAscending());
    }

    @Test
    public void testComplexExpr() {
        Expand[] expands = ExpandParser.parse("a(select:name,filters:id eq bingo and name is null,orderby:name desc)");
        assertEquals(1, expands.length);

        Expand expand = expands[0];
        assertEquals("a", expand.getName());
        Select.Item[] selectItems = SelectParser.parse(expand.getSelect()).items();
        assertEquals(1, selectItems.length);
        Select.Item selectItem = selectItems[0];
        assertEquals("name", selectItem.name());
        assertNull(selectItem.alias());
        assertNull(selectItem.joinAlias());
        ScelNode[] filtersItems = FiltersParser.parse(expand.getFilters()).nodes();
        assertEquals(7, filtersItems.length);
        OrderBy.Item[] orderByItems = OrderByParser.parse(expand.getOrderBy()).items();
        assertEquals(1, orderByItems.length);
        OrderBy.Item orderByItem = orderByItems[0];
        assertEquals("name", orderByItem.name());
        assertFalse(orderByItem.hasAlias());
        assertFalse(orderByItem.isAscending());
    }

}