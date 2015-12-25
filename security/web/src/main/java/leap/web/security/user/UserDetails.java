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
package leap.web.security.user;


public interface UserDetails {

	Object getId();
	
	/**
	 * Returns the name for displaying in screen of this user. Cannot return <code>null</code>.
	 */
	String getName();
	
	/**
	 * Returns the user name used to authenticate the user. Cannot return <code>null</code>.
	 */
	String getLoginName();

    /**
     * Returns <code>true</code> if the user is enabled.
     */
    default boolean isEnabled() {
        return true;
    }

}