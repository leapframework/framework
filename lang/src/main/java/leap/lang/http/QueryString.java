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

import java.util.Collections;
import java.util.Map;

import leap.lang.Emptiable;

public interface QueryString extends Emptiable {
    
    QueryString EMPTY = new QueryString() {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public String[] getParameterValues(String name) {
            return null;
        }
        
        @Override
        public String getParameter(String name) {
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Map<String, Object> getParameters() {
            return Collections.EMPTY_MAP;
        }
    };
    
    String getParameter(String name);
    
    String[] getParameterValues(String name);

    Map<String, Object> getParameters();
}