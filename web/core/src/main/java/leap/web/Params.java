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
package leap.web;

import leap.lang.Strings;
import leap.lang.convert.Converts;

import java.util.Set;

/**
 * A wrapper interface of request parameters.
 */
public interface Params {

    /**
     * Returns all the parameter names.
     */
    Iterable<String> names();
	
	/**
	 * Returns <code>true</code> if the param name exists in the request.
	 */
	default boolean exists(String name) {
		return null == get(name);
	}

	/**
	 * Returns <code>null</code> if the parameter not exists.
	 * 
	 * @see Request#getParameter(String)
	 */
	String get(String name);
	
	/**
	 * Returns an string array contains all the parameters matches the name.
	 * 
	 * <p>
	 * Returns an empty string array if the parameter not exists in the request.
	 * 
	 * @see Request#getParameterValues(String) 
	 */
	String[] getArray(String name);
	
	/**
	 * Example : 
	 * 
	 * <pre>
	 * If the request parameter is '<code>?i=1,2</code>', returns [1,2].
	 * If the request parameter is '<code>?i=1&i=2</code>', returns [1,2]
	 * </pre> 
	 */
	default String[] getArrayForNumber(String name) {
		String[] a = getArray(name);
		if(a.length == 1){
			return Strings.split(a[0],',');
		}
		return a;
	}
	
	default Short getShort(String name) {
		String v = get(name);
		return Strings.isEmpty(v) ? null : Converts.convert(v, Short.class);
	}
	
	default short getShort(String name,short defaultValue) {
		String v = get(name);
		return Strings.isEmpty(v) ? defaultValue : Converts.convert(v, Short.class);
	}
	
	default Integer getInteger(String name) {
		String v = get(name);
		return Strings.isEmpty(v) ? null : Converts.convert(v, Integer.class);
	}
	
	default int getInteger(String name, int defaultValue) {
		String v = get(name);
		return Strings.isEmpty(v) ? defaultValue : Converts.convert(v, Integer.class);
	}
	
	default Long getLong(String name) {
		String v = get(name);
		return Strings.isEmpty(v) ? null : Converts.convert(v, Long.class);
	}
	
	default long getLong(String name, long defaultValue) {
		String v = get(name);
		return Strings.isEmpty(v) ? defaultValue : Converts.convert(v, Long.class);
	}
	
	default Float getFloat(String name) {
		String v = get(name);
		return Strings.isEmpty(v) ? null : Converts.convert(v, Float.class);
	}
	
	default float getFloat(String name,float defaultValue) {
		String v = get(name);
		return Strings.isEmpty(v) ? defaultValue : Converts.convert(v, Float.class);
	}
	
	default Double getDouble(String name) {
		String v = get(name);
		return Strings.isEmpty(v) ? null : Converts.convert(v, Double.class);
	}
	
	default double getDouble(String name,double defaultValue) {
		String v = get(name);
		return Strings.isEmpty(v) ? defaultValue : Converts.convert(v, Double.class);
	}
	
	default short[] getShortArray(String name) {
		return Converts.convert(getArrayForNumber(name), short[].class);
	}
	
	default int[] getIntArray(String name) {
		return Converts.convert(getArrayForNumber(name), int[].class);
	}
	
	default long[] getLongArray(String name) {
		return Converts.convert(getArrayForNumber(name), long[].class);
	}
	
	default float[] getFloatArray(String name) {
		return Converts.convert(getArrayForNumber(name), float[].class);
	}
	
	default double[] getDoubleArray(String name) {
		return Converts.convert(getArrayForNumber(name), double[].class);
	}

}