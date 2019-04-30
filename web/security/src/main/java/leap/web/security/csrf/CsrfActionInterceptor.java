/*
 * Copyright 2014 the original author or authors.
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
package leap.web.security.csrf;

import leap.core.annotation.Inject;
import leap.core.validation.Validation;
import leap.lang.Strings;
import leap.lang.http.HTTP;
import leap.lang.intercepting.State;
import leap.web.Request;
import leap.web.action.ActionContext;
import leap.web.action.ActionInterceptor;
import leap.web.route.Route;
import leap.web.security.SecurityConfig;

public class CsrfActionInterceptor implements ActionInterceptor {
	
	protected @Inject SecurityConfig securityConfig;
	protected @Inject CsrfManager	 csrfManager;
	
	@Override
    public State preExecuteAction(ActionContext context, Validation validation) throws Throwable {
		if(!isEnabled(context)) {
			return State.CONTINUE;
		}
		
		Request request = context.getRequest();
		
		//Ignore GET request
		if(request.isMethod(HTTP.Method.GET)) {
			return State.CONTINUE;
		}
		
		//Check ignored
		if(CSRF.isIgnored(request.getServletRequest())) {
			return State.CONTINUE;
		}
		
		CsrfToken token = CSRF.getGeneratedToken(request);
		
		checkCsrfToken(request, token);
		
		return State.CONTINUE;
    }
	
	protected boolean isEnabled(ActionContext context) {
		if(!securityConfig.isEnabled()) {
			return false;
		}

		Route route = context.getRoute();
		if(route.isCsrfEnabled()) {
			return true;
		}
		if(route.isCsrfDisabled()) {
			return false;
		}
		return securityConfig.isCsrfEnabled();
	}
	
	protected void checkCsrfToken(Request request, CsrfToken expected) throws Throwable {
		String requestedToken = getCsrfTokenString(request);
		
		if(!csrfManager.verifyToken(request, requestedToken, expected)) {
			if(expected.isNew()) {
				throw new MissingCsrfTokenException("Expected CSRF token not found. Has your session expired?");
			}else{
				throw new InvalidCsrfTokenException("Invalid CSRF Token '" + requestedToken + 
													"' was found on the request parameter '" + 
													securityConfig.getCsrfParameterName() + "' or header '" +
													securityConfig.getCsrfHeaderName() + "'.");
			}
		}
	}
	
	protected String getCsrfTokenString(Request request) {
		String token = request.getHeader(securityConfig.getCsrfHeaderName());
		if(Strings.isEmpty(token)) {
			token = request.getParameter(securityConfig.getCsrfParameterName());
			if(Strings.isEmpty(token)) {
				token = CSRF.getRequestToken(request);
			}
		}
		return token;
	}
}