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

import leap.lang.Strings;
import leap.lang.text.AbstractStringParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SCEL means Simple condition expression language.
 */
public class ScelParser extends AbstractStringParser {

    private static final Map<String, ScelToken> OPS    = new HashMap<>();
    private static final ScelNode               LPAREN = new ScelNode(ScelToken.LPAREN, "(");
    private static final ScelNode               RPAREN = new ScelNode(ScelToken.RPAREN, ")");
    private static final ScelNode               AND    = new ScelNode(ScelToken.AND, ",");
    private static final ScelNode               EQ     = new ScelNode(ScelToken.EQ, ":");

    private static final void op(ScelToken token) {
        OPS.put(token.name(), token);
    }

    private static final void op(ScelToken token, String s) {
        OPS.put(s, token);
    }

    private boolean allowSingleExpr = false;

    static {
        op(ScelToken.LIKE);
        op(ScelToken.IS);
        op(ScelToken.NOT);
        op(ScelToken.IN);
        op(ScelToken.EQ);
        op(ScelToken.EQ, "=");
        op(ScelToken.GT);
        op(ScelToken.GT, ">");
        op(ScelToken.LT);
        op(ScelToken.LT, "<");
        op(ScelToken.GE);
        op(ScelToken.GE, ">=");
        op(ScelToken.LE);
        op(ScelToken.LE, "<=");
        op(ScelToken.NE);
        op(ScelToken.CO);
        op(ScelToken.SW);
        op(ScelToken.IN);
        op(ScelToken.EW);
        op(ScelToken.PR);
    }

    public static ScelExpr parse(String expr) {
        return new ScelParser(expr).expr();
    }

    private final List<ScelNode> nodes = new ArrayList<>();

    public ScelParser(String expr) {
        super(expr);
    }

    public ScelExpr expr() {

        int     parens = 0;
        boolean andOr  = false;

        nextChar();

        for (; ; ) {
            if (eof()) {
                break;
            }

            if (isWhitespace()) {
                nextChar();
                continue;
            }

            switch (ch) {

                case '(':
                    parens++;
                    nodes.add(LPAREN);
                    nextChar();
                    break;

                case ')':
                    if (parens == 0) {
                        error("Illegal char ')'");
                    }

                    parens--;
                    nodes.add(RPAREN);
                    nextChar();
                    break;

                default:
                    if (andOr) {
                        scanAndOr();
                        andOr = false;
                        break;
                    }

                    if (scanName()) {
                        Boolean scan = scanOperator();
                        if (null == scan) {
                            andOr = false;
                        } else {
                            if (scan) {
                                scanValue();
                            }
                            andOr = true;
                        }
                    }
            }
        }

        if (parens > 0) {
            error("Unclosed expression, ')' required");
        }

        return new ScelExpr(nodes.toArray(new ScelNode[0]));
    }

    private boolean scanName() {
        String alias = null;
        String name  = scanIdentifier(true);

        if (name.equalsIgnoreCase("not")) {
            nodes.add(new ScelNode(ScelToken.NOT, name));
            return false;
        }

        if (ch == '.') {
            nextChar();
            alias = name;
            name = scanIdentifier(false);
        }

        nodes.add(new ScelName(alias, name));
        return true;
    }

    private Boolean scanOperator() {
        skipWhitespaces();

        if (allowSingleExpr && eof()) {
            return null;
        } else {
            if (eof()) {
                error("Expected operator, but eof");
            }
        }


        if (ch == ':') {
            nodes.add(EQ);
            nextChar();
            return true;
        }

        String op = nextLiteral();

        if (!preProcessOp(op)) {
            return null;
        }

        ScelToken token = OPS.get(op.toUpperCase());
        if (null == token) {
            error("Invalid operator '" + op + "'");
        }

        return processOperator(token, op);
    }

    protected boolean preProcessOp(String op) {
        if (allowSingleExpr) {
            if (op.equalsIgnoreCase("and")) {
                nodes.add(new ScelNode(ScelToken.AND, op));
                return false;
            } else if (op.equalsIgnoreCase("or")) {
                nodes.add(new ScelNode(ScelToken.OR, op));
                return false;
            }
        }
        return true;
    }

    private boolean processOperator(ScelToken token, String op) {
        if (token == ScelToken.NOT) {
            String s = nextLiteral();
            if (OPS.get(s.toUpperCase()) == ScelToken.IN) {
                nodes.add(new ScelNode(ScelToken.NOT_IN, op + " " + s));
                scanInValue();
            } else {
                nodes.add(new ScelNode(ScelToken.IS_NOT, op));
                if (!s.equalsIgnoreCase("null")) {
                    error("Expected 'null' but '" + s + "'");
                }
                nodes.add(new ScelNode(ScelToken.NULL, s));
            }
            return false;
        } else if (token == ScelToken.IS) {
            String s = nextLiteral();
            if (s.equalsIgnoreCase("not")) {
                nodes.add(new ScelNode(ScelToken.IS_NOT, op + " " + s));
                s = nextLiteral();
                if (!s.equalsIgnoreCase("null")) {
                    error("Expected 'null' but '" + s + "'");
                }
                nodes.add(new ScelNode(ScelToken.NULL, s));
                return false;
            } else {
                nodes.add(new ScelNode(token, op));
                if (s.equalsIgnoreCase("null")) {
                    nodes.add(new ScelNode(ScelToken.NULL, s));
                    return false;
                }
                error("Unexpected literal '" + s + "' after IS");
            }
        } else if (token == ScelToken.IN) {
            nodes.add(new ScelNode(token, op));
            scanInValue();
            return false;
        } else {
            nodes.add(new ScelNode(token, op));
        }

        if (token == ScelToken.PR) {
            return false;
        }

        return true;
    }

