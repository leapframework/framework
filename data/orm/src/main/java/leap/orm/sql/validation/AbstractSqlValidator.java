/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.orm.sql.validation;

import leap.lang.text.AbstractStringParser;

abstract class AbstractSqlValidator extends AbstractStringParser {

    public AbstractSqlValidator(String expr) {
        super(expr);
    }

    protected boolean scanName() {
        skipWhitespaces();

        int start = pos;

        boolean end;
        for (;;) {
            nextChar();

            if (ch == ',' || eof()) {
                end = true;
                break;
            }

            if (isWhitespace()) {
                end = false;
                break;
            }

            if(ch != '.' && !isIdentifierChar(ch)) {
                error("Illegal identifier char '" + ch + "'");
            }
        }

        String s = substring(start, pos);

        if(s.isEmpty()) {
            error("Unexpected eof");
        }

        return end;
    }

    protected String scanWord() {
        skipWhitespaces();

        int start = pos;

        for (;;) {
            nextChar();

            if(eof() || !Character.isLetter(ch)) {
                break;
            }
        }

        String s = substring(start, pos);

        if(s.isEmpty()) {
            error("Unexpected eof");
        }

        return s;
    }

}
