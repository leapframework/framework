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
package leap.orm.sql;

import leap.core.value.Record;
import leap.orm.OrmTestCase;
import leap.orm.query.Query;
import leap.orm.tested.model.petclinic.Owner;
import org.junit.Test;

import java.util.List;

public class DynamicSqlTest extends OrmTestCase {
	
	@Test
	public void testSimpleNamedDynamicClause() {
		deleteAll(Owner.class);
		
		new Owner().setFullName("a", "0").save();
		new Owner().setFullName("b", "1").save();
		
		Query<Record> query = dao.createNamedQuery("test.sql.dynamic.clause.simple");
		assertEquals(2,query.count());
		assertEquals(1,query.param("lastName", "1").count());
		
		query = dao.createNamedQuery("test.sql.dynamic.clause.simple_1");
		assertEquals(2,query.count());
		assertEquals(1,query.param("lastName", "1").count());
	}
	
	@Test
	public void testSimpleSqlDynamicClause() {
		deleteAll(Owner.class);
		
		new Owner().setFullName("a", "0").save();
		new Owner().setFullName("b", "1").save();
		
		Query<Record> query =
				dao.createSqlQuery("select * from owners where 1=1 {?and last_name like #lastName#}");
		
		assertEquals(2,query.count());
		assertEquals(2,query.param("lastName", null).count());
		assertEquals(0,query.param("lastName", "").count());
		assertEquals(1,query.param("lastName", "1").count());
		
		query = dao.createSqlQuery("select * from owners where 1=1 {?and last_name like #lastName# ; nullable:true}");
        assertEquals(0, query.param("lastName", null).count());
        assertEquals(0, query.param("lastName", "").count());
        assertEquals(1, query.param("lastName", "1").count());
        
        query = dao.createSqlQuery("select * from owners where 1=1 {?and last_name = #lastName# ; nullable:true}");
        assertEquals(0, query.param("lastName", null).count());
	}
	@Test
	public void testIfClauseDynamicSql(){
		deleteAll(Owner.class);
		new Owner().setFullName("a", "01").save();
		new Owner().setFullName("b", "1").save();

		long nameLike1 = dao.createNamedQuery("test.sql.dynamic.clause.if").param("name","%1%").count();
		long nameEq01 = dao.createNamedQuery("test.sql.dynamic.clause.if").param("name","123456").count();
		long nameEq1 = dao.createNamedQuery("test.sql.dynamic.clause.if").count();
		assertEquals(nameLike1,2L);
		assertEquals(nameEq1,1L);
		assertEquals(nameEq01,1L);
		deleteAll(Owner.class);
	}

	@Test
	public void testNestIfClauseDynamicSql(){
		deleteAll(Owner.class);
		new Owner().setFullName("a", "01").save();
		new Owner().setFullName("b", "1").save();

		long nameLike1 = dao.createNamedQuery("test.sql.dynamic.clause.nest.if").param("name","%1%").count();
		long nameEq01 = dao.createNamedQuery("test.sql.dynamic.clause.nest.if").param("name","123456").count();
		long nameEq1 = dao.createNamedQuery("test.sql.dynamic.clause.nest.if").count();
		assertEquals(nameLike1,Owner.count());
		assertEquals(nameEq1,1L);
		assertEquals(nameEq01,1L);
		deleteAll(Owner.class);
	}
	@Test
	public void testSelectItemSemSql(){
		deleteAll(Owner.class);
		new Owner().setFullName("a", "01").save();
		new Owner().setFullName("b", "1").save();
		int result = dao.createSqlQuery("select (:price * (SELECT count(1) FROM owners)) AS num FROM owners").param("price",2).first().getInteger("num");
		assertEquals(4,result);
	}

    @Test
    public void testUpdateColumnWithNullValue() {

        dao.executeNamedUpdate("test.sql.dynamic.clause.update_with_null_column", new Object[]{null});

    }
	@Test
    public void testSqlUseEnvVariable(){
		Owner.deleteAll();
		new Owner().setFullName("a", "01").save();
		int size = dao.createNamedQuery("test.sql.dynamic.env.params").list().size();
		assertEquals(1,size);

	}

	@Test
	public void testFragmentAndUnionWithLimit(){
		Owner.deleteAll();
		new Owner().setFullName("a", "1").save();
		new Owner().setFullName("b", "2").save();
		new Owner().setFullName("c", "3").save();
		List<Record> records = dao.createNamedQuery("union_include")
				.param("name1","a")
				.param("name2","b")
				.param("name3","c")
				.limit(2).list();
		assertEquals(2,records.size());
	}

	@Test
	public void testFragmentAndUnionWithOrder(){
		Owner.deleteAll();
		new Owner().setFullName("a", "1").save();
		new Owner().setFullName("b", "2").save();
		new Owner().setFullName("c", "3").save();
		List<Record> records = dao.createNamedQuery("union_include")
				.param("name1","a")
				.param("name2","b")
				.param("name3","c")
				.orderBy("name DESC ")
				.list();
		assertEquals(3,records.size());
		assertEquals("c",records.get(0).get("name"));
	}

	@Test
	public void testFragmentAndUnionWithOrderAndLimit(){
		Owner.deleteAll();
		new Owner().setFullName("a", "1").save();
		new Owner().setFullName("b", "2").save();
		new Owner().setFullName("c", "3").save();
		List<Record> records = dao.createNamedQuery("union_include")
				.param("name1","a")
				.param("name2","b")
				.param("name3","c")
				.orderBy("name DESC ")
				.limit(2)
				.list();
		assertEquals(2,records.size());
		assertEquals("c",records.get(0).get("name"));
	}

	@Test
	public void testFragmentNested(){
		Owner.deleteAll();
		new Owner().setFullName("a","1").save();
		Record owner = dao.createNamedQuery("nestingIncludeSql").param("firstName","a").single();
		assertEquals("1",owner.get("lastName"));
	}
}