/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.core.aop;

import leap.core.annotation.Inject;
import leap.core.aop.MethodInvocation;
import leap.core.junit.AppTestBase;
import leap.lang.Types;
import leap.lang.enums.Bool;
import org.junit.Test;
import tested.aop.TAopBean;
import tested.aop.TAopInterceptor1;
import tested.aop.TAopInterceptor2;
import tested.aop.TIntercepted;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;

public class MethodInterceptionTest extends AppTestBase {

    protected @Inject TAopBean         bean;
    protected @Inject TAopInterceptor1 interceptor1;
    protected @Inject TAopInterceptor2 interceptor2;

    @Test
    public void testHelloInterceptor() throws Exception {
        interceptor1.resetInterceptedCount();
        interceptor2.resetInterceptedCount();

        assertNull(bean.getLastHello());
        bean.hello();
        assertEquals("Hello aop", bean.getLastHello());
        assertEquals(1, interceptor1.getInterceptedCount());
        assertEquals(1, interceptor2.getInterceptedCount());

        MethodInvocation invocation = interceptor1.getLastInvocation();
        assertNotNull(invocation);
        Method method = invocation.getMethod();
        TIntercepted a = method.getAnnotation(TIntercepted.class);
        assertNotNull(a);
        assertEquals("s",a.s());
        assertEquals(Bool.FALSE, a.b());
        assertEquals(TAopBean.class, a.c());
        assertEquals(10, a.i());

        assertEquals("hello aop$intercepted",   bean.getHello());
        assertEquals("hello world$intercepted", bean.getHello("world"));

        assertEquals(3, interceptor1.getInterceptedCount());
        assertEquals(3, interceptor2.getInterceptedCount());
    }

    @Test
    public void testException() {
        try {
            bean.testException("hello");

            fail("Should throw exception");
        }catch(IOException e) {
            fail("Should not throw I/O exception");
        }catch(Exception e) {
            assertEquals("Just a test", e.getMessage());
        }
    }

    @Test
    public void testNested() {
        interceptor1.resetInterceptedCount();

        bean.perfRoot();

        assertEquals(50, interceptor1.getInterceptedCount());
    }

    @Test
    public void testComplexMethod() throws Exception {
        Method m = TAopBean.class.getMethod("complex", String.class, List.class);

        //Test annotation in parameter.
        Parameter p1 = m.getParameters()[0];
        assertEquals(2, p1.getAnnotations().length);

        //Test generic type in parameter.
        Type p2GenericType = m.getGenericParameterTypes()[1];
        assertEquals(String.class,Types.getActualTypeArgument(p2GenericType));

        //Test annotation in method
        assertEquals(2, m.getAnnotations().length);

        //Test generic type in return type.
        assertEquals(Integer.class, Types.getActualTypeArgument(m.getGenericReturnType()));
    }
}
