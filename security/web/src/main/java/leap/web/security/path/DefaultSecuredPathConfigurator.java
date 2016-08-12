/*
 * Copyright 2016 the original author or authors.
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

import leap.lang.path.PathPattern;
import leap.web.route.Route;
import leap.web.security.SecurityFailureHandler;

public class DefaultSecuredPathConfigurator implements SecuredPathConfigurator {

    protected final DefaultSecuredPaths paths;
    protected final SecuredPathBuilder  path;

    public DefaultSecuredPathConfigurator(DefaultSecuredPaths paths, Route route) {
        this.paths = paths;
        this.path  = new DefaultSecuredPathBuilder(route);
    }


    public DefaultSecuredPathConfigurator(DefaultSecuredPaths paths, PathPattern pp) {
        this.paths = paths;
        this.path  = new DefaultSecuredPathBuilder(pp);
    }

    public DefaultSecuredPathConfigurator(DefaultSecuredPaths paths, SecuredPath old) {
        this.paths = paths;
        this.path  = new DefaultSecuredPathBuilder(old);
    }

    public DefaultSecuredPathConfigurator(DefaultSecuredPaths paths, SecuredPathBuilder sp) {
        this.paths = paths;
        this.path  = sp;
    }

    @Override
    public SecuredPathConfigurator setAllowAnonymous(boolean allow) {
        path.setAllowAnonymous(allow);
        return this;
    }

    @Override
    public SecuredPathConfigurator setAllowRememberMe(boolean allow) {
        path.setAllowRememberMe(allow);
        return this;
    }

    @Override
    public SecuredPathConfigurator setAllowCors(boolean allow) {
        path.setAllowCors(allow);
        return this;
    }

    @Override
    public SecuredPathConfigurator setAllowClientOnly(boolean allow) {
        path.setAllowClientOnly(allow);
        return this;
    }

    @Override
    public SecuredPathConfigurator setFailureHandler(SecurityFailureHandler handler) {
        path.setFailureHandler(handler);
        return this;
    }

    @Override
    public boolean isAllowAnonymous() {
        return path.isAllowAnonymous();
    }

    @Override
    public boolean isAllowRememberMe() {
        return path.isAllowRememberMe();
    }

    @Override
    public boolean isAllowClientOnly() {
        return path.isAllowClientOnly();
    }

    @Override
    public SecuredPathConfigurator setPermissionsAllowed(String... permissions) {
        path.setPermissionsAllowed(permissions);
        return this;
    }

    @Override
    public SecuredPathConfigurator setRolesAllowed(String... roles) {
        path.setRolesAllowed(roles);
        return this;
    }

    @Override
    public SecuredPath apply() {
        SecuredPath p = path.build();

        paths.apply(this,p);

        return p;
    }
}