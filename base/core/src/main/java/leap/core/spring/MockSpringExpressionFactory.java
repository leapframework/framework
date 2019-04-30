/*
 *  Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.core.spring;

import leap.core.annotation.Inject;
import leap.core.el.ExpressionLanguage;
import leap.lang.Strings;
import leap.lang.expression.Expression;

public class MockSpringExpressionFactory implements ExpressionFactory<Expression> {

    protected @Inject ExpressionLanguage el;

    @Override
    public Expression createExpression(String expr) {
        return createSpringExpression(expr);
    }

    @Override
    public Expression createSpringExpression(String expr) {
        return el.createExpression(removePrefixAndSuffix(expr));
    }

    protected String removePrefixAndSuffix(String expr) {
        if (expr.startsWith("#{")) {
            return Strings.removeEnd(Strings.removeStart(expr, "#{"), "}");
        } else {
            return expr;
        }
    }
}
