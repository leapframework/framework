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
package leap.core.i18n;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import leap.lang.exception.ObjectNotFoundException;

public interface MessageSource {
	
	/**
	 * Returns a formatted {@link String} of the given key.
	 * 
	 * @throws ObjectNotFoundException if the given key not exists.
	 */
	String getMessage(String key,Object... args) throws ObjectNotFoundException;
	
	/**
	 * Returns a formatted {@link String} of the given key for the given {@link Locale}.
	 * 
	 * @throws ObjectNotFoundException if the given key not exists for the given {@link Locale}.
	 */
	String getMessage(Locale locale,String key,Object... args) throws ObjectNotFoundException;
	
	/**
	 * Returns a formatted {@link String} of the given key and default locale.
	 * 
	 * <p>
	 * Returns <code>null</code> if the given key not exists for default {@link Locale}.
	 */
	String tryGetMessage(String key,Object... args);
	
	/**
	 * Returns a formatted {@link String} of the given key for the given {@link Locale}.
	 * 
	 * <p>
	 * Returns <code>null</code> if the given key not exists for the given {@link Locale}.
	 */
	default String tryGetMessage(Locale locale, String key, Object... args) {
	    return tryGetMessage(locale, key, Collections.emptyMap(), args);
    }

    /**
     * Returns a formatted {@link String} of the given key for the given {@link Locale}.
     *
     * <p>
     * Returns <code>null</code> if the given key not exists for the given {@link Locale}.
     */
    String tryGetMessage(Locale locale, String key, Map<String, Object> vars, Object... args);

	/**
	 * Returns a formatted {@link String} of the given key.
	 *
	 * @throws ObjectNotFoundException if the given key not exists.
	 */
	default String getMessage(MessageKey key) throws ObjectNotFoundException{
		if(key == null){
			return null;
		}
		return getMessage(key.getLocale(), key.getKey(), key.getArgs());
	}
	/**
	 * Returns a formatted {@link String} of the given key for the given {@link Locale}.
	 *
	 * <p>
	 * Returns <code>null</code> if the given key not exists for the given {@link Locale}.
	 */
	default String tryGetMessage(MessageKey key){
		if(key == null){
			return null;
		}
		return tryGetMessage(key.getLocale(), key.getKey(), key.getArgs());
	}
}