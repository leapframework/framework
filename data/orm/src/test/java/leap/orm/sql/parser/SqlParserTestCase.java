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
import leap.junit.TestBase;
import leap.junit.contexual.ContextualProviderBase;
import leap.junit.contexual.ContextualRule;
import leap.lang.New;
import leap.lang.expression.Expression;
import leap.lang.expression.ValuedExpression;
import leap.orm.sql.Sql;
import leap.orm.sql.Sql.ParseLevel;
import leap.orm.sql.ast.AstNode;
import leap.orm.sql.ast.SqlObjectName;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.Description;

public abstract class SqlParserTestCase extends TestBase {
	private static final List<String> levels = New.<String>arrayList("BASE","MORE");
	
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
	
	@Rule
	public final ContextualRule contextual = new ContextualRule(new ContextualProviderBase() {
		@Override
		public Iterable<String> names(Description description) {
			return levels;
		}
		
		@Override
		public void beforeTest(Description description, String name) throws Exception {
			if(name.equals("BASE")){
				level = ParseLevel.BASE;
			}else{
				level = ParseLevel.MORE;
			}
		}
	},true);
	
	protected ParseLevel level;
	protected Sql		 sql;
	
	@Before
	public void setup(){
		if(null == level){
			level = ParseLevel.BASE;
		}
	}
	
	protected final String parse(String sql){
		return sql(sql).toString();
	}
	
	protected final Sql sql(String text){
		List<Sql> sqls = parser(text).sqls();
        if(sqls.size() != 1) {
            throw new IllegalStateException("Must be one sql statement");
        }
        return sqls.get(0);
	}
	
	protected final SqlParser parser(String sql){
		return new SqlParser(new Lexer(sql,level),el);
	}
	
	protected final Sql assertParse(String text){
		Sql sql = sql(text);
		assertEquals(text,sql.toString());
		return sql;
	}
	
	protected static void assertEquals(String expected,AstNode node){
		assertNotNull(node);
		assertEquals(expected,node.toString());
	}

	protected static void assertObjectNames(AstNode[] nodes, String... names) {
		int i = 0;
		for (AstNode node : nodes) {
			if (node instanceof SqlObjectName) {
				assertEquals(names[i], node);
				i++;
			}
		}
	}

	protected List<String> split(String sqls){
		return parser(sqls).split();
	}
}