    private String scanInValues(List<ScelNode> values) {
        int start = pos;

        skipWhitespaces();

        if (ch == '(') {
            nextChar();
            scanInValues(values, true);
            return substring(start, pos);
        } else {
            scanInValues(values, false);
            return substring(start, pos);
        }
    }

    private void scanInValue() {
        List<ScelNode> values  = new ArrayList<>();
        String         literal = scanInValues(values).trim();
        nodes.add(new ScelNode(ScelToken.VALUE, literal, values));
    }

    private void scanInValues(List<ScelNode> values, boolean close) {
        Boolean comma = null;

        for (; ; ) {
            skipWhitespaces();

            if (ch == ',') {
                nextChar();
                comma = true;
                continue;
            }

            if(ch == ')') {
                if(close) {
                    nextChar();
                }
                break;
            }

            if (values.size() > 0 && (eof() || isWhitespace())) {
                break;
            }

            if (null == comma || comma) {
                values.add(scanInValueNode(close));
            } else {
                break;
            }

            comma = false;
        }
    }

    private ScelNode scanInValueNode(boolean close) {
        StringBuilder s = new StringBuilder();

        boolean quoted     = false;
        char    quotedChar = 0x0000;

        if (ch == '\'' || ch == '\"') {
            quoted = true;
            quotedChar = ch;
        }else {
            s.append(ch);
        }

        for (;;) {
            nextChar();

            if(ch == '\\') {
                nextChar();
                s.append(ch);
                continue;
            }

            if(!quoted) {
                if (ch == ',') {
                    break;
                }

                if (ch == ')') {
                    break;
                }

                if(eof()) {
                    break;
                }

                if (Character.isWhitespace(ch)) {
                    break;
                }
            }else {
                if(eof()) {
                    error("Unclosed in value");
                }
            }

            if(ch == quotedChar) {
                nextChar();
                break;
            }

            s.append(ch);
        }

        String value = s.toString().trim();
        if (!Strings.equalsIgnoreCase(ScelToken.NULL.name(), value)) {
            return new ScelNode(ScelToken.VALUE, value, quoted);
        } else {
            return new ScelNode(ScelToken.NULL, "null");
        }
    }

    private ScelNode scanValueOnly() {
        skipWhitespaces();

        int start = pos;
        int end   = 0;

        boolean quoted     = false;
        char    quotedChar = 0x0000;

        if (ch == '\'' || ch == '\"') {
            start = pos + 1;
            quoted = true;
            quotedChar = ch;
        }

        for (; ; ) {
            nextChar();

            //handles ' character
            if (ch == quotedChar) {
                //escaped ''
                if (charAt(pos + 1) == quotedChar) {
                    nextChar();
                    continue;
                }

                //end string value
                if (quoted) {
                    end = pos;
                    nextChar();
                    break;
                }

                //invalid ' character
                error("Invalid character [" + quoted + "], should use [" + quotedChar + quotedChar + "] instead");
            }

            if (ch == '(' && charAt(pos + 1) == ')') {
                nextChar();
                nextChar();
                end = pos;
                break;
            }

            if (quoted) {
                if (eof()) {
                    error("Unclosed string value");
                }
            } else if (isWhitespace() || eof() || ch == '(' || ch == ')') {
                end = pos;
                break;
            }
        }

        String value = Strings.replace(substring(start, end), "''", "'");
        if (!Strings.equals(ScelToken.NULL.name(), value.toUpperCase())) {
            return new ScelNode(ScelToken.VALUE, value, quoted);
        } else {
            return new ScelNode(ScelToken.NULL, "null");
        }
    }

    private void scanValue() {
        ScelNode node = scanValueOnly();
        if (!node.isNull()) {
            ScelToken last = nodes.get(nodes.size() - 1).token;
            if (last == ScelToken.IS_NOT || last == ScelToken.IS) {
                error("Invalid value of operation '" + last + "', it must be null");
            }
        }
        nodes.add(node);
    }

    private void scanAndOr() {
        if (ch == ',') {
            nodes.add(AND);
            nextChar();
            return;
        }

        String s = nextLiteral();

        if (s.equalsIgnoreCase("and")) {
            nodes.add(new ScelNode(ScelToken.AND, s));
        } else if (s.equalsIgnoreCase("or")) {
            nodes.add(new ScelNode(ScelToken.OR, s));
        } else {
            error("Expect 'AND' or 'OR' operator but was '" + s + "'");
        }
    }

    private String scanIdentifier(boolean dot) {
        skipWhitespaces();

        int start = pos;

        for (; ; ) {
            nextChar();

            if (dot && ch == '.') {
                break;
            }

            if (ch == ':' || isWhitespace() || eof()) {
                break;
            }

            if (!isIdentifierChar(ch)) {
                error("Illegal identifier char '" + ch + "'");
            }
        }

        String s = substring(start, pos);

        if (s.isEmpty()) {
            error("Unexpected eof");
        }

        return s;
    }

    private String nextLiteral() {
        skipWhitespaces();

        int start = pos;

        for (; ; ) {
            nextChar();

            if (isWhitespace() || ch == ')' || eof()) {
                break;
            }

            //            if(!isIdentifierChar(ch)) {
            //                error("Illegal identifier char '" + ch + "'");
            //            }
        }

        String s = substring(start, pos);

        if (s.isEmpty()) {
            error("Unexpected eof");
        }

        return s;
    }

    public void setAllowSingleExpr(boolean allowSingleExpr) {
        this.allowSingleExpr = allowSingleExpr;
    }
}
