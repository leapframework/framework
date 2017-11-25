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
package leap.web.security;

import leap.core.RequestContext;
import leap.web.action.ActionContext;
import leap.web.route.Route;
import leap.web.security.authc.AuthenticationContext;
import leap.web.security.authz.AuthorizationContext;
import leap.web.security.login.LoginContext;
import leap.web.security.logout.LogoutContext;
import leap.web.security.path.SecuredPath;

public interface SecurityContextHolder extends AuthenticationContext, AuthorizationContext {

    String CONTEXT_HOLDER_ATTRIBUTE_NAME = SecurityContextHolder.class.getName();

    static SecurityContextHolder current() {
		SecurityContextHolder context = (SecurityContextHolder)RequestContext.current().getAttribute(CONTEXT_HOLDER_ATTRIBUTE_NAME);
		
		if(null == context){
			throw new IllegalStateException("Current security context holder does not exists");
		}
		
		return context;
	}

    /**
     * Optional.
     */
    ActionContext getActionContext();

    /**
     * Required.
     */
    LoginContext getLoginContext();
    
    /**
     * Required.
     */
    LogoutContext getLogoutContext();

}