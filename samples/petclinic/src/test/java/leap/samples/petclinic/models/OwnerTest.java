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
package leap.samples.petclinic.models;

import leap.core.junit.AppTestBase;

import org.junit.Test;

public class OwnerTest extends AppTestBase {

	@Test
	public void testCRUD(){
		//create
		Owner oldOwner = new Owner();
		oldOwner.save();
		assertNotNull(oldOwner.id());
		
		//Retrieve
		Owner newOwner = Owner.find(oldOwner.id());
		assertEquals(oldOwner.id(),newOwner.id());
		
		//Update
		newOwner.setFirstName("Test");
		newOwner.save();
		assertEquals(newOwner.getFirstName(),((Owner)Owner.find(newOwner.id())).getFirstName());
		
		//Delete
		newOwner.delete();
		assertNull(Owner.find(newOwner.id()));
	}

	@Test
	public void testFinder(){
		assertNotNull(Owner.findAllByLastNameLike("x"));
	}
}
