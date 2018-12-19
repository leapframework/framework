/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.security.path;

import leap.core.security.Authentication;
import leap.core.security.SimpleSecurity;
import leap.lang.Strings;
import leap.lang.path.PathPattern;
import leap.web.route.Route;
import leap.web.security.SecuredObjectBase;
import leap.web.security.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

public class SecuredRoute extends SecuredObjectBase implements SecuredPath {

    private final Route route;

    public SecuredRoute(Route route) {
        this.route = route;
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
    public Boolean getAllowAnonymous() {
        return route.getAllowAnonymous();
    }

    @Override
    public Boolean getAllowRememberMe() {
        return route.getAllowRememberMe();
    }

    @Override
    public Boolean getAllowClientOnly() {
        return route.getAllowClientOnly();
    }

    @Override
    public String[] getPermissions() {
        return route.getPermissions();
    }

    @Override
    public String[] getRoles() {
        return route.getRoles();
    }

    @Override
    public Boolean tryCheckAuthentication(SecurityContextHolder context) {
        Authentication authc = context.getAuthentication();

        //allow anonymous
        if(route.getAllowAnonymous() == Boolean.TRUE) {
            return true;
        }

        SimpleSecurity[] securities = route.getSecurities();
        if(null != securities && securities.length > 0) {
            List<SimpleSecurity> matches = new ArrayList<>();
            for(SimpleSecurity security : securities) {
                if(security.matchAuthentication(authc)) {
                    matches.add(security);
                }
            }
            if(matches.isEmpty()) {
                context.setDenyMessage(getAuthenticationDenyMessage(authc, securities));
                return false;
            }else {
                context.setSecurities(matches.toArray(new SimpleSecurity[matches.size()]));
                return true;
            }
        }

        if(authc == null || !authc.isAuthenticated()){
            return false;
        }

        if(authc.isRememberMe() && (null == route.getAllowRememberMe() || !route.getAllowRememberMe())){
            return false;
        }

        if(authc.isClientOnly() && (null == route.getAllowClientOnly() || !route.getAllowClientOnly())) {
            context.setDenyMessage("client only authentication not allowed");
            return false;
        }

        return null;
    }

    @Override
    public Boolean tryCheckAuthorization(SecurityContextHolder context) {
        //Check roles
        if(!checkRoles(context, route.getRoles())) {
            context.setDenyMessage("Roles [" + Strings.join(route.getRoles(), ',') + "] required");
            return false;
        }

        //Check permissions
        if(!checkPermissions(context, route.getPermissions())) {
            context.setDenyMessage("Permissions [" + Strings.join(route.getPermissions(), ',') + "] required");
            return false;
        }

        SimpleSecurity[] securities = context.getSecurities();
        if(null != securities && securities.length > 0) {
            for(SimpleSecurity security : securities) {
                if(checkPermissions(context, security.getPermissions()) &&
                        checkRoles(context, security.getRoles())) {
                    return true;
                }
            }
            context.setDenyMessage(getAuthorizationDenyMessage(context.getAuthentication(), securities));
            return false;
        }

        return null;
    }

    protected String getAuthenticationDenyMessage(Authentication authc, SimpleSecurity[] securities) {
        StringBuilder s = new StringBuilder();

        s.append("Expected one of authentications [ ");

        for(int i=0;i<securities.length;i++) {
            SimpleSecurity sec = securities[i];

            if(i > 0) {
                s.append(" , ");
            }

            s.append("(");
            s.append("user: ").append(sec.isUserRequired());
            s.append(", client: ").append(sec.isClientRequired());
            s.append(")");
        }

        s.append(" ], Actual ");
        s.append("(");
        s.append("user: ").append(authc.isUserAuthenticated());
        s.append(", client: ").append(authc.isClientAuthenticated());
        s.append(")");

        return s.toString();
    }

    protected String getAuthorizationDenyMessage(Authentication authc, SimpleSecurity[] securities) {
        StringBuilder s = new StringBuilder();

        s.append("Expected one of authorizations [ ");

        for(int i=0;i<securities.length;i++) {
            SimpleSecurity sec = securities[i];

            if(i > 0) {
                s.append(" , ");
            }

            s.append("(");
            s.append(" perms: ").append(Strings.join(sec.getPermissions(), ' '));
            s.append(", roles: ").append(Strings.join(sec.getRoles(), ' '));
            s.append(")");
        }

        s.append(" ], Actual ");
        s.append("(");
        s.append("perms: ").append(Strings.join(authc.getPermissions(), ' '));
        s.append(", roles: ").append(Strings.join(authc.getRoles(), ' '));
        s.append(")");

        return s.toString();
    }

    @Override
    public String toString() {
        return route.toString();
    }
}