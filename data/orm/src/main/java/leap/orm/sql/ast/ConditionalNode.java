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

package leap.orm.sql.ast;

import leap.core.el.EL;
import leap.lang.New;
import leap.lang.expression.Expression;
import leap.lang.params.Params;
import leap.orm.sql.PreparedBatchSqlStatementBuilder;
import leap.orm.sql.SqlContext;
import leap.orm.sql.SqlStatementBuilder;

import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ConditionalNode extends SqlNodeContainer {

    private final Expression                   exprCondition;
    private final Function<SqlContext,Boolean> funcCondition;

    public ConditionalNode(Expression exprCondition, AstNode[] nodes) {
        this(null, exprCondition, nodes);
    }

    public ConditionalNode(Function<SqlContext,Boolean> funcCondition, AstNode[] nodes) {
        this(funcCondition,null, nodes);
    }

    public ConditionalNode(Function<SqlContext,Boolean> funcCondition, Expression exprCondition, AstNode[] nodes) {
        super(nodes);
        this.exprCondition = exprCondition;
        this.funcCondition = funcCondition;
    }

    @Override
    protected void prepareBatchStatement_(SqlContext context, PreparedBatchSqlStatementBuilder stm,Object[] params) throws IOException {
        if(null != funcCondition && !funcCondition.apply(context)) {
            return;
        }

        if(null != exprCondition && !EL.test(exprCondition, null, New.hashMap("sqlContext",context,"params",params))){
            return;
        }

        super.prepareBatchStatement_(context, stm,params);
    }

    @Override
    protected void buildStatement_(SqlContext context, SqlStatementBuilder stm, Params params) throws IOException {
        if(null != funcCondition && !funcCondition.apply(context)) {
            return;
        }

        if(null != exprCondition && !EL.test(exprCondition, null, params.map())){
            return;
        }

        super.buildStatement_(context, stm, params);
    }
}