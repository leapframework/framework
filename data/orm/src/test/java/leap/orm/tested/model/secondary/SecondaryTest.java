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

package leap.orm.tested.model.secondary;

import leap.junit.contexual.ContextualIgnore;
import leap.lang.New;
import leap.orm.OrmTestCase;
import org.junit.Test;

import java.util.List;

@ContextualIgnore
public class SecondaryTest extends OrmTestCase {

    @Test
    public void testInsert() {
        String id = insert().getId();

        assertEquals(new Integer(1), db.queryForInteger("select count(*) from primary_table1 where id_ = ?", new Object[]{id}));
        assertEquals(new Integer(1), db.queryForInteger("select count(*) from secondary_table1 where id_ = ?", new Object[]{id}));
    }

    @Test
    public void testBatch() {
        SecondaryEntity1.deleteAll();

        SecondaryEntity1 entity1 = new SecondaryEntity1("1");
        SecondaryEntity1 entity2 = new SecondaryEntity1("2");

        SecondaryEntity1.createAll(new SecondaryEntity1[]{entity1, entity2});

        assertEquals(2, SecondaryEntity1.count());
        SecondaryEntity1 loaded1 = SecondaryEntity1.find(entity1.getId());
        assertEquals(entity1.getCol1(), loaded1.getCol1());
        assertEquals(entity1.getCol2(), loaded1.getCol2());

        entity1.setCol1("a");
        entity2.setCol2("b");
        SecondaryEntity1.updateAll(new Object[]{entity1, entity2});

        List<SecondaryEntity1> all = SecondaryEntity1.<SecondaryEntity1>query().orderBy("col1 asc").list();
        assertEquals(entity1.getCol1(), all.get(0).getCol1());
        assertEquals(entity2.getCol2(), all.get(1).getCol2());

        SecondaryEntity1.deleteAll(new Object[]{entity1.getId(), entity2.getId()});
        assertEquals(new Integer(0), db.queryForInteger("select count(*) from primary_table1"));
        assertEquals(new Integer(0), db.queryForInteger("select count(*) from secondary_table1"));
    }

    @Test
    public void testUpdate() {
        String id = insert().getId();

        assertTrue(SecondaryEntity1.update(id, New.hashMap("col1", "c1_1")));
        assertEquals("c1_1", db.queryForString("select col1_ from primary_table1 where id_ = ?", new Object[]{id}));
        assertEquals("c2",   db.queryForString("select col2_ from secondary_table1 where id_ = ?", new Object[]{id}));

        assertTrue(SecondaryEntity1.update(id, New.hashMap("col2", "c2_1")));
        assertEquals("c1_1", db.queryForString("select col1_ from primary_table1 where id_ = ?", new Object[]{id}));
        assertEquals("c2_1", db.queryForString("select col2_ from secondary_table1 where id_ = ?", new Object[]{id}));

        assertTrue(SecondaryEntity1.update(id, New.hashMap("col1", "c1_2", "col2", "c2_2")));
        assertEquals("c1_2", db.queryForString("select col1_ from primary_table1 where id_ = ?", new Object[]{id}));
        assertEquals("c2_2", db.queryForString("select col2_ from secondary_table1 where id_ = ?", new Object[]{id}));
    }

    @Test
    public void testUpdateByQuery() {
        SecondaryEntity1.deleteAll();

        SecondaryEntity1 entity1 = new SecondaryEntity1("1").create();
        SecondaryEntity1 entity2 = new SecondaryEntity1("2").create();

        assertEquals(1, SecondaryEntity1.where("col1 = ?", entity1.getCol1()).update(New.hashMap("col1", "a")));
        assertEquals("a", SecondaryEntity1.<SecondaryEntity1>find(entity1.getId()).getCol1());

        assertEquals(2, SecondaryEntity1.query().update(New.hashMap("col1","c")));
        assertEquals("c",SecondaryEntity1.<SecondaryEntity1>find(entity1.getId()).getCol1());
        assertEquals("c",SecondaryEntity1.<SecondaryEntity1>find(entity2.getId()).getCol1());

        assertEquals(2, SecondaryEntity1.query().update(New.hashMap("col2","cc")));
        assertEquals("cc",SecondaryEntity1.<SecondaryEntity1>find(entity1.getId()).getCol2());
        assertEquals("cc",SecondaryEntity1.<SecondaryEntity1>find(entity2.getId()).getCol2());
    }

    @Test
    public void testDelete() {
        String id = insert().getId();

        SecondaryEntity1.delete(id);
        assertEquals(new Integer(0), db.queryForInteger("select count(*) from primary_table1 where id_ = ?", new Object[]{id}));
        assertEquals(new Integer(0), db.queryForInteger("select count(*) from secondary_table1 where id_ = ?", new Object[]{id}));
    }

    @Test
    public void testDeleteByQuery() {
        SecondaryEntity1.deleteAll();

        SecondaryEntity1 entity1 = new SecondaryEntity1("1").create();
        SecondaryEntity1 entity2 = new SecondaryEntity1("2").create();

        assertEquals(1, SecondaryEntity1.where(" col1 = ?", entity1.getCol1()).delete());
        assertNull(SecondaryEntity1.findOrNull(entity1.getId()));
        assertNotNull(SecondaryEntity1.findOrNull(entity2.getId()));

        assertEquals(1, SecondaryEntity1.query().delete());
        assertEquals(0, SecondaryEntity1.count());
    }

    @Test
    public void testDeleteAll() {
        SecondaryEntity1.deleteAll();
        insert();
        insert();
        assertEquals(new Integer(2), db.queryForInteger("select count(*) from primary_table1"));
        assertEquals(new Integer(2), db.queryForInteger("select count(*) from secondary_table1"));

        SecondaryEntity1.deleteAll();
        assertEquals(new Integer(0), db.queryForInteger("select count(*) from primary_table1"));
        assertEquals(new Integer(0), db.queryForInteger("select count(*) from secondary_table1"));
    }

    @Test
    public void testFind() {
        SecondaryEntity1.deleteAll();

        String id = insert().getId();

        SecondaryEntity1 entity = SecondaryEntity1.find(id);
        assertEquals("c1", entity.getCol1());
        assertEquals("c2", entity.getCol2());

        entity = SecondaryEntity1.<SecondaryEntity1>findAll().get(0);
        assertEquals("c1", entity.getCol1());
        assertEquals("c2", entity.getCol2());
    }

    @Test
    public void testSimpleCriteriaQuery() {
        SecondaryEntity1.deleteAll();
        SecondaryEntity1 entity = insert();

        SecondaryEntity1 loaded = SecondaryEntity1.<SecondaryEntity1>where("col2 = ?", entity.getCol2()).alias("t1").first();
        assertEquals(entity.getCol1(), loaded.getCol1());
        assertEquals(entity.getCol2(), loaded.getCol2());

        loaded = SecondaryEntity1.<SecondaryEntity1>where("col1 = ?", entity.getCol1()).alias("t2").first();
        assertEquals(entity.getCol1(), loaded.getCol1());
        assertEquals(entity.getCol2(), loaded.getCol2());

        loaded = SecondaryEntity1.<SecondaryEntity1>where("col1 = ? and col2 = ?", entity.getCol1(), entity.getCol2()).first();
        assertEquals(entity.getCol1(), loaded.getCol1());
        assertEquals(entity.getCol2(), loaded.getCol2());
    }

    private SecondaryEntity1 insert() {
        SecondaryEntity1 entity = new SecondaryEntity1();
        entity.setCol1("c1");
        entity.setCol2("c2");
        entity.create();

        return entity;
    }

}
