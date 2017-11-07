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

package tests;

import app.models.Entity1;
import leap.core.annotation.Inject;
import leap.core.junit.AppTestBase;
import leap.orm.OrmContext;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.RelationMapping;
import org.junit.Test;

public class CrossDataSourceTest extends AppTestBase{

    private @Inject(name = "db1") OrmContext oc1;
    private @Inject(name = "db2") OrmContext oc2;

    @Test
    public void testManyToOne() {
        EntityMapping em = oc1.getMetadata().getEntityMapping(Entity1.class);

        RelationMapping rm = em.getRelationMappings()[0];
        assertEquals("Entity2", rm.getTargetEntityName());
        assertTrue(rm.isRemote());

        EntityMapping target = oc1.getMetadata().getEntityMapping(rm.getTargetEntityName());
        assertEquals(true, target.isRemote());
        assertEmpty(target.getRemoteType());
        assertEmpty(target.getRemoteDataSource());
    }

}
