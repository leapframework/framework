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
package leap.lang.el;

public interface ElContext {

	/**
	 * Converts the given input value to the result value of target type.
	 */
	<T> T convert(Object v,Class<T> targetType);
	
	/**
	 * Converts the given value to string.
	 */
	String toString(Object v);

	/**
	 * Test the given value and returns <code>true</code> or <code>false</code>
	 */
	boolean test(Object v);
	
	/**
	 * Returns the formatted message of the given key and args.
	 */
	String getMessage(String key,Object... args);
	
}