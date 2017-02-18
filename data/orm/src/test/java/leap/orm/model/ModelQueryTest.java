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
package leap.orm.model;

import leap.core.value.Record;
import leap.junit.contexual.Contextual;
import leap.lang.New;
import leap.orm.OrmTestCase;
import leap.orm.query.PageResult;
import leap.orm.tested.model.file.Directory;
import leap.orm.tested.model.petclinic.Owner;
import leap.orm.tested.model.product.ChildProduct;
import leap.orm.tested.model.product.Product;
import leap.orm.value.EntityBase;

import org.junit.Test;

public class ModelQueryTest extends OrmTestCase {
	
	@Test
	public void testFirst() {
		deleteAll(Owner.class);
		
		assertNull(Owner.firstOrNull());
		
		Owner owner = new Owner();
		owner.setFirstName("a");
		owner.setLastName("b");
		owner.save();
		
		Owner o1 = Owner.first();
		Owner o2 = Owner.firstOrNull();
		Owner o3 = Owner.<Owner>first(2).get(0);
		Owner o4 = Owner.last();
		Owner o5 = Owner.lastOrNull();
		Owner o6 = Owner.<Owner>last(2).get(0);
		
		assertTrue(o1.id().equals(o2.id()) && o2.id().equals(o3.id()) && 
				   o3.id().equals(o4.id()) && o4.id().equals(o5.id()) && o5.id().equals(o6.id()));
	}
	@Test
	@Contextual("h2")
	public void testOrderBy(){
		ChildProduct.query("testOrderByWithFieldName").params(new Object[]{1}).orderBy("typeId ASC").list();
	}

	@Test
	public void testSqlQueryForModel() {
		deleteAll(Owner.class);
		
		new Owner().set("firstName", "a").set("lastName","b").save();

		assertEquals(1, Owner.query("select * from owner").list().size()); 
		
		assertEquals(1, Owner.query("select * from owner order by id desc").list().size());
		assertEquals(1, Owner.query("select * from owner").orderBy("id desc").list().size());
		assertEquals(1, Owner.query("select * from owner").orderBy("id desc").limit(2).list().size());
	}
	@Test
	public void testAdditionalField(){
		deleteAll(Owner.class);
		new Owner().set("firstName", "a").set("lastName","b").save();
		assertEquals(new Integer(1), Owner.query("simpleSqlIdWithAdditionalField").single().getInteger("additional"));
		assertEquals(new Integer(1), Owner.query("select o.*,1 a from owner o").single().getInteger("a"));
	}
	
	@Test
	public void testSqlQueryForRecord() {
		deleteAll(Owner.class);
		
		new Owner().set("firstName", "a").set("lastName","b").save();

		assertEquals(1, dao.createSqlQuery("select * from owner").list().size()); 
		assertEquals(1, dao.createSqlQuery("select * from owner order by id desc").list().size());
		assertEquals(1, dao.createSqlQuery("select * from owner").orderBy("id desc").list().size());
		assertEquals(1, dao.createSqlQuery("select * from owner").orderBy("id desc").limit(2).list().size());
		
		Record r = dao.createSqlQuery("select * from owner").first();
		assertEquals("a", r.get("firstName"));
		assertEquals("b", r.get("lastName"));
	}
	
	@Test
	public void testSqlQueryForEntity() {
		deleteAll(Owner.class);
		
		new Owner().set("firstName", "a").set("lastName","b").save();

		assertEquals(1, dao.createSqlQuery(Owner.metamodel(), "select * from owner").list().size()); 
		assertEquals(1, dao.createSqlQuery(Owner.metamodel(),"select * from owner order by id desc").list().size());
		assertEquals(1, dao.createSqlQuery(Owner.metamodel(),"select * from owner").orderBy("id desc").list().size());
		assertEquals(1, dao.createSqlQuery(Owner.metamodel(),"select * from owner").orderBy("id desc").limit(2).list().size());
		
		Record r = dao.createSqlQuery(Owner.metamodel(),"select * from owner").first();
		assertEquals("a", r.get("firstName"));
		assertEquals("b", r.get("lastName"));
	}
	
	@Test
	public void testQueryCount() {
		deleteAll(Owner.class);
		
		new Owner().set("firstName", "a").set("lastName","b").save();
		
		assertEquals(1, Owner.query("select * from owner").count());
		assertEquals(1, Owner.query("select * from owner order by id desc").count());
		assertEquals(1, Owner.query("select id,name from owner order by id desc").count());
	}
	
	@Test
	public void testPageQuery() {
		deleteAll(Owner.class);
		
		new Owner().setFullName("a", "0").save();
		new Owner().setFullName("b", "0").save();
		new Owner().setFullName("c", "0").save();
		new Owner().setFullName("d", "0").save();
		new Owner().setFullName("e", "0").save();
		new Owner().setFullName("f", "0").save();
		new Owner().setFullName("g", "0").save();
		
		PageResult<Owner> result = Owner.<Owner>query().orderBy("firstName ASC").pageResult(1, 2);
		assertEquals(2, result.size());
		assertEquals("a",result.list().get(0).getFirstName());
		assertEquals("b",result.list().get(1).getFirstName());
		
		result = Owner.<Owner>query().pageResult(1, 2);
		assertEquals(7,result.getTotalCount());
		
		result = Owner.<Owner>query().orderBy("firstName ASC").pageResult(2, 2);
		assertEquals(2, result.size());
		assertEquals("c",result.list().get(0).getFirstName());
		assertEquals("d",result.list().get(1).getFirstName());
		
		result = Owner.<Owner>query().pageResult(1, 2);
		assertEquals(7,result.getTotalCount());
		
		result = Owner.<Owner>query().pageResult(2, 100);
		assertEquals(0, result.size());
	}
}