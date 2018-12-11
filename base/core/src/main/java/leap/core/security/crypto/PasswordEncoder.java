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
package leap.core.security.crypto;

public interface PasswordEncoder {

	/**
	 * Encodes the plain password.
	 */
	String encode(String plainPassword);

    /**
     * Returns <code>true</code> if the plain password matches the encoded password.
     */
	boolean matches(String plainPassword,String encodedPassword);

    /**
     * Returns <code>true</code> if the string is an plain password.
     */
	default boolean isPlain(String s) {
	    return false;
    }

}