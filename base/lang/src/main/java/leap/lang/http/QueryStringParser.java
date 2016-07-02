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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import leap.lang.net.Urls;

public class QueryStringParser {
    
    public static QueryString parse(String s) {
        if(null == s || s.length() == 0) {
            return QueryString.EMPTY;
        }
            
        QueryStringMap params = new QueryStringMap();
        
        parse(s, params);
        
        return params;
    }
    
    public static Map<String, List<String>> parseMap(String s) {
        if(null == s || s.length() == 0) {
            return Collections.emptyMap();
        }
            
        Map<String, List<String>> params = new LinkedHashMap<>();
        
        parse(s, params);
        
        return params;
    }
    
    //Taken from io.netty.handler.codec.http.QueryStringDecoder
    private static void parse(String s, Map<String, List<String>> params) {
        String name = null;
        int pos = 0; // Beginning of the unprocessed region
        int i;       // End of the unprocessed region
        char c;  // Current character
        for (i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            if (c == '=' && name == null) {
                if (pos != i) {
                    name = Urls.decode(s.substring(pos, i));
                }
                pos = i + 1;
            // http://www.w3.org/TR/html401/appendix/notes.html#h-B.2.2
            } else if (c == '&' || c == ';') {
                if (name == null && pos != i) {
                    // We haven't seen an `=' so far but moved forward.
                    // Must be a param of the form '&a&' so add it with
                    // an empty value.
                    if (!addParam(params, Urls.decode(s.substring(pos, i)), "")) {
                        return;
                    }
                } else if (name != null) {
                    if (!addParam(params, name, Urls.decode(s.substring(pos, i)))) {
                        return;
                    }
                    name = null;
                }
                pos = i + 1;
            }
        }

        if (pos != i) {  // Are there characters we haven't dealt with?
            if (name == null) {     // Yes and we haven't seen any `='.
                addParam(params, Urls.decode(s.substring(pos, i)), "");
            } else {                // Yes and this must be the last value.
                addParam(params, name, Urls.decode(s.substring(pos, i)));
            }
        } else if (name != null) {  // Have we seen a name without value?
            addParam(params, name, "");
        }
    }

    private static boolean addParam(Map<String, List<String>> params, String name, String value) {
        List<String> values = params.get(name);
        if (values == null) {
            values = new ArrayList<String>(1);  // Often there's only 1 value.
            params.put(name, values);
        }
        values.add(value);
        return true;
    }
    
    protected QueryStringParser(){
        
    }
}
