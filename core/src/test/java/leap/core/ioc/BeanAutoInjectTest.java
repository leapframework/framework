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

import leap.core.AppConfig;
import leap.core.BeanFactory;
import leap.core.CoreTestCase;
import leap.core.annotation.Inject;
import org.junit.Test;
import tested.beans.RefBean;
import tested.beans.TAutoInjectBean;

public class BeanAutoInjectTest extends CoreTestCase {

    protected static @Inject BeanFactory factory;

    protected @Inject AppConfig config;

    protected @Inject                                TAutoInjectBean autoInjectBean;
    protected @Inject(name = "autoInjectRefElement") RefBean         autoInjectElement;
    protected @Inject(name = "autoInjectRefAttr")    RefBean         autoInjectAttr;
    protected @Inject(name = "refBean")              RefBean         refBean;

    @Test
    public void testTestCaseInject() {
        assertNotNull(config);
    }

    @Test
    public void testInjectPrivateField() {
        assertNotNull(autoInjectBean.nonGetterGetPrivateInjectPrimaryBean());
        assertNull(autoInjectBean.nonGetterGetNotInjectPrimaryBean());
    }

    @Test
    public void testInjectStatusField() {
        assertSame(config, autoInjectBean.config);
        assertNotNull(factory);
    }

    @Test
    public void testXMLRefWithTypeAndName(){
        assertNotNull(autoInjectElement);
        assertNotNull(autoInjectElement.getRefBean());
        assertEquals(autoInjectElement.getRefBean().getName(),"refbean");
        assertTrue(autoInjectElement.getRefBean() == refBean);

        assertNotNull(autoInjectAttr);
        assertNotNull(autoInjectAttr.getRefBean());
        assertEquals(autoInjectAttr.getRefBean().getName(),"refbean");
        assertTrue(autoInjectAttr.getRefBean() == refBean);
    }

}
