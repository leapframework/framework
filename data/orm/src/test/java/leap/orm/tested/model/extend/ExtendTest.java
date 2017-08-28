/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.orm.tested.model.extend;

import leap.orm.OrmTestCase;
import leap.orm.mapping.EntityMapping;
import org.junit.Test;

public class ExtendTest extends OrmTestCase {

    @Test
    public void testExtendModelMapping() {
        EntityMapping em1 = metadata.getEntityMapping(ExtendModelBase.class);
        EntityMapping em2 = metadata.getEntityMapping(ExtendModelEx.class);

        assertSame(em1, em2);

        assertEquals(ExtendModelBase.class, em1.getEntityClass());
        assertEquals(ExtendModelEx.class, em1.getExtendedEntityClass());

        assertEquals(3, em1.getFieldMappings().length);
        assertNotNull(em1.getFieldMapping("col2"));
    }

    @Test
    public void testExtendModelDao() {
        ExtendModelEx m1 = new ExtendModelEx();
        m1.setCol1("c1");
        m1.setCol2("c2");
        m1.create();

        ExtendModelBase m2 = ExtendModelBase.find(m1.id());
        assertEquals(m1.getCol1(), m2.getCol1());

        m2.delete();
    }

}
