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

package leap.orm.dao;

import leap.lang.New;
import leap.orm.OrmTestCase;
import leap.orm.tested.EmdEntity;
import org.junit.Ignore;
import org.junit.Test;

public class EmbeddedColumnsTest extends OrmTestCase {

    @Test
    @Ignore
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

        //criteria query
        dbRecord = dao.createCriteriaQuery(EmdEntity.class).first();
        assertEquals("s2", dbRecord.getC1());
        assertEquals(new Integer(2), dbRecord.getC2());

        assertEquals("s2", dao.createCriteriaQuery(EmdEntity.class).select("c1").scalar().getString());
        assertEquals(new Integer(2), dao.createCriteriaQuery(EmdEntity.class).select("c2").scalar().getInteger());

        dbRecord = dao.createCriteriaQuery(EmdEntity.class).select("name", "c1", "c2").first();
        assertEquals("s2", dbRecord.getC1());
        assertEquals(new Integer(2), dbRecord.getC2());

        //update by criteria query
        dao.createCriteriaQuery(EmdEntity.class).whereById("1").update(New.hashMap("name", "x", "c1", "s3", "c2", 3));
        dbRecord = dao.find(EmdEntity.class, "1");
        assertEquals("s3", dbRecord.getC1());
        assertEquals(new Integer(3), dbRecord.getC2());
    }

    @Test
    public void testBatchInsert() {
        dao.deleteAll(EmdEntity.class);

        EmdEntity r1 = new EmdEntity();
        r1.setId("1");
        r1.setC1("s1");
        r1.setC2(1);

        EmdEntity r2 = new EmdEntity();
        r2.setId("2");
        r2.setC1("s2");
        r2.setC2(2);

        dao.batchInsert(EmdEntity.class, New.arrayList(r1, r2));

        EmdEntity dbRecord1 = dao.find(EmdEntity.class, "1");
        assertEquals(r1.getC1(), dbRecord1.getC1());
        assertEquals(r1.getC2(), dbRecord1.getC2());

        EmdEntity dbRecord2 = dao.find(EmdEntity.class, "2");
        assertEquals(r2.getC1(), dbRecord2.getC1());
        assertEquals(r2.getC2(), dbRecord2.getC2());
    }
}
