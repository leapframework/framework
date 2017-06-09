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
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.sql.ast.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

class SqlQueryFilterProcessor {

    private final Sql    sql;
    private final String tagName;

    private boolean processed;

    public SqlQueryFilterProcessor(Sql sql, String tagName) {
        this.sql = sql;
        this.tagName = tagName;
    }

    boolean process() {
        if(sql.isDelete() || sql.isSelect() || sql.isUpdate()) {

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

                FieldMapping[] whereFields = em.getWhereFieldMappings();

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

        Tag tag = new Tag(tagName, em.getEntityName(), true);

        if(and) {
            nodes.add(new DynamicClause(new AstNode[]{new Text(" and "), tag}));
        }else{
            nodes.add(tag);
        }

    }

    //checks the where field(s) exists in the expression.
    private boolean isQueryFilterExists(AstNode node, EntityMapping em) {
        AtomicBoolean exists = new AtomicBoolean(false);

        node.traverse(n -> {
            if (n instanceof Tag) {
                String name = ((Tag) n).getName();
                String content = ((Tag) n).getContent();

                if (name.equalsIgnoreCase(tagName) && content.equalsIgnoreCase(em.getEntityName())) {
                    exists.set(true);
                    return false;
                }
            }

            return true;
        });

        return exists.get();
    }
}
