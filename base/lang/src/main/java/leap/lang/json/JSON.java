/*
 * Copyright 2010 the original author or authors.
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

import java.io.Reader;
import java.lang.reflect.Array;
import java.util.Map;

import leap.lang.convert.Converts;

public class JSON {
	
    private static final JsonDecoder decoder = new JsonDecoder();
    
    /**
     * Returns the {@link JsonWriterCreator} for creating a new {@link JsonWriter}.
     */
    public static JsonWriterCreator writer(Appendable out) {
    	return new JsonWriterCreatorImpl(out);
    }
    
    /**
     * Creates a {@link JsonWriter} use {@link StringBuilder} as output.
     */
    public static JsonWriter createWriter() {
    	return createWriter(new StringBuilder());
    }
    
    /**
     * Creates a {@link JsonWriter} use the given {@link Appendable} as output.
     */
    public static JsonWriter createWriter(Appendable out) {
    	return new JsonWriterCreatorImpl(out).create();
    }
    
    /**
     * Creates a {@link JsonEncoder} for the given value.
     */
    public static JsonEncoder createEncoder(Object value) {
    	return new JsonEncoderImpl(value);
    }
    
    /**
     * Creates a {@link JsonEncoder} for the given value.
     */
    public static JsonEncoder createEncoder(Object value, JsonSettings settings) {
    	return new JsonEncoderImpl(value, settings);
    }
    
    /**
     * Same as {@link #encode(Object)}.
     */
    public static String stringify(Object value){
    	return createEncoder(value).encodeToString();
    }
    
    /**
     * Encodes the value to json string.
     */
    public static String encode(Object value){
        return createEncoder(value).encodeToString();
    }
    
    /**
     * Encodes the value to json string.
     */
    public static String encode(Object value,JsonSettings settings){
        return createEncoder(value, settings).encodeToString();
    }
    
    /**
     * Sames as {@link #decode(String)}
     */
    public static Object parse(String json) {
    	return decoder.decode(json);
    }

    /**
     * Parse the json string and returns the raw value.
     */
	public static Object decode(String json){
		return decoder.decode(json);
	}
	
	/**
	 * Parse the json string and returns the raw value.
	 */
	public static Object decode(Reader json){
		return decoder.decode(json);
	}
	
	/**
	 * Parse the json string and converts the raw value to the target type.
	 */
	public static <T> T decode(String json,Class<? extends T> targetType){
	    return Converts.convert(decoder.decode(json),targetType);
	}
	
    /**
     * Parse the json string and converts the raw value to the target type.
     */
    public static <T> T decode(Reader json,Class<? extends T> targetType){
        return Converts.convert(decoder.decode(json),targetType);
    }
    
    /**
     * Parse the json string and returns the wrapped {@link JsonValue}.
     */
	public static JsonValue decodeToJsonValue(Reader json) {
	    return JsonValue.of(decoder.decode(json));
	}

    /**
     * Parse the json string and returns the wrapped {@link JsonValue}.
     */
	public static JsonValue decodeToJsonValue(String json) {
		return JsonValue.of(decoder.decode(json));
	}
    
    /**
     * Parse the json string and returns the result as {@link Map}.
     * 
     * @throws IllegalStateException if not an json object(map).
     */
    public static Map<String,Object> decodeToMap(String json) throws IllegalStateException{
        return decodeToJsonValue(json).asMap();
    }
    
    /**
     * Parse the json string and returns the result as object array.
     * 
     * @throws IllegalStateException if not an object array.
     */
    public static Object[] decodeToArray(String json) throws IllegalStateException {
        return decodeToJsonValue(json).asArray();
    }
    
    /**
     * Parse the json string and converts the object array to the array of given component type.
     * 
     * @throws IllegalStateException if not an object array.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] decodeToArray(String json,Class<T> componentType){
        T[] a = (T[])Array.newInstance(componentType, 0);
        return (T[])Converts.convert(decodeToJsonValue(json).asArray(),a.getClass());
    }
    
    /**
     * Parse the json string and converts the object array to the array of given component type.
     * 
     * @throws IllegalStateException if not an object array.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] decodeToArray(Reader json,Class<T> componentType){
        T[] a = (T[])Array.newInstance(componentType, 0);
        return (T[])Converts.convert(decodeToJsonValue(json).asArray(),a.getClass());
    }
}