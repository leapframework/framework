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
	}

}