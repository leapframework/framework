/*
 * Copyright 2023 the original author or authors.
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
package leap.orm.sql.validation;

public class SimpleSqlOrderByValidator extends AbstractSqlValidator {

    public SimpleSqlOrderByValidator(String expr) {
        super(expr);
    }

    public void validate() {
        nextChar();
        for (;;) {
            if (eof()) {
                break;
            }

            if (isWhitespace()) {
                nextChar();
                continue;
            }

            if (ch == '(' || ch == ')') {
                error("Illegal identifier char '" + ch + "'");
            }

            if (!scanName()) {
                String s = scanWord();
                if (!s.equalsIgnoreCase("asc") && !s.equalsIgnoreCase("desc")) {
                    error("Expect 'asc' or 'desc' but '" + s + "'");
                }
                skipWhitespaces();
                if (eof()) {
                    break;
                }
                if(ch != ',') {
                    error("Expect ',' but '" + ch + "'");
                }
                nextChar();
            } else if (ch == ',') {
                nextChar();
            }
        }
    }

}
