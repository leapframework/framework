/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.orm.sql;

import leap.lang.Collections2;
import leap.lang.Strings;
import leap.orm.OrmConfig;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.metadata.MetadataContext;
import leap.orm.sql.ast.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

class SqlFilterColumnProcessor {

    private final OrmConfig.FilterColumnConfig config;
    private final Sql                          sql;

    public SqlFilterColumnProcessor(MetadataContext context, Sql sql) {
        this.config = context.getConfig().getFilterColumnConfig();
        this.sql = sql;
    }

    void process() {
        if(sql.isDelete() || sql.isSelect() || sql.isUpdate()) {

            sql.traverse(node -> {

                if(node instanceof SqlQuery) {
                    processQuery((SqlQuery)node);
                }

                return true;
            });

        }
    }

    private void processQuery(SqlQuery query) {
        Set<SqlTableSource> joinTables = new HashSet<>();
        Set<SqlTableSource> tables = new HashSet<>();

        for(SqlTableSource ts : query.getTableSources()) {

            if(ts instanceof SqlTableName) {

                EntityMapping em = ((SqlTableName)ts).getEntityMapping();

                if(null != em && em.hasFilterFields()) {

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

                if(isWhereFieldExists(node, em)) {
                    return true;
                }

                String       alias = Strings.isEmpty(ts.getAlias()) ? em.getTableName() : ts.getAlias();
                FieldMapping fm    = em.getFilterFieldMappings()[0];

                AstNode[] olds = ((SqlJoin)node).getNodes();

                List<AstNode> nodes = new ArrayList<>();

                Collections2.addAll(nodes, olds);

                List<AstNode> filterNodes = new ArrayList<>();
                if(!join.hasOnExpression()) {
                    filterNodes.add(new Text(" on "));
                }else{
                    filterNodes.add(new Text(" and "));
                }
                addFilterNodes(filterNodes, fm, alias);

                Function<SqlContext, Boolean> func = (c) -> null == c.getFilterColumnEnabled() || c.getFilterColumnEnabled();

                if(null != config.getFilterIf()) {
                    nodes.add(new ConditionalNode(func, config.getFilterIf(), filterNodes.toArray(new AstNode[0])));
                }else{
                    nodes.add(new ConditionalNode(func, filterNodes.toArray(new AstNode[0])));
                }

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

                if(isWhereFieldExists(node, em)) {
                    return true;
                }

                String alias = Strings.isEmpty(ts.getAlias()) ? em.getTableName() : ts.getAlias();
                FieldMapping fm = em.getFilterFieldMappings()[0];

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
                    nodes.add(new Text(" where"));
                    nodes.add(new Text(" 1=1"));
                    filterNodes.add(new Text(" and "));
                }

                addFilterNodes(filterNodes, fm, alias);

                Function<SqlContext, Boolean> func = (c) -> null == c.getFilterColumnEnabled() || c.getFilterColumnEnabled();

                if(null != config.getFilterIf()) {
                    nodes.add(new ConditionalNode(func, config.getFilterIf(), filterNodes.toArray(new AstNode[0])));
                }else{
                    nodes.add(new ConditionalNode(func, filterNodes.toArray(new AstNode[0])));
                }

                ((SqlWhere)node).setNodes(nodes.toArray(new AstNode[0]));

                return false;
            }

            return true;
        });
    }

    private void addFilterNodes(List<AstNode> nodes, FieldMapping fm, String alias) {
        nodes.add(new Text(alias + "." + fm.getColumnName() + " = "));
        nodes.add(new ExprParamPlaceholder(Sql.Scope.WHERE, fm.getFilterValue().toString(), fm.getFilterValue()));
        nodes.add(new Text(" "));
    }

    //checks the where field(s) exists in the expression.
    private boolean isWhereFieldExists(AstNode node, EntityMapping em) {
        AtomicBoolean exists = new AtomicBoolean(false);
        node.traverse((n1) -> {

            if(n1 instanceof SqlObjectName) {

                FieldMapping fmInSQL = ((SqlObjectName)n1).getFieldMapping();

                for(FieldMapping fm : em.getFilterFieldMappings()) {
                    if(fmInSQL == fm) {
                        exists.set(true);
                        return false;
                    }
                }
            }

            return true;
        });

        return exists.get();
    }
}
