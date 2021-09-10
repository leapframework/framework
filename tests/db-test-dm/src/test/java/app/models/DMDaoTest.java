package app.models;

import leap.lang.New;
import leap.orm.junit.OrmTestBase;
import org.junit.Test;
import java.util.UUID;

public class DMDaoTest extends OrmTestBase {

    @Test
    public void testSchemaEntityInsert() {
        DMSchemaEntity1.deleteAll();

        dao.insert(new DMSchemaEntity1(UUID.randomUUID().toString()));
        dao.insert(DMSchemaEntity1.class, New.arrayList(new DMSchemaEntity1(UUID.randomUUID().toString()), new DMSchemaEntity1(UUID.randomUUID().toString())));

        assertEquals(3, dao.count(DMSchemaEntity1.class));
    }

}