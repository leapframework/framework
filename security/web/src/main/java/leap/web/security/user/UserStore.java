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

import leap.lang.Assert;

public interface UserStore {

	/**
	 * Returns the id as {@link Object} type from {@link String} type.
     */
	default Object getObjectId(String idString) {
		return idString;
	}

	/**
	 * Returns the {@link UserDetails} or <code>null</code>.
	 */
	UserDetails loadUserDetailsById(Object userId);

	/**
	 * Finds the {@link UserDetails} by the login name of user.
	 */
	UserDetails loadUserDetailsByLoginName(String loginName);

	/**
	 * @see {@link #loadUserDetailsById(Object)}
	 */
	default UserDetails loadUserDetailsByIdString(String idString) {
		return loadUserDetailsById(getObjectId(idString));
	}

	/**
	 * Returns the {@link UserDetails} or <code>null</code>.
	 * 
	 * <p>
	 * Checks the returned {@link UserDetails} is not <code>null</code>.
	 * 
	 * @throws IllegalStateException if {@link UserDetails#getLoginName()} or {@link UserDetails#getName()} is null.
	 */
	default UserDetails findAndCheckUserDetails(Object userId) throws IllegalStateException {
		UserDetails ud = loadUserDetailsById(userId);
		
		if(null != ud) {
		    Assert.notNull(ud.getName(), "The 'name' in 'UserDetails:" + ud.getClass() + "' cannot be null");
			Assert.notNull(ud.getLoginName(),"The 'loginName' in 'UserDetails:" + ud.getClass() + "' cannot be null");
		}
		
		return ud;
	}

	/**
	 * @see {@link #findAndCheckUserDetails(Object)}
     */
	default UserDetails findAndCheckUserDetailsByIdString(String idString) throws IllegalStateException {
		return findAndCheckUserDetails(getObjectId(idString));
	}
}