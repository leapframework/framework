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

import leap.lang.expression.Expression;

/**
 * Expression factory for creating spring expression.
 */
public interface ExpressionFactory<E> {

    /**
     * Is spring expression?
     */
    default boolean isExpr(Object v) {
        if(v instanceof String) {
            String s = (String)v;
            if(((String) v).startsWith("#{") && s.endsWith("}")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the original value or expression if the given value is an expression.
     */
    default Object tryCreateExpression(Object v) {
        if(isExpr(v)) {
            return createExpression((String)v);
        }else{
            return v;
        }
    }

    /**
     * Create the wrapped spring expression
     */
    Expression createExpression(String expr);

    /**
     * Creates the raw spring expression.
     */
    E createSpringExpression(String expr);

}