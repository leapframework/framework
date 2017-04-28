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
import tested.beans.PrimaryTypeBean;
import tested.beans.PrimaryTypeBean3;
import tests.core.CoreTestCase;
import org.junit.Test;
import tested.beans.TPrimaryBean1;
import tested.beans.TPrimaryBean2;
import tested.beans.TPrimaryBeanType1;
import tested.beans.TPrimaryBeanType2;

public class BeanPrimaryTest extends CoreTestCase {
    
    
    @Test
    public void testDeclaredPrimaryBean(){
        assertEquals("1",factory.getBean(TPrimaryBeanType1.class).getValue());
        assertEquals(factory.getBean(TPrimaryBeanType2.class).getValue(),"2");
    }

    @Test
    public void testSelfPrimaryBean() {
        assertNotNull(factory.getBean(TPrimaryBean1.class));
        assertNotNull(factory.getBean(TPrimaryBean2.class));
    }
    @Test
    public void testPrimaryDuplicateOverride(){
        assertTrue(factory.getBean(PrimaryTypeBean.class) instanceof PrimaryTypeBean3);
    }
}
