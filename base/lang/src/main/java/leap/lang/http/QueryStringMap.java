/*
 * Copyright 2015 the original author or authors.
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
package leap.lang.http;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class QueryStringMap extends LinkedHashMap<String, List<String>> implements QueryString {

    private static final long serialVersionUID = 2401756504509556691L;
    
    private Map<String, Object> map;
    
    public QueryStringMap() {
        super();
    }

    public QueryStringMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
    }

    public QueryStringMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public QueryStringMap(int initialCapacity) {
        super(initialCapacity);
    }

    public QueryStringMap(Map<? extends String, ? extends List<String>> m) {
        super(m);
    }

    @Override
    public String getParameter(String name) {
        List<String> values = get(name);
        if(null != values && values.size() > 0){
            return values.get(0);
        }
        return null;
    }

    @Override
    public String[] getParameterValues(String name) {
        List<String> values = get(name);
        if(null != values){
            return values.toArray(new String[values.size()]);
        }
        return null;
    }

    @Override
    public Map<String, Object> getParameters() {
        if(null == map) {
            map = new LinkedHashMap<String, Object>(size());
            for(Entry<String, List<String>> entry : entrySet()) {
                List<String> values = entry.getValue();
                if(values.size() == 1){
                    map.put(entry.getKey(), values.get(0));
                }else{
                    map.put(entry.getKey(), values.toArray(new String[values.size()]));
                }
            }
        }
        return map;
    }
}
