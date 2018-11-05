/*
 * Copyright 2014 the original author or authors.
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
package leap.core.i18n;

import leap.lang.expression.CompositeExpression;
import leap.lang.expression.Expression;

public final class Message {

    private final Object              source;
    private final CompositeExpression expression;

    protected Message(Object source, String string) {
        this(source, new CompositeExpression(string));
    }

    protected Message(Object source, CompositeExpression expression) {
        this.source = source;
        this.expression = expression;
    }

    public Object getSource() {
        return source;
    }

    public String getString() {
        return (String)expression.getValue();
    }

    public Expression getExpression() {
        return expression;
    }
}