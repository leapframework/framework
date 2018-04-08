/*
 * Copyright 2013 the original author or authors.
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
package leap.orm.model;

import leap.lang.New;
import leap.orm.OrmTestCase;
import leap.orm.tested.model.petclinic.Owner;

import org.junit.Test;

public class ModelUpdateTest extends OrmTestCase {
	
	@Test
	public void testStaticUpdate() {
		Owner owner = Owner.create(New.hashMap("firstName","a","lastName","0"));
		
		assertNotNull(owner);
		assertEquals("a",owner.getFirstName());
		assertEquals("0",owner.getLastName());

		assertTrue(Owner.update(owner.id(), New.hashMap("lastName","1")));
		
		owner.load();
		assertEquals("1",owner.getLastName());
	}
	
	@Test
	public void testDeleteAllWithCondition() {
		initData();
		
		long count = Owner.count();
		
		assertEquals(1,Owner.deleteAll("firstName = 'a'"));
		assertEquals(1,Owner.deleteAll("firstName = ?","b"));
		//assertEquals(1,Owner.<Owner>deleteAll(o -> o.getFirstName() == "c"));
		assertEquals(1,Owner.deleteAll("firstName = :firstName",New.hashMap("firstName", "d")));
		
		assertEquals(count-3,Owner.count());
	}
	
	@Test
	public void testUpdateAllWithCondition() {
		initData();

		Owner o = new Owner();
		o.setLastName("1");
		
		assertEquals(1,o.updateAll("firstName = 'a'"));
	}
	
	@Test
	public void testStaticUpdateAll() {
		initData();
		
		assertEquals(1,Owner.updateAll(New.hashMap("firstName","a"), "firstName = ?", "b"));
		
		assertEquals(2,Owner.where("firstName = ?","a").count());
		
		assertEquals(3,Owner.updateAll(New.hashMap("lastName","1"), "firstName in ?", new Object[]{new Object[]{"e","f","g"}}));
	}
	
	protected void initData() {
		Owner.deleteAll();
		
		new Owner().setFullName("a", "0").save();
		new Owner().setFullName("b", "0").save();
		new Owner().setFullName("c", "0").save();
		new Owner().setFullName("d", "0").save();
		new Owner().setFullName("e", "0").save();
		new Owner().setFullName("f", "0").save();
		new Owner().setFullName("g", "0").save();
	}
}