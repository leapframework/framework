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

package leap.core.ioc;

import leap.core.AppContext;
import leap.core.CoreTestCase;
import org.junit.Test;
import tested.beans.TBean;
import tested.basepackage.beans.TConstructorBean1;
import tested.basepackage.beans.TConstructorBean2;

public class BeanConstructorTest extends CoreTestCase {

    @Test
    public void testConstructor1(){
        TBean bean = AppContext.factory().getBean("testConstructor1");
        assertEquals("hello", bean.getString());
    }

    @Test
    public void testConstructorDefaultValue() {
        TBean bean = beanFactory.getBean("testConstructorDefaultValue");
        assertEquals("defaultStringValue", bean.getString());
    }

    @Test
    public void testNoPublicConstructor() {
        TConstructorBean1 bean = beanFactory.getBean(TConstructorBean1.class);

        assertNotNull(bean.getBean());
        assertNotEmpty(bean.getTestConfigProperty());
    }

    @Test
    public void testDefaultConstructor() {
        TConstructorBean2 bean = beanFactory.getBean(TConstructorBean2.class);

        assertNotNull(bean.getBean());
        assertNotEmpty(bean.getTestConfigProperty());
    }
}
