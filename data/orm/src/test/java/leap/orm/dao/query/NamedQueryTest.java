/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.orm.dao.query;

import java.util.Map;

import leap.orm.OrmTestCase;
import leap.orm.tested.model.petclinic.Owner;

import org.junit.Test;

public class NamedQueryTest extends OrmTestCase {
	@Test
	public void testFindOwnersByLastName() {
		deleteAll(Owner.class);

		assertTrue(dao.createNamedQuery(Owner.class, "findByLastName").param("lastName", "test").result().isEmpty());

		Owner older = dmo.getDataFactory().generate(Owner.class);
		older.setLastName("test");

		dao.insert(older);

		Owner newer = dao.createNamedQuery(Owner.class, "findByLastName").params(older).single();
		newer.getPets();//force to create pets set

		compareFields(older, newer.fields());
	}

	@Test
	public void testFindOwenrsByLastNameForMap() {
		deleteAll(Owner.class);

		assertTrue(dao.createNamedQuery("findOwnerByLastName").param("lastName", "test").result().isEmpty());
		assertTrue(dao.createNamedQuery("findOwnerByLastNameSimple").param("lastName", "test").result().isEmpty());

		Owner older = dmo.getDataFactory().generate(Owner.class);
		older.setLastName("test");

		dao.insert(older);

        Map<String, Object> newer = dao.createNamedQuery("findOwnerByLastName").params(older).single();
        compareFields(older, newer);

        newer = dao.createNamedQuery("findOwnerByLastNameSimple").params(older).single();
        compareFields(older, newer);
	}

	protected void compareFields(Owner older, Map<String, Object> newer) {
        Map<String, Object> olderFields = older.fields();
        Map<String, Object> newerFields = newer;

        if (db.isMySql()) {
            //TODO : the mill-seconds problem of mysql
            olderFields.remove("createdAt");
            newerFields.remove("createdAt");
            olderFields.remove("updatedAt");
            newerFields.remove("updatedAt");
        }

        assertMapEquals(olderFields, newerFields);
	}
}
