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
package leap.orm.sql;

import java.util.ArrayList;
import java.util.List;

import leap.core.annotation.Configurable;
import leap.core.cache.Cache;
import leap.core.cache.SimpleLRUCache;
import leap.core.el.ExpressionLanguage;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.metadata.MetadataContext;
import leap.orm.sql.Sql.ParseLevel;
import leap.orm.sql.parser.Lexer;
import leap.orm.sql.parser.SqlParser;

@Configurable(prefix="orm.dynamicSQL")
public class DynamicSqlLanguage implements SqlLanguage {
	
	private static final Log log = LogFactory.get(DynamicSqlLanguage.class);
	
	protected Boolean            smart;
	protected ExpressionLanguage expressionLanguage;
	
	private Cache<String, List<Sql>> cache = new SimpleLRUCache<String, List<Sql>>();
	
	@Configurable.Property
	public void setMode(String mode) {
	    if("simple".equals(mode)) {
	        smart = false;
	    }else if("smart".equals(mode)) {
	        smart = true;
	    }else{
	        throw new IllegalArgumentException("The mode must be 'simple' or 'smart'");
	    }
	}

	public boolean isSimple() {
	    return !smart();
	}
	
    public ExpressionLanguage getExpressionLanguage() {
		return expressionLanguage;
	}

	public void setExpressionLanguage(ExpressionLanguage expressionLanguage) {
		this.expressionLanguage = expressionLanguage;
	}

	@Override
    public List<SqlClause> parseClauses(MetadataContext context, String text) throws SqlClauseException {
		List<Sql> sqls = doParseSql(text);
		
		List<SqlClause> clauses = new ArrayList<SqlClause>();
		
		for(Sql sql : sqls){
			clauses.add(createClause(context,sql));
		}
		
		return clauses;
    }
	
	@Override
    public DynamicSqlClause parseClause(MetadataContext context, String sql) throws SqlClauseException {
		List<Sql> sqls = doParseSql(sql);
		return createClause(context, sqls.get(0));
    }
	
	protected List<Sql> doParseSql(String sql) {
		List<Sql> sqls = cache.get(sql);
		if(null == sqls) {
			log.debug("Parsing sql :\n {}", sql);
			sqls = createParser(sql).sqls();
			cache.put(sql, sqls);
		}
		return sqls;
	}

	protected SqlParser createParser(String text){
		if(smart()){
			return new SqlParser(new Lexer(text, ParseLevel.MORE),expressionLanguage);
		}else{
			return new SqlParser(new Lexer(text, ParseLevel.BASE),expressionLanguage);
		}
	}
	
	protected DynamicSqlClause createClause(MetadataContext context, Sql sql) {
		if(smart()){
			sql = new SqlResolver(context,sql).resolve();
		}
		return new DynamicSqlClause(this,sql);
	}
	
	protected boolean smart() {
	    if(null == smart) {
	        smart = true;
	    }
	    return smart;
	}
}