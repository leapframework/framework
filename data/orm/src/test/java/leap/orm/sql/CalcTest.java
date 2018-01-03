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

package leap.orm.sql;

import leap.core.value.Record;
import leap.junit.contexual.Contextual;
import leap.orm.OrmTestCase;
import leap.orm.tested.model.CalcModel;
import org.junit.Test;

import java.util.List;

/**
 * @author kael.
 */
public class CalcTest extends OrmTestCase {
    @Test
    @Contextual("h2")
    public void testCalc(){
        CalcModel.deleteAll();
        for(int i = 0; i < 5; i++){
            CalcModel cm = new CalcModel();
            cm.setFirst(i);
            cm.setSecond(i+1);
            cm.setThird(i+2);
            cm.setFourth(i+3);
            cm.create();
        }
        
        List<Record> recordList = dao.createNamedQuery("calc.CalcModel").list();
        assertTrue(recordList.get(0).containsKey("seat_percent"));
    }
}
