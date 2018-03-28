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
import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.PathPattern;
import leap.web.action.ActionContext;
import leap.web.route.Route;
import leap.web.security.SecuredObject;
import leap.web.security.SecuredObjectBase;
import leap.web.security.SecurityContextHolder;
import leap.web.security.SecurityFailureHandler;

public class DefaultSecuredPath extends SecuredObjectBase implements SecuredPath {

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
    public Boolean tryCheckAuthentication(SecurityContextHolder context) {
		if(isAllowAnonymous()) {
			return true;
		}

        Authentication authc = context.getAuthentication();

        //check route's config
        ActionContext ac = context.getActionContext();
        if(null != ac && null != ac.getRoute()) {
            SecuredRoute sr = new SecuredRoute(ac.getRoute());
            context.setSecuredObject(sr);
            Boolean result = sr.tryCheckAuthentication(context);
            if(null != result) {
                return result;
            }
        }

		if(authc == null || !authc.isAuthenticated()){
            log.debug("path [{}] : not authenticated, deny the request.", pattern);
			return false;
		}
		
		if(authc.isRememberMe() && !isAllowRememberMe()){
            log.debug("path [{}] : remember-me authentication not allowed.", pattern);
			return false;
		}
		
        if (authc.isClientOnly() && !isAllowClientOnly()) {
            log.debug("path [{}] : client-only authentication not allowed.", pattern);
            context.setDenyMessage("client only authentication not allowed");
            return false;
        }

		return true;
	}

    /**
     * Returns true if the path allows the authorization.
     */
    @Override
    public Boolean tryCheckAuthorization(SecurityContextHolder context) {
        //Check roles
        if(!checkRoles(context, roles)) {
            context.setDenyMessage("Roles [" + Strings.join(roles, ',') + "] required");
            return false;
        }

        //Check permissions
        if(!checkPermissions(context, permissions)) {
            context.setDenyMessage("Permissions [" + Strings.join(permissions, ',') + "] required");
            return false;
        }

        //check secured object
        SecuredObject so = context.getSecuredObject();
        return null == so ? true : so.checkAuthorization(context);
    }

}