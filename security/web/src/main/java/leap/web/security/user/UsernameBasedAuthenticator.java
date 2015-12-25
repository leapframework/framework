/*
 * Copyright 2015 the original author or authors.
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

import leap.core.annotation.Inject;
import leap.core.security.UserPrincipal;
import leap.core.validation.ValidationContext;
import leap.lang.Assert;
import leap.lang.Out;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.security.SecurityConfig;


public abstract class UsernameBasedAuthenticator {
	private static final Log log = LogFactory.get(UsernameBasedAuthenticator.class);

	public static final String USER_NOT_FOUND_MESSAGE_KEY     = "errors.user_not_found";
	public static final String USER_NOT_ENABLED_MESSAGE_KEY   = "errors.user_not_enabled";
	
	protected @Inject SecurityConfig sc;
	
	protected UserAccount resolveUserAccount(ValidationContext context, String username, Map<String, Object> params) {
		UserAccount account = 
				sc.getUserStore().findUserAccount(username,params);
		
		//User not found
		if(null == account){
			log.debug("User '{}' not found",username);
			context.validation().addError(UsernamePasswordCredentials.USERNAME, USER_NOT_FOUND_MESSAGE_KEY, "User not found");
			return null;
		}
		
		//Check enabled
		if(!account.isEnabled()){
			log.debug("User '{}' was disabled",username);
			context.validation().addError(UsernamePasswordCredentials.USERNAME, USER_NOT_ENABLED_MESSAGE_KEY,"User was disabled");
			return null;
		}
		
		return account;
	}
	
	protected boolean resolveUserPrincipal(String username, UserAccount account, Out<UserPrincipal> principal) {
		//Authenticated
		Assert.notEmpty(account.getId(), "The value of 'userId' property in the returned user account must not be empty");
		
		UserDetails details = sc.getUserStore().findAndCheckUserDetails(account.getId());
		
		Assert.notNull(details,"Cannot found the user details of user id '" + account.getId() + 
								   "', username : " + username);
		
		log.debug("User '{}' authenticated successfully!",username);
		
		principal.set(new SimpleUserDetailsPrincipal(account, details, true));
		return true;
	}
	
}
