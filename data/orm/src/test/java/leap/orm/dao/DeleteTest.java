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
package leap.orm.dao;

import org.junit.Test;

import leap.orm.OrmTestCase;
import leap.orm.tested.model.petclinic.Owner;

public class DeleteTest extends OrmTestCase {
	
	@Test
	public void testDeleteModel() {
		deleteAll(Owner.class);
		
		Owner owner = new Owner();
		owner.setFirstName("a");
		owner.setLastName("b");
		owner.save();
		
		assertEquals(1,Owner.count());
		assertEquals(1,Owner.deleteBy("firstName", "a"));
		assertEquals(0,Owner.count());
	}
	
	@Test
	public void testDeleteIn() {
		deleteAll(Owner.class);
		
		Owner owner = new Owner();
		owner.setFirstName("a");
		owner.setLastName("b");
		owner.save();
		
		Owner owner1 = new Owner();
		owner1.setFirstName("c");
		owner1.setLastName("d");
		owner1.save();
		
		assertEquals(2,Owner.count());
		
		Owner.where("id in :ids").param("ids",new Object[]{owner.id(),owner1.id()}).delete();
		
		assertEquals(0,Owner.count());
		
	}
	
}
