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

package leap.web.api.query;

import leap.lang.Strings;
import leap.web.exception.BadRequestException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AggregateParser {

    private static final Set<String> funcs = new HashSet<>();
    static {
        funcs.add("sum");
        funcs.add("avg");
        funcs.add("min");
        funcs.add("max");
        funcs.add("count");
    }

    public static Aggregate parse(String expr, Boolean allowSingleExpr) {
        return processParse(expr, allowSingleExpr);
    }

    public static Aggregate parse(String expr) {
        return processParse(expr, false);
    }

    private static Aggregate processParse(String expr, Boolean allowSingleExpr) {
        List<Aggregate.Item> items = new ArrayList<>();
        Aggregate.Item item;

        if(!Strings.isEmpty(expr)) {
            String[] parts = Strings.split(expr, ',');

            for(String part : parts) {
                int index0 = part.indexOf('(');

                if (!allowSingleExpr && index0 <= 0) {
                    invalidExpr(expr);
                } else if (allowSingleExpr && index0 <= 0) {
                    if (part.indexOf(" as ") > 0) {
                        String[] partItems = Strings.split(part.trim(), " as ");
                        item = new Aggregate.Item(partItems[0], null, partItems[1]);
                    } else if (part.indexOf(' ') > 0) {
                        String[] partItems = Strings.split(part.trim(), ' ');
                        item = new Aggregate.Item(partItems[0], null, partItems[1]);
                    } else {
                        item = new Aggregate.Item(part, null, null);
                    }
                    items.add(item);
                    continue;
                }

                int index1 = part.indexOf(')', index0);
                if(index1 <= index0) {
                    invalidExpr(expr);
                }

                String func  = part.substring(0, index0);
                if(!funcs.contains(func.toLowerCase())) {
                    throw new BadRequestException("Unsupported aggregation function '" + func + "'");
                }

                String name = part.substring(index0+1, index1).trim();
                if(Strings.isEmpty(name)) {
                    invalidExpr(expr);
                }

                if(func.equalsIgnoreCase("count") && !"*".equals(name)) {
                    throw new BadRequestException("Expected 'count(*)', actual '" + func + "(" + name + ")'");
                }

                String rest  = index1 == part.length() - 1 ? "" : part.substring(index1 + 1).trim();
                String alias = alias(rest, name, func);

                item = new Aggregate.Item(name, func, alias);
                items.add(item);
            }
        }

        return new Aggregate(items.toArray(new Aggregate.Item[items.size()]));
    }

    private static String alias(String rest, String field, String func) {
        String alias;
        if(Strings.isEmpty(rest)) {
            if("count".equalsIgnoreCase(func)) {
                return "total";
            }else {
                alias = Strings.lowerCamel(field, func);
            }
        }else {
            alias = Strings.removeStartIgnoreCase(rest, "as ").trim();
        }

        if(!isIdentifier(alias)) {
            throw new BadRequestException("Invalid alias '" + alias + "', must be identifier");
        }
        return alias;

    }

    private static void invalidExpr(String expr) {
        throw new BadRequestException("Invalid aggregates expr '" + expr + "'");
    }

    private final static boolean[] identifierFlags = new boolean[256];
    static {
        for (char c = 0; c < identifierFlags.length; ++c) {
            if (c >= 'A' && c <= 'Z') {
                identifierFlags[c] = true;
            } else if (c >= 'a' && c <= 'z') {
                identifierFlags[c] = true;
            } else if (c >= '0' && c <= '9') {
                identifierFlags[c] = true;
            }
        }
        identifierFlags['_'] = true;
        identifierFlags['$'] = true;
    }

    private static boolean isIdentifier(String s) {
        for(int i=0;i<s.length();i++) {
            if(!isIdentifierChar(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isIdentifierChar(char c) {
        return c > identifierFlags.length || identifierFlags[c];
    }
}