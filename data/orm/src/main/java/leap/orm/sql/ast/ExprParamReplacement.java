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
import leap.orm.sql.PreparedBatchSqlStatementBuilder;
import leap.orm.sql.Sql.Scope;
import leap.orm.sql.SqlClauseException;
import leap.orm.sql.SqlContext;

import java.io.IOException;

public class ExprParamReplacement extends ExprParamBase {
	
	public ExprParamReplacement(Scope scope, String text, Expression expression) {
	    super(scope, text, expression);
    }
	
	@Override
    public boolean isReplace() {
	    return true;
    }

	@Override
    protected void prepareBatchStatement_(SqlContext context, PreparedBatchSqlStatementBuilder stm) throws IOException {
		throw new SqlClauseException("Batch executing sql cannot use Replacement Expression [" + this + "]");	    
    }

	@Override
	protected void toString_(Appendable buf) throws IOException {
		buf.append("${").append(text).append('}');
    }
}