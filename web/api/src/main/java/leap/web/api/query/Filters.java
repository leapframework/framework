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

import leap.lang.Strings;

import java.util.LinkedHashMap;
import java.util.Map;

//todo :
public class Filters {


    public static Map<String,String> parse(String expr) {

        String[] fields = Strings.split(expr, ',');

        Map<String, String> map = new LinkedHashMap<>(fields.length);

        for(String field : fields) {
            String[] kv = Strings.split(field, ':');

            map.put(kv[0], kv[1]);
        }

        return map;
    }

}
