/*
 *
 *  * Copyright 2016 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package leap.web.security.path;

import leap.lang.Arrays2;
import leap.lang.path.PathPattern;
import leap.web.Request;
import leap.web.route.Route;
import leap.web.security.SecurityFailureHandler;
import leap.web.security.authc.AuthenticationContext;
import leap.web.security.authz.AuthorizationContext;

public class MergedSecuredPath implements SecuredPath {

    private final Route       route;
    private final SecuredPath merged;

    public MergedSecuredPath(Route route, SecuredPath p1, SecuredPath p2) {
        this.route  = route;
        this.merged = merge(p1, p2);
    }

    @Override
    public Route getRoute() {
        return route;
    }

    @Override
    public PathPattern getPattern() {
        return route.getPathTemplate();
    }

    @Override
    public boolean isAllowAnonymous() {
        return merged.isAllowAnonymous();
    }

    @Override
    public boolean isAllowClientOnly() {
        return merged.isAllowAnonymous();
    }

    @Override
    public boolean isAllowRememberMe() {
        return merged.isAllowRememberMe();
    }

    @Override
    public boolean isAllowCors() {
        return false;
    }

    @Override
    public String[] getPermissions() {
        return merged.getPermissions();
    }

    @Override
    public String[] getRoles() {
        return merged.getRoles();
    }

    @Override
    public SecurityFailureHandler getFailureHandler() {
        return merged.getFailureHandler();
    }

    @Override
    public boolean checkAuthentication(Request request, AuthenticationContext context) {
        return merged.checkAuthentication(request, context);
    }

    @Override
    public boolean checkAuthorization(Request request, AuthorizationContext context) {
        return merged.checkAuthorization(request, context);
    }

    @Override
    public int compareTo(SecuredPath o) {
        return merged.compareTo(o);
    }

    private SecuredPath merge(SecuredPath p1, SecuredPath p2) {
        SecuredPathBuilder spb = new DefaultSecuredPathBuilder(route);

        spb.setAllowAnonymous(p1.isAllowAnonymous() || p2.isAllowAnonymous());
        spb.setAllowRememberMe(p1.isAllowRememberMe() || p2.isAllowRememberMe());
        spb.setAllowClientOnly(p1.isAllowClientOnly() || p2.isAllowClientOnly());

        if(!Arrays2.isEmpty(p1.getPermissions())) {
            spb.setPermissionsAllowed(p1.getPermissions());
        }else{
            spb.setPermissionsAllowed(p2.getPermissions());
        }

        if(!Arrays2.isEmpty(p1.getRoles())) {
            spb.setRolesAllowed(p1.getRoles());
        }else{
            spb.setRolesAllowed(p2.getRoles());
        }

        if(null != p1.getFailureHandler()) {
            spb.setFailureHandler(p1.getFailureHandler());
        }else{
            spb.setFailureHandler(p2.getFailureHandler());
        }

        return spb.build();
    }
}
