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

import leap.core.AppConfig;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import org.junit.BeforeClass;
import org.junit.Test;
import tested.base.factorybean.*;
import tested.base.injected.AbstractInjectBean;
import tested.base.injected.InjectBean;
import tested.base.injected.InjectInterface1;
import tested.base.injected.InjectInterface2;
import tested.beans.TAutoInjectBean;
import tests.core.CoreTestCase;

public class BeanAutoInjectTest extends CoreTestCase {

    protected static @Inject BeanFactory factory;

    protected @Inject AppConfig       config;
    protected @Inject TAutoInjectBean autoInjectBean;

    protected @Inject InjectBean injectBean;
    protected @Inject AbstractInjectBean abstractInjectBean;
    protected @Inject InjectInterface1 injectBeanInterface1;
    protected @Inject InjectInterface2 injectBeanInterface2;
    
    protected @Inject CusBean cusBean;
    protected @Inject CusBean1 cusBean1;
    protected @Inject CusBean2 cusBean2;
    
    @BeforeClass
    public static void checkStaticInjection() {
        assertNotNull(factory);
    }

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
    public void testInjectNotWritableProperty() {
        assertNotNull(autoInjectBean.getPrimaryBean1());
    }

    @Test
    public void testInjectStaticField() {
        assertSame(config, autoInjectBean.config);
    }
    
    @Test
    public void testInjectBeanWithImplement(){
        assertNotNull(injectBeanInterface1);
        assertNull(injectBeanInterface2);
        assertNotNull(abstractInjectBean);
        assertNotNull(injectBean);
        
        assertNotNull(cusBean);
        assertNotNull(cusBean1);
        assertNotNull(cusBean2);
        assertEquals(CusFactoryBean.class.getName(),cusBean.getCreateBy());
        assertEquals(CusFactoryBean12.class.getName(),cusBean1.getCreateBy());
        assertEquals(CusFactoryBean12.class.getName(),cusBean2.getCreateBy());
    }
}
