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

import leap.core.el.ElConfig;
import leap.core.el.ExpressionLanguage;
import leap.junit.contexual.Contextual;
import leap.junit.contexual.ContextualIgnore;
import leap.lang.Strings;
import leap.lang.expression.Expression;
import leap.lang.expression.ValuedExpression;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.orm.OrmTestCase;
import leap.orm.sql.Sql;
import leap.orm.sql.Sql.ParseLevel;
import leap.orm.sql.SqlResolver;

import org.junit.Test;

@ContextualIgnore
public class SqlResolverTest extends OrmTestCase {
	
	private static final Log log = LogFactory.get(SqlResolverTest.class);
	
	private static ExpressionLanguage el = new ExpressionLanguage() {
		@Override
		public Expression createExpression(String expression) {
			return new ValuedExpression<String>(expression);
		}

		@Override
        public Expression createExpression(ElConfig config, String expression) {
			return new ValuedExpression<String>(expression);
        }
	};
	
	private static SqlParser parser(String sql) {
		return new SqlParser(new Lexer(sql,ParseLevel.MORE),el);
	}
	
	private static List<String> split(String sqls){
		return parser(sqls).split();
	}

	protected String resolve(String text){
		Sql sql = new SqlParser(new Lexer(text, ParseLevel.MORE)).sql();
		return new SqlResolver(context,sql).resolve().toSql();
	}
	
	@Test
	public void testSimpleSelect(){
		assertEquals("select id_ from person_",resolve("select id from Person").toString());
		assertEquals("select id_ as pid from person_",resolve("select id as pid from Person").toString());
		assertEquals("select id_ as pid,name_ from person_",resolve("select id as pid,name from Person").toString());
		assertEquals("select id_ pid from person_",resolve("select id pid from Person").toString());
		assertEquals("select id_ as 'pid' from person_",resolve("select id as 'pid' from Person").toString());
		
		assertEquals("select count(*) from person_",resolve("select count(*) from Person").toString());
		assertEquals("select count(*) as c from person_",resolve("select count(*) as c from Person").toString());
		assertEquals("select count(*) c from person_",resolve("select count(*) c from Person").toString());
		
		assertEquals("select t.* from person_ t where name_ = ?", resolve("select t.* from Person t where name = ?").toString());
		
		String text = "select id,name from Person p join (select id,name from Person) p1 on p.id = p1.name and exists (select id from Person)";
		String sql  = Strings.replace(text, "Person", "person_");
		sql = Strings.replace(sql, "id", "id_");
		sql = Strings.replace(sql, "name", "name_");
		assertEquals(sql, resolve(text));
		
		//order by
		assertEquals("select * from person_ order by id_ desc",resolve("select * from Person order by id desc").toString());
	}

	@Test
	public void testComplexSelectItem() {
		assertEquals("select (round(age_)), name_ from person_", resolve("select (round(age)), name from person"));
		assertEquals("select (round(p.age_)), name_ from person_ p", resolve("select (round(p.age)), name from person p"));
		assertEquals("select (age_ + 1), name_ from person_", resolve("select (age + 1), name from person"));
		assertEquals("select (t.age_ + 1), name_ from person_ t", resolve("select (t.age + 1), name from person t"));

		//todo: fix bug
		//assertEquals("select age_ + 1, name_ from person_", resolve("select age + 1, name from person"));
		//assertEquals("select 1 + age_, name_ from person_", resolve("select 1 + age_, name from person"));
	}

	@Test
	public void testSubQuery() {
		assertEquals("select * from person_ where id_ = (select model_id from model_with_id2 where model_id = ?)",
					 resolve("select * from Person where id = (select modelId from ModelWithId2 where modelId = ?)").toString());
		
		assertEquals("select * from person_ where id_ = (select mid from model_with_id1 where mid = ?)",
				 resolve("select * from Person where id = (select id from ModelWithId1 where id = ?)").toString());
		
		assertEquals("select * from person_ p where id_ = (select mid from model_with_id1 where mid = p.id_)",
				 resolve("select * from Person p where id = (select id from ModelWithId1 where id = p.id)").toString());		
	
	}
	
