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
package leap.lang.json;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import leap.lang.Strings;
import leap.lang.naming.NamingStyle;

public interface JsonWriter {
	
	int    MAX_DEPTH     = 100; 
	
    String HEX_PREFIX    = "0x";
	String NULL_STRING   = "null";
	String EMPTY_STRING  = "\"\"";
	char   OPEN_ARRAY    = '[';
	char   CLOSE_ARRAY   = ']';
	char   OPEN_OBJECT   = '{';
	char   CLOSE_OBJECT  = '}';
	char   CLOSE_KEY     = ':';
	char   DOUBLE_QUOTE  = '"';
	char   COMMA_CHAR    = ',';
	
	/**
	 * Returns the naming style for writing property's name in bean or map.
	 */
	NamingStyle getNamingStyle();
	
	/**
	 * Default is {@link #MAX_DEPTH}.
	 */
	int getMaxDepth();
	
	/**
	 * Returns <code>true</code> if the key will be quoted as "key".
	 * 
	 * <p>
	 * Default is <code>true</code>.
	 */
	boolean isKeyQuoted();
	
	/**
	 * Returns <code>true</code> if this writer ignores the property if the string value is empty.
	 * 
	 * <p>
	 * Default is <code>false</code>.
	 */
	boolean isIgnoreEmptyString();
	
	/**
	 * Returns <code>true</code> if this writer ignores the property if the array value is empty.
	 * 
	 * <p>
	 * Default is <code>false</code>.
	 */
	boolean isIgnoreEmptyArray();
	
	/**
	 * Returns <code>true</code> if the writer ignores the property if the boolean value is false.
	 * 
	 * <p>
	 * Default is <code>false</code>.
	 */
	boolean isIgnoreFalse();
	
	/**
	 * Returns <code>true</code> if the writer ignores the property if the value is <code>null</code>.
	 * 
	 * <p>
	 * Default is <code>false</code>.
	 */
	boolean isIgnoreNull();
	
	/**
	 * Returns <code>true</code> if the writer will checks the cyclic object references.
	 * 
	 * <p>
	 * Default is <code>true</code>.
	 */
	boolean isDetectCyclicReferences();
	
	/**
	 * Returns <code>true</code> if the writer ignores the cyclic references.
	 * 
	 * <p>
	 * Default is <code>true</code>.
	 */
	boolean isIgnoreCyclicReferences();
	
	/**
	 * Writes <code>'null'</code>
	 */
	JsonWriter null_();
	
	/**
	 * Writes property key <code>'key:'</code>
	 */
	JsonWriter key(String key);
	
	/**
	 * Writes the key use naming style.
	 */
	JsonWriter keyUseNamingStyle(String key);
	
	JsonWriter value(boolean bool);

	JsonWriter value(byte b);

	JsonWriter value(byte[] bytes);

	JsonWriter value(char c);
	
	JsonWriter value(short s);
	
	JsonWriter value(int i);
	
	JsonWriter value(long l);
	
	JsonWriter value(float f);
	
	JsonWriter value(double d);

	JsonWriter value(Number number);

	JsonWriter value(Date date);

	JsonWriter value(String string);
	
	JsonWriter value(Object value);
	
	@SuppressWarnings("rawtypes")
    JsonWriter map(Map map);
	
    JsonWriter bean(Object bean);
	
	/**
	 * Writes array item separator <code>','</code>.
	 */
	JsonWriter separator();
	
	/**
	 * Writes raw string.
	 */
	JsonWriter raw(String string);

	/**
	 * Writes <code>'{'</code>.
	 */
	JsonWriter startObject();
	
	/**
	 * Writers <code>'key : {'</code> .
	 */
	JsonWriter startObject(String key);
	
	/**
	 * Writes <code>'}'</code>.
	 */
	JsonWriter endObject();
	
	/**
	 * Writes <code>'['</code>.
	 */
	JsonWriter startArray();
	
	/**
	 * Writes <code>'key:['</code>
	 */
	JsonWriter startArray(String key);

	JsonWriter endArray();
	
	default JsonWriter emptyArray() {
	    return startArray().endArray();
	}
	
	default JsonWriter propertyOptional(String key, JsonStringable o) {
		if(null != o){
			property(key, o);
		}
		return this;
	}

	default JsonWriter propertyOptional(String key, String v) {
		if(!Strings.isEmpty(v)){
			property(key, v);
		}
		return this;
	}
	
