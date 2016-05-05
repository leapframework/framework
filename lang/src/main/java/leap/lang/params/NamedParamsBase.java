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
package leap.lang.params;

import java.util.Map;
import java.util.Map.Entry;

import leap.lang.collection.CaseInsensitiveMap;
import leap.lang.collection.WrappedCaseInsensitiveMap;

public abstract class NamedParamsBase implements Params {

	protected final Map<String,Object> map;
	
	protected NamedParamsBase(){
		this(new WrappedCaseInsensitiveMap<Object>());
	}
	
	protected NamedParamsBase(Map<String,Object> map){
		this.map = map instanceof CaseInsensitiveMap ? map : WrappedCaseInsensitiveMap.create(map);
	}
	
	@Override
    public boolean isEmpty() {
	    return map.isEmpty();
    }

	@Override
    public Map<String, Object> map() {
	    return map;
    }

	@Override
    public boolean contains(String name) {
	    return map.containsKey(name);
    }

	@Override
    public Object get(String name) {
	    return map.get(name);
    }

	@Override
    public Params set(String name, Object value) {
		setRawValue(name,value);
		map.put(name, value);
		return this;
    }
	
	@Override
    public Params setAll(Map<String, ? extends Object> m) {
		if(null != m){
			for(Entry<String, ? extends Object> entry : m.entrySet()){
				setRawValue(entry.getKey(), entry.getValue());
			}
			map.putAll(m);
		}
		return this;
	}
	
	@Override
    public boolean isIndexed() {
	    return false;
    }

	@Override
    public boolean isNamed() {
	    return true;
    }
	
	@Override
    public int maxIndex() {
	    return -1;
    }

	@Override
    public Object get(int i) throws IllegalStateException {
		throw new IllegalStateException("Not an indexed parameters");
	}

	protected void setRawValue(String name,Object value){
		
	}
}