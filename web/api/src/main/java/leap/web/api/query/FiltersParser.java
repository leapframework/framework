/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.web.api.query;

import leap.lang.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FiltersParser extends ParserBase {

    private static final Map<String, Token> OPS = new HashMap<>();
    private static final Node LPAREN = new Node(Token.LPAREN, "(");
    private static final Node RPAREN = new Node(Token.RPAREN, ")");
    private static final Node AND    = new Node(Token.AND, ",");
    private static final Node EQ     = new Node(Token.EQ, ":");

    private static final void op(Token token) {
        OPS.put(token.name(), token);
    }

    private static final void op(Token token, String s) {
        OPS.put(s, token);
    }

    static {
        op(Token.LIKE);
        op(Token.IS);
        op(Token.NOT);
        op(Token.IN);
        op(Token.EQ);
        op(Token.EQ, "=");
        op(Token.GT);
        op(Token.GT, ">");
        op(Token.LT);
        op(Token.LT, "<");
        op(Token.GE);
        op(Token.GE, ">=");
        op(Token.LE);
        op(Token.LE, "<=");
        op(Token.NE);
        op(Token.NE, "!=");
    }

    public static Filters parse(String expr) {
        return new FiltersParser(expr).filters();
    }

    private final List<Node> nodes = new ArrayList<>();

    public FiltersParser(String expr) {
        super(expr);
    }

    public Filters filters() {

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

                    scanName();
                    scanOperator();
                    scanValue();
                    andOr = true;
            }
        }

        if(parens > 0) {
            error("Unclosed expression, ')' required");
        }

        return new Filters(nodes.toArray(new Node[0]));
    }

    private void scanName() {
        String alias = null;
        String name  = scanIdentifier(true);

        if(ch == '.') {
            nextChar();
            alias = name;
            name  = scanIdentifier(false);
        }

        nodes.add(new Name(alias, name));
    }

    private void scanOperator() {
        skipWhitespaces();
        if(ch == ':') {
            nodes.add(EQ);
            nextChar();
            return;
        }

        String op = scanLetters();

        Token token = OPS.get(op.toUpperCase());
        if(null == token) {
            error("Invalid operator '" + op + "'");
        }
        nodes.add(new Node(token, op));
    }

    private void scanValue() {
        skipWhitespaces();

        int start = pos;
        int end   = 0;
        boolean quoted = false;
        char quotedChar = 0x0000;
        char escapedChar = 0x0000;

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
        if(!Strings.equals(Token.NULL.name(),value.toUpperCase())){
            Token last = nodes.get(nodes.size()-1).token;
            if(last == Token.NOT || last == Token.IS){
                error("Invalid value of operation '" + last + "', it must be null");
            }
            nodes.add(new Node(Token.VALUE, value, quoted));
        }else {
            nodes.add(new Node(Token.NULL, "null"));
        }
    }

    private void scanAndOr() {
        if(ch == ',') {
            nodes.add(AND);
            nextChar();
            return;
        }

        String s = scanLetters();

        if(s.equalsIgnoreCase("and")) {
            nodes.add(new Node(Token.AND, s));
        }else if(s.equalsIgnoreCase("or")) {
            nodes.add(new Node(Token.OR, s));
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

    private String scanLetters() {
        skipWhitespaces();

        int start = pos;

        for(;;) {
            nextChar();

            if(isWhitespace() || eof()) {
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

    public enum Token {
        NAME,
        VALUE,
        NULL,

        LPAREN,
        RPAREN,

        LIKE,
        IN,
        EQ,
        GT,
        LT,
        GE,
        LE,
        NE,

        IS,
        NOT,
        AND,
        OR;
    }

    public static class Node {
        private final Token  token;
        private final String literal;
        private final boolean quoted;

        public Node(Token token, String literal) {
            this(token, literal, false);
        }

        public Node(Token token, String literal, boolean quoted) {
            this.token = token;
            this.literal = literal;
            this.quoted = quoted;
        }

        public Token token() {
            return token;
        }

        public String literal() {
            return literal;
        }

        public boolean isQuoted() {
            return quoted;
        }

        public boolean isParen() {
            return token == Token.LPAREN || token == Token.RPAREN;
        }

        public boolean isAnd() {
            return token == Token.AND;
        }

        public boolean isOr() {
            return token == Token.OR;
        }
    }

    public static class Name extends Node {

        private final String alias;

        public Name(String alias, String name) {
            super(Token.NAME, name);
            this.alias = alias;
        }

        public String alias() {
            return alias;
        }

        @Override
        public String toString() {
            return null == alias ? literal() : alias + "." + literal();
        }
    }

}
