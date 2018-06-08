/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package leap.lang.text.scel;

import leap.lang.el.spel.SPEL;
import leap.lang.expression.Expression;

import java.util.Map;

public class ScelExpr {
    private final ScelNode[] nodes;

    private Expression expression;

    ScelExpr(ScelNode[] nodes) {
        this.nodes = nodes;
    }

    public ScelNode[] nodes() {
        return nodes;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for(int i=0;i<nodes.length;i++) {
            ScelNode node = nodes[i];

            if(i > 0) {
                s.append(' ');
            }

            if(node.isQuoted()) {
                s.append('\'');
            }
            s.append(node.literal());
            if(node.isQuoted()) {
                s.append('\'');
            }
        }

        return s.toString();
    }

    public boolean test(Map o) {
        if(null == expression) {
            expression = toExpression();
        }
        return Boolean.TRUE.equals(expression.getValue(o));
    }

    protected Expression toExpression() {
        StringBuilder s = new StringBuilder();
        for(int i=0;i<nodes.length;i++) {
            ScelNode node = nodes[i];

            ScelToken token = node.token();

            if(token == ScelToken.LPAREN) {
                s.append('(');
                continue;
            }

            if(token == ScelToken.RPAREN) {
                s.append(')');
                continue;
            }

            if(token == ScelToken.AND) {
                s.append("&&");
                continue;
            }

            if(token == ScelToken.OR) {
                s.append("||");
                continue;
            }

            if(token == ScelToken.NOT) {
                s.append("!");
                continue;
            }

            if(token == ScelToken.NAME) {
                s.append(node.toString()).append(' ');
                continue;
            }

            if(token == ScelToken.VALUE) {
                s.append(' ');

                if(node.quoted) {
                    s.append('\'');
                }
                s.append(node.literal());
                if(node.quoted) {
                    s.append('\'');
                }
                continue;
            }

            if(token == ScelToken.EQ) {
                s.append("==");
                continue;
            }

            if(token == ScelToken.GE) {
                s.append(">=");
                continue;
            }

            if(token == ScelToken.LE) {
                s.append("<=");
                continue;
            }

            if(token == ScelToken.LT) {
                s.append("<");
                continue;
            }

            if(token == ScelToken.CO) {
                s.append("contains");
                continue;
            }

            if(token == ScelToken.SW) {
                s.append("startsWith");
                continue;
            }

            if(token == ScelToken.EW) {
                s.append("endsWith");
                continue;
            }

            throw new IllegalStateException("Can't convert token '" + token + "' to el expression");
        }

        return SPEL.createExpression(s.toString());
    }
}
