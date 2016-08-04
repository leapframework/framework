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

import leap.lang.Args;
import leap.lang.path.AntPathPattern;
import leap.lang.path.PathPattern;
import leap.web.Request;
import leap.web.route.Route;
import leap.web.security.authc.AuthenticationContext;
import leap.web.security.authz.AuthorizationContext;

import java.util.Iterator;
import java.util.TreeMap;

public class DefaultSecuredPaths implements SecuredPaths {

    protected final TreeMap<PathPattern,PathEntry> paths = new TreeMap<>(PathPattern.DEFAULT_COMPARATOR);

    @Override
    public boolean isEmpty() {
        return paths.isEmpty();
    }

    @Override
    public Iterator<SecuredPath> iterator() {
        Iterator it = paths.values().iterator();

        return it;
    }

    @Override
    public SecuredPathConfigurator of(String path) {
        Args.notEmpty(path, "path");
        return configurator(new AntPathPattern(path));
    }

    @Override
    public SecuredPathConfigurator of(PathPattern pp) {
        Args.notNull(pp, "path pattern");
        return configurator(pp);
    }

    @Override
    public SecuredPathConfigurator of(Route route) {
        Args.notNull(route, "route");
        return configurator(route.getPathTemplate());
    }

    protected SecuredPathConfigurator configurator(PathPattern pp) {
        PathEntry pe = paths.get(pp);

        if(null == pe) {
            return new DefaultSecuredPathConfigurator(this, pp);
        }

        if(null == pe.configurator) {
            pe.configurator = new DefaultSecuredPathConfigurator(this,pe);
        }

        return pe.configurator;
    }

    @Override
    public SecuredPaths apply(SecuredPath p) {
        Args.notNull(p);
        paths.put(p.getPattern(), new PathEntry(p));
        return this;
    }

    @Override
    public SecuredPaths apply(String path, boolean allowAnonymous) {
        of(path).setAllowAnonymous(allowAnonymous).apply();
        return this;
    }

    @Override
    public SecuredPath remove(String path) {
        Args.notEmpty(path, "path");
        return paths.remove(new AntPathPattern(path));
    }

    public void apply(SecuredPathConfigurator c, SecuredPath p) {
        paths.put(p.getPattern(), new PathEntry(p, c));
    }

    protected static final class PathEntry implements SecuredPath {
        SecuredPath             path;
        SecuredPathConfigurator configurator;

        PathEntry(SecuredPath p) {
            this.path = p;
        }

        PathEntry(SecuredPath p, SecuredPathConfigurator c) {
            this.path = p;
            this.configurator = c;
        }

        @Override
        public PathPattern getPattern() {
            return path.getPattern();
        }

        @Override
        public boolean isAllowAnonymous() {
            return path.isAllowAnonymous();
        }

        @Override
        public boolean isAllowClientOnly() {
            return path.isAllowClientOnly();
        }

        @Override
        public boolean isAllowRememberMe() {
            return path.isAllowRememberMe();
        }

        @Override
        public boolean isAllowCors() {
            return path.isAllowCors();
        }

        @Override
        public String[] getPermissions() {
            return path.getPermissions();
        }

        @Override
        public String[] getRoles() {
            return path.getRoles();
        }

        @Override
        public boolean checkAuthentication(Request request, AuthenticationContext context) {
            return path.checkAuthentication(request, context);
        }

        @Override
        public boolean checkAuthorization(Request request, AuthorizationContext context) {
            return path.checkAuthorization(request, context);
        }

        @Override
        public int compareTo(SecuredPath o) {
            return path.compareTo(o);
        }

        @Override
        public String toString() {
            return path.toString();
        }
    }
}