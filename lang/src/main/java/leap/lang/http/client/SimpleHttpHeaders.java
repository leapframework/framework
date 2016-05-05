/*
 * Copyright 2016 the original author or authors.
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
package leap.lang.http.client;

import leap.lang.collection.CaseInsensitiveMap;
import leap.lang.collection.WrappedCaseInsensitiveMap;

import java.util.*;
import java.util.function.BiConsumer;

// https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
// field name is case-insensitive
public class SimpleHttpHeaders implements HttpHeaders {

    private static final List<String> EMPTY = Collections.emptyList();

    private final CaseInsensitiveMap<List<String>> map = WrappedCaseInsensitiveMap.create(new LinkedHashMap<>(5));

    @Override
    public boolean exists(String name) {
        return map.containsKey(name);
    }

    @Override
    public List<String> get(String name) {
        List<String> values = map.get(name);
        return null == values ? EMPTY : values;
    }

    @Override
    public void add(String name, String value) {
        mustGet(name).add(value);
    }

    @Override
    public void set(String name, String value) {
        List<String> values = mustGet(name);
        if(!values.isEmpty()) {
            values.clear();
        }
        values.add(value);
    }

    @Override
    public void forEach(BiConsumer<String, String> consumer) {
        if(map.isEmpty()) {
            return;
        }

        for(Map.Entry<String,List<String>> entry : map.entrySet()) {
            String name = entry.getKey();
            for(String value : entry.getValue()) {
                consumer.accept(name, value);
            }
        }
    }

    protected List<String> mustGet(String name) {
        List<String> values = map.get(name);
        if(null == values){
            values = new ArrayList<>(1);
            map.put(name, values);
        }
        return values;
    }
}
