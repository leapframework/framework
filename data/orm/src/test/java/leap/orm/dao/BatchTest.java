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

import leap.lang.New;
import leap.orm.OrmTestCase;
import leap.orm.tested.model.petclinic.Owner;

import org.junit.Test;

import java.sql.Statement;
import java.util.List;

public class BatchTest extends OrmTestCase {
	
	@Test
	public void testBatchInsert() {
		deleteAll(Owner.class);
		
		Owner o1 = new Owner();
		o1.setFirstName("a");
		o1.setLastName("b");
		
		Owner o2 = new Owner();
		o2.setFirstName("c");
		o2.setLastName("d");
		
		assertSuccess(Owner.createAll(new Object[]{o1,o2}));

		o1.setId(null);
		o1.setId(null);
		assertSuccess(dao.batchInsert(New.arrayList(o1, o2)));

		o1.setId(null);
		o1.setId(null);
		assertSuccess(dao.batchInsert(new Owner[]{o1, o2}));

		o1.setId(null);
		o1.setId(null);
		assertSuccess(dao.batchInsert(Owner.class, New.arrayList(o1, o2)));

		o1.setId(null);
		o1.setId(null);
		assertSuccess(dao.batchInsert(Owner.class, new Object[]{o1, o2}));

		o1.setId(null);
		o1.setId(null);
		assertSuccess(dao.batchInsert("Owner", New.arrayList(o1, o2)));

		o1.setId(null);
		o1.setId(null);
		assertSuccess(dao.batchInsert("Owner", new Object[]{o1, o2}));

		Owner[] owners = new Owner[]{};
		int[] results = dao.batchInsert(owners);
		assertEquals(0, results.length);
	}

	protected void assertSuccess(int[] results) {
		assertEquals(2,results.length);
		// batch insert will return Statement.SUCCESS_NO_INFO in oracle
		// batch insert will return update count giving the number of rows in other database
		assertTrue(Statement.SUCCESS_NO_INFO == results[0] || 1 == results[0]);
		assertTrue(Statement.SUCCESS_NO_INFO == results[1] || 1 == results[1]);
	}

	@Test
	public void testBatchUpdate() {
		deleteAll(Owner.class);

		Owner o1 = new Owner();
		o1.setFirstName("a");
		o1.setLastName("b");

		Owner o2 = new Owner();
		o2.setFirstName("c");
		o2.setLastName("d");

		//Create first
		Owner.createAll(new Owner[]{o1, o2});

		List<Owner> all = Owner.all();
		o1 = all.get(0);
		o2 = all.get(1);

		assertSuccess(Owner.updateAll(new Object[]{o1,o2}));
		assertSuccess(dao.batchUpdate(New.arrayList(o1, o2)));
		assertSuccess(dao.batchUpdate(new Owner[]{o1, o2}));
		assertSuccess(dao.batchUpdate(Owner.class, New.arrayList(o1, o2)));
		assertSuccess(dao.batchUpdate(Owner.class, new Object[]{o1, o2}));
		assertSuccess(dao.batchUpdate("Owner", New.arrayList(o1, o2)));
		assertSuccess(dao.batchUpdate("Owner", new Object[]{o1, o2}));

		Owner[] owners = new Owner[]{};
		int[] results = dao.batchUpdate(owners);
		assertEquals(0, results.length);
	}

	@Test
	public void testBatchDeleteSingleKeyModel() {
		deleteAll(Owner.class);
		
		Owner o1 = new Owner();
		o1.setFirstName("a");
		o1.setLastName("b");
		o1.save();
		
		Owner o2 = new Owner();
		o2.setFirstName("c");
		o2.setLastName("d");
		o2.save();
		
		assertEquals(2,Owner.count());
		
		int[] results = Owner.deleteAll(new Object[]{o1.id(),o2.id()});
		assertEquals(2, results.length);
		
		assertEquals(0,Owner.count());
		
		//Test again
		o1.setId(null);
		o2.setId(null);
		o1.save();
		o2.save();

		assertEquals(2,Owner.count());
		Integer[] ids = new Integer[]{o1.getId(),o2.getId()};
		
		results = Owner.deleteAll(ids);
		assertEquals(2, results.length);
		assertEquals(0,Owner.count());
		
		//Test again
		o1.setId(null);
		o2.setId(null);
		o1.save();
		o2.save();

		assertEquals(2,Owner.count());
		Object[] owners = new Object[]{o1,o2};
		
		results = Owner.deleteAll(owners);
		assertEquals(2, results.length);
		assertEquals(0,Owner.count());
	}
	
}
