/*
 * Copyright 2014 the original author or authors.
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
package leap.lang.yaml;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import leap.lang.Strings;
import leap.lang.convert.Converts;

@SuppressWarnings({"rawtypes","unchecked"})
public class YamlObject implements YamlValue {

	private final Map map;

	public YamlObject(Map map){
		this.map = map;
	}
	
	@Override
    public Object raw() {
	    return map;
    }

    @Override
    public Map<String, Object> asMap() {
	    return map;
    }
    
	@Override
    public YamlObject asYamlObject() {
		return this;
	}

	public Set<String> keys() {
		return map.keySet();
	}
	
	public Collection<Object> values(){
		return map.values();
	}
	
	public boolean hasProperties() {
		return map.isEmpty();
	}
	
	public boolean hasProperty(String key){
		return map.containsKey(key);
	}
	
	public <T> T get(String key){
		return (T)map.get(key);
	}
	
	public <T> T get(String key,Class<T> type){
		return Converts.convert(map.get(key), type);
	}
	
	public YamlValue getValue(String key){
		Object o = get(key);
		return null == o ? null : YamlValue.of(o);
	}
	
	public YamlCollection getArray(String key){
		List list = get(key);
		return null == list ? null : new YamlCollection(list);
	}
	
	public YamlObject getObject(String key){
		Map map = get(key);
		return null == map ? null : new YamlObject(map);
	}
	
	public String getString(String key){
		return (String)map.get(key);
	}
	
	public String getString(String key,String defaultValue){
		String s = (String)map.get(key);
		return Strings.isEmpty(s) ? defaultValue : s;
	}
	
	public Integer getInteger(String key){
		return get(key); 
	}
	
	public Integer getInteger(String key,Integer defaultValue){
		Integer i = get(key);
		return null == i ? defaultValue : i;
	}
	
    public Long getLong(String key){
    	return get(key);
    }
    
    public Long getLong(String key,Long defaultValue){
    	Long l = get(key);
    	return null == l ? defaultValue : l;
    }
    
    public Boolean getBoolean(String key){
    	return get(key);
    }
    
    public Boolean getBoolean(String key,Boolean defaultValue){
    	Boolean b = get(key);
    	return null == b ? defaultValue : b;
    }
    
    public Float getFloat(String key){
    	return get(key);
    }
    
    public Float getFloat(String key,Float defaultValue){
    	Float f = get(key);
    	return null == f ? defaultValue : f;
    }
    
    public Double getDouble(String key){
    	return get(key);
    }
    
    public Double getDouble(String key,Double defaultValue){
    	Double d = get(key);
    	return null == d ? defaultValue : d;
    }

	@Override
    public String toString() {
		return map.toString();
	}
}
