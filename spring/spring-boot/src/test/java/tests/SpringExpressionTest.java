/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tests;

import leap.core.el.ExpressionLanguage;
import leap.lang.New;
import leap.lang.expression.Expression;
import leap.lang.time.StopWatch;
import leap.spring.boot.spel.SpringExpressionFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.Map;

public class SpringExpressionTest extends AbstractTest {

    @Autowired
    protected SpringExpressionFactory sef;

    @Autowired
    protected ExpressionLanguage el;

    private Object eval(String expr) {
        return sef.createExpression(expr).getValue();
    }

    private Object eval(String expr, Map<String, Object> vars) {
        return sef.createExpression(expr).getValue(vars);
    }

    @Test
    public void testSimple() {
        assertNotNull(sef);
        assertEquals(new Integer(1), eval("1"));
        assertEquals(new Integer(1), eval("#{1}"));
        assertEquals("11",           eval("#{1}1"));
    }

    @Test
    public void testEnv() {
        Object now = eval("env.now");
        assertNotNull(now);
        assertTrue(now instanceof Timestamp);

        Object hello = eval("env.hello");
        assertEquals("hello", hello);
    }

    @Test
    public void testBean() {
        Object v = eval("@helloBean.test()");
        assertEquals("hello", v);

        v = eval("@hello.test()");
        assertEquals("hello", v);

        v = eval("@helloBean.testStatic()");
        assertEquals("hello static", v);
    }

    @Test
    public void testVariables() {
        assertEquals("a", eval("name", New.hashMap("name", "a")));
    }

    @Test
    @Ignore
    public void testPerf() {
        Expression e1 = el.createExpression("simple.name  + 's'");
        Expression e2 = sef.createExpression("simple.name + 's'");

        Map vars = New.hashMap("simple", new Simple("test"));

        assertEquals("tests", e1.getValue(vars));
        assertEquals("tests", e2.getValue(vars));

        for(int i=0;i<5;i++) {
            runPerf("leap", e1, vars);
            runPerf("spel", e2, vars);
            System.out.println();
            runPerf("spel", e2, vars);
            runPerf("leap", e1, vars);
            System.out.println();
        }
    }

    private void runPerf(String type, Expression expr, Map vars) {
        int count = 50000;

        StopWatch sw = StopWatch.startNew();
        for(int i=0;i<count;i++) {
            expr.getValue(vars);
        }

        System.out.println(type + " : " + sw.getElapsedMilliseconds() + "ms");
    }

    private static class Simple {
        private String name;

        public Simple(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}

