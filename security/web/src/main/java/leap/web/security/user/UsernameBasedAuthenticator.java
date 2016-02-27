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

import leap.core.annotation.Inject;
import leap.core.validation.ValidationContext;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.security.SecurityConfig;

import java.util.Map;

public abstract class UsernameBasedAuthenticator {
	private static final Log log = LogFactory.get(UsernameBasedAuthenticator.class);

	public static final String USER_NOT_FOUND_MESSAGE_KEY     = "errors.user_not_found";
	public static final String USER_NOT_ENABLED_MESSAGE_KEY   = "errors.user_not_enabled";
	
	protected @Inject SecurityConfig sc;
	
	protected UserDetails resolveUserDetails(ValidationContext context, String username, Map<String, Object> params) {
		UserDetails details =
				sc.getUserStore().findUserDetailsByUsername(username);
		
		//User not found
		if(null == details){
			log.debug("User '{}' not found",username);
			context.validation().addError(UsernamePasswordCredentials.USERNAME, USER_NOT_FOUND_MESSAGE_KEY, "User not found");
			return null;
		}
		
		//Check enabled
		if(!details.isEnabled()){
			log.debug("User '{}' was disabled",username);
			context.validation().addError(UsernamePasswordCredentials.USERNAME, USER_NOT_ENABLED_MESSAGE_KEY,"User was disabled");
			return null;
		}
		
		return details;
	}

}
