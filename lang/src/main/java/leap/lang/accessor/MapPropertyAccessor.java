/*
 * Copyright 2013 the original author or authors.
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
package leap.lang.accessor;

import java.util.HashMap;
import java.util.Map;

import leap.lang.Args;

public class MapPropertyAccessor implements PropertyAccessor {
	
	protected final Map<String,String> properties;
	
	public MapPropertyAccessor(){
		this.properties = new HashMap<String, String>();
	}
	
	public MapPropertyAccessor(Map<String,String> map){
		Args.notNull(map,"the map");
		this.properties = map;
	}

	@Override
    public String getProperty(String name) {
	    return properties.get(name);
    }

	@Override
    public boolean hasProperty(String name) {
	    return properties.containsKey(name);
    }

	@Override
    public void setProperty(String name, String value) {
	    properties.put(name, value);
    }

	@Override
    public String removeProperty(String name) {
	    return properties.remove(name);
    }
}