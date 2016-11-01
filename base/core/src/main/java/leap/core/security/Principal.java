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
package leap.core.security;

import leap.lang.convert.Converts;

import java.io.Serializable;

public interface Principal extends Serializable {
	
	/**
	 * Returns the id of principal.
	 */
	Object getId();

	/**
	 * Returns the id as {@link String} type.
     */
	default String getIdAsString() {
		Object id = getId();
		return null == id ? null : Converts.toString(id);
	}

	/**
	 * Returns <code>true</code> if the principal is anonymous (not authenticated).
	 */
	default boolean isAnonymous() {
        return false;
    }

	/**
	 * Refresh this principal in memory, override it when we need refresh this principal
	 * from db or other storage
	 */
	default void refresh(){}
}