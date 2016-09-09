/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.orm.mapping;

import leap.orm.OrmTestCase;
import leap.orm.tested.model.api.Api;
import leap.orm.tested.model.api.ApiCategory;
import leap.orm.tested.model.api.ApiPath;
import leap.orm.tested.model.api.Category;
import org.junit.Test;

public class RelationPropertyTest extends OrmTestCase {

    @Test
    public void testOneToMany() {
        EntityMapping em = Api.metamodel();

        RelationProperty p = em.tryGetRelationProperty("paths");
        assertNotNull(p);
        assertTrue(p.isMany());
        assertEquals(ApiPath.metamodel().getEntityName(), p.getTargetEntityName());
    }

    @Test
    public void testManyToManyByJoinEntity() {
        EntityMapping em = Api.metamodel();

        RelationProperty p = em.tryGetRelationProperty("categories");
        assertNotNull(p);
        assertTrue(p.isMany());
        assertEquals(Category.metamodel().getEntityName(), p.getTargetEntityName());
        assertEquals(ApiCategory.metamodel().getEntityName(), p.getJoinEntityName());
    }

}
