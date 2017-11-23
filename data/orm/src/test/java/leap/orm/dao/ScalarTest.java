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
package leap.orm.dao;

import leap.core.exception.EmptyRecordsException;
import leap.core.value.Scalar;
import leap.core.value.Scalars;
import leap.orm.OrmTestCase;
import leap.orm.tested.model.petclinic.Owner;

import org.junit.Test;

public class ScalarTest extends OrmTestCase {

	@Test
	public void testSimpleScalar() {
		deleteAll(Owner.class);
		
		assertNull(Owner.query().select("firstName").scalarOrNull());
		
		try {
	        Owner.query().select("firstName").scalar();
	        fail("Should throw EmptyRecordsException");
        } catch (EmptyRecordsException e) {
        	
        }
		
		new Owner().setFullName("a", "0").save();
		
		Scalar firstName = Owner.query().select("firstName").scalar();
		assertNotNull(firstName);
		assertEquals("a", firstName.get());
	}

	@Test
	public void testSimpleScalars() {
		deleteAll(Owner.class);
		
		assertTrue(Owner.query().select("firstName").scalars().isEmpty());
		assertEquals(0,Owner.query().select("firstName").scalars().size());
		
		new Owner().setFullName("a", "0").save();
		new Owner().setFullName("b", "1").save();
		
		Scalars firstNames = Owner.query().select("firstName").scalars();
		assertNotNull(firstNames);
		assertEquals(2, firstNames.size());
		
		Object[] firstNameArray = firstNames.array();
		assertEquals("a",firstNameArray[0]);
		assertEquals("b",firstNameArray[1]);
		
		Scalars lastNames = Owner.query().select("lastName").scalars();
		Integer[] lastNameArray = lastNames.array(Integer.class);
		assertEquals(new Integer(0),lastNameArray[0]);
		assertEquals(new Integer(1),lastNameArray[1]);
		
		int[] lastNameIntArray = lastNames.intArray();
		assertEquals(0,lastNameIntArray[0]);
		assertEquals(0,lastNameIntArray[0]);
	}
}