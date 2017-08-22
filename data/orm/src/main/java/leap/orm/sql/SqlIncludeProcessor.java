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

package leap.orm.sql;

import leap.lang.Chars;
import leap.lang.Strings;
import leap.orm.metadata.MetadataContext;

final class SqlIncludeProcessor {

    static final String INCLUDE    = "include";
    static final String AT_INCLUDE = "@" + INCLUDE;

    private final MetadataContext context;
    private final SqlCommand      command;
    private final char[] chars;

    private int pos;

    public SqlIncludeProcessor(MetadataContext context, SqlCommand command, String content) {
        this.context = context;
        this.command = command;
        this.chars   = content.toCharArray();
    }

    public String process() {
        //@include(key;required=true|false)
        StringBuilder sb = new StringBuilder(chars.length);

        for(pos=0;pos<chars.length;pos++) {

            char c = chars[pos];

            if(c == '@') {
                int mark = pos;
                if(nextInclude()) {
                    String content = scanIncludeContent();
                    if(null != content) {
                        SqlFragment fragment = context.getMetadata().tryGetSqlFragment(content);
                        if(null == fragment) {
                            throw new SqlConfigException("The included sql fragment '" + content + "' not found in sql '" + command + "', check " + command.getSource());
                        }
                        String fragmentContent = fragment.getContent();
                        if(!Strings.containsIgnoreCase(fragmentContent, SqlIncludeProcessor.AT_INCLUDE)) {
                            sb.append(fragmentContent);
                        }else{
                            sb.append(new SqlIncludeProcessor(context,command,fragmentContent).process());
                        }
                        continue;
                    }
                }

                sb.append(Chars.substring(chars, mark, pos));
            }

            sb.append(c);
        }

        return sb.toString();
    }

    protected boolean nextInclude() {
        int start = pos + 1;
        if(start == chars.length) {
            return false;
        }

        for(pos = start; pos < chars.length; pos++) {
            char c = chars[pos];

            if(!Character.isLetter(c)) {
                if(pos > start) {
                    String word = Chars.substring(chars, start, pos);
                    if(Strings.equalsIgnoreCase(INCLUDE, word)) {
                        return true;
                    }
                }

                break;
            }
        }

        return false;
    }

    protected String scanIncludeContent() {
        if(pos == chars.length) {
            return null;
        }

        int left = 0;
        for(; pos < chars.length; pos++) {

            char c = chars[pos];

            if(left > 0) {

                if(c == ')') {
                    return Chars.substring(chars, left + 1, pos);
                }

            }else{
                if(Character.isWhitespace(c)) {
                    continue;
                }

                if(c == '(') {
                    left = pos;
                }
            }
        }

        return null;
    }

}
