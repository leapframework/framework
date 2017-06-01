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
import leap.web.route.Route;
import leap.web.security.SecurityFailureHandler;
import leap.web.security.authc.AuthenticationContext;
import leap.web.security.authz.AuthorizationContext;
import leap.web.security.permission.PermissionManager;

public class DefaultSecuredPath implements SecuredPath {

	private static final Log log = LogFactory.get(DefaultSecuredPath.class);

    protected final Route                  route;
    protected final PathPattern            pattern;
    protected final Boolean                allowAnonymous;
    protected final Boolean                allowClientOnly;
    protected final Boolean                allowRememberMe;
    protected final SecurityFailureHandler failureHandler;
    protected final String[]               permissions;
    protected final String[]               roles;

	public DefaultSecuredPath(Route route,
                              PathPattern pattern,
                              Boolean allowAnonymous,
                              Boolean allowClientOnly,
                              Boolean allowRememberMe,
                              SecurityFailureHandler failureHandler,
                              String[] permissions,
                              String[] roles) {
		Args.notNull(pattern,"path pattern");
        this.route           = route;
		this.pattern         = pattern;
	    this.allowAnonymous  = allowAnonymous;
	    this.allowClientOnly = allowClientOnly;
	    this.allowRememberMe = allowRememberMe;
        this.failureHandler  = failureHandler;
        this.permissions     = permissions;
        this.roles           = roles;
    }

    @Override
    public Route getRoute() {
        return route;
    }

    /**
     * Returns the path pattern.
     */
	@Override
    public PathPattern getPattern() {
		return pattern;
	}

    @Override
    public Boolean getAllowAnonymous() {
        return allowAnonymous;
    }

    @Override
    public Boolean getAllowClientOnly() {
        return allowClientOnly;
    }

    @Override
    public Boolean getAllowRememberMe() {
        return allowRememberMe;
    }

    @Override
    public SecurityFailureHandler getFailureHandler() {
        return failureHandler;
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
                allow = pm.checkPermissionImpliesAll(grantedPermissions,permissions);
            }

            if(!allow) {
                allow = authz.hasAllPermission(permissions);
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