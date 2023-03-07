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

import leap.junit.contexual.Contextual;
import leap.junit.contexual.ContextualIgnore;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;
import leap.orm.sql.Sql;
import leap.orm.sql.Sql.ParseLevel;
import leap.orm.sql.Sql.Scope;
import leap.orm.sql.ast.*;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

@ContextualIgnore
public class SqlParserMoreTest extends SqlParserTestCase {
	
	private static final Log log = LogFactory.get(SqlParserMoreTest.class);

	@Override
	@Before
	public void setup(){
		if(null == level){
			level = ParseLevel.MORE;
		}
	}
	
	@Test
	public void testSimpleSubQuery() {
		Sql sql = sql("select * from t where id = (select id from t1");
		
		SqlSelect select = (SqlSelect)sql.nodes()[0];

		SqlSelect subQuery = select.findLastNode(SqlSelect.class);
		assertNotNull(subQuery);
	}

    @Test
    public void testSimpleAndExpr() {
        assertParse("select * from t where a = 1 and b is null");
    }

    @Test
    public void testSimpleSelect() {
        assertParse("select * from t where lastName = :lastName");
    }

	@Test
	public void testSimpleJoin() {
		assertParse("select * from t join t1 on t.id = t1.id");
	}
	
	@Test
	public void testSimpleUpdate() {
		assertParse("update person set firstName = ?");
		
		assertParse("update person set firstName = ? where id = ?");
		
		assertParse("update person set firstName = #firstName# where id=#id#");
	}

	@Test
	public void testUpdateSetSubStatement() {
		Sql sql = assertParse("update person set name1 = ?, name2 = (case when name1 != name2 then name1 else name3 end) where id = ?");
		assertObjectNames(sql.findFirstNode(SqlUpdate.class).getNodes(), "name1", "name2", "name1", "name2", "name1", "name3");
		sql = assertParse("update person set name1 = ?, name2 = (case) where id = ?");
		assertObjectNames(sql.findFirstNode(SqlUpdate.class).getNodes(), "name1", "name2", "case");
	}
	
	@Test
	public void testSimpleDynamic() {
	    assertParse("select * from t {?limit :count}");
	    
	    split("select * from t {?limit :count}");
	}
	
    @Test
    public void testDynamicWithParams() {
        assertParse("select * from t {?limit :count;nullable:true}");
        
        split("select * from t {?limit :count; nullable:true}");
    }

    @Test
    public void testUnderscoreColumn() {
        assertParse("select t._col from table t");
    }

    @Test
    public void testSelectWhenColumn() {
        Sql sql = assertParse("select when from t");

        SqlSelect select = sql.findFirstNode(SqlSelect.class);
        AstNode[] items  = select.getSelectList().getNodes();
        assertEquals(2, items.length);

        SqlObjectNameBase when = (SqlObjectNameBase)items[0];
        assertEquals("when", when.getLastName());
    }

    @Test
    public void testSelectWhenWhere() {
        Sql sql = assertParse("select * from t where when = ?");

        SqlObjectName when = sql.findFirstNode(SqlObjectName.class);
        assertEquals("when", when.getLastName());
    }

	@Test
	public void testSelectItem() {
		Sql sql = sql("select count(*) from t");
		SqlSelect select = sql.findFirstNode(SqlSelect.class);
		assertEquals(1, select.getSelectList().getNodes().length);

		sql = sql("select count(1) a from t");
		select = sql.findFirstNode(SqlSelect.class);
		assertTrue(select.getSelectItemAliases().containsKey("a"));
	}

	@Test
	public void testSelectWithCalculate() {
		Sql sql = assertParse("select id+(count-sales) alias from table t where 1=1");
		assertObjectNames(sql.findFirstNode(SqlSelectList.class).getNodes(), "id", "count", "sales");

		sql = assertParse("select t.id, ifnull(t.count,0)-(ifnull(t.loss,0)+ifnull(t.sales,0)) result from book t");
		assertObjectNames(sql.findFirstNode(SqlSelectList.class).getNodes(), "t.id", "t.count", "t.loss", "t.sales");
	}

	@Test
	@Contextual
	public void testBug(){
		String text = Strings.trim(Resources.getContent("classpath:/test/sqls/parse/bug.sql"));
		if(!Strings.isEmpty(text)){
            List<String> sqls = split(text);

            log.info("Test {} sql statement(s)", sqls.size());

            for (int i = 0; i < sqls.size(); i++) {
                String sql = Strings.trim(sqls.get(i));
                log.debug("  Sql {} \n {}", (i + 1), sql);
                Sql result = assertParse(sql);
				log.info(result.toString());
            }
		}
	}
	
