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
	
	protected static final String CONTEXT_ATTRIBUTE_NAME = SecurityContext.class.getName();

    /**
     * Returns the instance of {@link SecurityContext} in current request.
     *
     * @throws IllegalStateException if the context not exists in current request.
     */
	public static SecurityContext current() {
		SecurityContext context = (SecurityContext)RequestContext.current().getAttribute(CONTEXT_ATTRIBUTE_NAME);
		
		if(null == context){
			throw new IllegalStateException("Current security context does not exists");
		}
		
		return context;
	}

    /**
     * Returns the instance of {@link SecurityContext} in current request of returns <code>null</code> if the context not exists.
     */
    public static SecurityContext tryGetCurrent(){
        RequestContext rc = RequestContext.tryGetCurrent();
        if(null == rc) {
            return null;
        }else{
            return (SecurityContext)rc.getAttribute(CONTEXT_ATTRIBUTE_NAME);
        }
    }

    /**
     * Returns the {@link UserPrincipal} in current request.
     */
    public static UserPrincipal user() {
		return current().getUser();
	}

    /**
     * Returns the {@link Authentication} in current request.
     */
    public static Authentication authentication() {
        return current().authentication;
    }

    /**
     * Returns the {@link Authorization} in current request.
     */
    public static Authorization authorization() {
        return current().getAuthorization();
    }

	protected Authentication authentication;
	protected Authorization  authorization;
	protected String         accessMode;
	protected Object         accessModeContext;

    /**
     * Required. Returns the {@link Authentication}.
     */
    public Authentication getAuthentication() {
        return authentication;
    }

    /**
     * Required. Returns the {@link Authorization}.
     */
    public Authorization getAuthorization() {
        return authorization;
    }

    /**
     * Required. Returns the {@link UserPrincipal}.
     */
    public UserPrincipal getUser() {
        return null == authentication ? null : authentication.getUser();
    }

    /**
     * Optional. Returns the {@link ClientPrincipal}.
     */
    public ClientPrincipal getClient() {
        return null == authentication ? null : authentication.getClient();
    }

    /**
     * Returns access mode.
     */
    public String getAccessMode() {
        return accessMode;
    }

    /**
     * Sets access mode.
     */
    public void setAccessMode(String accessMode) {
        this.accessMode = accessMode;
    }

    /**
     * Returns context of access mode.
     */
    public Object getAccessModeContext() {
        return accessModeContext;
    }

    /**
     * Sets context of access mode.
     */
    public void setAccessModeContext(Object accessModeContext) {
        this.accessModeContext = accessModeContext;
    }
}