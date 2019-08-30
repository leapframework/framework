/*
 *  Copyright 2019 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package leap.orm.sql;

import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.orm.metadata.MetadataContext;

import java.util.Map;

public class SqlFunctionMappingInterceptor implements SqlInterceptor {

    private static final Log log = LogFactory.get(SqlFunctionMappingInterceptor.class);

    @Override
    public String preParsingSql(MetadataContext context, String sql) {
        Map<String, Map<String, String>> functions = context.getSqlMappings().getFunctionsMap();
        if (functions.isEmpty()) {
            return null;
        }

        final String type = context.getDb().getType().toLowerCase();

        for (Map.Entry<String, Map<String, String>> entry : functions.entrySet()) {
            final String              name     = entry.getKey();
            final Map<String, String> mappings = entry.getValue();

            final String mappingTo = mappings.get(type);
            if (null == mappingTo) {
                continue;
            }
            sql = replaceMappingFunction(sql, name, mappingTo);
        }

        return sql;
    }

    protected String replaceMappingFunction(String sql, String name, String mappingTo) {
        int index = Strings.indexOfIgnoreCase(sql, name);
        if(index >= 0) {
            do {
                int start = index + name.length();

                boolean match = false;
                for(int i=start;i<sql.length();i++) {
                    char c = sql.charAt(i);
                    if(Character.isWhitespace(c)) {
                        continue;
                    }
                    if(c == '(') {
                        match = true;
                    }
                    break;
                }

                if(match) {
                    sql = sql.substring(0, index) + mappingTo + sql.substring(index+name.length());
                }

                index = Strings.indexOfIgnoreCase(sql, name, index);

            }while (index >=0);
        }

        return sql;
    }
}