	@Test
	public void testJoin() {
		String sql_original = "select team.* from Team team " + 
							  "join TeamMember member on team.id = member.teamId " + 
							  "join Role role on member.roleId = role.id where member.userId = ?";
		
		String sql_expected = "select team.* from team_ team " + 
							  "join team_member member on team.id_ = member.team_id " + 
							  "join role_ role on member.role_id = role.role_id where member.user_id = ?";
		
		String sql_resolved = resolve(sql_original).toString();
		
		assertEquals(sql_expected, sql_resolved);
	}
	
	@Test
	public void testOrderByAlias() {
		assertEquals("select name userName from user_ order by userName asc",
					 resolve("select name userName from User order by userName asc"));
		
		assertEquals("select name_ teamName from team_ order by teamName asc",
				 resolve("select name teamName from Team order by teamName asc"));
		
		String sql_original = "select team.*,user.name ownerName from Team team " + 
							  "join TeamMember member on team.id = member.teamId " + 
							  "join User user on member.userId = user.id " + 
							  "join Role role on member.roleId = role.id where member.userId = ? " + 
							  "order by ownerName asc";

		String sql_expected = "select team.*,user.name ownerName from team_ team " + 
							  "join team_member member on team.id_ = member.team_id " +
							  "join user_ user on member.user_id = user.user_id " + 
							  "join role_ role on member.role_id = role.role_id where member.user_id = ? " + 
							  "order by ownerName asc";

		String sql_resolved = resolve(sql_original).toString();
		
		assertEquals(sql_expected, sql_resolved);
	}
	
	@Test
	public void testUnion() {
		assertEquals("select id_,name_ from person_ where id_ = ? union select id_,name_ from team_ where name_ = ?",
					 resolve("select id,name from Person where id = ? union select id,name from Team where name = ?"));
		
		assertEquals("select id_,name_ from person_ where id_ = ? union select id_,name_ from team_ where name_ = ? union select id_,name_ from person_",
				 resolve("select id,name from Person where id = ? union select id,name from Team where name = ? union select id,name from Person"));

		assertEquals("select * from (select id_, name_ from person_ union select '', '' from dual) t order by t.name_",
				resolve("select * from (select id, name from person union select '', '' from dual) t order by t.name"));
	}
	
	@Test
	public void testSimpleDelete() {
		assertEquals("delete from person_",resolve("delete from Person").toString());
		assertEquals("delete from person_ where id_ = ?",resolve("delete from Person where id = ?").toString());
		assertEquals("delete from person_ t where id_ = ?",resolve("delete from Person t where id = ?").toString());
		assertEquals("delete t from person_ t where id_ = ?",resolve("delete t from Person t where id = ?").toString());
	}
	
	@Test
	public void testSimpleUpdate() {
		assertEquals("update person_ set name_ = ?",resolve("update Person set name = ?").toString());
		
		assertEquals("update person_ set name_ = ? where id_ = ?",
				     resolve("update Person set name = ? where id = ?").toString());
		
		assertEquals("update person_ t set name_ = ? where t.id_ = ?",
			     	 resolve("update Person t set name = ? where t.id = ?").toString());
	}
	
	@Test
	public void testBug(){
		String text = Strings.trim(Resources.getContent("classpath:/test/sqls/resolve/bug.sql"));
		if(!Strings.isEmpty(text)){
			List<String> sqls = split(text);
			
			for(int i=0;i<sqls.size();i+=2){
				String sql      = Strings.trim(sqls.get(i));
				String expected = Strings.trim(sqls.get(i+1));
				
				log.debug("  Sql {} \n {}  \n {}",(i+1),sql,expected);
				assertEquals(expected,resolve(sql));
			}
		}
	}
	
	@Test
	public void testFromResources() throws Exception{
		for(Resource res : Resources.scan("classpath:/test/sqls/resolve/**/*.sql")){
			String text = res.getContent();
			
			List<String> sqls = split(text);
			
			for(int i=0;i<sqls.size();i+=2){
				String sql      = Strings.trim(sqls.get(i));
				String expected = Strings.trim(sqls.get(i+1));
				
				log.debug("  Sql {} \n {}  \n {}",(i+1),sql,expected);
				assertEquals(expected,resolve(sql));
			}
		}
	}
}
