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
import leap.lang.path.AntPathPattern;
import leap.lang.path.PathPattern;

import java.util.ArrayList;
import java.util.List;

public class DefaultSecuredPathBuilder implements SecuredPathBuilder {

	protected PathPattern 		 pattern;
	protected boolean            allowAnonymous  = false;
	protected boolean            allowClientOnly = false;
	protected boolean            allowRememberMe = true;
    protected boolean            allowCors       = false;
	protected List<String>		 permissions	 = new ArrayList<>();
    protected List<String>       roles           = new ArrayList<>();

	public DefaultSecuredPathBuilder() {
	    super();
    }
	
	public DefaultSecuredPathBuilder(String path) {
		this.path(path);
	}

    public DefaultSecuredPathBuilder(PathPattern pattern) {
        this.pattern = pattern;
    }

	public DefaultSecuredPathBuilder(SecuredPath path) {
		this.pattern         = path.getPattern();
		this.allowAnonymous  = path.isAllowAnonymous();
		this.allowClientOnly = path.isAllowClientOnly();
		this.allowRememberMe = path.isAllowRememberMe();

		Collections2.addAll(permissions, path.getPermissions());
		Collections2.addAll(roles, path.getRoles());
	}

	public PathPattern getPattern() {
		return pattern;
	}

	public SecuredPathBuilder setPattern(PathPattern pattern) {
		this.pattern = pattern;
		return this;
	}
	
	public SecuredPathBuilder path(String pattern) {
		return setPattern(new AntPathPattern(pattern));
	}
	
	@Override
    public boolean isAllowAnonymous() {
		return allowAnonymous;
	}

	@Override
    public DefaultSecuredPathBuilder setAllowAnonymous(boolean allowAnonymous) {
		this.allowAnonymous = allowAnonymous;
		return this;
	}
	
	@Override
    public boolean isAllowClientOnly() {
        return allowClientOnly;
    }

    @Override
    public DefaultSecuredPathBuilder setAllowClientOnly(boolean allowClientOnly) {
        this.allowClientOnly = allowClientOnly;
        return this;
    }

	@Override
    public boolean isAllowRememberMe() {
		return allowRememberMe;
	}

	@Override
    public SecuredPathBuilder setAllowRememberMe(boolean denyRememberMe) {
		this.allowRememberMe = denyRememberMe;
		return this;
	}

    @Override
    public SecuredPathBuilder setAllowCors(boolean allow) {
        this.allowCors = allow;
        return this;
    }

    @Override
    public boolean isAllowCors() {
        return allowCors;
    }

    @Override
    public SecuredPathBuilder setPermissionsAllowed(String... permissions) {
        this.permissions.clear();
        Collections2.addAll(this.permissions, permissions);
        return this;
    }

    @Override
    public SecuredPathBuilder setRolesAllowed(String... roles) {
        this.roles.clear();
        Collections2.addAll(this.roles, roles);
        return this;
    }

    @Override
    public SecuredPathBuilder addPermissionsAllowed(String... permissions) {
        Collections2.addAll(this.permissions, permissions);
        return this;
    }

    @Override
    public SecuredPathBuilder addRolesAllowed(String... roles) {
        Collections2.addAll(this.roles, roles);
        return this;
    }

    @Override
    public SecuredPath build() {
        return new DefaultSecuredPath(pattern,
                                      allowAnonymous,
                                      allowClientOnly,
                                      allowRememberMe,
                                      allowCors,
                                      permissions.toArray(Arrays2.EMPTY_STRING_ARRAY),
                                      roles.toArray(Arrays2.EMPTY_STRING_ARRAY));
    }
}