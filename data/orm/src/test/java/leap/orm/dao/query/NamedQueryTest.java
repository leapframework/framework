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

import leap.orm.OrmTestCase;
import leap.orm.annotation.SqlKey;
import leap.orm.dao.DaoCommand;
import leap.orm.tested.model.petclinic.Owner;
import leap.orm.tested.model.product.Product;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class NamedQueryTest extends OrmTestCase {

    protected @SqlKey("findOwnerByLastNameWithAlias") DaoCommand findOwnerByLastNameWithAlias;

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
    public void testFindOwnerByLastNameWithAlias() {
        deleteAll(Owner.class);

        new Owner().setFullName("a", "b").create();

        Owner found = findOwnerByLastNameWithAlias.createQuery(Owner.class, new Object[]{"b"}).first();
        assertEquals("a", found.getFirstName());
    }

	@Test
	public void testFindOwnersByLastNameForMap() {
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
	@Test
	public void testQueryWithResultClass(){
		Product.deleteAll();
		Product p = new Product();
		p.setId("id");
		p.setTypeId(1);
		p.create();
		List<Product> prdts = dao.createNamedQuery("queryProductWithResultClass",Product.class).list();
		prdts.forEach((prdt)->{
			assertNotNull(prdt.getId());
		});
		prdts = dao.createSqlQuery(Product.class,"select * from product").list();
		prdts.forEach((prdt)->{
			assertNotNull(prdt.getId());
		});
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
