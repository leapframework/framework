package app.models;

import leap.lang.New;
import leap.orm.junit.OrmTestBase;
import leap.orm.model.Model;
import org.junit.Test;

import java.util.List;

public class DMEntityModelTest extends OrmTestBase {

    @Test
    public void testDMEntity1Model() {
        Object id = DMEntity1.create(New.hashMap("id", "1", "name", "bingo")).id();

        DMEntity1 dmEntity1 = DMEntity1.find(id);
        assertEquals("bingo", dmEntity1.getName());

        List<Model> results = DMEntity1.query().list();
        assertEquals(1, results.size());

        DMEntity1.delete(id);

        assertEquals(0, DMEntity1.deleteAll());
    }

    @Test
    public void testDMEntity3Model() {
        DMEntity1.create(New.hashMap("id", "1", "name", "dmEntity1_1"));
        DMEntity1.create(New.hashMap("id", "2", "name", "dmEntity1_2"));
        DMEntity2.create(New.hashMap("id", "1", "name", "dmEntity2_1"));
        DMEntity2.create(New.hashMap("id", "2", "name", "dmEntity2_2"));

        DMEntity3.create(New.hashMap("id", "11", "entity1Id", "1", "entity2Id", "1"));
        DMEntity3.create(New.hashMap("id", "22", "entity1Id", "2", "entity2Id", "2"));

        List<Model> results = DMEntity3.query().leftJoin(DMEntity1.class, "dm1").leftJoin(DMEntity2.class, "dm2").list();
        assertEquals(2, results.size());

        DMEntity3.deleteAll();
        DMEntity1.deleteAll();
        DMEntity2.deleteAll();
    }

}
