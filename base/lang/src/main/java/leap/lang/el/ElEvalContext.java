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

public interface ElEvalContext extends ElContext {
	
	/**
	 * Returns the root object or <code>null</code>
	 */
	Object getRoot();
	
	/**
	 * Returns the resolved variable or <code>null</code>.
	 */
	Object resolveVariable(String name);
	
	/**
	 * Returns <code>true</code> if the given variable was resolved.
	 */
	boolean isVariableResolved(String name);
	
	/**
	 * Returns the resolved method or <code>null</code>.
	 */
	ElMethod resolveMethod(Object owner, Class<?> cls,String name,Object[] args);
	
	/**
	 * Returns the resolved method or <code>null</code>.
	 */
	ElMethod resolveMethod(Class<?> cls,String name,Object[] args);
	
	/**
	 * Returns the resolved property or <code>null</code>.
	 */
	ElProperty resolveProperty(Object owner, Class<?> cls, String name);
	
	/**
	 * Returns the resolved static property(field) of the given class or <code>null</code>.
	 */
	ElProperty resolveProperty(Class<?> cls, String name);

	/**
	 * Returns the resolved function or <code>null</code>
	 */
	ElFunction resolveFunction(String fullName);
	
	/**
	 * Returns the item in the given array.
	 */
	Object getArrayItem(Object a,int index);
	
}