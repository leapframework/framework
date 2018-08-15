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

        for(;;) {
            if(eof()) {
                break;
            }

            if(isWhitespace()) {
                nextChar();
                continue;
            }

            switch (ch) {

                case '(' :
                    parens++;
                    nodes.add(LPAREN);
                    nextChar();
                    break;

                case ')' :
                    if(parens == 0) {
                        error("Illegal char ')'");
                    }

                    parens--;
                    nodes.add(RPAREN);
                    nextChar();
                    break;

                default:
                    if(andOr) {
                        scanAndOr();
                        andOr = false;
                        break;
                    }

                    if(scanName()) {
                        if (scanOperator()) {
                            scanValue();
                        }
                        andOr = true;
                    }
            }
        }

        if(parens > 0) {
            error("Unclosed expression, ')' required");
        }

        return new ScelExpr(nodes.toArray(new ScelNode[0]));
    }

    private boolean scanName() {
        String alias = null;
        String name  = scanIdentifier(true);

        if(name.equalsIgnoreCase("not")) {
            nodes.add(new ScelNode(ScelToken.NOT, name));
            return false;
        }

        if(ch == '.') {
            nextChar();
            alias = name;
            name  = scanIdentifier(false);
        }

        nodes.add(new ScelName(alias, name));
        return true;
    }

    private boolean scanOperator() {
        skipWhitespaces();

        if(eof()) {
            error("Expected operator, but eof");
        }

        if(ch == ':') {
            nodes.add(EQ);
            nextChar();
            return true;
        }

        String op = nextLiteral();

        ScelToken token = OPS.get(op.toUpperCase());
        if(null == token) {
            error("Invalid operator '" + op + "'");
        }
        nodes.add(new ScelNode(token, op));

        if(token == ScelToken.IS) {
            String s = nextLiteral();
            if(s.equalsIgnoreCase("not")) {
                nodes.add(new ScelNode(ScelToken.NOT, s));

                s = nextLiteral();
                if(!s.equalsIgnoreCase("null")) {
                    error("Expected 'null' but '" + s + "'");
                }
                nodes.add(new ScelNode(ScelToken.NULL, s));
                return false;
            }

            if(s.equalsIgnoreCase("null")) {
                nodes.add(new ScelNode(ScelToken.NULL, s));
                return false;
            }

            error("Unexpected literal '" + s + "' after IS");
        }

        if(token == ScelToken.PR) {
            return false;
        }

        return true;
    }

    private void scanValue() {
        skipWhitespaces();

        int start = pos;
        int end   = 0;

        boolean quoted     = false;
        char    quotedChar = 0x0000;

        if(ch == '\'' || ch == '\"') {
            start  = pos + 1;
            quoted = true;
            quotedChar = ch;
        }

        for(;;) {
            nextChar();

            //handles ' character
            if(ch == quotedChar) {
                //escaped ''
                if(charAt(pos+1) == quotedChar) {
                    nextChar();
                    continue;
                }

                //end string value
                if(quoted){
                    end = pos;
                    nextChar();
                    break;
                }

                //invalid ' character
                error("Invalid character [" + quoted + "], should use [" + quotedChar + quotedChar + "] instead");
            }

            if(ch == '(' && charAt(pos+1) == ')') {
                nextChar();
                nextChar();
                end = pos;
                break;
            }

            if(quoted) {
                if(eof()) {
                    error("Unclosed string value");
                }
            }else if(isWhitespace() || eof() || ch == '(' || ch == ')') {
                end = pos;
                break;
            }
        }

        String value = Strings.replace(substring(start, end), "''", "'");
        if(!Strings.equals(ScelToken.NULL.name(),value.toUpperCase())){
            ScelToken last = nodes.get(nodes.size()-1).token;
            if(last == ScelToken.NOT || last == ScelToken.IS){
                error("Invalid value of operation '" + last + "', it must be null");
            }
            nodes.add(new ScelNode(ScelToken.VALUE, value, quoted));
        }else {
            nodes.add(new ScelNode(ScelToken.NULL, "null"));
        }
    }

    private void scanAndOr() {
        if(ch == ',') {
            nodes.add(AND);
            nextChar();
            return;
        }

        String s = nextLiteral();

        if(s.equalsIgnoreCase("and")) {
            nodes.add(new ScelNode(ScelToken.AND, s));
        }else if(s.equalsIgnoreCase("or")) {
            nodes.add(new ScelNode(ScelToken.OR, s));
        }else{
            error("Expect 'AND' or 'OR' operator but was '" + s + "'");
        }
    }

    private String scanIdentifier(boolean dot) {
        skipWhitespaces();

        int start = pos;

        for(;;) {
            nextChar();

            if(dot && ch == '.') {
                break;
            }

            if(ch == ':' || isWhitespace() || eof()) {
                break;
            }

            if(!isIdentifierChar(ch)) {
                error("Illegal identifier char '" + ch + "'");
            }
        }

        String s = substring(start, pos);

        if(s.isEmpty()) {
            error("Unexpected eof");
        }

        return s;
    }

    private String nextLiteral() {
        skipWhitespaces();

        int start = pos;

        for(;;) {
            nextChar();

            if(isWhitespace() || ch == ')' || eof()) {
                break;
            }

//            if(!isIdentifierChar(ch)) {
//                error("Illegal identifier char '" + ch + "'");
//            }
        }

        String s = substring(start, pos);

        if(s.isEmpty()) {
            error("Unexpected eof");
        }

        return s;
    }

}
