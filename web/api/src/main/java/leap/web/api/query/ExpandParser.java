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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

                    int index = exprEndIndex(expr, i);

                    if(index < 0) {
                        throw new BadRequestException("Invalid expand : " + expr);
                    }

                    String name = expr.substring(start, i).trim();
                    String expandExpr = expr.substring(i + 1, index);

                    list.add(applyExpr(name, expandExpr));

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

    protected static int exprEndIndex(String expr, int beginIndex) {
        int k = 0;
        int i = beginIndex;

        char[] chars = expr.toCharArray();
        for (; i < chars.length; i++) {
            char c = chars[i];

            switch (c) {
                case '(':
                    k++;
                    break;
                case ')':
                    k--;
                default:
                    break;
            }

            if (k == 0) {
                break;
            }
        }

        if (k != 0) {
            throw new IllegalStateException("Invalid expand expression: " + expr);
        }

        return i;
    }

    protected static Expand applyExpr(String name, String expandExpr) {
        int index = expandExpr.indexOf(":");
        if (index > 0) {
            Map<String, String> keys = new HashMap<>();

            applyParameter(keys, expandExpr, 0);

            return new Expand(name, keys.get(SelectParser.SELETE), keys.get(FiltersParser.FILTERS), keys.get(OrderByParser.ORDER_BY));
        } else if (index == 0) {
            throw new IllegalStateException("Invalid expand expression: " + expandExpr);
        } else {
            return new Expand(name, expandExpr, null, null);
        }
    }

    protected static void applyParameter(Map<String, String> keys, String expandExpr, int i) {
        int start = expandExpr.indexOf(":", i);

        String key = expandExpr.substring(i, start);
        if (!key.equals(SelectParser.SELETE) && !key.equals(FiltersParser.FILTERS) && !key.equals(OrderByParser.ORDER_BY)) {
            throw new IllegalStateException("Invalid expand query parameter: " + key);
        }

        int end = expandExpr.indexOf(":", start + 1);
        if (end < 0) {
            keys.put(key, expandExpr.substring(start + 1));
        } else {
            String expr = expandExpr.substring(start + 1, end);

            int m = expr.lastIndexOf(",");
            if (m <= 0) {
                throw new IllegalStateException("Invalid expand expression: " + expandExpr);
            }

            String mExpr = expr.substring(0, m);
            keys.put(key, mExpr);

            applyParameter(keys, expandExpr, start + m + 2);
        }
    }

}
