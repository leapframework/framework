/*
 *
 *  * Copyright 2016 the original author or authors.
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

package leap.orm.tested.model;

import leap.core.value.Record;
import leap.junit.contexual.Contextual;
import leap.orm.OrmTestCase;
import org.junit.Test;

import java.util.List;

public class VarbinaryModelTest extends OrmTestCase {

    @Test
    @Contextual("mysql")
    public void testVarbinaryBool() {
        VarbinaryModel.deleteAll();

        VarbinaryModel recordFalse = new VarbinaryModel();
        recordFalse.b = false;
        recordFalse.s = "abcd";
        recordFalse.create();

        VarbinaryModel recordTrue = new VarbinaryModel();
        recordTrue.b = true;
        recordTrue.s = "bcd中文";
        recordTrue.create();

        List<Record> records =
                dao.createSqlQuery("select * from (select id, b,s from VarbinaryModel union select '', 0, '' from dual) t order by t.s").list();

        assertEquals(3, records.size());

        Record record0 = records.get(0);
        assertEquals("0", record0.get("b"));

        Record record1 = records.get(1);
        assertEquals("0", record1.get("b"));
        assertEquals("abcd", record1.get("s"));

        Record record2 = records.get(2);
        assertEquals("1", record2.get("b"));
        assertEquals("bcd中文", record2.get("s"));
    }

}
