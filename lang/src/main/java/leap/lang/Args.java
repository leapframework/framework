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


public class Args {
	
	/**
	 * Ensures the truth of an expression involving one or more parameters to the calling method.
	 * 
	 * @param expression a boolean expression
	 * @param message argument name
	 * @throws IllegalArgumentException if {@code expression} is false
	 */
	public static void assertTrue(boolean expression,String message) {
		if (!expression) {
			throw new IllegalArgumentException(message);
		}
	}
	
	/**
	 * Ensures not the truth of an expression involving one or more parameters to the calling method.
	 * 
	 * @param expression a boolean expression
	 * @param message argument name
	 * @throws IllegalArgumentException if {@code expression} is true
	 */
	public static void assertFalse(boolean expression,String message) {
		if (expression) {
			throw new IllegalArgumentException(message);
		}
	}
	
	/**
	 * Ensures that an object reference passed as a parameter to the calling method is not null.
	 * 
	 * @param reference an object reference
	 * @return the non-null reference that was validated
	 * @throws IllegalArgumentException if {@code reference} is null
	 */
	public static <T> T notNull(T reference) {
		if (reference == null) {
			throw new IllegalArgumentException("argument must not be null");
		}
		return reference;
	}
	
	/**
	 * Ensures that an object reference passed as a parameter to the calling method is not null.
	 * 
	 * @param reference an object reference
	 * @param name argument name
	 * @return the non-null reference that was validated
	 * @throws IllegalArgumentException if {@code reference} is null
	 */
	public static <T> T notNull(T reference,String name) {
		if (reference == null) {
			throw new IllegalArgumentException("argument '" + name + "' must not be null");
		}
		return reference;
	}
	
	public static String notEmpty(String s) {
		if(null == s || s.isEmpty()) {
			throw new IllegalArgumentException("Argument must not be null or empty");
		}
		return s;
	}
	
	public static String notEmpty(String s, String name) {
		if(null == s || s.isEmpty()) {
			throw new IllegalArgumentException("Argument '" + name + "' must not be null or empty");
		}
		return s;
	}
	
	/**
	 * Ensures that an object reference passed as a parameter to the calling method is not null or empty.
	 * 
	 * @param reference an object reference
	 * @return the non-null reference that was validated
	 * @throws IllegalArgumentException if {@code reference} is null
	 */
	public static <T> T notEmpty(T reference) {
		if (Objects2.isEmpty(reference)) {
			throw new IllegalArgumentException("argument must not be null or empty");
		}
		return reference;
	}
	
	/**
	 * Ensures that an object reference passed as a parameter to the calling method is not null or empty.
	 * 
	 * @param reference an object reference
	 * @param name argument name
	 * @return the non-null reference that was validated
	 * @throws IllegalArgumentException if {@code reference} is null
	 */
	public static <T> T notEmpty(T reference,String name) {
		if (Objects2.isEmpty(reference)) {
			throw new IllegalArgumentException("argument '" + name + "' must not be null or empty");
		}
		return reference;
	}

	protected Args(){
		
	}
}