	default JsonWriter propertyOptional(String key, Boolean v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}
	
	default JsonWriter propertyOptional(String key, Byte v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}
	
	default JsonWriter propertyOptional(String key, Short v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}
	
	default JsonWriter propertyOptional(String key, Integer v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}
	
	default JsonWriter propertyOptional(String key, Long v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}
	
	default JsonWriter propertyOptional(String key, Float v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}
	
	default JsonWriter propertyOptional(String key, Double v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}
	
	default JsonWriter propertyOptional(String key, Number v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}
	
	default JsonWriter propertyOptional(String key, Date v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}
	
	default JsonWriter propertyOptional(String key, Object v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}
	
	default JsonWriter propertyOptional(String key, String[] a) {
		return null == a || a.length == 0 ? this : property(key, a);
	}
	
	default JsonWriter propertyOptional(String key, int[] a) {
		return null == a || a.length == 0 ? this : property(key, a);
	}
	
	default JsonWriter propertyOptional(String key, long[] a) {
		return null == a || a.length == 0 ? this : property(key, a);
	}
	
	default JsonWriter propertyOptional(String key, Number[] a) {
		return null == a || a.length == 0 ? this : property(key, a);
	}
	
	default JsonWriter propertyOptional(String key, Object[] a) {
		return null == a || a.length == 0 ? this : property(key, a);
	}
	
	JsonWriter propertyIgnorable(String key, Object v);
	
	JsonWriter propertyIgnorable(String key, String s);
	
	JsonWriter propertyIgnorable(String key, boolean b);
	
	default JsonWriter property(String key, JsonStringable o) {
		key(key);
		
		if(null == o){
			null_();
		}else{
			o.toJson(this);
		}
		
		return this;
	}
	
	default JsonWriter property(String key, Runnable propertyWriter) {
		key(key);
		
		if(null == propertyWriter){
			null_();
		}else{
			propertyWriter.run();
		}
		
		return this;
	}
	
	JsonWriter property(String key, String v);
	
	JsonWriter property(String key, boolean v);

	JsonWriter property(String key, byte v);

	JsonWriter property(String key, short v);

	JsonWriter property(String key, int v);

	JsonWriter property(String key, long v);

	JsonWriter property(String key, float v);

	JsonWriter property(String key, double v);
	
	JsonWriter property(String key, Number v);

	JsonWriter property(String key, Date v);
	
	JsonWriter property(String key, Object v);
	
	default JsonWriter property(String key, String[] a) {
		return key(key).array(a);
	}
	
	default JsonWriter property(String key, int[] a) {
		return key(key).array(a);
	}
	
	default JsonWriter property(String key, long[] a) {
		return key(key).array(a);
	}
	
	default JsonWriter property(String key, Number[] a) {
		return key(key).array(a);
	}
	
	default JsonWriter property(String key, Object[] a) {
		return key(key).array(a);
	}

	JsonWriter array(String... array);
	
	/**
	 * Calling {@link Class#isArray()} must be <code>true</code>. 
	 */
	JsonWriter objectArray(Object array) throws IllegalStateException;
	
	/**
	 * Writes an json array and skip the item if the string is empty.
	 */
	JsonWriter arrayIgnoreEmptyItem(String... array);
	
	JsonWriter array(short... array);
	
	JsonWriter array(int... array);
	
	JsonWriter array(long... array);
	
	JsonWriter array(float... array);
	
	JsonWriter array(double... array);
	
	JsonWriter array(Number... array);
	
	JsonWriter array(Date... array);
	
	JsonWriter array(Iterable<?> array);
	
	JsonWriter array(Iterator<?> array);
	
	JsonWriter array(Object[] array);
	
	/**
	 * Writes an array.
	 */
	default JsonWriter array(JsonStringable... array) {
		for(JsonStringable o : array){
			o.toJson(this);
		}
		return this;
	}
	
	/**
	 * Writes an array property.
	 */
	default JsonWriter array(String key, JsonStringable... array) {
		key(key).startArray();
		for(JsonStringable o : array){
			o.toJson(this);
		}
		return endArray();
	}
	
	/**
	 * Writes an array property.
	 */
	default JsonWriter array(String key, Iterable<? extends JsonStringable> iterable) {
		key(key).startArray();
		for(JsonStringable o : iterable){
			o.toJson(this);
		}
		return endArray();
	}
}