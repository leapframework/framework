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

import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.core.cache.Cache;
import leap.core.cache.SimpleLRUCache;
import leap.core.el.ExpressionLanguage;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.metadata.MetadataContext;
import leap.orm.sql.Sql.ParseLevel;
import leap.orm.sql.parser.Lexer;
import leap.orm.sql.parser.SqlParser;

import java.util.ArrayList;
import java.util.List;

@Configurable(prefix="orm.dynamicSQL")
public class DynamicSqlLanguage implements SqlLanguage {
	
	private static final Log log = LogFactory.get(DynamicSqlLanguage.class);
	
	protected Boolean            smart;
	protected @Inject ExpressionLanguage expressionLanguage;
	
	private Cache<String, List<Sql>> cache = new SimpleLRUCache<>();
	
	@ConfigProperty
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
		List<Sql> sqls = doParseSql(context, text);
		
		List<SqlClause> clauses = new ArrayList<>();
		
		for(Sql sql : sqls){
			clauses.add(createClause(context,sql));
		}
		
		return clauses;
    }
	
	@Override
    public DynamicSqlClause parseClause(MetadataContext context, String sql) throws SqlClauseException {
		List<Sql> sqls = doParseSql(context, sql);
		return createClause(context, sqls.get(0));
    }
	
	protected List<Sql> doParseSql(MetadataContext context, String sql) {
        String key = context.getName() + "___" + sql;

		List<Sql> sqls = cache.get(key);
		if(null == sqls) {
			log.trace("Parsing sql :\n {}", sql);
			sqls = createParser(sql).sqls();

            if(smart()) {

                List<Sql> resolvedSqls = new ArrayList<>();

                for(Sql s : sqls) {
                    s = new SqlResolver(context,s).resolve();

                    processShardingTable(s);
                    processWhereFields(s);

                    resolvedSqls.add(s);
                }

                sqls = resolvedSqls;

            }

			cache.put(key, sqls);
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
		return new DynamicSqlClause(this,sql);
	}

    protected void processShardingTable(Sql sql) {
        new SqlShardingProcessor(sql).processShardingTable();
    }

    protected void processWhereFields(Sql sql) {
        new SqlWhereProcessor(sql).processWhereFields();
    }
	
	protected boolean smart() {
	    if(null == smart) {
	        smart = true;
	    }
	    return smart;
	}
}