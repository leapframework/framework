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
package leap.core.el;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import leap.lang.el.ElFunction;

public interface ElConfig {

	String FUNCTION_NAME_SEPERATOR = ":";
	
	/**
	 * Returns the registered packages.
	 */
	List<String> getImportedPackages();

	/**
	 * Returns the registered variables map.
	 */
	Map<String, Object> getRegisteredVariables();

	/**
	 * Returns the registered functions map.
	 */
	Map<String, ElFunction> getRegisteredFunctions();
	
	/**
	 * Returns <code>true</code> if the given package name was imported.
	 */
	boolean isPackageImported(String name);
	
	/**
	 * Returns <code>true</code> if the given variable name aleady registered.
	 */
	boolean isVariableRegistered(String name);

	/**
	 * Returns <code>true
	 */
	boolean isFunctionRegistered(String prefix, String name);
	
	/**
	 * Imports a package.
	 * 
	 * <p>
	 * An imported package must not ends with ".", such as "java.lang".
	 */
	ElConfig importPackage(String packageName);

	/**
	 * Register a global variable.
	 */
	ElConfig registerVariable(String name, Object value);

	/**
	 * Register a public static method as function.
	 */
	void registerFunction(String prefix, String name, Method m);
	
	/**
	 * Register a function.
	 */
	void registerFunction(String prefix, String name, ElFunction func);

	/**
	 * Register filtered methods in the give class as functions.
	 * 
	 * <p>
	 * The methods are not public and static will be removed default.
	 */
	void registerFunctions(String prefix, Class<?> c, Predicate<Method> filter);
}