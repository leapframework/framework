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
import leap.orm.mapping.FieldMapping;
import leap.orm.metadata.MetadataContext;
import leap.orm.sql.ast.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

class SqlQueryFilterProcessor {

    private final MetadataContext context;
    private final OrmConfig       config;
    private final Sql             sql;

    private boolean processed;

    public SqlQueryFilterProcessor(MetadataContext context, Sql sql) {
        this.context = context;
        this.config  = context.getConfig();
        this.sql     = sql;
    }

    boolean process() {
        //todo : update and delete

        if(sql.isSelect()) {

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

                if(isQueryFilterExists(node, em)) {
                    return true;
                }

                AstNode[] olds = ((SqlJoin)node).getNodes();

                List<AstNode> nodes = new ArrayList<>();

                Collections2.addAll(nodes, olds);

                if(!join.hasOnExpression()) {
                    nodes.add(new Text(" on "));
                }

                String alias = Strings.isEmpty(ts.getAlias()) ? em.getTableName() : ts.getAlias();

                addQueryFilter(nodes, em, alias, join.hasOnExpression());

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

                EntityMapping  em = ((SqlTableName)ts).getEntityMapping();

                if(isQueryFilterExists(node, em)) {
                    return true;
                }

                AstNode[] olds = ((SqlWhere)node).getNodes();

                List<AstNode> nodes = new ArrayList<>();

                if(olds.length > 0) {
                    //where ( original expression ) and (...)
                    nodes.add(new Text(olds[0].toString()).append(" ("));
                    for (int i = 1; i < olds.length; i++) {
                        nodes.add(olds[i]);
                    }
                    nodes.add(new Text(" )"));
                }else {
                    nodes.add(new Text(" where 1=1 "));
                }

                String alias = Strings.isEmpty(ts.getAlias()) ? em.getTableName() : ts.getAlias();

                addQueryFilter(nodes, em, alias, true);

                ((SqlWhere)node).setNodes(nodes.toArray(new AstNode[0]));

                return false;
            }

            return true;
        });
    }

    private void addQueryFilter(List<AstNode> nodes, EntityMapping em, String alias, boolean and) {

        processed = true;

        String content = config.getQueryFilterPrefix() + em.getEntityName();

        Tag tag = new QfTag(config.getQueryFilterName(), content, true, config.getQueryFilterAlias(), alias);
        tag.prepare(context);

        if(and) {
            nodes.add(new DynamicClause(new AstNode[]{new Text(" and "), tag}));
        }else{
            nodes.add(tag);
        }

    }

    //checks the query filter exists in the expression.
    private boolean isQueryFilterExists(AstNode node, EntityMapping em) {
        AtomicBoolean exists = new AtomicBoolean(false);

        node.traverse(n -> {
            if (n instanceof Tag) {
                String name = ((Tag) n).getName();
                if (name.equalsIgnoreCase(config.getQueryFilterName())) {
                    exists.set(true);
                    return false;
                }
            }

            return true;
        });

        return exists.get();
    }

    protected static final class QfTag extends Tag {

        private final String qfAlias;
        private final String emAlias;

        public QfTag(String name, String content, boolean optional, String qfAlias, String emAlias) {
            super(name, content, optional);
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
