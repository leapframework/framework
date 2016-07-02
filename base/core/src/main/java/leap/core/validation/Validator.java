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
package leap.core.validation;

import java.util.Locale;

import leap.core.i18n.MessageSource;

/**
 * A value validator.
 */
public interface Validator {
	
	/**
	 * Validates the given value.
	 * 
	 * <p>
	 * Returns <code>false</code> if value does not pass.
	 */
	boolean validate(Object value);
	
	/**
	 * Returns the error code of this validator.
	 */
	String getErrorCode();
	
	/**
	 * Returns the error message without title or <code>null</code>.
	 */
	String getErrorMessage(MessageSource ms, Locale locale);
	
	/**
	 * Returns the error message with title or <code>null</code>
	 */
	String getErrorMessage(String title, MessageSource ms, Locale locale);

}