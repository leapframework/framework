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
package leap.core.variable;


import leap.lang.el.ElEvalContext;

public interface VariableEnvironment {
	
	/**
	 * Checks is the given variable name exists.
	 * 
	 * <p>
	 * Returns <code>true</code> if the given variable name is exists.
	 */
	boolean checkVariableExists(String variable);
	
	/**
	 * Returns the value of the given variable name.
	 * 
	 * <p>
	 * Returns <code>null</code> if the given variable name not exists.
	 */
	Object resolveVariable(String variable);

	/**
	 * Returns the value of the given variable name.
	 *
	 * <p>
	 * Returns <code>null</code> if the given variable name not exists.
	 */
	Object resolveVariable(String variable, ElEvalContext context);

	/**
	 * Returns a {@link String} object converted from the value of the given variable name.
	 */
	String resolveVariableAsString(String variable);
}