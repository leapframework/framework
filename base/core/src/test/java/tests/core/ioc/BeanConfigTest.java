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

import tests.core.CoreTestCase;
import leap.core.config.dyna.Property;
import org.junit.Test;
import tested.base.beans.TConfigBean;

public class BeanConfigTest extends CoreTestCase {

    @Test
    public void testConfigPropertyAutoInject() {
        assertConfigBean(factory.getBean(TConfigBean.class));
        assertConfigBean(factory.inject(new TConfigBean()));
        assertConfigBean(factory.createBean(TConfigBean.class));
    }

    protected void assertConfigBean(TConfigBean bean) {
        assertNotNull(bean);

        assertEquals("s1", bean.rawStringProperty1);
        assertEquals("s1", bean.getPublicRawStringProperty2());
        assertEquals("s1", bean.getRawStringProperty3());
        assertEquals("s1", bean.stringProperty1.get());
        assertEquals("s2", bean.stringProperty2.get());

        assertEquals(10, bean.getIntPropertyWithDefaultValue());
        assertEquals("ok", bean.getStrPropertyWithDefaultValue());
        assertTrue(bean.isBoolPropertyWithDefaultValue());
        assertEquals(2, bean.getArrayPropertyWithDefaultValue().length);
        assertEquals("a", bean.getArrayPropertyWithDefaultValue()[0]);
        assertEquals("b", bean.getArrayPropertyWithDefaultValue()[1]);

        assertEquals(1, bean.integerProperty1.get().intValue());
        assertEquals(2, bean.integerProperty2.get().intValue());

        assertEquals(11L, bean.longProperty1.get().longValue());
        assertEquals(12L, bean.longProperty2.get().longValue());

        assertEquals(false, bean.booleanProperty1.get());
        assertEquals(true,  bean.booleanProperty2.get());

        assertEquals(new Double(1.1d), bean.doubleProperty1.get());
        assertEquals(new Double(1.2d), bean.doubleProperty2.get());

        assertNull(bean.property1.get());

        String[] arrayProperty1 = bean.arrayProperty1;
        assertNotNull(arrayProperty1);
        assertEquals(3,   arrayProperty1.length);
        assertEquals("a", arrayProperty1[0]);
        assertEquals("b", arrayProperty1[1]);
        assertEquals("c", arrayProperty1[2]);

        String[] arrayPropertyWithSlash = bean.arrayPropertyWithSlash;
        assertEquals(1, arrayPropertyWithSlash.length);
        assertEquals("/test", arrayPropertyWithSlash[0]);

        Property<TConfigBean.CProp> cprop1 = bean.complexProperty1;
        assertNotNull(cprop1.get());
        assertEquals("n1", cprop1.get().name);
        assertEquals(100, cprop1.get().value);

        TConfigBean.CProp cprop2 = bean.complexProperty2;
        assertNotNull(cprop2);
        assertEquals("n2", cprop2.name);
        assertEquals(200, cprop2.value);

        //no field property
        assertEquals("1", bean.testNotFieldProperty1);
        assertEquals("2", bean.testNotFieldProperty2);
        assertEquals("3", bean.testNotFieldProperty3);

        //nested
        assertNotNull("1", bean.nested1.prop1);
        assertNotNull("2", bean.nested2.prop1);
        assertNotNull("3", bean.nested.prop1);
        assertNotNull("4", bean.nestedConfig.prop1);
    }

}
