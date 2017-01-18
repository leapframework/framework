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

package tests.core.ioc;

import leap.core.annotation.Inject;
import leap.core.junit.AppTestBase;
import org.junit.Test;
import tested.base.order.SortBean;
import tested.base.order.SortInterface;

/**
 * Created by kael on 2016/12/28.
 */
public class TestSortOrder extends AppTestBase {
    @Inject
    private SortBean[] sortBeans;
    @Inject
    private SortInterface[] sortInterfaces;
    
    @Test
    public void testSortOrder(){
        assertNotEmpty(sortBeans);
        for(int i = 1; i < sortBeans.length; i ++){
            assertTrue(sortBeans[i].getOrder() > sortBeans[i-1].getOrder());
        }

        assertNotEmpty(sortInterfaces);
        for(int i = 1; i < sortInterfaces.length; i ++){
            assertTrue(sortInterfaces[i].getOrder() > sortInterfaces[i-1].getOrder());
        }
        
    }
}
