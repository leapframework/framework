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

public class FiltersParserTest extends TestBase {

    @Test
    public void testExprWithAlias() {
        Filters filters = FiltersParser.parse("a.b eq 1");

        FiltersParser.Name name = (FiltersParser.Name)filters.nodes()[0];
        assertEquals("a", name.alias());
        assertEquals("b", name.literal());
    }

    @Test
    public void testSingleQuoteChar() {
        Filters filters = FiltersParser.parse("a.name eq 'not exists'");
        assertEquals("not exists", filters.nodes()[2].literal());

        filters = FiltersParser.parse("a like %''%");
        assertEquals("%'%", filters.nodes()[2].literal());

        filters = FiltersParser.parse("a like '%''%'");
        assertEquals("%'%", filters.nodes()[2].literal());

        filters = FiltersParser.parse("a like %''");
        assertEquals("%'", filters.nodes()[2].literal());

        filters = FiltersParser.parse("a like '%'''");
        assertEquals("%'", filters.nodes()[2].literal());

        filters = FiltersParser.parse("a like %''_''%");
        assertEquals("%'_'%", filters.nodes()[2].literal());

        filters = FiltersParser.parse("a like '%''_''%'");
        assertEquals("%'_'%", filters.nodes()[2].literal());

        filters = FiltersParser.parse("a like %''''%");
        assertEquals("%''%", filters.nodes()[2].literal());

        filters = FiltersParser.parse("a like '%''''%'");
        assertEquals("%''%", filters.nodes()[2].literal());
    }

    @Test
    public void testSingleQuoteCharError() {
        try{
            FiltersParser.parse("a like %'%");
        }catch (Exception e) {
            assertContains(e.getMessage(), "Invalid character \"'\"");
        }
    }

    @Test
    public void testEnvExpr() {
        Filters filters = FiltersParser.parse("name eq name() and gender eq 'm'");
        assertEquals("name()", filters.nodes()[2].literal());

        filters = FiltersParser.parse("gender eq 'm' and name eq user.name()");
        assertEquals("user.name()", filters.nodes()[6].literal());
    }
}