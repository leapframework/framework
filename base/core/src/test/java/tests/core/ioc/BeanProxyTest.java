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

package tests.core.ioc;

import leap.core.annotation.Inject;
import org.junit.Test;
import tested.beans.proxy.TBeanProxy;
import tested.beans.proxy.TBeanType;
import tested.beans.proxy.TBeanType1;
import tests.core.CoreTestCase;

public class BeanProxyTest extends CoreTestCase {

    private @Inject                        TBeanType bean;
    private @Inject(id = "testProxyBean1") TBeanType idBean1;
    private @Inject(id = "testProxyBean2") TBeanType idBean2;
    private @Inject(name = "bean1")        TBeanType nameBean1;
    private @Inject(name = "bean2")        TBeanType nameBean2;

    private @Inject(name = "bean1") TBeanType1 nameBean11;
    private @Inject(name = "bean2") TBeanType1 nameBean12;

    @Test
    public void testPrimaryBeanProxy() {
        assertEquals("proxy", bean.getTestValue());

        TBeanProxy proxy = (TBeanProxy)bean;
        assertNotNull(proxy.getTargetBean());
    }

    @Test
    public void testIdentifiedBeanProxy() {
        assertEquals("impl",  idBean1.getTestValue());
        assertEquals("proxy", idBean2.getTestValue());
    }

    @Test
    public void testNamedBeanProxy() {
        assertEquals("proxy", nameBean1.getTestValue());
        assertEquals("impl",  nameBean2.getTestValue());
    }

    @Test
    public void testTypedBeanProxy() {
        assertEquals(2, nameBean11.getCount());
        assertEquals(1, nameBean12.getCount());
    }
}
