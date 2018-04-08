/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package tests.core.ioc;

import org.junit.Test;
import tested.beans.remove.RemoveType;
import tests.core.CoreTestCase;

import java.util.List;

public class BeanRemoveTest extends CoreTestCase {

    @Test
    public void testRemoveId() {
        assertNull(factory.tryGetBean("remove_id"));
    }

    @Test
    public void testRemoveTypeAndName() {
        assertNull(factory.tryGetBean(RemoveType.class,"remove_name"));
    }

    @Test
    public void testRemoveTypeAndClass() {
        List<RemoveType> beans = factory.getBeans(RemoveType.class);
        assertEquals(0, beans.size());
    }

}