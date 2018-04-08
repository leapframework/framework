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

package leap.orm.tested;

import leap.orm.junit.OrmTestBase;
import leap.orm.tested.model.Owner;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kael.
 */
public class NullValueTest extends OrmTestBase {
    @Ignore
    @Test
    public void testNullParamQuery(){
        Owner.query("queryWithNullParams").param("lastName",null).list();
        Map<String, Object> p = new HashMap<>();
        p.put("lastName",null);
        Owner.query("queryWithNullParams").params(p).list();
        
    }
    
}
