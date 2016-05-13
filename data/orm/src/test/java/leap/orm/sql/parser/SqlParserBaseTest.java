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
package leap.orm.sql.parser;

import java.util.List;

import leap.junit.contexual.Contextual;
import leap.orm.sql.Sql;
import leap.orm.sql.ast.IfClause;
import leap.orm.sql.ast.ParamPlaceholder;
import leap.orm.sql.ast.SqlOrderBy;

import org.junit.Test;

public class SqlParserBaseTest extends SqlParserTestCase {
	
	@Test
	public void testSqlTypes(){
		assertTrue(sql("select * from t").isSelect());
		assertFalse(sql("selectsss * from t").isSelect());
		assertTrue(sql("insert into t(c1) values(1)").isInsert());
		assertTrue(sql("update t set a = 1").isUpdate());
		assertTrue(sql("delete from t").isDelete());
		assertTrue(sql("truncate table d").isUnresolved());
	}
	
	@Test
	public void testMultiStatements(){
		String sql = "select * from t;";
		assertEquals("select * from t",parse(sql));
		
		sql = "select * from t; select * from t";
		List<Sql> sqls = parser(sql).sqls();
		assertEquals(2,sqls.size());
		for(Sql sqlObject : sqls){
			assertEquals("select * from t",sqlObject.toString());
		}
	}
	
	@Test
	@Contextual
	public void testSkipComment(){
		String sql = "select * from t --comment";
		//assertEquals("select * from t",parse(sql));
		
		sql = "select * from t --comment \n where 1=1";
		//assertEquals("select * from t  where 1=1",parse(sql));
		
		sql = "select * from t //comment";
		//assertEquals("select * from t",parse(sql));
		
		sql = "select * from t //comment \n where 1=1";
		//assertEquals("select * from t  where 1=1",parse(sql));
		
		sql = "select * from t /* \n ddd\n */ where 1=1";
		//assertEquals("select * from t  where 1=1",parse(sql));
		
		sql = "--comment\nselect * from t // comment";
		//assertEquals("select * from t",parse(sql));
		
		sql = "//comment\nselect * from t -- comment";
		//assertEquals("select * from t",parse(sql));
		
		sql = "/* comment\n  comment */select * from t";
		//assertEquals("select * from t",parse(sql));
		
		sql = "/* comment\n  comment */";
		/*
		try {
	        parse(sql);
	        fail("should throw exception");
        } catch (Exception e) {

        }
        */
	}
	
	@Test
	public void testSimpleText(){
		String sql = "  text ";
		assertEquals("text", parse(sql));
	}
	
	@Test
	public void testSpecialString(){
		String sql = "";
		sql = "select '$$a', 'a$$', '$$$a$', '${', '$${', '$$$${a}' from t";
		assertEquals(sql,parse(sql));
	}
	
	@Test
	public void testSimplePlaceholderParameter(){
		String text = "like :lastName";
		assertEquals(text,parse(text));
		
		text = "name like :a.b";
		Sql sql = sql(text);
		assertEquals(text,sql.toString());
		assertEquals("a.b",sql.lastNode().as(ParamPlaceholder.class).getName());
	}
	
	@Test
	@Contextual
	public void testSimpleParse(){
		String sql = "select $columns$ from ${table.name} where col1 = #col1# and col2 = #{params.col2} {? and col3 like '%$col3$%' }";
		assertEquals(sql, parse(sql));

		sql = "select $columns$ from ${table.name} where col1 = :col1 and col2 = #{params.col2} {? and col3 like '%$col3$%' }";
		assertEquals(sql, parse(sql));
		
		/*
		StopWatch sw = StopWatch.startNew();
		
		int count = 100;
		
		for(int i=0;i<count;i++){
			sql(sql);
		}
		
		System.out.println("Parsing " + count + " sqls used " + sw.getElapsedMilliseconds() + "ms");
		*/
	}
	
	@Test
	@Contextual
	public void testIfEndParse(){
		String sql = "select * from table \n" +
					 "@if(null != joins && joins.size() > 0)\n" +
					 "  $join_sql$\n" +
					 "@endif";
		assertEquals(sql, parse(sql));
		
		sql = "select $columns$ from ${table.name} where col1 = #col1# and col2 = #{params.col2} {? and col3 like '%#col3#%' } \n" +
			  "@if(null != joins && joins.size() > 0)\n" +
			  "  $join_sql$\n" +
			  "@endif";
		
		assertEquals(sql, parse(sql));
	}
	
	@Test
	@Contextual
	public void testIfElseEndParse(){
		String text = "select * from table \n" +
					 "@if(null != joins && joins.size() > 0)\n" +
					 "  $join_sql$\n" +
					 "@else\n" +
					 "  test\n" + 
					 "@endif\n" +
					 "where 1 = 1";
		Sql sql = sql(text);
		assertEquals(text, sql.toString());
		IfClause clause = sql.findLastNode(IfClause.class);
		assertEquals(1,clause.getIfStatements().length);
		
		text = "select * from table \n" +
			  "@if(null != joins && joins.size() > 0)\n" +
			  "  $join_sql$\n" +
			  "@elseif(true)\n" +
			  "  col1\n " +			  
			  "@else\n" +
			  "  test\n" + 
			  "@endif where 1=0";
		sql = sql(text);
		assertEquals(text, sql.toString());
		clause = sql.findLastNode(IfClause.class);
		assertEquals(2,clause.getIfStatements().length);
	}
	
	@Test
	@Contextual
	public void testOrderBy(){
		String text;
		Sql    sql;

		text = "select * from t order by a";
		sql = sql(text);
		assertEquals(text, sql.toString());
		assertEquals("order by a",sql.findLastNode(SqlOrderBy.class));
		
		text = "select order,a,b from t where order = ? order by sub_str(a,10),b desc";
		sql = sql(text);
		assertEquals(text, sql.toString());
		assertEquals("order by sub_str(a,10),b desc",sql.findLastNode(SqlOrderBy.class));
		
		text = "select * from t order by sub_str(a,10) asc,b limit 1";
		sql = sql(text);
		assertEquals(text,sql.toString());
		assertEquals("order by sub_str(a,10) asc,b ",sql.findLastNode(SqlOrderBy.class));
		
		text = "select * from t join (select * from t1 order by a) t1 on t.id = t1.id order by a for update";
		sql = sql(text);
		assertEquals(text,sql.toString());
		assertEquals("order by a ",sql.findLastNode(SqlOrderBy.class));
	}
	
	@Test
	public void testParseOrderByOnly() {
		SqlOrderBy orderBy = SqlParser.parseOrderBy("order by id asc");
		assertEquals("order by id asc", orderBy.toString());
	}

}