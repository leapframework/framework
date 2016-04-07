/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.core;

import leap.core.cache.Cache;
import leap.core.junit.AppTestBase;
import leap.core.variable.Variable;
import leap.lang.Lazy;
import org.junit.Test;
import tested.beans.*;
import tested.variables.NowVariable1;

import java.util.List;

public class BeanFactoryTest extends AppTestBase {

	@Test
	public void testConstructor1(){
		TestBean bean = AppContext.factory().getBean("testConstructor1");
		assertEquals("hello", bean.getString());
	}
	
	@Test
	public void testPrimaryBean(){
		assertNotNull(factory.getBean(PrimaryBean.class));
		assertEquals("1",factory.getBean(PrimaryBean1.class).getValue());
		assertEquals(factory.getBean(PrimaryBean2.class).getValue(),"2");
		
	}
	
	@Test
	public void testProfile(){
		assertNull(AppContext.factory().tryGetBean("testProfile.shouldNotCreated"));
		assertNotNull(AppContext.factory().getBean("testProfile.shouldBeCreated"));
	}
	
	@Test
	public void testAliasBean() {
		Object bean      = factory.getBean(Cache.class,"test");
		Object aliasBean = factory.getBean(Cache.class,"testAlias");
		assertSame(bean, aliasBean);
	}

	@Test
	public void testLazy() {
		InjectBean bean = factory.getBean(InjectBean.class);
		Lazy<PrimaryBean> lazyPrimaryBean = bean.lazyPrimaryBean;
		assertNotNull(lazyPrimaryBean);
		assertSame(factory.getBean(PrimaryBean.class), lazyPrimaryBean.get());
		assertSame(lazyPrimaryBean.get(), lazyPrimaryBean.get());
		
		Lazy<List<PrimaryBean>> lazyPrimaryBeans = bean.lazyPrimaryBeans;
		assertNotNull(lazyPrimaryBeans);
		
		List<PrimaryBean> primaryBeans = lazyPrimaryBeans.get();
		assertNotNull(primaryBeans);
		assertEquals(1, primaryBeans.size());
	}
	
	@Test
	public void testInjectPrivateField() {
		InjectBean bean = factory.getBean(InjectBean.class);
		assertNotNull(bean.nonGetterGetPrivateInjectPrimaryBean());
		assertNull(bean.nonGetterGetNotInjectPrimaryBean());
	}

    @Test
    public void testOverrideNamedBean() {
        assertEquals(NowVariable1.class,factory.getBean(Variable.class,"now").getClass());
    }

    @Test
    public void testPropertyInject() {
        PBean pbean = factory.getBean(PBean.class);
        assertNotNull(pbean);

        assertEquals("s1", pbean.rawStringProperty1);
        assertEquals("s1", pbean.stringProperty1.get());
        assertEquals("s2", pbean.stringProperty2.get());

        assertEquals(1, pbean.integerProperty1.get().intValue());
        assertEquals(2, pbean.integerProperty2.get().intValue());

        assertEquals(11L, pbean.longProperty1.get().longValue());
        assertEquals(12L, pbean.longProperty2.get().longValue());

        assertEquals(false, pbean.booleanProperty1.get());
        assertEquals(true,  pbean.booleanProperty2.get());

        assertEquals(new Double(1.1d), pbean.doubleProperty1.get());
        assertEquals(new Double(1.2d), pbean.doubleProperty2.get());

        assertNull(pbean.property1.get());

        String[] arrayProperty1 = pbean.arrayProperty1;
        assertNotNull(arrayProperty1);
        assertEquals(3, arrayProperty1.length);
        assertEquals("a", arrayProperty1[0]);
        assertEquals("b", arrayProperty1[1]);
        assertEquals("c", arrayProperty1[2]);
    }

}