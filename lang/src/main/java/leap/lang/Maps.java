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
package leap.lang;

import java.util.Map;
import java.util.Map.Entry;

import leap.lang.convert.Converts;
import leap.lang.text.PlaceholderResolver;

@SuppressWarnings("rawtypes")
public class Maps {
	
    public static boolean isEmpty(Map map) {
		return null == map || map.isEmpty();
	}

	/**
	 * Returns the first entry's key if the entry's value equals to the given value.
	 * 
	 * <p>
	 * returns <code>null</code> if map or value is null, or can not found the matched entry .
	 */
	public static <K,V> K getFirstKey(Map<K, V> map,V value){
		if(null == map || null == value){
			return null;
		}
		
		for(Entry<K, V> entry : map.entrySet()){
			if(value.equals(entry.getValue())){
				return entry.getKey();
			}
		}
		
		return null;
	}
	
	public static <T> T get(Map map, Object key, Class<T> type) {
		Object v = map.get(key);
		return null == v ? null : Converts.convert(v, type);
	}
	
	public static <T> T get(Map map, Object key, Class<T> type, T defaultValue) {
		Object v = map.get(key);
		return null == v ? defaultValue : Converts.convert(v, type);
	}
	
	public static String getString(Map map,Object key) {
		Object v = map.get(key);
		return null == v ? null : Converts.toString(v); 
	}
	
	public static String getString(Map map,Object key,String defaultValue) {
		Object v = map.get(key);
		return null == v ? defaultValue : Converts.toString(v); 
	}
	
	public static String getStringRequired(Map map,Object key) {
		Object v = map.get(key);
		if(null == v){
			throw new IllegalStateException("The key '" + key + "' must be exists in map");
		}
		
		String s = Converts.toString(v);
		if(s.length() == 0){
			throw new IllegalStateException("The string of key '" + key + "' must not be empty in map");
		}
		return s;
	}
	
	public static Integer getInteger(Map map,Object key) {
		Object v = map.get(key);
		return null == v ? null : Converts.convert(v, Integer.class);
	}
	
	public static Integer getInteger(Map map,Object key,Integer defaultValue) {
		Object v = map.get(key);
		return null == v ? defaultValue : Converts.convert(v, Integer.class);
	}
	
	public static Integer getIntegerRequired(Map map,Object key) {
		Object v = map.get(key);
		if(null == v){
			throw new IllegalStateException("The key '" + key + "' must be exists in map");
		}
		return Converts.convert(v, Integer.class);
	}
	
	public static Long getLong(Map map,Object key) {
		Object v = map.get(key);
		return null == v ? null : Converts.convert(v, Long.class);
	}
	
	public static Long getLong(Map map,Object key,Long defaultValue) {
		Object v = map.get(key);
		return null == v ? defaultValue : Converts.convert(v, Long.class);
	}
	
	public static Long getLongRequired(Map map,Object key) {
		Object v = map.get(key);
		if(null == v){
			throw new IllegalStateException("The key '" + key + "' must be exists in map");
		}
		return Converts.convert(v, Long.class);
	}
	
	public static Boolean getBoolean(Map map,Object key) {
		Object v = map.get(key);
		return null == v ? null : Converts.convert(v, Boolean.class);
	}
	
	public static Boolean getBoolean(Map map,Object key,Boolean defaultValue) {
		Object v = map.get(key);
		return null == v ? defaultValue : Converts.convert(v, Boolean.class);
	}
	
	public static Boolean getBooleanRequired(Map map,Object key) {
		Object v = map.get(key);
		if(null == v){
			throw new IllegalStateException("The key '" + key + "' must be exists in map");
		}
		return Converts.convert(v, Boolean.class);
	}
	
	public static Float getFloat(Map map,Object key) {
		Object v = map.get(key);
		return null == v ? null : Converts.convert(v, Float.class);
	}
	
	public static Float getFloat(Map map,Object key,Float defaultValue) {
		Object v = map.get(key);
		return null == v ? defaultValue : Converts.convert(v, Float.class);
	}
	
	public static Float getFloatRequired(Map map,Object key) {
		Object v = map.get(key);
		if(null == v){
			throw new IllegalStateException("The key '" + key + "' must be exists in map");
		}
		return Converts.convert(v, Float.class);
	}
	
	public static Double getDouble(Map map,Object key) {
		Object v = map.get(key);
		return null == v ? null : Converts.convert(v, Double.class);
	}
	
	public static Double getDouble(Map map,Object key,Double defaultValue) {
		Object v = map.get(key);
		return null == v ? defaultValue : Converts.convert(v, Double.class);
	}
	
	public static Double getDoubleRequired(Map map,Object key) {
		Object v = map.get(key);
		if(null == v){
			throw new IllegalStateException("The key '" + key + "' must be exists in map");
		}
		return Converts.convert(v, Double.class);
	}
	
	public static <T> T getValue(Map map,Object key,Class<T> targetType) {
		Object v = map.get(key);
		return null == v ? null : Converts.convert(v, targetType);
	}
	
	public static <T> T getValue(Map map,Object key,Class<T> targetType,T defaultValue) {
		Object v = map.get(key);
		return null == v ? defaultValue : Converts.convert(v, targetType);
	}
	
	@SuppressWarnings("unchecked")
    public static <T> T getValueRequired(Map map,Object key,Class<T> targetType) {
		if(String.class == targetType){
			return (T)getStringRequired(map, key);
		}
		Object v = map.get(key);
		if(null == v){
			throw new IllegalStateException("The key '" + key + "' must be exists in map");
		}
		return Converts.convert(v, targetType);
	}
	
	public static void resolveValues(Map<String, String> map, PlaceholderResolver pr) {
		if(null != map && null != pr) {
			for(Entry<String,String> entry : map.entrySet()) {
				String name  = entry.getKey();
				String value = entry.getValue();
				
				if(!Strings.isEmpty(value)) {
					String resolved = pr.resolveString(value);
					if(!value.equals(resolved)) {
						map.put(name, resolved);
					}
				}
			}
		}
	}
	
	protected Maps(){
		
	}
}
