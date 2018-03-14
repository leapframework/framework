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

import leap.core.AppReloadListener;
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
public class DynamicSqlLanguage implements SqlLanguage,AppReloadListener {
	
	private static final Log log = LogFactory.get(DynamicSqlLanguage.class);
	
	protected Boolean                    smart;
	protected @Inject ExpressionLanguage expressionLanguage;
	
	private Cache<String, List<Sql>> cache = new SimpleLRUCache<>();

    private Cache<String, List<DynamicSql.ExecutionSqls>> executionCache = new SimpleLRUCache<>();

    @Override
    public void postAppReload() {
        log.info("App reload, clear all cache");
        cache.clear();
        executionCache.clear();
    }

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
    public List<SqlClause> parseClauses(MetadataContext context, String text, Options options) throws SqlClauseException {
        List<Sql> sqls = doParseDynaSql(context, text);

        List<SqlClause> clauses = new ArrayList<>();

        for(Sql sql : sqls){
            clauses.add(new DynamicSqlClause(this, new DynamicSql(this, context, sql, options)));
        }

        return clauses;
    }

    @Override
    public SqlClause parseClause(MetadataContext context, String sql, Options options) throws SqlClauseException {
        List<Sql> sqls = doParseDynaSql(context, sql);
        return new DynamicSqlClause(this, new DynamicSql(this, context, sqls.get(0), options));
    }

    public DynamicSql.ExecutionSqls parseExecutionSqls(MetadataContext context, String sql, Options options) {
        List<DynamicSql.ExecutionSqls> sqls = doParseExecutionSqls(context, sql, options);

        if(sqls.size() > 1) {
            throw new IllegalStateException("Parsing multi sqls");
        }

        return sqls.get(0);
    }

    protected List<DynamicSql.ExecutionSqls> doParseExecutionSqls(MetadataContext context, String text, Options options) {
        String key = context.getName() + "___" + text;

        List<DynamicSql.ExecutionSqls> sqls = executionCache.get(key);

        if(null == sqls) {
            log.trace("Parsing sql :\n {}", text);
            sqls = new ArrayList<>();

            List<Sql> rawSqls = createParser(text).sqls();
            for(Sql sql : rawSqls) {
                sql.prepare(context);
            }

            if(smart()) {
                List<Sql> resolvedSqls = createParser(text).sqls();
                for(Sql sql : resolvedSqls) {
                    sql.prepare(context);
                }

                for(int i=0;i<resolvedSqls.size();i++) {
                    Sql s = resolvedSqls.get(i);

                    s = new SqlResolver(context,s).resolve();

                    if(null == options.getFilterColumnEnabled() || options.getFilterColumnEnabled()) {
                        processWhereFields(context, s);
                    }

                    if(null == options.getQueryFilterEnabled() || options.getQueryFilterEnabled()) {
                        processQueryFilter(context, s);
                    }

                    sqls.add(new DynamicSql.ExecutionSqls(rawSqls.get(i),s));
                }

            }else{
                for(Sql s : rawSqls) {
                    sqls.add(new DynamicSql.ExecutionSqls(s,s));
                }
            }

            executionCache.put(key, sqls);
        }
        return sqls;
    }
	
	protected List<Sql> doParseDynaSql(MetadataContext context, String text) {
        String key = context.getName() + "___" + text;

		List<Sql> sqls = cache.get(key);

		if(null == sqls) {
			log.trace("Parsing dyna sql :\n {}", text);

            SqlParser parser = new SqlParser(new Lexer(text, ParseLevel.DYNA), expressionLanguage);
            sqls = parser.sqls();
            for(Sql sql : sqls) {
                sql.prepare(context);
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

    protected void processWhereFields(MetadataContext context, Sql sql) {
        new SqlFilterColumnProcessor(context, sql).process();
    }

    protected void processQueryFilter(MetadataContext context, Sql sql) {
        if(context.getConfig().isQueryFilterEnabled()) {
            new SqlQueryFilterProcessor(context, sql).process();
        }
    }
	
	protected boolean smart() {
	    if(null == smart) {
	        smart = true;
	    }
	    return smart;
	}
}