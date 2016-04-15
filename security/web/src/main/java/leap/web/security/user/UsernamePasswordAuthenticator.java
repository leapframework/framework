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

import leap.core.annotation.Inject;
import leap.core.security.Credentials;
import leap.core.security.UserPrincipal;
import leap.lang.Out;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.security.SecurityConfig;
import leap.web.security.authc.AuthenticationException;
import leap.web.security.authc.credentials.CredentialsAuthenticationContext;
import leap.web.security.authc.credentials.CredentialsAuthenticator;

public class UsernamePasswordAuthenticator extends UsernameBasedAuthenticator implements CredentialsAuthenticator {
	
	private static final Log log = LogFactory.get(UsernamePasswordAuthenticator.class);
	
	public static final String INCORRECT_PASSWORD_MESSAGE_KEY = "errors.incorrect_password";

	protected @Inject SecurityConfig sc;
	
	@Override
    public boolean authenticate(CredentialsAuthenticationContext context, 
    							Credentials credentials, 
    							Out<UserPrincipal> principal) throws AuthenticationException {
		
		if(credentials instanceof UsernamePasswordCredentials){
			UsernamePasswordCredentials usernamePassword = (UsernamePasswordCredentials)credentials;
			
			UserDetails details = resolveUserDetails(context, usernamePassword.getUsername(), usernamePassword.getParameters());
			if(null == details) {
				return true;
			}
			
			//Check password
			String rawPassword = Strings.nullToEmpty(usernamePassword.getPassword());
			if(!sc.getPasswordEncoder().matches(rawPassword, details.getPassword())){
				log.debug("Incorrect password of user '{}'",usernamePassword.getUsername());
				context.validation().addError(UsernamePasswordCredentials.PASSWORD, INCORRECT_PASSWORD_MESSAGE_KEY,"Incorrect password");
			}else{
				principal.set(details);
			}

			return true;
		}
		
		return false;
    }
}