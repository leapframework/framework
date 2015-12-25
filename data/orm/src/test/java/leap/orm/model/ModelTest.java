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

import leap.orm.OrmTestCase;
import leap.orm.mapping.EntityMapping;
import leap.orm.tested.model.ModelBase;
import leap.orm.tested.model.Model1;
import leap.orm.tested.model.ModelWithId4;
import leap.orm.tested.model.NotAModel;
import leap.orm.tested.model.petclinic.Owner;

import org.junit.Test;

public class ModelTest extends OrmTestCase {

	@Test
	public void testCreatedAtAndUpdatedAt() {
		Owner o = new Owner();
		o.setFullName("a", "0").create();
		assertNotNull(o.createdAt());
		assertNotNull(o.updatedAt());
		
		Model1 m = new Model1().create();
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
	
}