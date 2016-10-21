/*
 *
 *  * Copyright 2016 the original author or authors.
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

package leap.web.api.query;

import leap.lang.Strings;
import leap.web.exception.BadRequestException;

import java.util.ArrayList;
import java.util.List;

public class ExpandParser {

    private static final Expand[] EMPTY_ARRAY = new Expand[0];

    public static Expand[] parse(String expr) {

        if(Strings.isEmpty(expr)) {
            return EMPTY_ARRAY;
        }

        List<Expand> list = new ArrayList<>();

        char[] chars = expr.toCharArray();
        int start = 0;

        for(int i=0;i<chars.length;i++) {

            char c = chars[i];

            switch (c) {

                case ',' :
                    list.add(new Expand(expr.substring(start, i).trim()));
                    start = i+1;
                    break;

                case '(' :

                    int index = expr.indexOf(')', i);

                    if(index < 0) {
                        throw new BadRequestException("Invalid expand : " + expr);
                    }

                    String name   = expr.substring(start, i).trim();
                    String select = expr.substring(i + 1, index);
                    list.add(new Expand(name, select));

                    i = index + 1;

                    for(;i<chars.length;i++) {

                        char c1 = chars[i];

                        if(Character.isWhitespace(c1)) {
                            continue;
                        }

                        if(c1 == ',') {
                            i++;
                            break;
                        }

                        throw new BadRequestException("Invalid expand : " + expr);
                    }

                    start = i;

                default :
                    break;

            }
        }

        if(start == 0) {
            list.add(new Expand(expr.trim()));
        }else if(start < chars.length - 1) {
            list.add( new Expand(expr.substring(start).trim()));
        }

        return list.toArray(EMPTY_ARRAY);
    }

}
