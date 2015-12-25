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
package leap.web.security.csrf;

import leap.web.Request;

public interface CsrfManager {
	
	/**
	 * Generates a new csrf token.
	 */
	String generateToken(Request request) throws Throwable;

	/**
	 * Loads the saved csrf token associated with current request..
	 */
	String loadToken(Request request) throws Throwable;
	
	/**
	 * Returns <code>true</code> if the given token is valid.
	 * 
	 * @throws CsrfTokenExpiredException if the given token was expired.
	 */
	boolean verifyToken(Request request, String token, CsrfToken expected) throws CsrfTokenExpiredException;

	/**
	 * Saves the csrf token.
	 */
	void saveToken(Request request, String token) throws Throwable;
	
	/**
	 * Removes the csrf token.
	 */
	void removeToken(Request request) throws Throwable;
}