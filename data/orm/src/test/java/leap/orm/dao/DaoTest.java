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

import leap.core.value.Record;
import leap.db.platform.oracle.OraclePlatform;
import leap.orm.OrmTestCase;
import leap.orm.mapping.EntityMapping;
import leap.orm.tested.NoIdEntity;
import leap.orm.tested.NoIdModel;
import leap.orm.tested.TestedEntity;
import leap.orm.tested.TestedIntIdentity;
import leap.orm.tested.model.petclinic.Owner;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
	public void testExists() {
		TestedEntity entity = new TestedEntity();
		entity.setChar1('c');
		dao.insert(entity);

		assertNotNull(entity.getId());

		boolean isexist = dao.exists(TestedEntity.class, entity.getId());
		assertTrue(isexist);
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
		if(db.getPlatform() instanceof OraclePlatform){
			// oracle must specified id but other database is not need.
			assertEquals(1,dao.executeNamedUpdate("simpleInsertOwner_oracle", new Object[]{1,"a","b", new Date(),new Date()}));
		}else{
			assertEquals(1,dao.executeNamedUpdate("simpleInsertOwner", new Object[]{"a","b", new Date(),new Date()}));
		}
		assertEquals(1, Owner.count());
		deleteAll(Owner.class);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id",2);
		params.put("firstName", "c");
		params.put("lastName",  "d");
		params.put("now", new Date());
		if(db.getPlatform() instanceof OraclePlatform){
			assertEquals(1, dao.executeNamedUpdate("simpleInsertOwner1_oracle",params));
		}else{
			assertEquals(1, dao.executeNamedUpdate("simpleInsertOwner1",params));
		}
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
		assertEquals(new Integer(1),record.getInteger("additional"));
		assertEquals(new Integer(2),record.getInteger("secondAdditional"));
	}

    @Test
    public void testNoIdEntity() {
        EntityMapping em = metadata.getEntityMapping(NoIdEntity.class);
        assertEquals(0, em.getKeyFieldNames().length);

        NoIdEntity entity = new NoIdEntity();
        entity.setCol1("col1");
        dao.insert(entity);

        assertEquals(entity.getCol1(),dao.createCriteriaQuery(NoIdEntity.class).where("col1 =?", entity.getCol1()).first().getCol1());
    }

    @Test
    public void testNoIdModel() {
        EntityMapping em = metadata.getEntityMapping(NoIdModel.class);
        assertEquals(0, em.getKeyFieldNames().length);

        NoIdModel entity = new NoIdModel();
        entity.setCol1("col1");
        dao.insert(entity);

        assertEquals(entity.getCol1(),dao.createCriteriaQuery(NoIdModel.class).where("col1 =?", entity.getCol1()).first().getCol1());
    }
}