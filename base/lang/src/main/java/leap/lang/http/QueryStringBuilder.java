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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import leap.lang.Buildable;
import leap.lang.Charsets;
import leap.lang.Emptiable;
import leap.lang.Strings;
import leap.lang.net.Urls;
import leap.lang.value.ImmutableEntry;

public class QueryStringBuilder implements Buildable<String>, Emptiable {

    private final String                      charset;
    private final List<Entry<String, String>> params = new ArrayList<>(5);
    
    public QueryStringBuilder() {
        this(null);
    }
    
    public QueryStringBuilder(String charset) {
        this.charset = null == charset ? Charsets.UTF_8_NAME : charset; 
    }

    public QueryStringBuilder add(String name, String v) {
        return add(name, v, true);
    }

    public QueryStringBuilder add(String name, String v, boolean urlEncode) {
        params.add(new ImmutableEntry<String,String>(name, urlEncode ? Urls.encode(v, charset) : v ));
        return this;
    }

    public QueryStringBuilder addIfNotEmpty(String name, String v) {
        if(!Strings.isEmpty(v)) {
            add(name, v);
        }
        return this;
    }
    
    @Override
    public boolean isEmpty() {
        return params.isEmpty();
    }

    @Override
    public String build() {
        StringBuilder qs = new StringBuilder();
        
        for(int i=0;i<params.size();i++) {
            Entry<String, String> p = params.get(i);
            
            if(i > 0) {
                qs.append('&');
            }
            
            String name  = p.getKey();
            String value = p.getValue();
            
            qs.append(name).append('=').append(null == value ? "" : value);
        }
        
        return qs.toString();
    }

    @Override
    public String toString() {
        return build();
    }

}