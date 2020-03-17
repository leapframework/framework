package app.models;

import leap.orm.junit.OrmTestBase;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;

public class DMComJdbcTypeTest extends OrmTestBase {

    @Test
    public void testCommonJdbcType() {
        Map dm = new HashMap();
        dm.put("number", 123456);
        dm.put("enabled", true);
        Object id = DMComJdbcType.create(dm).id();

        DMComJdbcType dmComJdbcType = DMComJdbcType.find(id);
        assertNotNull(dmComJdbcType.getNumber());
        assertNotNull(dmComJdbcType.getCreatedAt());
        assertTrue(dmComJdbcType.getEnabled());

        DMComJdbcType.deleteAll();
    }

}
