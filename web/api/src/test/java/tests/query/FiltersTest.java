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
import leap.web.api.query.FiltersParser;
import org.junit.Test;

public class FiltersTest extends TestBase {

    @Test
    public void testSimpleParseExprs() {
        assertParse("a eq 10");
        assertParse("( a eq 10 )");
        assertParse("a eq 's s'");

        assertParse("a : 10");
        assertParse("a :10", "a : 10");
        assertParse("a:10", "a : 10");
        assertParse("a: 10", "a : 10");
        assertParse("( a : 10 )");
    }

    @Test
    public void testComplexParseExprs() {
        assertParse("a eq b and c ge 10");
        assertParse("a eq b , c ge 10");
        assertParse("a : b , c : 10");
        assertParse("( a eq b ) and ( c ge 10 )");

        assertParse("( a eq b ) and ( c ge 10 ) or d like 'ddd'");
        assertParse("( a eq b ) and ( c ge 10 ) or ( d like 'ddd' )");
        assertParse("( a eq b ) and ( c ge 10 ) or ( d like 'ddd' ) and ( ( a eq b ) and ( c ge 10 ) or ( d like 'ddd' ) )");

    }

    private void assertParse(String expr) {
        assertEquals(expr, FiltersParser.parse(expr).toString());
    }

    private void assertParse(String expr, String expected) {
        assertEquals(expected, FiltersParser.parse(expr).toString());
    }

}