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
import leap.lang.expression.Expression;
import leap.lang.params.Params;
import leap.orm.sql.SqlStatementBuilder;

import java.io.IOException;

public class ConditionalNode extends SqlNodeContainer {

    private final Expression condition;

    public ConditionalNode(Expression condition) {
        this.condition = condition;
    }

    public ConditionalNode(Expression condition, AstNode[] nodes) {
        super(nodes);
        this.condition = condition;
    }

    @Override
    protected void buildStatement_(SqlStatementBuilder stm, Params params) throws IOException {
        if(EL.test(condition, null, params.map())) {
            super.buildStatement_(stm, params);
        }
    }
}