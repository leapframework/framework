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
package leap.web.security.login;

import leap.core.annotation.Inject;
import leap.core.security.Credentials;
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityContextHolder;
import leap.web.security.authc.Authentication;
import leap.web.security.authc.AuthenticationException;
import leap.web.security.authc.AuthenticationManager;

public class CredentialsAuthenticationHandler implements LoginHandler {
	
    private static final Log log = LogFactory.get(CredentialsAuthenticationHandler.class);
    
	protected @Inject AuthenticationManager authenticationManager;
	
	public AuthenticationManager getAuthenticationManager() {
		return authenticationManager;
	}

	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	@Override
    public State handleLoginAuthentication(Request request, Response response, SecurityContextHolder context) throws Throwable {
	    LoginContext sc = context.getLoginContext();
	    
		if(!sc.isError()) {
			Credentials credentials = sc.getCredentials();
			if(null != credentials){
				try {
					Authentication authc = 
					        authenticationManager.authenticate(context, credentials);
					
		            if(null != authc){
		            	sc.setUser(authc.getUserPrincipal());
		            }
		            
	            } catch (AuthenticationException e) {
	            	//TODO : handle authentication exception
	                log.error(e.getMessage(), e);
	            }
			}
		}
		
	    return State.CONTINUE;
    }

}