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
import leap.core.cache.Cache;
import leap.core.cache.SimpleLRUCache;
import leap.core.el.ExpressionLanguage;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.metadata.MetadataContext;
import leap.orm.sql.Sql.ParseLevel;
import leap.orm.sql.ast.*;
import leap.orm.sql.parser.Lexer;
import leap.orm.sql.parser.SqlParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Configurable(prefix="orm.dynamicSQL")
public class DynamicSqlLanguage implements SqlLanguage {
	
	private static final Log log = LogFactory.get(DynamicSqlLanguage.class);
	
	protected Boolean            smart;
	protected ExpressionLanguage expressionLanguage;
	
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
			log.debug("Parsing sql :\n {}", sql);
			sqls = createParser(sql).sqls();

            if(smart()) {

                List<Sql> resolvedSqls = new ArrayList<>();

                for(Sql s : sqls) {
                    s = new SqlResolver(context,s).resolve();
                    processingWhereFields(s);
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

    protected void processingWhereFields(Sql sql) {
        if(sql.isDelete() || sql.isSelect() || sql.isUpdate()) {

            List<SqlTableSource> tables = new ArrayList<>();

            AstNode clause = sql.nodes()[0];

            if(clause instanceof SqlQuery) {

                SqlQuery query = (SqlQuery)clause;

                for(SqlTableSource ts : query.getTableSources()) {

                    if(ts instanceof SqlTableName) {

                        EntityMapping em = ((SqlTableName)ts).getEntityMapping();

                        if(null != em && em.hasWhereFields()) {
                            tables.add(ts);
                        }

                    }

                }

                if(tables.isEmpty()) {
                    return;
                }
            }

            sql.traverse((node) -> {

                if(node instanceof SqlWhere) {

                    //todo : take the first one only
                    SqlTableSource ts = tables.get(0);
                    EntityMapping  em = ((SqlTableName)ts).getEntityMapping();

                    //checks the where field(s) exists in the where expression.
                    AtomicBoolean exists = new AtomicBoolean(false);
                    node.traverse((n1) -> {

                        if(n1 instanceof SqlObjectName) {

                            FieldMapping fmInSQL = ((SqlObjectName)n1).getFieldMapping();

                            for(FieldMapping fm : em.getWhereFieldMappings()) {
                                if(fmInSQL == fm) {
                                    exists.set(true);
                                    return false;
                                }
                            }
                        }

                        return true;
                    });

                    //if not exists, add the where condition
                    if(!exists.get()) {
                        FieldMapping[] whereFields = em.getWhereFieldMappings();

                        AstNode[] olds = ((SqlWhere)node).getNodes();

                        List<AstNode> nodes = new ArrayList<>();

                        //where ( original expression ) and (...)
                        nodes.add(new Text(olds[0].toString()).append(" ("));
                        for(int i=1;i<olds.length;i++) {
                            nodes.add(olds[i]);
                        }
                        nodes.add(new Text(" )"));

                        String alias = Strings.isEmpty(ts.getAlias()) ? em.getTableName() : ts.getAlias();
                        if(whereFields.length == 1 && whereFields[0].getWhereIf() == null) {
                            addWhereFieldNode(nodes, whereFields[0], alias);
                        }else{
                            nodes.add(new Text(" and ( 1=1"));

                            for(int i=0;i<em.getWhereFieldMappings().length;i++) {
                                FieldMapping fm = em.getWhereFieldMappings()[i];

                                addWhereFieldNode(nodes, fm, alias);
                            }

                            nodes.add(new Text(" ) "));
                        }

                        ((SqlWhere)node).setNodes(nodes.toArray(new AstNode[0]));

                        return false;
                    }
                }

                return true;
            });
        }
    }

    protected void addWhereFieldNode(List<AstNode> nodes, FieldMapping fm, String alias) {
        List<AstNode> list = new ArrayList<>();

        list.add(new Text(" and "));

        list.add(new Text(alias + "." + fm.getColumnName() + " = "));
        list.add(new ExprParamPlaceholder(fm.getWhereValue().toString(), fm.getWhereValue()));

        if(null != fm.getWhereIf()) {
            nodes.add(new ConditionalNode(fm.getWhereIf(), nodes.toArray(new AstNode[0])));
        }else{
            nodes.addAll(list);
        }
    }
	
	protected boolean smart() {
	    if(null == smart) {
	        smart = true;
	    }
	    return smart;
	}
}