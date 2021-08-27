package app.models;

import leap.lang.New;
import leap.orm.junit.OrmTestBase;
import org.junit.Test;

public class KBComJdbcTypeTest extends OrmTestBase {

    @Test
    public void testCommonJdbcType() {
        KBColumnTypes types = new KBColumnTypes();
        types.setNumber(123456);
        types.setEnabled(true);
        types.setMap(New.hashMap("name", "test"));
        KBEntity1 entity1 = new KBEntity1();
        entity1.setId("1");
        entity1.setName("bingo");
        types.setKbEntity1(entity1);
        Object id = types.create();

        KBColumnTypes dmComJdbcType = KBColumnTypes.find(id);
        assertNotNull(dmComJdbcType.getNumber());
        assertNotNull(dmComJdbcType.getCreatedAt());
        assertTrue(dmComJdbcType.getEnabled());
        assertEquals("test", types.getMap().get("name"));
        assertEquals("1", dmComJdbcType.getKbEntity1().getId());
        assertEquals("bingo", dmComJdbcType.getKbEntity1().getName());

        KBColumnTypes.deleteAll();
    }

}