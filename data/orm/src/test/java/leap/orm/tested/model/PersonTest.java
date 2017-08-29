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
package leap.orm.tested.model;

import leap.junit.contexual.ContextualIgnore;
import leap.orm.OrmTestCase;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.model.Model;

import org.junit.Test;

public class PersonTest extends OrmTestCase {
	
	@Test
	@ContextualIgnore
	public void testModel(){
		EntityMapping em = Person.metamodel();
		assertNotNull(em);
		assertSame(Person.class, em.getModelClass());
		
		assertNotNull(Person.dao());
		assertNotNull(Person.dmo());
		assertNotNull(Person.db());
		
		Person person = new Person().set("test", null);
		assertNotNull(person);
		
		Model model = new Person().set("test", null);
		assertNotNull(model);
		
		Person.test();
		
		Person p = Person.newInstance();
		assertNotNull(p);
	}
	
	@Test
	public void testCaseInsensitive(){
		Person p = new Person();
		p.set("Name", "n1");
		assertEquals("n1",p.get("name"));
		
		p.set("nAme", "n2");
		assertEquals("n2",p.get("NAME"));
		
		p.save();
		
		p.load();
		assertEquals("n2",p.get("name"));
	}
	
	@Test
	public void testGetterSetter(){
		Person p = Person.newInstance();
		
		p.setAddress("Addr1");
		assertEquals("Addr1", p.getAddress());
		assertEquals(p.getAddress(),p.get("address"));
		
		p.save();
	}
	
	@Test
	public void testCrud(){
		deleteAll(Person.class);
		
		Person p = new Person();
		assertTrue(p.trySave());
		assertNotNull(p.id());
		
		assertEquals(1, Person.all().size());
		
		p = Person.find(p.id());
		assertNotNull(p);
		assertTrue(p.set("name","hello").trySave());
		
		//save it again (test lock version update)
		assertTrue(p.trySave());
		
		assertTrue(p.load());
		assertEquals("hello",p.get("name"));
		
		assertTrue(p.tryDelete());
        assertNull(Person.findOrNull(p.id()));
        
        p = new Person();
        p.save();
        Person.delete(p.id());
        assertNull(Person.findOrNull(p.id()));
	}
	
	@Test
	public void testValidation(){
		EntityMapping em = Person.metamodel();
		FieldMapping  fm = em.getFieldMapping("name");

		assertNotEmpty(fm.getValidators());
		
		Person p = new Person();
		assertFalse(p.validate());
	}
}