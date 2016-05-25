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

import leap.core.AppResources;
import leap.core.CoreTestCase;
import leap.lang.accessor.MapPropertyAccessor;
import leap.lang.accessor.PropertyAccessor;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;
import org.junit.Test;
import tested.beans.TBean;
import tested.beans.TInitedClass;

import java.util.List;
import java.util.Map;

public class BeanContainerTest extends CoreTestCase {

    protected void assertTestBean(TBean testBean){
        assertEquals(100, testBean.getInt1());
        assertEquals(1000, testBean.getInt2());

        List<String> listString = testBean.getListString();
        assertEquals(2, listString.size());
        assertEquals("placeholder.val1",listString.get(0));
    }

    protected void assertCollectionBean(TBean testBean, BeanContainer container){
        List<Object> list = testBean.getListObject();
        @SuppressWarnings("unchecked")
        Map<Object, Object> map = testBean.getMap();
        assertEquals(2, list.size());
        assertEquals("string1", list.get(0));
        assertEquals("string2", list.get(1));
        assertEquals(2, map.size());
        assertEquals(map.get("bean1"),container.getBean("testBean1"));
        assertEquals(map.get("bean2"),container.getBean("testBeanWithConstructor"));
    }

    @Test
    public void testLoadSimpleBeanDefinitions(){
        PropertyAccessor props  = new MapPropertyAccessor();
        props.setProperty("placeholder1", "placeholder.val1");

        BeanContainer container = new BeanContainer(props);
        ResourceSet resources = Resources.scan("classpath:/test/beans.xml");

        container.loadFromResources(AppResources.convertFrom(resources)).init();

        assertTrue(TInitedClass.inited);

        assertNotEmpty(container.getAllBeanDefinitions());
        assertNotEmpty(container.getAliasDefinitions());

        assertNotNull(container.findBeanDefinition("testBean"));
        assertNotNull(container.findBeanDefinition("testBean").getConstructorArguments());
        assertNotNull(container.findBeanDefinition("testBean").getProperties());
        assertNotNull(container.findBeanDefinition("imp.simpleBean"));

        TBean testBean = container.getBean("testBean");
        TBean collectionBean = container.getBean("testBeanWithCollectionConstructor");
        TBean definedBean = container.getBean("testBeanWithDefinedBeanConstructor");
        TBean constructArgBean = container.getBean("testBeanWithConstructorArgRefConstructor");
        TBean constructArgBean1 = container.getBean("testBeanWithConstructorArgRefConstructor1");
        Object mapBean = container.getBean("mapBean");
        assertNotNull(testBean);
        assertNotNull(collectionBean);
        assertTestBean(testBean);
        assertCollectionBean(collectionBean,container);
        assertEquals("testBeanWithDefinedBeanConstructor", definedBean.getDefinedBean().getString());
        assertEquals(constructArgBean.getDefinedBean(),collectionBean);
        assertEquals(constructArgBean1.getMap(), mapBean);
    }

}
