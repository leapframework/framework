package app.models;

import leap.lang.New;
import leap.orm.junit.OrmTestBase;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;

public class DMComJdbcTypeTest extends OrmTestBase {

    @Test
    public void testCommonJdbcType() {
        DMColumnTypes types = new DMColumnTypes();
        types.setNumber(123456);
        types.setEnabled(true);
        types.setMap(New.hashMap("name", "test"));
        DMEntity1 entity1 = new DMEntity1();
        entity1.setId("1");
        entity1.setName("bingo");
        types.setDmEntity1(entity1);
        Object id = types.create();

        DMColumnTypes dmComJdbcType = DMColumnTypes.find(id);
        assertNotNull(dmComJdbcType.getNumber());
        assertNotNull(dmComJdbcType.getCreatedAt());
        assertTrue(dmComJdbcType.getEnabled());
        assertEquals("test", types.getMap().get("name"));
        assertEquals("1", dmComJdbcType.getDmEntity1().getId());
        assertEquals("bingo", dmComJdbcType.getDmEntity1().getName());

        DMColumnTypes.deleteAll();
    }

}