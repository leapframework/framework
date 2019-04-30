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

import leap.lang.Arrays2;
import leap.lang.Collections2;
import leap.lang.enums.Bool;
import leap.lang.path.AntPathPattern;
import leap.lang.path.PathPattern;
import leap.web.route.Route;
import leap.web.security.SecurityFailureHandler;

import java.util.ArrayList;
import java.util.List;

public class DefaultSecuredPathBuilder implements SecuredPathBuilder {

    protected Object      source;
    protected Route       route;
    protected PathPattern pattern;

    protected Boolean                allowAnonymous  = null;
    protected Boolean                allowClientOnly = null;
    protected Boolean                allowRememberMe = true;
    protected SecurityFailureHandler failureHandler  = null;
    protected List<String>           permissions     = new ArrayList<>();
    protected List<String>           roles           = new ArrayList<>();

	public DefaultSecuredPathBuilder() {
	    super();
    }
	
	public DefaultSecuredPathBuilder(String path) {
		this.path(path);
	}

    public DefaultSecuredPathBuilder(PathPattern pattern) {
        this.pattern = pattern;
    }

    public DefaultSecuredPathBuilder(Route route) {
        this.route = route;
        this.pattern = route.getPathTemplate();
    }

	public DefaultSecuredPathBuilder(SecuredPath path) {
        this.route           = path.getRoute();
		this.pattern         = path.getPattern();
		this.allowAnonymous  = path.getAllowAnonymous();
		this.allowClientOnly = path.getAllowClientOnly();
		this.allowRememberMe = path.getAllowRememberMe();
        this.failureHandler  = path.getFailureHandler();

		Collections2.addAll(permissions, path.getPermissions());
		Collections2.addAll(roles, path.getRoles());
	}

    @Override
    public SecuredPathBuilder setSource(Object source) {
	    this.source = source;
        return this;
    }

    public Route getRoute() {
        return route;
    }

    public SecuredPathBuilder path(String pattern) {
        return setPattern(new AntPathPattern(pattern));
    }

    public PathPattern getPattern() {
		return pattern;
	}

	public SecuredPathBuilder setPattern(PathPattern pattern) {
		this.pattern = pattern;
		return this;
	}

    @Override
    public Boolean getAllowAnonymous() {
        return allowAnonymous;
    }

    @Override
    public Boolean getAllowRememberMe() {
        return allowRememberMe;
    }

    @Override
    public Boolean getAllowClientOnly() {
        return allowClientOnly;
    }

	@Override
    public DefaultSecuredPathBuilder setAllowAnonymous(Boolean allowAnonymous) {
		this.allowAnonymous = allowAnonymous;
		return this;
	}
	
    @Override
    public DefaultSecuredPathBuilder setAllowClientOnly(Boolean allowClientOnly) {
        this.allowClientOnly = allowClientOnly;
        return this;
    }

	@Override
    public SecuredPathBuilder setAllowRememberMe(Boolean denyRememberMe) {
		this.allowRememberMe = denyRememberMe;
		return this;
	}

    public SecurityFailureHandler getFailureHandler() {
        return failureHandler;
    }

    public SecuredPathBuilder setFailureHandler(SecurityFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
        return this;
    }

    @Override
    public SecuredPathBuilder setPermissions(String... permissions) {
        this.permissions.clear();
        Collections2.addAll(this.permissions, permissions);
        return this;
    }

    @Override
    public SecuredPathBuilder setRoles(String... roles) {
        this.roles.clear();
        Collections2.addAll(this.roles, roles);
        return this;
    }

    @Override
    public SecuredPath build() {
        return new DefaultSecuredPath(source,
                                      route,
                                      pattern,
                                      allowAnonymous,
                                      allowClientOnly,
                                      allowRememberMe,
                                      failureHandler,
                                      permissions.toArray(Arrays2.EMPTY_STRING_ARRAY),
                                      roles.toArray(Arrays2.EMPTY_STRING_ARRAY));
    }
}