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
package leap.core.security;

import leap.core.RequestContext;

public abstract class SecurityContext {
	
	public static final String CONTEXT_ATTRIBUTE_NAME = "REQ_" + SecurityContext.class.getName();

	public static SecurityContext current() {
		SecurityContext context = (SecurityContext)RequestContext.current().getAttribute(CONTEXT_ATTRIBUTE_NAME);
		
		if(null == context){
			throw new IllegalStateException("Current security context does not exists");
		}
		
		return context;
	}
	
	public static UserPrincipal currentUser() {
		return current().getUser();
	}
	
	public static SecurityContext tryGetCurrent(){
		return (SecurityContext)RequestContext.current().getAttribute(CONTEXT_ATTRIBUTE_NAME);
	}
	
	public static void setCurrent(SecurityContext context){
		RequestContext.current().setAttribute(CONTEXT_ATTRIBUTE_NAME, context);
	}
	
	public static void removeCurrent(){
		RequestContext.current().removeAttribute(CONTEXT_ATTRIBUTE_NAME);
	}

	protected Authentication authentication;

    public Authentication getAuthentication() {
        return authentication;
    }

    public UserPrincipal getUser() {
        return null == authentication ? null : authentication.getUser();
    }

    public ClientPrincipal getClient() {
        return null == authentication ? null : authentication.getClient();
    }
}