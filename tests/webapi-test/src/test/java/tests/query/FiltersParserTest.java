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
import leap.lang.text.scel.ScelExpr;
import leap.lang.text.scel.ScelNode;
import leap.web.api.query.FiltersParser;
import org.junit.Test;

public class FiltersParserTest extends TestBase {

    @Test
    public void testInvalidExpr() {
        assertInvalidExpr("default0");
        assertEquals("default0", FiltersParser.parse("default0", true).nodes()[0].literal());
    }

    @Test
    public void testSimpleParseExprs() {
        assertParse(3, "a like '()'");
        assertParse(3, "a like '%()%'");

        assertParse(3, "a eq 10");
        assertParse(5, "( a eq 10 )");
        assertParse(3, "a eq 's s'");

        assertParse(3, "a : 10");
        assertParse("a :10", "a : 10");
        assertParse("a:10", "a : 10");
        assertParse("a: 10", "a : 10");
        assertParse(5, "( a : 10 )");
    }

    @Test
    public void testComplexParseExprs() {
        assertParse(7, "a not null and c ge 10");
        assertParse(7, "a is null and c ge 10");
        assertParse(7, "a eq b and c ge 10");
        assertParse(7, "a eq b , c ge 10");
        assertParse(7, "a : b , c : 10");
        assertParse(11, "( a eq b ) and ( c ge 10 )");
        assertParse(15, "( a eq b ) and ( c ge 10 ) or d like 'ddd'");
        assertParse(17, "( a eq b ) and ( c ge 10 ) or ( d like 'ddd' )");
        assertParse(37, "( a eq b ) and ( c ge 10 ) or ( d like 'ddd' ) and ( ( a eq b ) and ( c ge 10 ) or ( d like 'ddd' ) )");
    }

    @Test
    public void testIn() {
        assertParse(5, "( v in 1 )");
        assertParse(5, "( v in (1) )");
        assertParse(7, "( ( v in (1) ) )");
        assertParse(3, "name in ',a'");
        assertParse(3, "name in ,'a'");
        assertParse(3, "name in 1,2");
        assertParse(3, "name in 1, 2");
        assertParse(3, "name in 1 , 2");
        assertParse(3, "name in '1' , 2");
        assertParse(3, "name in ('1' , 2)");
        assertParse(7, "name in 1,2 and id eq admin");

        ScelExpr expr = FiltersParser.parse("name in a,b");
        ScelNode node = expr.nodes()[2];
        assertEquals(2, node.values().size());
    }

    private void assertParse(int length, String expr) {
        assertEquals(length, FiltersParser.parse(expr).nodes().length);
    }

    private void assertParse(String expr, String expected) {
        assertEquals(expected, FiltersParser.parse(expr).toString());
    }

    private static void assertInvalidExpr(String expr) {
        try{
            FiltersParser.parse(expr);
            fail("Should throw IllegalStateException");
        }catch (IllegalStateException e) {

        }
    }

}