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

package leap.orm.sql;

import leap.core.value.Record;
import leap.junit.contexual.Contextual;
import leap.lang.New;
import leap.orm.OrmTestCase;
import leap.orm.tested.model.petclinic.Owner;
import org.junit.Test;

public class ProcedureTest extends OrmTestCase {

    @Test
    @Contextual("mysql")
    public void testUpdateNamedProcedure() {
        deleteAll(Owner.class);

        int affected = dao.executeNamedUpdate("testProcedureExecuteUpdate", New.hashMap("p1", "a"));
        assertEquals(0, affected);

        new Owner().setFullName("a", "b").create();
        affected = dao.executeNamedUpdate("testProcedureExecuteUpdate", New.hashMap("p1", "a"));
        assertEquals(1, affected);

        affected = dao.executeNamedUpdate("testProcedureExecuteUpdate", New.hashMap("p1", "aaa"));
        assertEquals(0, affected);
    }

    @Test
    @Contextual("mysql")
    public void testQueryNamedProcedure() {
        deleteAll(Owner.class);

        Record record;

        record = dao.createNamedQuery("testProcedureExecuteQuery")
                            .param("p1", "a")
                            .firstOrNull();
        assertNull(record);

        new Owner().setFullName("a", "b").create();
        record = dao.createNamedQuery("testProcedureExecuteQuery")
                    .param("p1", "a")
                    .firstOrNull();

        assertNotNull(record);
        assertEquals("b", record.get("lastName"));
    }

}
