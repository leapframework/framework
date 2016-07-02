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
package leap.lang.json;

import leap.lang.Strings;
import leap.lang.convert.Converts;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings({"rawtypes","unchecked"})
public class JsonObject implements JsonValue {

	private final Map<String,Object> map;

	public JsonObject(Map map){
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
    public JsonObject asJsonObject() {
		return this;
	}

    /**
     * Returns the property keys of json object.
     */
	public Set<String> keys() {
		return map.keySet();
	}

    /**
     * Returns the property values of json object.
     */
	public Collection<Object> values(){
		return map.values();
	}

    /**
     * Returns true if the json object has properties.
     */
	public boolean hasProperties() {
		return map.isEmpty();
	}

    /**
     * Returns true if the json object contains the property key.
     */
	public boolean hasProperty(String key){
		return map.containsKey(key);
	}

    /**
     * Process all the properties of this object.
     */
    public void forEachProperty(BiConsumer<String, Object> func) {
        if(map.isEmpty()) {
            return;
        }
        for(Map.Entry<String,Object> entry : map.entrySet()) {
            func.accept(entry.getKey().toString(), entry.getValue());
        }
    }

    /**
     * Return the property value.
     */
	public <T> T get(String key){
		return (T)map.get(key);
	}

    /**
     * Converts the property value to the given type.
     */
	public <T> T get(String key,Class<T> type){
		return Converts.convert(map.get(key), type);
	}

    /**
     * Wraps the property value as {@link JsonValue}.
     */
	public JsonValue getValue(String key){
		Object o = get(key);
		return null == o ? null : JsonValue.of(o);
	}

    /**
     * Wraps the property value as {@link JsonArray}.
     */
	public JsonArray getArray(String key){
		List list = get(key);
		return null == list ? null : new JsonArray(list);
	}

    /**
     * Invokes the given function for every item of array.
     *
     * <p/>
     * Do nothing if the array not exists or empty item.
     */
    public void getArray(String key, Consumer<JsonValue> func) {
        JsonArray a = getArray(key);
        if(null == a) {
            return;
        }
        a.forEach(func);
    }

    /**
     * Returns the property value as {@link List}.
     */
    public List<Object> getList(String key) {
        return (List<Object>)get(key);
    }

    /**
     * Returns the property value as {@link Map}.
     */
    public Map<String,Object> getMap(String key){
        return (Map<String,Object>)get(key);
    }

    /**
     * Wraps the property value as {@link JsonObject}.
     */
	public JsonObject getObject(String key){
		Map map = get(key);
		return null == map ? null : new JsonObject(map);
	}

    /**
     * Process all the properties of the object property in this json object.
     */
    public void getObject(String key, BiConsumer<String,Object> func) {
        Map<String,Object> map = get(key);
        if(null != map) {
            for(Map.Entry<String,Object> entry : map.entrySet()) {
                func.accept(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Returns the property value as {@link String}.
     */
	public String getString(String key){
		return (String)map.get(key);
	}

    /**
     * Returns the property value as {@link String} or returns the default value if empty.
     */
	public String getString(String key,String defaultValue){
		String s = (String)map.get(key);
		return Strings.isEmpty(s) ? defaultValue : s;
	}

    /**
     * Returns the property value as {@link Integer}.
     */
	public Integer getInteger(String key){
		return get(key); 
	}

    /**
     * Returns the property value as {@link Integer} or return the default value if null.
     */
	public Integer getInteger(String key,Integer defaultValue){
		Integer i = get(key);
		return null == i ? defaultValue : i;
	}

    /**
     * Returns the property value as {@link Long}.
     */
    public Long getLong(String key){
    	return get(key);
    }

    /**
     * Returns the property value as {@link Long} or return the default value if null.
     */
    public Long getLong(String key,Long defaultValue){
    	Long l = get(key);
    	return null == l ? defaultValue : l;
    }

    /**
     * Returns the property value as {@link Boolean}.
     */
    public Boolean getBoolean(String key){
    	return get(key);
    }

    /**
     * Returns the property value as {@link Boolean} or return the default value if null.
     */
    public Boolean getBoolean(String key,Boolean defaultValue){
    	Boolean b = get(key);
    	return null == b ? defaultValue : b;
    }

    /**
     * Returns the property value as {@link Float}.
     */
    public Float getFloat(String key){
    	return get(key);
    }

    /**
     * Returns the property value as {@link Float} or returns the default value if null.
     */
    public Float getFloat(String key,Float defaultValue){
    	Float f = get(key);
    	return null == f ? defaultValue : f;
    }

    /**
     * Returns the property value as {@link Double}.
     */
    public Double getDouble(String key){
    	return get(key);
    }

    /**
     * Returns the property value as {@link Double} or returns the default value if null.
     */
    public Double getDouble(String key,Double defaultValue){
    	Double d = get(key);
    	return null == d ? defaultValue : d;
    }

	@Override
    public String toString() {
		return map.toString();
	}
}