/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests;

import app.beans.IfBean;
import app.beans.TestAppConfigProcessor;
import leap.core.BeanFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SpringPropertiesTest extends AbstractTest {

    @Autowired
    protected BeanFactory factory;

    @Test
    public void testBeanIf() {
        assertNull(factory.tryGetBean(IfBean.class, "if1"));
        assertNull(factory.tryGetBean(IfBean.class, "if2"));
        assertNotNull(factory.tryGetBean(IfBean.class, "if3"));
    }

    @Test
    public void testSpringPropertyAtConfigProcessor() {
        assertEquals("b", TestAppConfigProcessor.getProperties().get("name"));
    }
}
