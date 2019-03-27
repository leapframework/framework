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

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public final class Message {

    private final Object              source;
    private final Locale              locale;
    private final CompositeExpression expression;

    protected Message(Object source, Locale locale, String string) {
        this(source, locale, new CompositeExpression(string));
    }

    protected Message(Object source, Locale locale, CompositeExpression expression) {
        this.source = source;
        this.locale = locale;
        this.expression = expression;
    }

    public Object getSource() {
        return source;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getString(Map<String, Object> vars) {
        return (String) expression.getValue(null == vars ? Collections.emptyMap() : vars);
    }

    public Expression getExpression() {
        return expression;
    }
}