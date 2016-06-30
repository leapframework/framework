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

package leap.orm.model;

import leap.orm.OrmTestCase;
import leap.orm.tested.model.instrument.AbstractClass;
import leap.orm.tested.model.instrument.ModelClass;
import org.junit.Test;

/**
 * Created by kael on 2016/6/29.
 */
public class InstrumentTest extends OrmTestCase {
    private static int count = 0;
    @Test
    public void testClassLoad1(){
        System.out.println(count++);
        test(ModelClass.newInstance());
    }

    private void test(AbstractClass a){
        if(a != null){
            ClassLoader c = a.getClass().getClassLoader();
            for(;c != null && c.getParent() != c;){
                System.out.println(c.getClass().getName());
                c = c.getParent();
            }
            a.doSetId("test");
            a.tryDelete();
        }else{
            System.out.println("null");
        }

    }
}
