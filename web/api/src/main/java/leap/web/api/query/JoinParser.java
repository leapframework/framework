/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.api.query;

import java.util.ArrayList;
import java.util.List;

/**
 * Join expression : {relation} {alias} [, {relation} {alias} ]...
 *
 * <p/>
 * Example:
 * <pre>
 *     join=User u, Org o
 * </pre>
 */
public class JoinParser extends ParserBase {

    public static Join[] parse(String expr) {
        return new JoinParser(expr).joins();
    }

    private final List<Join> joins = new ArrayList<>();

    public JoinParser(String expr) {
        super(expr);
    }

    public Join[] joins() {
        nextChar();

        for(;;) {
            if(eof()) {
                break;
            }

            if(isWhitespace()) {
                nextChar();
                continue;
            }

            if(ch == ',') {
                if(joins.size() == 0) {
                    error("Unexpected char ','");
                }
                nextChar();
            }

            String relation = scanIdentifier();
            String alias    = scanIdentifier();

            joins.add(new Join(relation, alias));
        }

        return joins.toArray(new Join[joins.size()]);
    }

    private String scanIdentifier() {
        skipWhitespaces();

        int start = pos;

        for(;;) {
            nextChar();

            if(ch == ',' || isWhitespace() || eof()) {
                break;
            }

            if(!isIdentifierChar(ch)) {
                error("Illegal identifier char '" + ch + "'");
            }
        }

        String s = "";

        if(pos <= chars.length() && pos > start) {
            s = substring(start, pos);
        }

        if(s.isEmpty()) {
            error("Unexpected eof");
        }

        return s;
    }

}
