/*
 * Copyright 2012 the original author or authors.
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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import leap.lang.Strings;
import leap.lang.collection.SimpleCaseInsensitiveMap;
import leap.lang.enums.CaseType;

public class Header implements Serializable {

    private static final long serialVersionUID = -3825530827173249033L;
    
    private static final HeaderElement[] EMPTY_ELEMENTS = new HeaderElement[]{};
    
	private final String name;
    private final String value;
    
    private HeaderElement[] elements;
    
    public Header(String name,String value){
    	this.name  = name;
    	this.value = value;
    }

	public String getName() {
    	return name;
    }

	public String getValue() {
    	return value;
    }
	
	public HeaderElement[] getElements(){
		if(null == elements){
			if(Strings.isEmpty(value)){
				elements = EMPTY_ELEMENTS;
			}else{
				elements = HeaderParser.parseElements(value).toArray(new HeaderElement[]{});
			}
		}
		return elements;
	}

	public static final class HeaderElement {
	    private final String 			  name;
	    private final String 			  value;
	    private final Map<String, String> parameters;
	    
	    HeaderElement(String name,String value,Map<String, String> parameters){
	    	this.name       = name;
	    	this.value      = value;
	    	this.parameters = createParametersMap(parameters);
	    }

		public String getName() {
        	return name;
        }

		public String getValue() {
        	return value;
        }
		
		public String getParameter(String name){
			return parameters.get(name);
		}

		public Map<String, String> getParameters(){
			return parameters;
		}
		
		public static Map<String, String> createParametersMap(Map<String, String> m) {
			Map<String, String> map = new SimpleCaseInsensitiveMap<String>(new LinkedHashMap<String, String>(),CaseType.LOWER);
			if(null != m){
				map.putAll(m);
			}
			return Collections.unmodifiableMap(map);
		}
	}
}
