package app.models;

import leap.core.value.Record;
import leap.lang.New;
import leap.lang.json.JSON;
import leap.orm.junit.OrmTestBase;
import leap.orm.mapping.EntityMapping;
import org.junit.Test;

public class DMComJdbcTypeTest extends OrmTestBase {

    @Test
    public void testCommonJdbcType() {
        DMColumnTypes types = new DMColumnTypes();
        types.setNumber(123456);
        types.setEnabled(true);
        types.setMap(New.hashMap("name", "test"));
        String clob = JSON.encode(types.getMap());
        types.setClob(clob);
        DMEntity1 entity1 = new DMEntity1();
        entity1.setId("1");
        entity1.setName("bingo");
        types.setDmEntity1(entity1);

        Object result = types.create();

        // read as class
        DMColumnTypes dmComJdbcType = DMColumnTypes.find(result);
        assertNotNull(dmComJdbcType.getNumber());
        assertNotNull(dmComJdbcType.getCreatedAt());
        assertTrue(dmComJdbcType.getEnabled());
        assertEquals("test", types.getMap().get("name"));
        assertEquals("1", dmComJdbcType.getDmEntity1().getId());
        assertEquals("bingo", dmComJdbcType.getDmEntity1().getName());
        assertEquals(clob, dmComJdbcType.getClob());

        // read as map
        EntityMapping em = dao.getOrmContext().getMetadata().tryGetEntityMapping(DMColumnTypes.class);
        Record record = dao.createSqlQuery("select * from " + em.getTableName() + " where id = ?", types.getId()).first();
        assertTrue(record.get("clob").getClass().isAssignableFrom(String.class));
        assertEquals(clob, record.get("clob"));

        DMColumnTypes.deleteAll();
    }

}