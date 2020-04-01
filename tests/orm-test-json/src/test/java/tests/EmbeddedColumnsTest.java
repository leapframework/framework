/*
 *  Copyright 2020 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package tests;

import app.EmdEntity;
import leap.core.annotation.Inject;
import leap.core.junit.AppTestBase;
import leap.lang.New;
import leap.orm.dao.Dao;
import org.junit.Test;

public class EmbeddedColumnsTest extends AppTestBase {

    @Inject
    protected Dao dao;

    @Test
    public void testSimpleCRUD() {
        dao.deleteAll(EmdEntity.class);

        EmdEntity record = new EmdEntity();
        record.setId("1");
        record.setC1("s1");
        record.setC2(1);

        //insert and find
        dao.insert(record);
        EmdEntity dbRecord = dao.find(EmdEntity.class, "1");
        assertEquals(record.getC1(), dbRecord.getC1());
        assertEquals(record.getC2(), dbRecord.getC2());

        //update and find
        dao.cmdUpdate(EmdEntity.class).withId("1").setAll(New.hashMap("c1", "s2")).execute();
        dbRecord = dao.find(EmdEntity.class, "1");
        assertEquals("s2", dbRecord.getC1());
        assertEquals(record.getC2(), dbRecord.getC2());

        dao.cmdUpdate(EmdEntity.class).withId("1").setAll(New.hashMap("c2", 2)).execute();
        dbRecord = dao.find(EmdEntity.class, "1");
        assertEquals("s2", dbRecord.getC1());
        assertEquals(new Integer(2), dbRecord.getC2());
    }
}
