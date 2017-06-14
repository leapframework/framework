/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.orm.sql;

import leap.lang.Collections2;
import leap.lang.Strings;
import leap.lang.params.Params;
import leap.orm.OrmConfig;
import leap.orm.mapping.EntityMapping;
import leap.orm.metadata.MetadataContext;
import leap.orm.sql.ast.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

class SqlQueryFilterProcessor {

    private final MetadataContext             context;
    private final OrmConfig.QueryFilterConfig config;
    private final Sql                         sql;

    private boolean processed;

    public SqlQueryFilterProcessor(MetadataContext context, Sql sql) {
        this.context = context;
        this.config  = context.getConfig().getQueryFilterConfig();
        this.sql     = sql;
    }

    boolean process() {
        //todo : update and delete

        if(sql.isSelect()) {

            AtomicBoolean filterExists = new AtomicBoolean();

            sql.traverse(n -> {
                if (n instanceof Tag) {
                    String name = ((Tag) n).getName();
                    if (name.equalsIgnoreCase(config.getTagName())) {
                        filterExists.set(true);
                        return false;
                    }
                }

                return true;
            });

            if(filterExists.get()) {
                return false;
            }

            sql.traverse(node -> {

                if(node instanceof SqlQuery) {
                    processQuery((SqlQuery)node);
                }

                return true;
            });

        }

        return processed;
    }

    private void processQuery(SqlQuery query) {
        Set<SqlTableSource> joinTables = new HashSet<>();
        Set<SqlTableSource> tables = new HashSet<>();

        for(SqlTableSource ts : query.getTableSources()) {

            if(ts instanceof SqlTableName) {

                EntityMapping em = ((SqlTableName)ts).getEntityMapping();

                if(null != em && em.isQueryFilterEnabled()) {

                    if(ts.isJoin()) {
                        joinTables.add(ts);
                    }else{
                        tables.add(ts);
                    }
                }

            }

        }

        if(joinTables.isEmpty() && tables.isEmpty()) {
            return;
        }

        query.traverse((node) -> {

            if(node instanceof SqlJoin) {
                SqlJoin join = (SqlJoin)node;
                if(!joinTables.contains(join.getTable())) {
                    return true;
                }
                if(join.isCommaJoin()) {
                    tables.add(join.getTable());
                    return true;
                }

                SqlTableSource ts = join.getTable();
                EntityMapping  em = ((SqlTableName)ts).getEntityMapping();

                String alias = Strings.isEmpty(ts.getAlias()) ? em.getTableName() : ts.getAlias();

                AstNode[] olds = ((SqlJoin)node).getNodes();

                List<AstNode> nodes = new ArrayList<>();

                Collections2.addAll(nodes, olds);

                List<AstNode> filterNodes = new ArrayList<>();
                if(!join.hasOnExpression()) {
                    filterNodes.add(new Text(" on "));
                }else{
                    filterNodes.add(new Text(" and "));
                }
                addQueryFilter(filterNodes, em, alias);

                Function<SqlContext, Boolean> func = (c) -> null == c.getQueryFilterEnabled() || c.getQueryFilterEnabled();
                nodes.add(new ConditionalNode(func, new AstNode[]{new DynamicClause(filterNodes.toArray(new AstNode[0]))}));

                ((SqlJoin)node).setNodes(nodes.toArray(new AstNode[0]));

                return true;
            }

            if(node instanceof SqlWhere && !tables.isEmpty()) {
                SqlWhere where = (SqlWhere)node;

                //todo : take the first one only
                SqlTableSource ts = tables.iterator().next();

                if(!where.getQuery().getTableSources().contains(ts)) {
                    return true;
                }

                EntityMapping em    = ((SqlTableName)ts).getEntityMapping();
                String        alias = Strings.isEmpty(ts.getAlias()) ? em.getTableName() : ts.getAlias();

                AstNode[] olds = ((SqlWhere)node).getNodes();

                List<AstNode> nodes = new ArrayList<>();
                List<AstNode> filterNodes = new ArrayList<>();

                if(olds.length > 0) {
                    //where ( original expression ) and (...)
                    nodes.add(new Text(olds[0].toString()).append(" ("));
                    for (int i = 1; i < olds.length; i++) {
                        nodes.add(olds[i]);
                    }
                    nodes.add(new Text(" )"));

                    filterNodes.add(new Text(" and "));
                }else{
                    filterNodes.add(new Text(" where "));
                }

                addQueryFilter(filterNodes, em, alias);

                Function<SqlContext, Boolean> func = (c) -> null == c.getQueryFilterEnabled() || c.getQueryFilterEnabled();
                nodes.add(new ConditionalNode(func, new AstNode[]{new DynamicClause(filterNodes.toArray(new AstNode[0]))}));

                ((SqlWhere)node).setNodes(nodes.toArray(new AstNode[0]));

                return false;
            }

            return true;
        });
    }

    private void addQueryFilter(List<AstNode> nodes, EntityMapping em, String alias) {
        processed = true;

        String content = em.getEntityName();

        Tag tag = new QfTag(config.getTagName(), content, config.getAlias(), alias);
        tag.prepare(context);

        nodes.add(tag);
    }

    protected static final class QfTag extends Tag {

        private final String qfAlias;
        private final String emAlias;

        public QfTag(String name, String content, String qfAlias, String emAlias) {
            super(name, content);
            this.qfAlias = qfAlias;
            this.emAlias = emAlias;
        }

        @Override
        protected void buildStatement_(SqlContext context, SqlStatementBuilder stm, Params params) throws IOException {
            SqlStatementBuilder.SavePoint sp = stm.createSavePoint();

            super.buildStatement_(context, stm, params);

            if(sp.hasChanges()) {

                String text = sp.removeAppendedText();

                text = Strings.replaceIgnoreCase(text, qfAlias + ".", Strings.isEmpty(emAlias) ? "" : emAlias + ".");

                stm.append(text);
            }
        }
    }
}
