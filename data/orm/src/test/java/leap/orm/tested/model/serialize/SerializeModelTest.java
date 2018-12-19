package leap.orm.tested.model.serialize;

import leap.core.value.Record;
import leap.lang.New;
import leap.lang.convert.Converts;
import leap.orm.OrmTestCase;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import org.junit.Test;

import java.util.List;
import java.util.Map;

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

        SerializeModel m = newTestModel();

        m.create();
        assertFind(m);
        assertRecord(m);

        m.update();
        assertFind(m);
        assertRecord(m);
    }

    @Test
    public void testBatch() {
        SerializeModel.deleteAll();

        SerializeModel m = newTestModel();

        SerializeModel.createAll(new Object[]{m});
        assertFind(m);
        assertRecord(m);

        SerializeModel.updateAll(new Object[]{m});
        assertFind(m);
        assertRecord(m);
    }

    private SerializeModel newTestModel() {
        SerializeModel m = new SerializeModel();
        m.setName("Hello");
        m.setStringArray(new String[]{"item1", "item2"});
        m.setIntArray(new int[]{0,1});
        m.setIntegerArray(new Integer[]{100,200});
        m.setNestedMap(New.hashMap("a", "b"));
        m.setNestedBean(new NestedBean("k", "v"));
        return m;
    }

    private void assertRecord(SerializeModel m) {
        Record record =
                dao.createCriteriaQuery(SerializeModel.metamodel()).where("id = ?", m.id()).first();

        List<String> stringArray = record.getAs("stringArray");
        assertEquals(2, stringArray.size());
        assertEquals("item1", stringArray.get(0));
        assertEquals("item2", stringArray.get(1));

        List<Integer> intArray = record.getAs("intArray");
        assertEquals(2, intArray.size());
        assertEquals(new Integer(0), intArray.get(0));
        assertEquals(new Integer(1), intArray.get(1));

        List<Integer> integerArray = record.getAs("integerArray");
        assertEquals(2, integerArray.size());
        assertEquals(new Integer(100), integerArray.get(0));
        assertEquals(new Integer(200), integerArray.get(1));

        Map<String,Object> nestedMap = record.getAs("nestedMap");
        assertEquals(1, nestedMap.size());
        assertEquals("b", nestedMap.get("a"));

        NestedBean nestedBean = Converts.convert(record.getAs("nestedBean"),NestedBean.class);
        assertEquals("k", nestedBean.getName());
        assertEquals("v", nestedBean.getValue());
    }

    private void assertFind(SerializeModel m) {
        m = SerializeModel.find(m.id());

        String[] stringArray = m.getStringArray();
        assertEquals(2, stringArray.length);
        assertEquals("item1", stringArray[0]);
        assertEquals("item2", stringArray[1]);

        int[] intArray = m.getIntArray();
        assertEquals(2, intArray.length);
        assertEquals(0, intArray[0]);
        assertEquals(1, intArray[1]);

        Integer[] integerArray = m.getIntegerArray();
        assertEquals(2, integerArray.length);
        assertEquals(new Integer(100), integerArray[0]);
        assertEquals(new Integer(200), integerArray[1]);

        Map<String,Object> nestedMap = m.getNestedMap();
        assertEquals(1, nestedMap.size());
        assertEquals("b", nestedMap.get("a"));

        NestedBean nestedBean = m.getNestedBean();
        assertEquals("k", nestedBean.getName());
        assertEquals("v", nestedBean.getValue());
    }
}
