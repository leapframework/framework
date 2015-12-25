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

import leap.core.AppContext;
import leap.lang.accessor.PropertyGetter;
import leap.lang.text.DefaultPlaceholderResolver;
import leap.lang.text.PlaceholderResolver;

public class ENV {
	
	private static final PropertyGetter envPropertyGetter = new PropertyGetter() {
		@Override
		public String getProperty(String name) {
			return ENV.getString(name);
		}
	};
	
	private static final PlaceholderResolver placeHolderResolver = new DefaultPlaceholderResolver(envPropertyGetter);
	
	public static String PREFIX = "env.";
	
	/**
	 * Returns current {@link VariableEnvironment} object contains all the variables in current application.
	 */
	public static VariableEnvironment container(){
		return AppContext.factory().getBean(VariableEnvironment.class);
	}
	
	/**
	 * Returns a string which has been replaced all the variable placeholders in the given text.
	 * 
	 * <p>
	 * A variable placeholder is a variable name wrapped in ${ .. }.
	 * 
	 * <p>
	 * Example : 
	 * 
	 * <pre>
	 * 	Env.parse("hello ${name}") will return the string "hello world" if the variable 'name' resolved as 'world'.
	 * </pre>
	 * 
	 * <p>
	 * Returns <code>null</code> if the given text is <code>null</code>
	 */
	public static String parse(String text){
		if(null == text){
			return null;
		}
		return placeHolderResolver.resolveString(text);
	}
	
	/**
	 * Checks is the given variable name exists.
	 * 
	 * <p>
	 * Returns <code>true</code> if the given variable name is exists.
	 */
	public static boolean exists(String variable){
		return container().checkVariableExists(variable);
	}
	
	/**
	 * Returns the value of the given variable name.
	 * 
	 * <p>
	 * Returns <code>null</code> if the given variable name not exists.
	 */
	public static Object get(String variable) {
		return container().resolveVariable(variable);
	}
	
	/**
	 * Returns a {@link String} object converted from the value of the given variable name.
	 */
	public static String getString(String variable){
		return container().resolveVariableAsString(variable);
	}
	
	protected ENV(){
		
	}
}