	@Test
	@Contextual
	public void testFromResources() throws Exception{
		for(Resource res : Resources.scan("classpath:/test/sqls/parse/**/*.sql")){
			String text = res.getContent();
			
			log.info("Split sqls in '{}'",res.getFilename());
			
			List<String> sqls = split(text);
			
			log.info("Test {} sql statement(s) in '{}'",sqls.size(),res.getFilename());
			
			for(int i=0;i<sqls.size();i++){
				String sql = Strings.trim(sqls.get(i));
				log.debug("  Sql {} \n {}",(i+1),sql);
				assertParse(sql);
			}
		}
	}

    @Test
    @Contextual
    public void testDynamicFromResources() throws Exception{
        for(Resource res : Resources.scan("classpath:/test/sqls/dynamic/**/*.sql")){
            String text = res.getContent();

            log.info("Split sqls in '{}'",res.getFilename());

            List<String> sqls = split(text);

            log.info("Test {} sql statement(s) in '{}'",sqls.size(),res.getFilename());

            for(int i=0;i<sqls.size();i++){
                String sql = Strings.trim(sqls.get(i));
                log.debug("  Sql {} \n {}",(i+1),sql);
                assertParse(sql);
            }
        }
    }
	
	@Test
	@Contextual
	public void testFromDruidSqls() {
	    ResourceSet rs1 = Resources.scan("classpath:/test/sqls/druid/**/*.sql");
	    ResourceSet rs2 = Resources.scan("classpath:/test/sqls/druid/**/*.txt");
	    
	    for(Resource res : rs1.concat(rs2)){
	        
            String text = res.getContent();

            String[] sqls = Strings.split(text,"---------------------------");

            log.info("Test {} sql statement(s) in '{}'", sqls.length, res.getFilename());

            for (int i = 0; i < sqls.length; i++) {
                String sql = Strings.trim(sqls[i]);
                
                if(sql.endsWith(";")) {
                    sql = sql.substring(0, sql.length() - 1).trim();
                }
                
                log.debug("  Sql {} \n {}", (i + 1), sql);
                assertParse(sql);
            }
	    }
	}

	/*
	@Test
	public void testParse(){
		String sql = "SELECT d.department_id" + 
					 " FROM departments d FULL OUTER JOIN employees e " + 
					 " ON d.department_id = e.department_id";
		assertParse(sql);
	}
	*/
	
	@Test
	public void testTop(){
		SqlTop top = assertParse("select top 10 * from t").findFirstNode(SqlTop.class);
		assertNotNull(top);
		assertEquals(10, top.getNumber());
		
		top = assertParse("select distinct top 10 * from t").findFirstNode(SqlTop.class);
		assertNotNull(top);
		assertEquals(10, top.getNumber());
		
		top = assertParse("select all top 10 * from t").findFirstNode(SqlTop.class);
		assertNotNull(top);
		assertEquals(10, top.getNumber());
	}
	
	@Test
	public void testDotName(){
		assertParse("select p.col1 from s1.s2.t p");
		assertParse("select t.* from t where t.id = ?");
	}
	
	@Test
	public void testSelectItems(){
		SqlAllColumns allColumns = assertParse("select * from t").findFirstNode(SqlAllColumns.class);
		assertNotNull(allColumns);
		
		assertParse("select c1 c1 from t");
		
		sql = assertParse("select c1,c2,c3,t1.c5,t2.*,c5+1 from table1 t1, table 2 t2 where t1.id = t2.id");
		allColumns = sql.findFirstNode(SqlAllColumns.class);
		assertNotNull(allColumns);
		assertEquals("t2",allColumns.getTableAlias());
		
		SqlObjectName c5 = AstUtils.findFirstObjectName(sql.nodes(), "c5");
		assertNotNull(c5);
		assertEquals(Scope.SELECT_LIST,c5.getScope());
	}
	
	@Test
	public void testFromItems(){
		SqlTableName t = AstUtils.findFirstTableName(assertParse("select * from t").nodes(),"t");
		assertNotNull(t);
		
		t = AstUtils.findFirstTableName(assertParse("select * from t join t1 on t.id = t1.id").nodes(),"t");
		assertNotNull(t);
		assertNull(t.getAlias());
	}

	@Test
	public void testParamPlaceholder() {
		Sql sql = sql("update person set firstName = current_firstName + #firstName# where id = ?");
		assertEquals("firstName", sql.findFirstNode(ParamPlaceholder.class).getName());
		sql = sql("select current_firstName + #firstName# from person where id = ?");
		assertEquals("firstName", sql.findFirstNode(ParamPlaceholder.class).getName());
	}

	@Test
	public void testDelete() {
		assertParse("delete person where id in (select m.member_id from member m where m.user_id = ?)");
	}
}