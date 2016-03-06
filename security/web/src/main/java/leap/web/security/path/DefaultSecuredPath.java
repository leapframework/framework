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
package leap.web.security.path;

import leap.core.security.Authentication;
import leap.core.security.Authorization;
import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.PathPattern;
import leap.web.Request;
import leap.web.security.authc.AuthenticationContext;
import leap.web.security.authz.AuthorizationContext;
import leap.web.security.permission.PermissionManager;

public class DefaultSecuredPath implements SecuredPath {

	private static final Log log = LogFactory.get(DefaultSecuredPath.class);

	protected final PathPattern pattern;
	protected final boolean     allowAnonymous;
	protected final boolean     allowClientOnly;
	protected final boolean     allowRememberMe;
    protected final String[]    permissions;
    protected final String[]    roles;

	public DefaultSecuredPath(PathPattern pattern,
                              boolean allowAnonymous,
                              boolean allowClientOnly,
                              boolean allowRememberMe,
                              String[] permissions,
                              String[] roles) {
		Args.notNull(pattern,"path pattern");
		this.pattern         = pattern;
	    this.allowAnonymous  = allowAnonymous;
	    this.allowClientOnly = allowClientOnly;
	    this.allowRememberMe = allowRememberMe;
        this.permissions     = permissions;
        this.roles           = roles;
    }

    /**
     * Returns the path pattern.
     */
	@Override
    public PathPattern getPattern() {
		return pattern;
	}

    /**
     * Returns true if the path pattern allows anonymous access (that means no authentication required).
     */
    @Override
    public boolean isAllowAnonymous() {
        return allowAnonymous;
    }

    /**
     * Returns true if the path pattern allows client-only authentication.
     */
    @Override
    public boolean isAllowClientOnly() {
        return allowClientOnly;
    }

    /**
     * Returns true if the path pattern allows remember-me authentication.
     */
    @Override
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

    /**
     * Returns true if the path allows the authentication.
     */
	@Override
    public boolean checkAuthentication(Request request, AuthenticationContext context) {
		if(isAllowAnonymous()) {
			return true;
		}

        Authentication authc = context.getAuthentication();
		
		if(!authc.isAuthenticated()){
            log.debug("path [{}] : not authenticated, deny the request.", pattern);
			return false;
		}
		
		if(authc.isRememberMe() && !isAllowRememberMe()){
            log.debug("path [{}] : remember-me authentication not allowed.", pattern);
			return false;
		}
		
        if (authc.isClientOnly() && !isAllowClientOnly()) {
            log.debug("path [{}] : client-only authentication not allowed.", pattern);
            return false;
        }

		return true;
	}

    /**
     * Returns true if the path allows the authorization.
     */
    @Override
    public boolean checkAuthorization(Request request, AuthorizationContext context) {
        Authentication authc = context.getAuthentication();
        Authorization  authz = context.getAuthorization();

        //Check roles
        if(roles.length > 0) {
            boolean allow = false;
            String[] grantedRoles = authc.getRoles();
            if(null != grantedRoles && grantedRoles.length > 0) {
                allow = Arrays2.containsAny(grantedRoles,roles);
            }

            if(!allow) {
                allow = authz.hasAnyRole(roles);
            }

            if(!allow) {
                return false;
            }
        }

        //Check permissions
        if(permissions.length > 0) {
            PermissionManager pm = context.getPermissionManager();

            boolean allow = false;
            String[] grantedPermissions = authc.getPermissions();
            if(null != grantedPermissions && grantedPermissions.length > 0) {
                allow = pm.checkPermissionImpliesAny(grantedPermissions,permissions);
            }

            if(!allow) {
                allow = authz.hasAnyPermission(permissions);
            }

            if(!allow) {
                return false;
            }
        }

        return true;
    }

	@Override
    public int compareTo(SecuredPath o) {
	    return COMPARATOR.compare(this, o);
    }
}