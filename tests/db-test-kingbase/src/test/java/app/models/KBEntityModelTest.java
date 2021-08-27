package app.models;

import leap.lang.New;
import leap.orm.junit.OrmTestBase;
import leap.orm.model.Model;
import org.junit.Test;
import java.util.List;

public class KBEntityModelTest extends OrmTestBase {

    @Test
    public void testKBEntity1Model() {
        Object id = KBEntity1.create(New.hashMap("id", "1", "name", "bingo")).id();

        KBEntity1 kbEntity1 = KBEntity1.find(id);
        assertEquals("bingo", kbEntity1.getName());

        List<Model> results = KBEntity1.query().list();
        assertEquals(1, results.size());

        KBEntity1.delete(id);

        assertEquals(0, KBEntity1.deleteAll());
    }

    @Test
    public void testKBEntity3Model() {
        KBEntity1.create(New.hashMap("id", "1", "name", "kbEntity1_1"));
        KBEntity1.create(New.hashMap("id", "2", "name", "kbEntity1_2"));
        KBEntity2.create(New.hashMap("id", "1", "name", "kbEntity2_1"));
        KBEntity2.create(New.hashMap("id", "2", "name", "kbEntity2_2"));

        KBEntity3.create(New.hashMap("id", "11", "entity1Id", "1", "entity2Id", "1"));
        KBEntity3.create(New.hashMap("id", "22", "entity1Id", "2", "entity2Id", "2"));

        List<Model> results = KBEntity3.query().leftJoin(KBEntity1.class, "kb1").leftJoin(KBEntity2.class, "kb2").list();
        assertEquals(2, results.size());

        KBEntity3.deleteAll();
        KBEntity1.deleteAll();
        KBEntity2.deleteAll();
    }

}