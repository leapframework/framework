/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.spring.boot.spel;

import leap.lang.el.ElException;
import leap.lang.expression.AbstractExpression;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;

import java.util.HashMap;
import java.util.Map;

public class SpringExpression extends AbstractExpression {

    protected final Expression          expr;
    protected final EvaluationContext   context;
    protected final Map<String, Object> globalVariables;

    public SpringExpression(Expression expr, EvaluationContext context, Map<String, Object> globalVariables) {
        this.expr = expr;
        this.context = context;
        this.globalVariables = globalVariables;
    }

    @Override
    protected Object eval(Object context, Map<String, Object> vars) {
        if (null == vars) {
            vars = new HashMap<>();
        }
        if (null != globalVariables) {
            vars.putAll(globalVariables);
        }

        try {
            return expr.getValue(this.context, vars);
        } catch (Exception e) {
            throw new ElException("Err eval expr [ " + expr.getExpressionString() + " ], " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return expr.getExpressionString();
    }

}