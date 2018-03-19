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

import tested.base.beans.*;
import tests.core.CoreTestCase;
import leap.core.cache.Cache;
import leap.core.variable.Variable;
import leap.junit.concurrent.Concurrent;
import leap.lang.Lazy;
import org.junit.Test;
import tested.beans.*;
import tested.variables.NowVariable1;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BeanSimpleTest extends CoreTestCase {

    @Test
    public void testAliasBean() {
        Object bean      = factory.getBean(Cache.class,"test");
        Object aliasBean = factory.getBean(Cache.class,"testAlias");
        assertSame(bean, aliasBean);
    }
	
	@Test
	public void testBeanList(){
		List<TBeanType1> list = beanFactory.getBeans(TBeanType1.class);
		assertEquals(2, list.size());
		
		List<TBeanType2> list1 = beanFactory.getBeans(TBeanType2.class);
		assertEquals(3, list1.size());

        List<TBeanType2> list1_1 = beanFactory.getBeans(TBeanType2.class);
        for(int i=0;i<list1_1.size();i++) {
            assertSame(list1.get(i), list1_1.get(i));
        }
		
		List<TBeanType2> list2 = beanFactory.getBeans(TBeanType2.class, "a");
		assertEquals(2, list2.size());

        List<TBeanType2> list2_1 = beanFactory.getBeans(TBeanType2.class, "a");
        for(int i=0;i<list2_1.size();i++) {
            assertSame(list2.get(i), list2_1.get(i));
        }
	}

    @Test
    public void testBeanListWithNonSingleton(){
        List<TBeanType3> list1 = beanFactory.getBeans(TBeanType3.class);
        List<TBeanType3> list2 = beanFactory.getBeans(TBeanType3.class);
        assertEquals(list1.size(), list2.size());
        assertNotSame(list1.get(0), list2.get(0));
        assertSame(list1.get(1), list2.get(1));

        list1 = beanFactory.getBeans(TBeanType3.class, "a");
        list2 = beanFactory.getBeans(TBeanType3.class, "a");
        assertEquals(list1.size(), list2.size());
        assertSame(list1.get(0), list2.get(0));
        assertNotSame(list1.get(1), list2.get(1));
    }

    @Test
    public void testNonSingleton() {
        TNonSingletonBean bean1 = beanFactory.getBean(TNonSingletonBean.class);
        TNonSingletonBean bean2 = beanFactory.getBean(TNonSingletonBean.class);
        assertNotSame(bean1, bean2);
    }

    @Test
    public void testLazy() {
        TAutoInjectBean bean = factory.getBean(TAutoInjectBean.class);
        Lazy<TPrimaryBean1> lazyPrimaryBean = bean.lazyPrimaryBean;
        assertNotNull(lazyPrimaryBean);
        assertSame(factory.getBean(TPrimaryBean1.class), lazyPrimaryBean.get());
        assertSame(lazyPrimaryBean.get(), lazyPrimaryBean.get());

        Lazy<List<TPrimaryBean1>> lazyPrimaryBeans = bean.lazyPrimaryBeans;
        assertNotNull(lazyPrimaryBeans);

        List<TPrimaryBean1> primaryBeans = lazyPrimaryBeans.get();
        assertNotNull(primaryBeans);
        assertEquals(1, primaryBeans.size());
    }

	@Test
	public void testAnnotationBean() {
		TAnnotationBeanType abean = beanFactory.tryGetBean(TAnnotationBeanType.class);
		assertNotNull(abean);
		assertTrue(abean.getClass().equals(TAnnotationBean1.class));
		
		TAnnotationBean2 abean1 = beanFactory.tryGetBean(TAnnotationBean2.class);
		assertNotNull(abean1);
	}

    @Test
    public void testOverrideNamedBean() {
        assertEquals(NowVariable1.class,factory.getBean(Variable.class,"now").getClass());
    }

    private static final List<Object> concurrentBeans = new CopyOnWriteArrayList<>();

    @Test
    @Concurrent
    public void testConcurrentGetBean() {
        concurrentBeans.add(factory.getBean(TConcurrentBean.class));

        if(concurrentBeans.size() > 1) {
            for(int i=1;i<concurrentBeans.size();i++){
                assertSame(concurrentBeans.get(i), concurrentBeans.get(i-1));
            }
        }
    }
    @Test
    public void testDuplicateBean(){
        List<IXMLAndAnnotationBean> beans = factory.getBeans(IXMLAndAnnotationBean.class);
        assertEquals(2,beans.size());
    }
	
}