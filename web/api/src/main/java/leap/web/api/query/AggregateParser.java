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
import java.util.List;

public class AggregateParser {

    public static Aggregate[] parse(String expr) {
        List<Aggregate> aggregates = new ArrayList<>();

        if(!Strings.isEmpty(expr)) {
            String[] parts = Strings.split(expr, ',');

            for(String part : parts) {
                int index0 = part.indexOf('(');
                if(index0 <= 0) {
                    invalidExpr(expr);
                }

                int index1 = part.indexOf(')', index0);
                if(index1 <= index0) {
                    invalidExpr(expr);
                }

                String func  = part.substring(0, index0);
                String field = part.substring(index0+1, index1).trim();

                if(Strings.isEmpty(field)) {
                    invalidExpr(expr);
                }

                String rest  = index1 == part.length() - 1 ? "" : part.substring(index1 + 1).trim();
                String alias = Strings.isEmpty(rest) ? Strings.lowerCamel(field, func) : Strings.removeStartIgnoreCase(rest, "as ").trim();

                if(!isIdentifier(alias)) {
                    throw new BadRequestException("Invalid alias '" + alias + "', must be identifier");
                }

                aggregates.add(new Aggregate(field, func, alias));
            }
        }

        return aggregates.toArray(new Aggregate[aggregates.size()]);
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