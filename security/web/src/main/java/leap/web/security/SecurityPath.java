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
package leap.web.security;

import leap.core.web.RequestBase;
import leap.core.web.RequestMatcher;
import leap.lang.Args;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.PathPattern;
import leap.web.Request;
import leap.core.security.Authentication;
import leap.web.security.authc.AuthenticationContext;

import java.util.Comparator;

public class SecurityPath implements RequestMatcher,Comparable<SecurityPath> {

	private static final Log log = LogFactory.get(SecurityPath.class);

    public static Comparator<SecurityPath> COMPARATOR = (o1, o2) -> {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }

        int i = o1.getPathPatternString().length() - o2.getPathPatternString().length();
        if(i != 0){
            return i;
        }

        return -1;
    };

	protected final PathPattern    pathPattern;
	protected final boolean        allowAnonymous;
	protected final boolean        allowClientOnly;
	protected final boolean        allowRememberMe;
    protected final String[]       permissions;
    protected final String[]       roles;

	protected final SecurityRule[] rules;
	
	public SecurityPath(PathPattern pathPattern,
                        boolean allowAnonymous,
                        boolean allowClientOnly,
                        boolean allowRememberMe,
                        String[] permissions,
                        String[] roles,
                        SecurityRule[] rules) {
		Args.notNull(pathPattern,"path pattern");
		this.pathPattern	 = pathPattern;
	    this.allowAnonymous  = allowAnonymous;
	    this.allowClientOnly = allowClientOnly;
	    this.allowRememberMe = allowRememberMe;
        this.permissions     = permissions;
        this.roles           = roles;
	    this.rules           = rules;
    }

    /**
     * Returns the path pattern.
     */
	public PathPattern getPathPattern() {
		return pathPattern;
	}

    /**
     * Returns the path pattern as string.
     */
	public String getPathPatternString(){
		return pathPattern.getPattern();
	}

    /**
     * Returns true if the path pattern allows anonymous access (that means no authentication required).
     */
    public boolean isAllowAnonymous() {
        return allowAnonymous;
    }

    /**
     * Returns true if the path pattern allows client-only authentication.
     */
    public boolean isAllowClientOnly() {
        return allowClientOnly;
    }

    /**
     * Returns true if the path pattern allows remember-me authentication.
     */
    public boolean isAllowRememberMe() {
        return allowRememberMe;
    }

    /**
     * Returns the permissions allowed.
     */
    public String[] getPermissions() {
        return permissions;
    }

    /**
     * Returns the roles allowed.
     */
    public String[] getRoles() {
        return roles;
    }

    @Override
    public boolean matches(RequestBase request) {
	    return pathPattern.matches(request.getPath());
    }

    /**
     * Returns true if the path pattern matches the given path.
     */
	public boolean matches(String path) {
		return pathPattern.matches(path);
	}

    /**
     * Returns true if the path pattern allows the authentication.
     */
	public boolean checkAuthentication(Request request, AuthenticationContext context) {
		if(isAllowAnonymous()) {
			return true;
		}

        Authentication authc = context.getAuthentication();
		
		if(!authc.isAuthenticated()){
            log.debug("path [{}] : not authenticated, deny the request.", pathPattern);
			return false;
		}
		
		if(authc.isRememberMe() && !isAllowRememberMe()){
            log.debug("path [{}] : remember-me authentication not allowed.", pathPattern);
			return false;
		}
		
        if (authc.isClientOnly() && !isAllowClientOnly()) {
            log.debug("path [{}] : client-only authentication not allowed.", pathPattern);
            return false;
        }

		return applyRules(request, context, authc);
	}
	
	private boolean applyRules(Request request, AuthenticationContext context, Authentication authc)  {
		
		if(null != rules && rules.length > 0){
			for(int i=0;i<rules.length;i++) {
				if(!rules[i].apply(request, context, authc)) {
					return false;
				}
			}
		}
		
	    return true;
    }

	@Override
    public int compareTo(SecurityPath o) {
	    return COMPARATOR.compare(this, o);
    }
}