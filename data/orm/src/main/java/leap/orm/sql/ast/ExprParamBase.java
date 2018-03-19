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

import leap.lang.Args;
import leap.lang.expression.Expression;
import leap.lang.params.Params;
import leap.orm.sql.Sql;
import leap.orm.sql.SqlStatementBuilder;

import java.util.HashMap;
import java.util.Map;

public abstract class ExprParamBase extends ParamBase {
	
	protected final String     text;
	protected final Expression expression;
	
	public ExprParamBase(Sql.Scope scope, String text, Expression expression){
        super(scope);

		Args.notEmpty(text);
		Args.notNull(expression);
		this.text       = text;
		this.expression = expression;
	}
	
	public String getText() {
		return text;
	}

	public Expression getExpression() {
		return expression;
	}

    @Override
    public Object eval(SqlStatementBuilder stm, Params params) {
        return getParameterValue(stm, params);
    }

    @Override
    protected Object getParameterValue(SqlStatementBuilder stm, Params params) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("$params", params);
        vars.putAll(params.map());
        if(null != stm.getVars()) {
            vars.putAll(stm.getVars());
        }
		return expression.getValue(stm.context(), vars);
    }
}