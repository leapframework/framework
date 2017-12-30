/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package tests.core.injected;

import leap.core.annotation.Inject;
import leap.core.junit.AppTestBase;
import org.junit.Test;
import tested.base.injected.classes.BeanClass;

/**
 * @author kael.
 */
public class ClassesInjectorTest extends AppTestBase {
    
    private @Inject BeanClass bean;
    
    @Test
    public void testInjected(){
        assertEquals(2,bean.getClasses1().length);
        assertEquals(2,bean.getClasses2().size());
        assertEquals(1,bean.getClasses3().length);
        assertEquals(1,bean.getClasses4().size());
        assertEquals(0,bean.getClasses5().size());
        assertNull(bean.getClasses6());
    }
    
}
