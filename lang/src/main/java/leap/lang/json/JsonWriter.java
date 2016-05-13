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

import leap.lang.Strings;
import leap.lang.naming.NamingStyle;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("rawtypes")
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

    /**
     * Writes a boolean value.
     */
	JsonWriter value(boolean bool);

    /**
     * Writes a byte value.
     */
	JsonWriter value(byte b);

    /**
     * Writes a byte array as single value (not a json array).
     *
     * <p/>
     * The implementation will converts the bytes to base64 string.
     */
	JsonWriter value(byte[] bytes);

    /**
     * Wrties a char value.
     */
	JsonWriter value(char c);

    /**
     * Writes a short value.
     */
	JsonWriter value(short s);

    /**
     * Writes an int value.
     */
	JsonWriter value(int i);

    /**
     * Writes a long value.
     */
	JsonWriter value(long l);

    /**
     * Writes a float value.
     */
	JsonWriter value(float f);

    /**
     * Writes a double value.
     */
	JsonWriter value(double d);

    /**
     * Writes a number value.
     */
	JsonWriter value(Number number);

    /**
     * Writes a date value.
     *
     * <p/>
     * Json has no data type of date, the implementation will converts the date to string value of {@link Date#getTime()}.
     */
	JsonWriter value(Date date);

    /**
     * Writes a string value.
     */
	JsonWriter value(String string);

    /**
     * Writes a value according to the type of the given object.
     *
     * <p/>
     * The implementation will checks the real data type of the value.
     *
     * <p/>
     * Supports writing simple value, array value and object value in json.
     */
	JsonWriter value(Object value);

    /**
     * Writes a json object.
     */
    JsonWriter map(Map map);

    /**
     * Writs a json object.
     */
    JsonWriter bean(Object bean);
	
	/**
	 * Writes array item separator <code>,</code>.
	 */
	JsonWriter separator();
	
	/**
	 * Writes raw string.
	 */
	JsonWriter raw(String string);

	/**
	 * Writes <code>{</code>.
	 */
	JsonWriter startObject();
	
	/**
	 * Writers <code>key : {</code> .
	 */
	JsonWriter startObject(String key);
	
	/**
	 * Writes <code>}</code>.
	 */
	JsonWriter endObject();
	
	/**
	 * Writes <code>[</code>.
	 */
	JsonWriter startArray();
	
	/**
	 * Writes <code>key:[</code>
	 */
	JsonWriter startArray(String key);

    /**
     * Writes <code>]</code>.
     */
	JsonWriter endArray();

    /**
     * Writes empty array : <code>[]</code>.
     */
	default JsonWriter emptyArray() {
	    return startArray().endArray();
	}

    /**
     * Writes a property optional. That means it will not writes the property if the object is null.
     */
	default JsonWriter propertyOptional(String key, JsonStringable o) {
		if(null != o){
			property(key, o);
		}
		return this;
	}

    /**
     * Writes a property optional. That means it will not writes the property if the string is null or empty.
     */
	default JsonWriter propertyOptional(String key, String v) {
		if(!Strings.isEmpty(v)){
			property(key, v);
		}
		return this;
	}

    /**
     * Writes a property optional. That means it will not writes the property if the value is null.
     */
	default JsonWriter propertyOptional(String key, Boolean v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}

    /**
     * Writes a property optional. That means it will not writes the property if the value is null.
     */
	default JsonWriter propertyOptional(String key, Byte v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}

    /**
     * Writes a property optional. That means it will not writes the property if the value is null.
     */
	default JsonWriter propertyOptional(String key, Short v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}

    /**
     * Writes a property optional. That means it will not writes the property if the value is null.
     */
	default JsonWriter propertyOptional(String key, Integer v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}

    /**
     * Writes a property optional. That means it will not writes the property if the value is null.
     */
	default JsonWriter propertyOptional(String key, Long v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}

    /**
     * Writes a property optional. That means it will not writes the property if the value is null.
     */
	default JsonWriter propertyOptional(String key, Float v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}

    /**
     * Writes a property optional. That means it will not writes the property if the value is null.
     */
	default JsonWriter propertyOptional(String key, Double v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}

    /**
     * Writes a property optional. That means it will not writes the property if the value is null.
     */
	default JsonWriter propertyOptional(String key, Number v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}

    /**
     * Writes a property optional. That means it will not writes the property if the value is null.
     */
	default JsonWriter propertyOptional(String key, Date v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}

    /**
     * Writes a property optional. That means it will not writes the property if the value is null.
     */
	default JsonWriter propertyOptional(String key, Object v) {
		if(null != v){
			property(key, v);
		}
		return this;
	}

    /**
     * Writes a object property optional. That means it will not writes the property if the map is null or empty.
     */
    default JsonWriter propertyOptional(String key, Map map) {
        if(null != map && !map.isEmpty()){
            property(key, map);
        }
        return this;
    }

    /**
     * Writes a property optional. That means it will not writes the property if the array is null or empty.
     */
	default JsonWriter propertyOptional(String key, String[] a) {
		return null == a || a.length == 0 ? this : property(key, a);
	}

    /**
     * Writes a property optional. That means it will not writes the property if the array is null or empty.
     */
	default JsonWriter propertyOptional(String key, int[] a) {
		return null == a || a.length == 0 ? this : property(key, a);
	}

    /**
     * Writes a property optional. That means it will not writes the property if the array is null or empty.
     */
	default JsonWriter propertyOptional(String key, long[] a) {
		return null == a || a.length == 0 ? this : property(key, a);
	}

    /**
     * Writes a property optional. That means it will not writes the property if the array is null or empty.
     */
	default JsonWriter propertyOptional(String key, Number[] a) {
		return null == a || a.length == 0 ? this : property(key, a);
	}

    /**
     * Writes a property optional. That means it will not writes the property if the array is null or empty.
     */
	default JsonWriter propertyOptional(String key, Object[] a) {
		return null == a || a.length == 0 ? this : property(key, a);
	}

    /**
     * Writes a property optional. That means it will not writes the property if the value can be ignored.
     *
     * <p/>
     * See {@link #isIgnoreEmptyArray()}, {@link #isIgnoreEmptyString()}, {@link #isIgnoreEmptyString()}, etc....
     */
	JsonWriter propertyIgnorable(String key, Object v);

    /**
     * Writes a property optional. That means it will not writes the property if the string can be ignored.
     *
     * <p/>
     * See {@link #isIgnoreEmptyString()}.
     */
	JsonWriter propertyIgnorable(String key, String s);

    /**
     * Writes a property optional. That means it will not writes the property if the value can be ignored.
     *
     * <p/>
     * See {@link #isIgnoreFalse()} ()}
     */
	JsonWriter propertyIgnorable(String key, boolean b);

    /**
     * Writes a property, the value will be wrote by the given {@link JsonStringable} object.
     */
	default JsonWriter property(String key, JsonStringable o) {
		key(key);
		
		if(null == o){
			null_();
		}else{
			o.toJson(this);
		}
		
		return this;
	}

    /**
     * Writes a property, the value will be wrote by the given {@link Runnable} function.
     */
	default JsonWriter property(String key, Runnable propertyWriter) {
		key(key);
		
		if(null == propertyWriter){
			null_();
		}else{
			propertyWriter.run();
		}
		
		return this;
	}

    /**
     * Writes a string property.
     */
	JsonWriter property(String key, String v);

    /**
     * Writes a boolean property.
     */
	JsonWriter property(String key, boolean v);

    /**
     * Writes a byte property.
     */
	JsonWriter property(String key, byte v);

    /**
     * Writes a short property.
     */
	JsonWriter property(String key, short v);

    /**
     * Writes an int property.
     */
	JsonWriter property(String key, int v);

    /**
     * Writes a long property.
     */
	JsonWriter property(String key, long v);

    /**
     * Writes a float property.
     */
	JsonWriter property(String key, float v);

    /**
     * Writes a double property.
     */
	JsonWriter property(String key, double v);

    /**
     * Writes a number property.
     */
	JsonWriter property(String key, Number v);

    /**
     * Writes a date property.
     *
     * <p/>
     * See {@link #value(Date)}.
     */
	JsonWriter property(String key, Date v);

    /**
     * Writes a property, the value will be handled according to the real data type.
     *
     * <p/>
     * See {@link #value(Object)}.
     */
	JsonWriter property(String key, Object v);

    /**
     * Writes a json object property.
     */
    JsonWriter property(String key, Map v);

    /**
     * Writes a string array property.
     */
	default JsonWriter property(String key, String[] a) {
		return key(key).array(a);
	}

    /**
     * Writes an int array property.
     */
	default JsonWriter property(String key, int[] a) {
		return key(key).array(a);
	}

    /**
     * Writes a long array property.
     */
    default JsonWriter property(String key, long[] a) {
		return key(key).array(a);
	}

    /**
     * Writes a number array property.
     */
    default JsonWriter property(String key, Number[] a) {
		return key(key).array(a);
	}

    /**
     * Writes an object array property.
     */
	default JsonWriter property(String key, Object[] a) {
		return key(key).array(a);
	}

    /**
     * Writes an object array property.
     */
    default JsonWriter property(String key, Iterable<Object> a) {
        return key(key).array(a);
    }

    /**
     * Writes a string array property.
     */
    default JsonWriter propertyString(String key, Iterable<String> a) {
        return key(key).arrayString(a);
    }

    /**
     * Writes a property optional. That means it will not writes the property if the array is null or empty.
     */
    default JsonWriter propertyStringOptional(String key, Collection<String> a) {
        return null == a || a.size() == 0 ? this : property(key, a);
    }

	/**
	 * Writes an array property.
	 */
	default JsonWriter propertyJsonable(String key, JsonStringable... array) {
		key(key).startArray();
        if(null != array) {
            for(JsonStringable o : array){
                o.toJson(this);
            }
        }
		return endArray();
	}

	/**
	 * Writes an array property.
	 */
	default JsonWriter propertyJsonable(String key, Iterable<? extends JsonStringable> iterable) {
		key(key).startArray();
        if(null != iterable) {
            for(JsonStringable o : iterable){
                o.toJson(this);
            }
        }
		return endArray();
	}

    /**
     * Writes an array property.
     */
    default JsonWriter propertyJsonableOptional(String key, Collection<? extends  JsonStringable> array) {
        if(null == array || array.isEmpty()) {
            return this;
        }
        return propertyJsonable(key, array);
    }

    /**
     * Writes a string array.
     */
	JsonWriter array(String... array);
	
	/**
     * Writes an object array.
     *
     * <p/>
	 * Calling {@link Class#isArray()} must be <code>true</code>. 
	 */
	JsonWriter objectArray(Object array) throws IllegalStateException;
	
	/**
	 * Writes an json array and skip the item if the string is empty.
	 */
	JsonWriter arrayIgnoreEmptyItem(String... array);

    /**
     * Writes a short array.
     */
	JsonWriter array(short... array);

    /**
     * Writes an int array.
     */
    JsonWriter array(int... array);

    /**
     * Writes a long array.
     */
    JsonWriter array(long... array);

    /**
     * Writes a float array.
     */
    JsonWriter array(float... array);

    /**
     * Writes a double array.
     */
    JsonWriter array(double... array);

    /**
     * Writes a number array.
     */
    JsonWriter array(Number... array);

    /**
     * Writes a date array.
     *
     * <p/>
     * See {@link #value(Date}.
     */
    JsonWriter array(Date... array);

    /**
     * Writes an object array.
     *
     * <p/>
     * See {@link #value(Object}.
     */
    JsonWriter array(Iterable<?> array);

    /**
     * Writes an object array.
     *
     * <p/>
     * See {@link #value(Object}.
     */
	JsonWriter array(Iterator<?> array);

    /**
     * Writes an object array.
     *
     * <p/>
     * See {@link #value(Object}.
     */
    JsonWriter array(Object[] array);

    /**
     * Writes a string array.
     */
    JsonWriter arrayString(Iterable<String> array);

	/**
	 * Writes an array.
	 */
	default JsonWriter arrayJsonable(JsonStringable... array) {
		for(JsonStringable o : array){
			o.toJson(this);
		}
		return this;
	}
}