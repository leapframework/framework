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

import java.io.IOException;

import javax.servlet.ServletException;

import leap.core.security.Credentials;
import leap.core.web.RequestBase;
import leap.lang.Out;
import leap.lang.http.HTTP.Method;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.security.authc.AuthenticationContext;
import leap.web.security.authc.credentials.CredentialsResolver;

public class UsernamePasswordResolver implements CredentialsResolver {
	
	private static final Log log = LogFactory.get(UsernamePasswordResolver.class);
	
	protected String  usernameParameter = UsernamePasswordCredentials.USERNAME;
	protected String  passwordParameter = UsernamePasswordCredentials.PASSWORD;
	protected boolean passwordRequired  = true;

	public String getUsernameParameter() {
		return usernameParameter;
	}

	public void setUsernameParameter(String usernameParameter) {
		this.usernameParameter = usernameParameter;
	}

	public String getPasswordParameter() {
		return passwordParameter;
	}

	public void setPasswordParameter(String passwordParameter) {
		this.passwordParameter = passwordParameter;
	}
	
	public boolean isPasswordRequired() {
		return passwordRequired;
	}

	public void setPasswordRequired(boolean passwordRequired) {
		this.passwordRequired = passwordRequired;
	}

	@Override
    public boolean resolveCredentials(AuthenticationContext context, RequestBase request, Out<Credentials> out) throws ServletException, IOException {
		if(request.isMethod(Method.POST) && request.hasParameter(usernameParameter)){
			//Validates username
			String username = request.getParameter(usernameParameter);
			if(context.validation()
					   .required(usernameParameter, username)
					   .hasErrors()){
				return true;
			}
				
			//Validates password if necessary
			String password = request.getParameter(passwordParameter);
			if(passwordRequired && context.validation().required(passwordParameter, password).hasErrors()){
				return true;
			}
			
			log.debug("Resolved 'UsernamePasswordCredentials' : [username={}]",username);
			
			out.set(new SimpleUsernamePasswordCredentials(username, password));
			return true;
		}
		
		return false;
    }
}