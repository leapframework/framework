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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import leap.core.value.Record;
import leap.lang.New;
import leap.orm.OrmTestCase;
import leap.orm.tested.TestedEntity;
import leap.orm.tested.TestedIntIdentity;
import leap.orm.tested.model.petclinic.Owner;

import org.junit.Test;

public class DaoTest extends OrmTestCase {

	@Test
	public void testSimpleInsert(){
		TestedEntity entity = new TestedEntity();
		entity.setChar1('c');
		dao.insert(entity);
		
		assertNotNull(entity.getId());
		
		TestedEntity newEntity = dao.find(TestedEntity.class, entity.getId());
		assertNotNull(newEntity);
		
		assertFieldsEquals(entity, newEntity);
	}
	
	@Test
	public void testIntIdentity(){
		TestedIntIdentity entity = new TestedIntIdentity();
		
		dao.insert(entity);
		
		assertTrue(entity.getId() > 0);
		
		TestedIntIdentity newEntity = dao.find(TestedIntIdentity.class, entity.getId());
		assertNotNull(newEntity);
		
		assertFieldsEquals(entity, newEntity);
	}
	
	@Test
	public void testExecuteNamedUpdate() {
		deleteAll(Owner.class);
		
		assertEquals(1,dao.executeNamedUpdate("simpleInsertOwner", new Object[]{"a","b", new Date(),new Date()}));
		assertEquals(1, Owner.count());
		deleteAll(Owner.class);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("firstName", "c");
		params.put("lastName",  "d");
		params.put("now", new Date());
		
		assertEquals(1, dao.executeNamedUpdate("simpleInsertOwner1",params));
		assertEquals(1, Owner.count());
	}
	@Test
	public void testSqlKeyWithMapParams(){
		Map<String, Object> params = new HashMap<>();
		params.put("city","city");
		Dao.get().createNamedQuery("simpleMapParamsSql").params(params).count();
	}
	@Test
	public void testSqlIdQueryAdditionalField(){
		deleteAll(Owner.class);
		new Owner().set("firstName", "a").set("lastName","b").save();
		assertEquals(1,Owner.all().size());
		Record record = dao.createNamedQuery("simpleSqlIdWithAdditionalField").single();
		assertEquals(1,record.get("additional"));
		assertEquals(2,record.get("secondAdditional"));
	}

}