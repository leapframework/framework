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
package leap.orm.sql.ast;

import leap.lang.expression.Expression;
import leap.lang.params.Params;
import leap.orm.sql.PreparedBatchSqlStatementBuilder;
import leap.orm.sql.Sql;
import leap.orm.sql.SqlContext;
import leap.orm.sql.SqlStatementBuilder;

import java.io.IOException;

public class IfCondition extends ExprParamBase {
	
	public IfCondition(Sql.Scope scope, String text, Expression expression) {
	    super(scope, text, expression);
    }

	@Override
	protected void toString_(Appendable buf) throws IOException {
		buf.append("(").append(text).append(")");
    }

	@Override
    protected void prepareBatchStatement_(SqlContext context, PreparedBatchSqlStatementBuilder stm,Object[] params) throws IOException {
		 new IllegalStateException("If condition not implemented");
    }

	@Override
	protected void buildStatement_(SqlContext context, SqlStatementBuilder stm, Params params) throws IOException {
		throw new IllegalStateException("If condition not implemented");
    }
}