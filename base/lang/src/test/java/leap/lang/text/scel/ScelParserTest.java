/*
 *  Copyright 2018 the original author or authors.
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

package leap.lang.text.scel;

import leap.junit.TestBase;
import org.junit.Test;

public class ScelParserTest extends TestBase {

    @Test
    public void testExprWithAlias() {
        ScelExpr expr = ScelParser.parse("a.b eq 1");

        ScelName name = (ScelName)expr.nodes()[0];
        assertEquals("a", name.alias());
        assertEquals("b", name.literal());
    }

    @Test
    public void testDoubleQuoted() {
        ScelExpr expr = ScelParser.parse("a eq \"s\"");
        assertEquals("s", expr.nodes()[2].literal());
    }

    @Test
    public void testInvalidExpr() {
        try {
            ScelParser.parse("deleted0");
        }catch (Exception e) {
            assertTrue(e instanceof IllegalStateException);
        }
    }

    @Test
    public void testIsNull() {
        ScelExpr expr = ScelParser.parse("a is null");
        assertEquals("a is null", expr.toString());

        expr = ScelParser.parse("a is not null");
        assertEquals("a is not null", expr.toString());

        expr = ScelParser.parse("a is null and b is not null");
        assertEquals("a is null and b is not null", expr.toString());

        expr = ScelParser.parse("(a is null) and (b is not null)");
        assertEquals("( a is null ) and ( b is not null )", expr.toString());
    }

    @Test
    public void testPr() {
        ScelExpr expr = ScelParser.parse("a pr");
        assertEquals("a pr", expr.toString());

        expr = ScelParser.parse("a pr and b pr");
        assertEquals("a pr and b pr", expr.toString());
    }

    @Test
    public void testLikeOps() {
        String s = "a like 'b' and c sw 'a' and d ew 'b' or e co 's\'";
        assertEquals(s, ScelParser.parse(s).toString());
    }

    @Test
    public void testSingleQuoteChar() {
        ScelExpr expr = ScelParser.parse("a.name eq 'not exists'");
        assertEquals("not exists", expr.nodes()[2].literal());

        expr = ScelParser.parse("a like %''%");
        assertEquals("%'%", expr.nodes()[2].literal());

        expr = ScelParser.parse("a like '%''%'");
        assertEquals("%'%", expr.nodes()[2].literal());

        expr = ScelParser.parse("a like %''");
        assertEquals("%'", expr.nodes()[2].literal());

        expr = ScelParser.parse("a like '%'''");
        assertEquals("%'", expr.nodes()[2].literal());

        expr = ScelParser.parse("a like %''_''%");
        assertEquals("%'_'%", expr.nodes()[2].literal());

        expr = ScelParser.parse("a like '%''_''%'");
        assertEquals("%'_'%", expr.nodes()[2].literal());

        expr = ScelParser.parse("a like %''''%");
        assertEquals("%''%", expr.nodes()[2].literal());

        expr = ScelParser.parse("a like '%''''%'");
        assertEquals("%''%", expr.nodes()[2].literal());
    }

    @Test
    public void testSingleQuoteCharError() {
        try{
            ScelParser.parse("a like %'%");
        }catch (Exception e) {
            assertContains(e.getMessage(), "Invalid character \"'\"");
        }
    }

    @Test
    public void testEnvExpr() {
        ScelExpr expr = ScelParser.parse("name eq name() and gender eq 'm'");
        assertEquals("name()", expr.nodes()[2].literal());

        expr = ScelParser.parse("gender eq 'm' and name eq user.name()");
        assertEquals("user.name()", expr.nodes()[6].literal());
    }
}
