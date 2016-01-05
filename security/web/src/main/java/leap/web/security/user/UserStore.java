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

import java.util.Map;

import leap.lang.Assert;
import leap.lang.annotation.Nullable;

public interface UserStore {

	/**
	 * Returns the id as {@link Object} type from string type.
     */
	default Object idFromString(String idString) {
		return idString;
	}

	/**
	 * Optional. Returns the {@link UserAccount} by searching the given username.
     */
	default UserAccount findUserAccount(String username){
		return findUserAccount(username,null);
	}
	
	/**
	 * Optional. Returns the {@link UserAccount} by searching the given username.
	 */
	UserAccount findUserAccount(String username,@Nullable Map<String, Object> parameters);
	
	/**
	 * Returns the {@link UserDetails} or <code>null</code>.
	 */
	UserDetails findUserDetails(Object userId);

	/**
	 * @see {@link #findUserDetails(Object)}
	 */
	default UserDetails findUserDetailsByIdString(String idString) {
		return findUserDetails(idFromString(idString));
	}

	/**
	 * Finds the {@link UserDetails} by the login name of user.
	 */
	default UserDetails findUserDetailsByUsername(String username) {
	    throw new IllegalStateException("Not implemented");
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
		UserDetails ud = findUserDetails(userId);
		
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
		return findAndCheckUserDetails(idFromString(idString));
	}
}