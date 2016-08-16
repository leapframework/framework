package leap.orm.tested.model.serialize;

import leap.orm.OrmTestCase;
import leap.orm.mapping.EntityMapping;
import org.junit.Test;

public class SerializeModelTest extends OrmTestCase {

    @Test
    public void testComplexModelMapping() {
        EntityMapping em = SerializeModel.metamodel();

        assertNotNull(em);

    }

}
