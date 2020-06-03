/*
 *
 *  * Copyright 2019 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.lang.jdbc;

import leap.lang.Arrays2;
import leap.lang.Collections2;
import leap.lang.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SimpleWhereBuilder implements WhereBuilder {

    private final StringBuilder where;
    private final List<Object>  args;

    public SimpleWhereBuilder() {
        this(new StringBuilder(), new ArrayList<>());
    }

    public SimpleWhereBuilder(StringBuilder where, List<Object> args) {
        this.where = where;
        this.args = args;
    }

    public StringBuilder getWhere() {
        return where;
    }

    public List<Object> getArgs() {
        return args;
    }

    @Override
    public boolean isEmpty() {
        return where.length() == 0;
    }

    @Override
    public WhereBuilder and(String expr) {
        return and(expr, Arrays2.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public WhereBuilder and(String expr, Object... args) {
        if (this.where.length() == 0) {
            this.where.append(expr);
        } else {
            this.where.insert(0, '(')
                    .append(") and (")
                    .append(expr)
                    .append(')');
        }
        addArgs(args);
        return this;
    }

    @Override
    public WhereBuilder and(Consumer<Expr> func) {
        ExprImpl expr = new ExprImpl();
        func.accept(expr);
        if (!Strings.isBlank(expr.buf)) {
            return and(expr.buf.toString());
        }
        return this;
    }

    @Override
    public WhereBuilder or(Consumer<Expr> func) {
        ExprImpl expr = new ExprImpl();
        func.accept(expr);
        if (!Strings.isBlank(expr.buf)) {
            return or(expr.buf.toString());
        }
        return this;
    }

    @Override
    public WhereBuilder or(String expr) {
        return or(expr, Arrays2.EMPTY_OBJECT_ARRAY);
    }

    @Override
    public WhereBuilder or(String expr, Object... args) {
        if (this.where.length() == 0) {
            this.where.append(expr);
        } else {
            this.where.insert(0, '(')
                    .append(") or (")
                    .append(expr)
                    .append(')');
        }
        addArgs(args);
        return this;
    }

    private void addArgs(Object... args) {
        Collections2.addAll(this.args, args);
    }

    @Override
    public String toString() {
        return where.toString();
    }

    protected class ExprImpl implements Expr {
        private final StringBuilder buf = new StringBuilder();

        public Expr append(String s) {
            buf.append(s);
            return this;
        }

        public Expr append(char c) {
            buf.append(c);
            return this;
        }

        public Expr arg(Object arg) {
            args.add(arg);
            return this;
        }
    }
}
