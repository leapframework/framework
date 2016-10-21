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

import java.util.ArrayList;
import java.util.List;

public class OrderByParser extends ParserBase {

    public static OrderBy parse(String expr) {
        return new OrderByParser(expr).orderBy();
    }

    private List<OrderBy.Item> items = new ArrayList<>();

    public OrderByParser(String expr) {
        super(expr);
    }

    public OrderBy orderBy() {

        nextChar();

        OrderBy.Item item = null;

        for(;;) {
            if(eof()) {
                break;
            }

            if(isWhitespace()) {
                nextChar();
                continue;
            }

            if(null == item) {
                String name = scanIdentifier();
                item = new OrderBy.Item(name);
                items.add(item);
                continue;
            }else{
                if(ch == ',') {
                    item = null;
                    nextChar();
                    continue;
                }

                String s = scanWord();
                if(s.equalsIgnoreCase("asc")) {
                    continue;
                }

                if(s.equalsIgnoreCase("desc")) {
                    item.desc();
                    continue;
                }

                error("Expect 'asc' or 'desc' but '" + s + "'");
            }
        }

        return new OrderBy(items.toArray(new OrderBy.Item[items.size()]));
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

        String s = substring(start, pos);

        if(s.isEmpty()) {
            error("Unexpected eof");
        }

        return s;
    }

    private String scanWord() {
        skipWhitespaces();

        int start = pos;

        for(;;) {
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