package leap.orm.tested.model.serialize;

import leap.orm.OrmTestCase;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import org.junit.Test;

public class SerializeModelTest extends OrmTestCase {

    @Test
    public void testSerializeModelMapping() {
        EntityMapping em = SerializeModel.metamodel();

        assertNotNull(em);

        FieldMapping fm = em.getFieldMapping("stringArray");
        assertNotNull(fm.getSerializer());

    }

    @Test
    public void testSimpleCRUD() {
        SerializeModel.deleteAll();

        SerializeModel m = new SerializeModel();
        m.setName("Hello");

        m.create();
        m.delete();

        m = new SerializeModel();
        m.setName("Hello");
        m.setStringArray(new String[]{"item1", "item2"});
        m.create();

        m = SerializeModel.find(m.id());
        String[] stringArray = m.getStringArray();
        assertEquals(2, stringArray.length);
        assertEquals("item1", stringArray[0]);
        assertEquals("item2", stringArray[1]);
    }
}
