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

import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.sql.ast.*;
import leap.orm.sql.parser.Token;

import java.util.ArrayList;
import java.util.List;

class SqlShardingProcessor {

    private final Sql sql;

    SqlShardingProcessor(Sql sql) {
        this.sql = sql;
    }

    void processShardingTable() {
        if(sql.isDelete() || sql.isSelect() || sql.isUpdate()) {

            sql.traverse((node) -> {

                if(node instanceof SqlWhere) {

                    SqlWhere where = (SqlWhere)node;
                    SqlQuery query = where.getQuery();

                    for(SqlTableSource ts : query.getTableSources()) {

                        if(ts instanceof SqlTableName) {

                            EntityMapping em = ((SqlTableName)ts).getEntityMapping();

                            if(null != em && em.isSharding()) {

                                FieldMapping shardingField = em.getShardingField();
                                if(null == shardingField) {
                                    throw new IllegalStateException("Sharding field cannot be null of sharding entity '" + em.getEntityName() + "'");
                                }

                                ShardingCondition sc = findShardingCondition(where, shardingField);
                                if(null != sc) {
                                    processShardingCondition(where, (SqlTableName)ts, sc);
                                }

                            }
                        }

                    }

                }

                return true;
            });
        }
    }

    //remove the sharding condition.
    //update the table name.
    private void processShardingCondition(SqlWhere where, SqlTableName table, ShardingCondition sc) {

        //remove the sharding condition.

        List<AstNode> nodes = new ArrayList<>();

        for(int i=0;i<where.getNodes().length;i++) {

            AstNode node = where.getNodes()[i];

            if(node == sc.nodes.get(0)) {

                i += sc.nodes.size();

            }else{

                nodes.add(node);

            }

        }

        where.setNodes(nodes.toArray(new AstNode[0]));

        //todo :
        AstNode value = sc.value;
    }

    //( sharding_column = ? [and] ) or ( [and] sharding_column = ? )
    private ShardingCondition findShardingCondition(SqlWhere where, FieldMapping shardingField) {
        for(int i=0;i<where.getNodes().length;i++) {

            AstNode wn = where.getNodes()[i];

            if(wn instanceof SqlObjectName) {

                SqlObjectName column = (SqlObjectName)wn;
                if(column.getFieldMapping() == shardingField) {

                    SqlToken eq = AstUtils.nextNode(where.getNodes(), i, SqlToken.class);
                    if(null != eq && eq.isToken(Token.EQ)) {

                        AstNode value = AstUtils.nextNode(where.getNodes(), i+1);

                        if(null != value) {

                            ShardingCondition sc = new ShardingCondition();
                            sc.column = column;
                            sc.value  = value;

                            SqlToken prev = AstUtils.prevNode(where.getNodes(), i, SqlToken.class);
                            if(null != prev && prev.isToken(Token.AND)) {
                                sc.add(prev);
                            }

                            sc.add(column);
                            sc.add(eq);
                            sc.add(value);

                            if(sc.nodes.size() == 3) {
                                SqlToken and = AstUtils.nextNode(where.getNodes(), i + 2, SqlToken.class);
                                if(null != and && and.isToken(Token.AND)) {
                                    sc.add(and);
                                }
                            }

                            return sc;
                        }
                    }

                }
            }

        }
        return null;
    }


    private static final class ShardingCondition {

        private List<AstNode> nodes = new ArrayList<>();
        private SqlObjectName column;
        private AstNode       value;

        void add(AstNode node) {
            nodes.add(node);
        }
    }
}
