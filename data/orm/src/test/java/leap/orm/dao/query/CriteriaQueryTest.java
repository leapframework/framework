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

import java.util.List;

import leap.junit.contexual.Contextual;
import leap.lang.Confirm;
import leap.orm.tested.model.file.Directory;
import org.junit.Test;

import leap.orm.OrmTestCase;
import leap.orm.tested.model.petclinic.Owner;

public class CriteriaQueryTest extends OrmTestCase {
	@Test
	@Contextual("mysql")
	public void testEmptyStringInInMySql(){
		List<Owner> owner = Owner.<Owner>query().where("id in :ids").param("ids",new Object[]{""}).list();
		assertTrue(owner.isEmpty());
	}

	@Test
	public void testIdInAndNotIn() {
		deleteAll(Owner.class);
		
		Owner o1 = new Owner();
		Owner o2 = new Owner();
		
		o1.setFullName("a", "0");
		o2.setFullName("b", "0");
		
		o1.save();
		o2.save();
		
		Object[] ids = new Object[]{o1.id(),o2.id()};
		
		List<Owner> owners = Owner.<Owner>query().where("id in :ids").param("ids",ids).orderBy("id asc").list();
		assertEquals(2,owners.size());
		assertEquals("a 0",owners.get(0).getFullName());
		assertEquals("b 0",owners.get(1).getFullName());
		
		//test null or empty in parameter
		assertTrue(Owner.<Owner>query().where("id in :ids").list().isEmpty());
		assertTrue(Owner.<Owner>query().where("id in :ids").param("ids",new Object[]{}).list().isEmpty());
		
		Object[] notInIds = new Object[]{-1,-2};
		owners = Owner.<Owner>query().where("id not in :ids").param("ids",notInIds).orderBy("id asc").list();
		assertEquals(2,owners.size());
		assertEquals("a 0",owners.get(0).getFullName());
		assertEquals("b 0",owners.get(1).getFullName());
		
		//test int parameter
		int[] lastNames = new int[]{0};
		owners = Owner.<Owner>query().where("lastName in :lastNames").param("lastNames", lastNames).list();
		assertEquals(2,owners.size());
		
		//test null or empty in parameter
		assertTrue(Owner.<Owner>query().where("address not in :address").list().isEmpty());
		assertTrue(Owner.<Owner>query().where("id not in :ids").list().isEmpty());
		assertTrue(Owner.<Owner>query().where("id not in :ids").param("ids",new Object[]{}).list().isEmpty());

		owners = Owner.<Owner>where().cnd("id","in",ids).q().orderByIdAsc().list();
		assertEquals(2,owners.size());
		assertEquals("a 0",owners.get(0).getFullName());
		assertEquals("b 0",owners.get(1).getFullName());
		
		owners = Owner.<Owner>where().name("id").not_in(notInIds).q().orderByIdAsc().list();
		assertEquals(2,owners.size());
		assertEquals("a 0",owners.get(0).getFullName());
		assertEquals("b 0",owners.get(1).getFullName());
		
		owners = Owner.findList(ids);
		assertEquals(2,owners.size());
		assertEquals("a 0",owners.get(0).getFullName());
		assertEquals("b 0",owners.get(1).getFullName());		
	}
	
	@Test
	public void testEqualsToIsNull() {
		deleteAll(Owner.class);
		
		new Owner().setFullName("a", "0").save();
		
		Owner o = Owner.<Owner>query().where("address = ?",new Object[]{null}).first();
		assertNotNull(o);
		
		o = Owner.<Owner>where().cnd("address","=",null).q().first();
		assertNotNull(o);
		
		o = Owner.<Owner>query().where("address = ?",new Object[]{null}).first();
		assertNotNull(o);
		
        o = Owner.<Owner>query().where("address = ?",new Object[]{""}).firstOrNull();
        assertNull(o);
		
		o = Owner.<Owner>query().where("address = ?",new Object[]{"xx"}).firstOrNull();
		assertNull(o);
	}
	
	@Test
	public void testSelectByFilter() {
		deleteAll(Owner.class);
		
		new Owner().setFullName("a", "0").save();
		
		Owner o = Owner.<Owner>query().select((f) -> f.getFieldName().equalsIgnoreCase("firstName")).first();
		assertNull(o.getLastName());
		assertEquals("a",o.getFirstName());
		assertNull(o.getId());
		
		o = Owner.<Owner>query().select("firstName").first();
		assertNull(o.getLastName());
		assertEquals("a",o.getFirstName());
		assertNull(o.getId());
	}

	@Test
	public void testColumnNameMapping() {
		deleteAll(Directory.class);

		Directory parent = new Directory();
		parent.setId("1");
		parent.setName("parent");
		dao.insert(parent);

		Directory child = new Directory();
		child.setId("2");
		child.setName("child");
		child.setParentId("1");
		dao.insert(child);

		Directory childFromDb = dao.createCriteriaQuery(Directory.class).where("parent_id = ?", parent.getId()).list().get(0);
		assertEquals(child.getId(), childFromDb.getId());
		assertEquals(child.getName(), childFromDb.getName());
		assertEquals(child.getParentId(), childFromDb.getParentId());
	}
	
}