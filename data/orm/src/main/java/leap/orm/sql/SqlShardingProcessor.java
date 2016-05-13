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

import leap.db.Db;
import leap.lang.params.Params;
import leap.orm.OrmContext;
import leap.orm.dmo.Dmo;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.sql.ast.*;
import leap.orm.sql.parser.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class SqlShardingProcessor {

    private final Sql sql;

    SqlShardingProcessor(Sql sql) {
        this.sql = sql;
    }

    void processShardingTable() {
        if(sql.isInsert()) {
            processInsert();
            return ;
        }

        if(sql.isDelete() || sql.isUpdate() || sql.isSelect()) {
            processQuery();
            return;
        }

    }

    protected void processInsert() {
        if(!(sql.nodes()[0] instanceof SqlInsert)) {
            return;
        }

        SqlInsert insert = (SqlInsert)sql.nodes()[0];

        SqlTableName tn = insert.getTableName();
        if(null == tn) {
            return;
        }

        EntityMapping em = tn.getEntityMapping();
        if(null == em || !em.isSharding()) {
            return;
        }

        FieldMapping shardingField = em.getShardingField();
        if(null == shardingField) {
            throw new IllegalStateException("Sharding field cannot be null of sharding entity '" + em.getEntityName() + "'");
        }

        int index=-1;
        for(int i=0;i<insert.getColumns().size();i++) {
            SqlObjectName col = insert.getColumn(i);

            if(shardingField == col.getFieldMapping()) {
                index = i;
                break;
            }
        }

        if(index >= 0) {
            AstNode value = insert.getValue(index);
            updateShardingTableName(tn, insert.getColumn(index), value);
        }
    }

    protected void processQuery() {
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

    //remove the sharding condition.
    //update the table name.
    private void processShardingCondition(SqlWhere where, SqlTableName table, ShardingCondition sc) {

        //remove the sharding condition.

        List<AstNode> nodes = new ArrayList<>();

        for(int i=1;i<where.getNodes().length;i++) {

            AstNode node = where.getNodes()[i];

            if(node == sc.nodes.get(0)) {

                i += sc.nodes.size();

            }else{

                nodes.add(node);

            }

        }

        if(AstUtils.isAllBlank(nodes)) {
            where.setNodes(new AstNode[0]);
        }else{
            nodes.add(0, where.getNodes()[0]);
            where.setNodes(nodes.toArray(new AstNode[0]));
        }

        updateShardingTableName(table, sc.column, sc.value);
    }

    private void updateShardingTableName(SqlTableName table, SqlObjectName column, AstNode value) {
        if(value instanceof SqlLiteral) {
            table.setDynamicTableName(new ShardingTableName(table.getEntityMapping(), ((SqlLiteral)value).getValue().toString()));
            return;
        }

        if(value instanceof ParamBase) {
            table.setDynamicTableName(new ShardingTableName(table.getEntityMapping(), (ParamBase)value));
            return;
        }

        throw new IllegalStateException("Unsupported value '" + value + "' of sharding column '" + column.getLastName() + "'");
    }

    //( sharding_column = ? [and] ) or ( [and] sharding_column = ? )
    private ShardingCondition findShardingCondition(SqlWhere where, FieldMapping shardingField) {
        for(int i=0;i<where.getNodes().length;i++) {

            AstNode wn = where.getNodes()[i];

            if(wn instanceof SqlObjectName) {

                SqlObjectName column = (SqlObjectName)wn;
                if(column.getFieldMapping() == shardingField) {

                    AtomicInteger index = new AtomicInteger(i);

                    SqlToken eq = AstUtils.nextNodeSkipBlank(where.getNodes(), index, SqlToken.class);
                    if(null != eq && eq.isToken(Token.EQ)) {

                        AstNode value = AstUtils.nextNodeSkipBlank(where.getNodes(), index);

                        if(null != value) {

                            ShardingCondition sc = new ShardingCondition();
                            sc.column = column;
                            sc.value  = value;

                            int start = i;
                            int end   = index.get();

                            AtomicInteger prevIndex = new AtomicInteger(i);
                            SqlToken prev = AstUtils.prevNodeSkipBlank(where.getNodes(), prevIndex, SqlToken.class);
                            if(null != prev && prev.isToken(Token.AND)) {
                                start = prevIndex.get();
                            }else{
                                SqlToken and = AstUtils.nextNodeSkipBlank(where.getNodes(), index, SqlToken.class);
                                if(null != and && and.isToken(Token.AND)) {
                                    end = index.get();
                                }
                            }

                            for(int j=start;j<=end;j++) {
                                sc.add(where.getNodes()[j]);
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

    private static final class ShardingTableName implements DynamicName {

        private final EntityMapping em;
        private final String        value;
        private final ParamBase     param;

        public ShardingTableName(EntityMapping em, String value) {
            this.em     = em;
            this.value  = value;
            this.param  = null;
        }

        public ShardingTableName(EntityMapping em, ParamBase param) {
            this.em    = em;
            this.param = param;
            this.value = null;
        }

        @Override
        public String get(SqlStatementBuilder stm, Params params) {
            if(null != value) {
                return getTableName(stm.context().getOrmContext(), value);
            }else{
                stm.increaseAndGetParameterIndex();

                Object v = param.eval(stm, params);

                if(null == v) {
                    //todo : do not allow null value.
                    return getTableName(stm.context().getOrmContext(), "");
                }else{
                    return getTableName(stm.context().getOrmContext(), v);
                }
            }
        }

        protected String getTableName(OrmContext context,Object v) {
            Db db = context.getDb();

            String tableName = em.getShardingAlgorithm().evalShardingTableName(em, v);

            if(em.isAutoCreateShardingTable() && !db.checkTableExists(tableName)) {
                Dmo dmo = Dmo.get(context.getName());
                dmo.cmdCreateTable(em).changeTableName(tableName).execute();
            }

            return tableName;
        }
    }
}
