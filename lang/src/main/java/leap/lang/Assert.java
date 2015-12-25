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


public class Assert {

	/**
	 * throw an {@link IllegalStateException}
	 */
	public static void fail(String message) throws IllegalStateException {
		throw new IllegalStateException(message);
	}
	
	/**
	 * throw an {@link IllegalStateException}
	 * 
	 * @see {@link Strings#format(String, Object...)} 
	 */
	public static void fail(String errorMessageTemplate,Object... errorMessageArgs) throws IllegalStateException {
		throw new IllegalStateException(Strings.format(errorMessageTemplate, errorMessageArgs));
	}
	
	/**
	 * throw an {@link IllegalStateException} if the input is null
	 */
	public static <T> T  notNull(T object) throws IllegalStateException{
		if (object == null) {
			throw new IllegalStateException("object must not be null");
		}
		return object;
	}
	
	/**
	 * throw an {@link IllegalStateException} if the input is null
	 */
	public static <T> T notNull(T object,String errorMessage) throws IllegalStateException{
		if (object == null) {
			throw new IllegalStateException(errorMessage);
		}
		return object;
	}
	
	/**
	 * throw an {@link IllegalStateException} if the input is null
	 * 
	 * @see {@link Strings#format(String, Object...)}
	 */
	public static <T> T notNull(T object,String errorMessageTemplate,Object... errorMessageArgs) throws IllegalStateException{
		if (object == null) {
			throw new IllegalStateException(Strings.format(errorMessageTemplate, errorMessageArgs));
		}
		return object;
	}
	
	/**
	 * throw an {@link IllegalStateException} if the input is not null
	 */
	public static <T> T  isNull(T object) throws IllegalStateException{
		if (object != null) {
			throw new IllegalStateException("object must be null");
		}
		return object;
	}
	
	/**
	 * throw an {@link IllegalStateException} if the input is not null
	 */
	public static <T> T isNull(T object,String errorMessage) throws IllegalStateException{
		if (object != null) {
			throw new IllegalStateException(errorMessage);
		}
		return object;
	}
	
	/**
	 * throw an {@link IllegalStateException} if the input is not null
	 * 
	 * @see {@link Strings#format(String, Object...)}
	 */
	public static <T> T isNull(T object,String errorMessageTemplate,Object... errorMessageArgs) throws IllegalStateException{
		if (object != null) {
			throw new IllegalStateException(Strings.format(errorMessageTemplate, errorMessageArgs));
		}
		return object;
	}
	
	/**
	 * throw an {@link IllegalStateException} if the input is null or empty.
	 * 
	 * @see Objects2#isEmpty(Object)
	 */
	public static <T> T notEmpty(T object, String errorMessage) throws IllegalStateException{
		if (Objects2.isEmpty(object)) {
			throw new IllegalStateException(errorMessage);
		}
		return object;
	}
	
	/**
	 * throw an {@link IllegalStateException} if the input is null or empty.
	 * 
	 * @see Objects2#isEmpty(Object)
	 */
	public static <T> T notEmpty(T object, String errorMessageTemplate,Object... errorMessageArgs) throws IllegalStateException{
		if (Objects2.isEmpty(object)) {
			throw new IllegalStateException(Strings.format(errorMessageTemplate, errorMessageArgs));
		}
		return object;
	}
	
	public static void isTrue(boolean expression) throws IllegalStateException{
		isTrue(expression, "the expression must be true");
	}
	
	public static void isTrue(boolean expression, String message) throws IllegalStateException {
		if (!expression) {
			throw new IllegalStateException(message);
		}
	}
	
	public static void isTrue(boolean expression, String errorMessageTemplate,Object... errorMessageArgs) throws IllegalStateException {
		if (!expression) {
			throw new IllegalStateException(Strings.format(errorMessageTemplate, errorMessageArgs));
		}
	}
	
	public static void isFalse(boolean expression) throws IllegalStateException{
		isFalse(expression, "the expression must be false");
	}
	
	public static void isFalse(boolean expression, String message) throws IllegalStateException {
		if (expression) {
			throw new IllegalStateException(message);
		}
	}
	
	protected Assert(){
		
	}
}
