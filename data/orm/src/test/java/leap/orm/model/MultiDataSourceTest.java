/*
 * Copyright 2015 the original author or authors.
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

import leap.junit.contexual.ContextualIgnore;
import leap.orm.OrmTestCase;
import leap.orm.dao.Dao;
import leap.orm.mapping.EntityNotFoundException;
import leap.orm.tested.model.Model2;
import leap.orm.tested.model.Model3;

import org.junit.Test;

import tested.model.Model1;

@ContextualIgnore
public class MultiDataSourceTest extends OrmTestCase {

	@Test
	public void testDataSource1Model1() {
		Model1.deleteAll();
		
		Object id = new Model1().save().id();
		
		try {
	        Dao.get().find(Model1.class, id);
	        fail("should throw exception");
        } catch (EntityNotFoundException e) {
        	
        }
		
		Model1 record = Dao.get("ds1").find(Model1.class, id);
		assertEquals(record.id(), id);
	}
	
	@Test
	public void testDataSource1Model2() {
		Model2.deleteAll();
		
		Object id = new Model2().save().id();
		
		try {
	        Dao.get().find(Model2.class, id);
	        fail("should throw exception");
        } catch (EntityNotFoundException e) {
        	
        }
		
		Model2 record = Dao.get("ds1").find(Model2.class, id);
		assertEquals(record.id(), id);
	}
	
	@Test
	public void testDataSource1Model3() {
		Model3.deleteAll();
		
		Object id = new Model3().save().id();
		
		try {
	        Dao.get().find(Model3.class, id);
	        fail("should throw exception");
        } catch (EntityNotFoundException e) {
        	
        }
		
		Model3 record = Dao.get("ds1").find(Model3.class, id);
		assertEquals(record.id(), id);
	}
	
}
