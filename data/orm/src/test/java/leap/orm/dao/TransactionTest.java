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

import leap.core.transaction.TransactionCallback;
import leap.core.transaction.TransactionStatus;
import leap.orm.OrmTestCase;
import leap.orm.tested.model.petclinic.Owner;
import leap.orm.tested.model.team.Team;

import org.junit.Test;

public class TransactionTest extends OrmTestCase {
	
	@Test
	public void testCreateAndReleaseTransaction() {
		dao.doTransaction((TransactionStatus s) -> {
			s.setRollbackOnly();
		});

		dao.doTransaction(new TransactionCallback() {
			@Override
			public void doInTransaction(TransactionStatus s) throws Throwable {
				assertFalse(s.isRollbackOnly());
			}
		});
	}

	@Test
	public void testSimpleRollback() {
		deleteAll(Owner.class);

		dao.doTransaction(new TransactionCallback() {

			@Override
			public void doInTransaction(TransactionStatus s) throws Throwable {
				Owner o = new Owner();
				o.setFirstName("a");
				o.setLastName("b");
				o.save();

				s.setRollbackOnly();
			}
		});
		
		assertEquals(0, Owner.count());
	}

	@Test
	public void testSimpleCommit() {
		deleteAll(Owner.class);

		dao.doTransaction(new TransactionCallback() {

			@Override
			public void doInTransaction(TransactionStatus s) throws Throwable {
				Owner o = new Owner();
				o.setFirstName("a");
				o.setLastName("b");
				o.save();
			}
		});

		assertEquals(1, Owner.count());
	}

	@Test
	public void testNestedRollback() {
		deleteAll(Owner.class);

		dao.doTransaction(new TransactionCallback() {

			@Override
			public void doInTransaction(TransactionStatus s) throws Throwable {

				assertTrue(s.isNewTransaction());

				Owner o = new Owner();
				o.setFirstName("a");
				o.setLastName("b");
				o.save();

				dao.doTransaction(new TransactionCallback() {

					@Override
					public void doInTransaction(TransactionStatus s1) throws Throwable {
						assertFalse(s1.isNewTransaction());

						Owner o1 = new Owner();
						o1.setFirstName("c");
						o1.setLastName("d");
						o1.save();

						s1.setRollbackOnly();
					}
				});

				assertTrue(s.isRollbackOnly());
			}
		});

		assertEquals(0, Owner.count());
	}

	@Test
	public void testNestedCommit() {
		deleteAll(Owner.class);

		dao.doTransaction(new TransactionCallback() {

			@Override
			public void doInTransaction(TransactionStatus s) throws Throwable {

				assertTrue(s.isNewTransaction());

				Owner o = new Owner();
				o.setFirstName("a");
				o.setLastName("b");
				o.save();

				dao.doTransaction(new TransactionCallback() {

					@Override
					public void doInTransaction(TransactionStatus s1) throws Throwable {
						assertFalse(s1.isNewTransaction());

						Owner o1 = new Owner();
						o1.setFirstName("c");
						o1.setLastName("d");
						o1.save();
					}
				});

				assertFalse(s.isRollbackOnly());
			}
		});

		assertEquals(2, Owner.count());
	}
	
	@Test
	public void testRollbackWithNestedTransaction() {
		deleteAll(Owner.class);
		deleteAll(Team.class);
		
		assertEquals(0, Owner.count());
		assertEquals(0, Team.count());
		
		dao.doTransaction(new TransactionCallback() {
			@Override
			public void doInTransaction(TransactionStatus status) throws Throwable {
				assertTrue(status.isNewTransaction());
				
				Owner o = new Owner();
				o.setFirstName("a");
				o.setLastName("b");
				o.save();
				
				
				dao.doTransaction(new TransactionCallback() {
					
					@Override
					public void doInTransaction(TransactionStatus s1) throws Throwable {
						assertFalse(s1.isNewTransaction());
						
						Team o1 = new Team();
						o1.setName("team1");
						o1.save();
					}
				}, false);
				
				status.setRollbackOnly();
			}
		});
		
		assertEquals(0, Owner.count());
		assertEquals(0, Team.count());
	}
	
	@Test
	public void testSimpleRequiresNew() {
		deleteAll(Owner.class);
		deleteAll(Team.class);
		
		assertEquals(0, Owner.count());
		assertEquals(0, Team.count());
		
		dao.doTransaction(new TransactionCallback() {
			@Override
			public void doInTransaction(TransactionStatus status) throws Throwable {
				assertTrue(status.isNewTransaction());
				
				Owner o = new Owner();
				o.setFirstName("a");
				o.setLastName("b");
				o.save();
				
				
				dao.doTransaction(new TransactionCallback() {
					
					@Override
					public void doInTransaction(TransactionStatus s1) throws Throwable {
						assertTrue(s1.isNewTransaction());
						
						Team o1 = new Team();
						o1.setName("team1");
						o1.save();
					}
				}, true);
				
				status.setRollbackOnly();
			}
		});
		
		assertEquals(0, Owner.count());
		assertEquals(1, Team.count());
	}

}
