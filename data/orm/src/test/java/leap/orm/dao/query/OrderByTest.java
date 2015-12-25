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
package leap.orm.dao.query;

import java.util.List;

import leap.orm.OrmTestCase;
import leap.orm.tested.model.petclinic.Owner;
import leap.orm.tested.model.team.TeamMember;

import org.junit.Test;

public class OrderByTest extends OrmTestCase {

	@Test
	public void testSimpleOrderByWithParam() {
		deleteAll(Owner.class);
		
		new Owner().setFullName("b", "l").save();
		new Owner().setFullName("a", "l").save();
		new Owner().setFullName("c", "l").save();
		
		//without limit
		List<Owner> owners = 
			Owner.<Owner>query("select * from Owner owner where lastName = :lastName")
				 .orderBy("firstName asc").param("lastName", "l").list();
		
		assertEquals(3, owners.size());
		assertEquals("a",owners.get(0).getFirstName());
		assertEquals("b",owners.get(1).getFirstName());
		assertEquals("c",owners.get(2).getFirstName());
		
		//With limit
		owners = 
				Owner.<Owner>query("select * from Owner owner where lastName = :lastName")
					 .orderBy("firstName asc").param("lastName", "l")
					 .limit(2)
					 .list();
		
		assertEquals(2, owners.size());
		assertEquals("a",owners.get(0).getFirstName());
		assertEquals("b",owners.get(1).getFirstName());
		
		//with default order by 
		owners = 
				Owner.<Owner>query("select * from Owner owner where lastName = :lastName order by id asc")
					 .orderBy("firstName asc").param("lastName", "l").list();
			
			assertEquals(3, owners.size());
			assertEquals("a",owners.get(0).getFirstName());
			assertEquals("b",owners.get(1).getFirstName());
			assertEquals("c",owners.get(2).getFirstName());
			
		//With limit
		owners = 
				Owner.<Owner>query("select * from Owner owner where lastName = :lastName order by id asc")
					 .orderBy("firstName asc").param("lastName", "l")
					 .limit(2)
					 .list();
		
		assertEquals(2, owners.size());
		assertEquals("a",owners.get(0).getFirstName());
		assertEquals("b",owners.get(1).getFirstName());		
	}
	
	@Test
	public void testSimpleOrderByWithParamByNamedSql() {
		deleteAll(Owner.class);
		
		new Owner().setFullName("b", "l").save();
		new Owner().setFullName("a", "l").save();
		new Owner().setFullName("c", "l").save();
		
		//without limit
		List<Owner> owners = 
			Owner.<Owner>query("testOrderByQuery.testSimpleOrderByWithParam")
				 .orderBy("firstName asc").param("lastName", "l").list();
		
		assertEquals(3, owners.size());
		assertEquals("a",owners.get(0).getFirstName());
		assertEquals("b",owners.get(1).getFirstName());
		assertEquals("c",owners.get(2).getFirstName());
		
		//With limit
		owners = 
				Owner.<Owner>query("testOrderByQuery.testSimpleOrderByWithParam")
					 .orderBy("firstName asc").param("lastName", "l")
					 .limit(2)
					 .list();
		
		assertEquals(2, owners.size());
		assertEquals("a",owners.get(0).getFirstName());
		assertEquals("b",owners.get(1).getFirstName());
		
		//with default order by 
		owners = 
				Owner.<Owner>query("testOrderByQuery.testSimpleOrderByWithParam.orderById")
					 .orderBy("firstName asc").param("lastName", "l").list();
			
			assertEquals(3, owners.size());
			assertEquals("a",owners.get(0).getFirstName());
			assertEquals("b",owners.get(1).getFirstName());
			assertEquals("c",owners.get(2).getFirstName());
			
		//With limit
		owners = 
				Owner.<Owner>query("testOrderByQuery.testSimpleOrderByWithParam.orderById")
					 .orderBy("firstName asc").param("lastName", "l")
					 .limit(2)
					 .list();
		
		assertEquals(2, owners.size());
		assertEquals("a",owners.get(0).getFirstName());
		assertEquals("b",owners.get(1).getFirstName());		
	}
	
	@Test
	public void testOrderByAliasColumn() {
		deleteAll(Owner.class);
		
		new Owner().setFullName("b", "l").save();
		new Owner().setFullName("a", "l").save();
		new Owner().setFullName("c", "l").save();
		
		List<Owner> owners = 
				Owner.<Owner>query("select owner.firstName ownerFirstName from Owner owner")
					 .orderBy("ownerFirstName asc").limit(5).list();

		assertEquals(3, owners.size());
		assertEquals("a",owners.get(0).get("ownerFirstName"));
		assertEquals("b",owners.get(1).get("ownerFirstName"));
		assertEquals("c",owners.get(2).get("ownerFirstName"));
		
		String sql = "select member.*, u.name userName,r.id roleId, r.name roleName,org.name orgName " + 
					 "from TeamMember member " + 
					 "join User u on member.userId = u.userId " + 
				     "join Role r on member.roleId = r.id " + 
					 "left join Organization org on u.orgId = org.orgId";
		
		TeamMember.query(sql).orderBy("userName asc").firstOrNull();
	}
	
	/*
	@Test
	public void testOrderByInUnionQuery() {
		deleteAll(Owner.class);
		
		new Owner().setFullName("b", "l").save();
		new Owner().setFullName("a", "l").save();
		
		String sql = "select o1.firstName from Owner o1 where o1.firstName = 'a' " + 
		             "union " + 
				     "select o2.firstName from Owner o2 where o2.firstName = 'b' ";
		
		List<Owner> owners = Owner.<Owner>query(sql).orderBy("firstName desc").list();
		
		assertEquals(2,owners.size());
		assertEquals("a",owners.get(0).getFirstName());
		assertEquals("b",owners.get(1).getFirstName());
	}
	*/
}
