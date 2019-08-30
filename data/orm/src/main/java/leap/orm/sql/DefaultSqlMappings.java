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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultSqlMappings implements SqlMappings {

    private final Map<String, Map<String, String>> functions = new HashMap<>();

    @Override
    public Map<String, Map<String, String>> getFunctionsMap() {
        return Collections.unmodifiableMap(functions);
    }

    @Override
    public void addFunction(String name, String dbType, String mappingTo) {
        Map<String, String> func = functions.get(name);
        if (null == func) {
            func = new HashMap<>();
            functions.put(name, func);
        }
        func.put(dbType.toLowerCase(), mappingTo);
    }

    @Override
    public void addFunctions(Map<String, Map<String, String>> map) {
        if (null == map) {
            return;
        }
        map.forEach((name, mappings) -> {
            mappings.forEach((dbType, mappingTo) -> {
                addFunction(name, dbType, mappingTo);
            });
        });
    }
}
