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

import leap.lang.New;
import leap.lang.convert.ConvertContext;
import leap.lang.convert.Converts;

import java.io.Reader;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JSON {
	
    private static final JsonDecoder decoder = new JsonDecoder();

    static final ConvertContext convertContext = new ConvertContext() {
        @Override
        public ConcreteTypes getConcreteTypes() {
            return JsonConcreteTypes.INSTANCE;
        }
    };

    /**
     * Returns the {@link JsonBuilder} for build a json
     */
    public static JsonBuilder builder(){
        return JsonBuilder.create();
    }
    /**
     * Returns the {@link JsonWriterCreator} for creating a new {@link JsonWriter}.
     */
    public static JsonWriterCreator writer() {
        return new JsonWriterCreatorImpl(new StringBuilder());
    }
    
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
     * Creates a {@link JsonWriter} use {@link StringBuilder} as output.
     */
    public static JsonWriter createWriter(JsonSettings settings) {
        return writer().setSettings(settings).create();
    }

    /**
     * Creates a {@link JsonWriter} use the given {@link Appendable} as output.
     */
    public static JsonWriter createWriter(Appendable out) {
    	return new JsonWriterCreatorImpl(out).create();
    }

    /**
     * Creates a {@link JsonWriter} use the given {@link Appendable} as output.
     */
    public static JsonWriter createWriter(Appendable out, JsonSettings settings) {
        return writer(out).setSettings(settings).create();
    }
    
    /**
     * Encodes the value to json string.
     *
     * <p/>
     * Same as {@link #encode(Object)}.
     */
    public static String stringify(Object value){
    	return createWriter().value(value).toString();
    }

    /**
     * Encodes the value to json string with the given settings.
     *
     * <p/>
     * Same as {@link #encode(Object, JsonSettings)}.
     */
    public static String stringify(Object value, JsonSettings settings){
        return createWriter(settings).value(value).toString();
    }

    /**
     * Parse the json string and returns the result as {@link JsonValue}.
     */
    public static JsonValue parse(String json) {
        return JsonValue.of(decode(json));
    }

    /**
     * Parse the json string and returns the result as {@link JsonValue}.
     */
    public static JsonValue parse(Reader json) {
        return JsonValue.of(decode(json));
    }

    /**
     * Encodes the value to json string.
     */
    public static String encode(Object value){
        return createWriter().value(value).toString();
    }

    /**
     * Encodes the value to json string with the given settings.
     */
    public static String encode(Object value, JsonSettings settings){
        return createWriter(settings).value(value).toString();
    }

    /**
     * Encodes the value to json string.
     */
    public static void encode(Object value, Appendable out){
        createWriter(out).value(value);
    }

    /**
     * Encodes the value to json string with the given settings.
     */
    public static void encode(Object value, JsonSettings settings, Appendable out){
        createWriter(out, settings).value(value);
    }
    
    /**
     * Parse the json string and returns the raw value.
     *
     * <p/>
     * The raw value may be : map, list, null or simple value.
     */
	public static <T> T decode(String json){
		return (T)decoder.decode(json);
	}
	
	/**
	 * Parse the json string and returns the raw value.
     *
     * <p/>
     * The raw value may be : map, list, null or simple value.
     */
	public static <T> T decode(Reader json){
		return (T)decoder.decode(json);
	}

    /**
     * Parse the json string and converts the raw value to the target type.
     */
    public static <T> T decode(String json,Class<? extends T> targetType){
        return Converts.convert(decoder.decode(json), targetType, null, convertContext);
    }

    /**
     * Parse the json string and converts the raw value to the target type.
     */
    public static <T> T decode(String json, Class<? extends T> targetType, Type genericType){
        return Converts.convert(decoder.decode(json), targetType, genericType, convertContext);
    }

    /**
     * Parse the json string and converts the raw value to the target type
     * @since 0.5.0b
     */
    public static <T> Map<String, T> decodeMap(String json, Class<? extends T> valueType){
        Map<String, Object> map = decodeMap(json);
        for(Map.Entry<String, Object> entry : map.entrySet()){
            entry.setValue(Converts.convert(entry.getValue(),valueType, null, convertContext));
        }
        return (Map<String, T>) map;
    }

    /**
     * Parse the json string and converts the raw value to the target type
     * @since 0.5.0b
     */
    public static <T> List<T> decodeList(String json, Class<? extends T> valueType){
        T[] obj = decodeArray(json,valueType);
        return New.arrayList(obj);
    }
    
    /**
     * Parse the json string and converts the raw value to the target type.
     */
    public static <T> T decode(Reader json,Class<? extends T> targetType){
        return Converts.convert(decoder.decode(json),targetType, null, convertContext);
    }

    /**
     * Parse the json string and converts the raw value to the target type.
     */
    public static <T> T decode(Reader json,Class<? extends T> targetType, Type genericType){
        return Converts.convert(decoder.decode(json),targetType, genericType, convertContext);
    }

    /**
     * Parse the json string and returns the the array.
     */
    public static Map<String,Object> decodeMap(String json){
        return parse(json).asMap();
    }

    /**
     * Parse the json string and returns the the array.
     */
    public static Object[] decodeArray(String json){
        return parse(json).asArray();
    }

    /**
     * Parse the json string and returns the the array.
     */
    public static Object[] decodeArray(Reader json){
        return parse(json).asArray();
    }

    /**
     * Parse the json string and returns the the array of the given type.
     */
    public static <T> T[] decodeArray(String json, Class<T> componentType){
        T[] a = (T[])Array.newInstance(componentType, 0);
        return (T[])Converts.convert(parse(json).asArray(), a.getClass(), null, convertContext);
    }

    /**
     * Parse the json string and returns the the array of the given type.
     */
    public static <T> T[] decodeArray(Reader json, Class<T> componentType){
        T[] a = (T[])Array.newInstance(componentType, 0);
        return (T[])Converts.convert(parse(json).asArray(), a.getClass(), null, convertContext);
    }

    /**
     * Converts the decoded json value to the given type.
     */
    public static <T> T convert(Object json, Class<T> type) {
        return Converts.convert(json, type, convertContext);
    }

    /**
     * Converts the decoded json value to the given type.
     */
    public static <T> T convert(Object json, Class<T> type, Type genericType) {
        return Converts.convert(json, type, genericType, convertContext);
    }

    /**
     * Returns the missing properties exists in map but not exists in the given type.
     */
    public static Set<String> checkMissingProperties(Class<?> type, Map map) {
        return JsonDecoder.checkMissingProperties(type, map);
    }
}