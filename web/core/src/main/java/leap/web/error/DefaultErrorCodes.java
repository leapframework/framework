/*
 * Copyright 2015 the original author or authors.
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
package leap.web.error;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import leap.core.i18n.MessageSource;
import leap.lang.Args;

public class DefaultErrorCodes implements ErrorCodes {
	
	public static final String KEY_PREFIX = "errors.";

	protected final Map<Class<?>, String> exceptionCodeMappigns = new ConcurrentHashMap<>();
	protected final Map<Class<?>, String> exceptionCodeMappingsImmutableView = Collections.unmodifiableMap(exceptionCodeMappigns);
	
	@Override
	public ErrorCodes addErrorCode(Class<?> exceptionClass, String code) {
		Args.notNull(exceptionClass,"exception class");
		Args.notEmpty(code, "code");
		exceptionCodeMappigns.put(exceptionClass, code);
		return this;
	}
	
	@Override
    public ErrorCodes addErrorCodes(Map<Class<?>, String> m) {
		if(null != m) {
			exceptionCodeMappigns.putAll(m);
		}
		return this;
	}

	@Override
	public String getErrorCode(Class<?> exceptionClass) {
		return exceptionCodeMappigns.get(exceptionClass);
	}
	
	@Override
    public String getErrorMessage(String errorCode, Throwable exception, MessageSource ms, Locale locale) {
		if(null == exception) {
			return ms.getMessage(locale, KEY_PREFIX + errorCode);	
		}else{
			return ms.getMessage(locale, KEY_PREFIX + errorCode, exception.getMessage(), exception.getClass().getName());
		}
    }

	@Override
	public Map<Class<?>, String> getExceptionCodeMappings() {
		return exceptionCodeMappingsImmutableView;
	}

}