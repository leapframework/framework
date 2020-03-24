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
package leap.orm.model;

import leap.lang.New;
import leap.orm.OrmTestCase;
import leap.orm.mapping.EntityMapping;
import leap.orm.tested.model.*;
import leap.orm.tested.model.petclinic.Owner;
import org.junit.Test;

public class ModelTest extends OrmTestCase {
	@Test
	public void testUpsertModel(){
		Owner.deleteAll();
		Owner o = new Owner();
		o.setFullName("a", "0").create();
		Owner o1 = new Owner();
		o1.setId(o.getId());
		o1.setFullName("b","1");
		o1.upsert();
		o = Owner.find(o.id());
		assertEquals(o1.getFirstName(),o.getFirstName());
		assertEquals(o1.getLastName(),o.getLastName());

		Owner o2 = new Owner();
		o2.setId(o.getId());
		o2.setFullName("c","2");
		o2.setId(o1.getId()+100);
		o2.upsert();
		o = Owner.find(o2.id());
		assertEquals(o2.getFirstName(),o.getFirstName());
		assertEquals(o2.getLastName(),o.getLastName());

		Owner o3 = new Owner();
		o3.setId(o.getId());
		o3.setFullName("d","3");
		o3.upsert();
		o = Owner.find(o2.id());
		assertEquals(o3.getFirstName(),o.getFirstName());
		assertEquals(o3.getLastName(),o.getLastName());
	}

	@Test
	public void testCreatedAtAndUpdatedAt() {
		Model1.deleteAll();
		Owner.deleteAll();
		
		Owner o = new Owner();
		o.setFullName("a", "0").create();
		assertNotNull(o.createdAt());
		assertNotNull(o.updatedAt());
		
		Model1 m = new Model1().id("1").create();
		assertNotNull(m.createdAt());
		assertNotNull(m.updatedAt());
		assertSame(m.get("creationTime"), m.createdAt());
		assertSame(m.get("lastModified"), m.updatedAt());
	}
	
	@Test
	public void testNonModel() {
		EntityMapping em = metadata.tryGetEntityMapping(ModelBase.class);
		assertNull(em);
		
		em = metadata.tryGetEntityMapping(NotAModel.class);
		assertNull(em);
	}
	
	@Test
	public void testNotGenerateIntegerId() {
		deleteAll(ModelWithId4.class);
		
		ModelWithId4 m = new ModelWithId4();
		
		m.create();
		assertNotNull(ModelWithId4.find(0));
		
		m.setId(1);
		m.create();
		assertNotNull(ModelWithId4.find(1));
	}

	@Test
	public void testModelDoSetId(){
		ModelWithId4 m = new ModelWithId4();
		m.doSetId(3);
		assertEquals(3,m.getId());
	}

	@Test
	public void testCompositeIdModelSet() {
		ModelWithCompositeId mc = new ModelWithCompositeId();
		mc.set("id", "123");
		assertEquals("123", mc.getId());

		ModelWithId3 m3 = new ModelWithId3();
		m3.set("id", New.hashMap("id1", "test1", "id2", "test2"));
		assertEquals("test1", m3.getId1());
		assertEquals("test2", m3.getId2());
	}
}