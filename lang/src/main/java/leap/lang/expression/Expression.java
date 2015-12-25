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
package leap.lang.expression;

import java.util.Map;

public interface Expression {
	
	/**
	 * Returns the expression value.
	 */
	Object getValue();
	
	/**
	 * Returns the expression value.
	 */
	Object getValue(Object context);
	
	/**
	 * Returns the expression value.
	 */
	Object getValue(Map<String, Object> vars);
	
	/**
	 * Returns the expression value.
	 */
	Object getValue(Object context, Map<String, Object> vars);

	/**
	 * Returns the expression value.
	 */
	<T> T getValue(Class<T> targetType);
	
	/**
	 * Returns the expression value.
	 */
	<T> T getValue(Class<T> targetType, Object context);
	
	/**
	 * Returns the expression value.
	 */
	<T> T getValue(Class<T> targetType, Map<String, Object> vars);
	
	/**
	 * Returns the expression value.
	 */
	<T> T getValue(Class<T> targetType, Object context, Map<String, Object> vars);
}