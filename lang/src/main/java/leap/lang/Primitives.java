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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * null safe utils for java primitive types.
 */
public class Primitives {

	/** A map from primitive types to their corresponding wrapper types. */
	private static final Map<Class<?>, Class<?>>	PRIMITIVE_TO_WRAPPER_TYPE;

	/** A map from wrapper types to their corresponding primitive types. */
	private static final Map<Class<?>, Class<?>>	WRAPPER_TO_PRIMITIVE_TYPE;

	static {
		Map<Class<?>, Class<?>> primToWrap = new HashMap<Class<?>, Class<?>>(16);
		Map<Class<?>, Class<?>> wrapToPrim = new HashMap<Class<?>, Class<?>>(16);

		add(primToWrap, wrapToPrim, boolean.class, Boolean.class);
		add(primToWrap, wrapToPrim, byte.class,    Byte.class);
		add(primToWrap, wrapToPrim, char.class,    Character.class);
		add(primToWrap, wrapToPrim, double.class,  Double.class);
		add(primToWrap, wrapToPrim, float.class,   Float.class);
		add(primToWrap, wrapToPrim, int.class,     Integer.class);
		add(primToWrap, wrapToPrim, long.class,    Long.class);
		add(primToWrap, wrapToPrim, short.class,   Short.class);
		add(primToWrap, wrapToPrim, void.class,    Void.class);

		PRIMITIVE_TO_WRAPPER_TYPE = Collections.unmodifiableMap(primToWrap);
		WRAPPER_TO_PRIMITIVE_TYPE = Collections.unmodifiableMap(wrapToPrim);
	}

	private static void add(Map<Class<?>, Class<?>> forward, Map<Class<?>, Class<?>> backward, Class<?> key, Class<?> value) {
		forward.put(key, value);
		backward.put(value, key);
	}

	public static boolean isPrimitiveType(Class<?> type){
		return null == type ? false : PRIMITIVE_TO_WRAPPER_TYPE.keySet().contains(type);
	}
	
	public static boolean isWrapperType(Class<?> type){
		return null == type ? false : WRAPPER_TO_PRIMITIVE_TYPE.keySet().contains(type);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Class<T> wrap(Class<T> type) {
		if(null == type){
			return null;
		}
		Class<T> wrapped = (Class<T>) PRIMITIVE_TO_WRAPPER_TYPE.get(type);
		return wrapped == null ? type : wrapped;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Class<T> unwrap(Class<T> type) {
		if(null == type){
			return null;
		}
		Class<T> unwrapped = (Class<T>) WRAPPER_TO_PRIMITIVE_TYPE.get(type);
		return unwrapped == null ? type : unwrapped;
	}
	
	protected Primitives() {

	}
}
