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
import leap.lang.Strings;
import leap.lang.path.AntPathPattern;
import leap.lang.path.PathPattern;
import leap.web.route.Route;
import leap.web.security.SecurityContextHolder;
import leap.web.security.SecurityFailureHandler;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;

public class DefaultSecuredPaths implements SecuredPaths {

    protected static final Comparator<PathPattern> COMPARATOR = (o1, o2) -> {
        if (o1 == null && o2 == null) {
            return 1;
        }

        if (o1 == null) {
            return 1;
        }

        if (o2 == null) {
            return -1;
        }

        if (o1 == o2) {
            return 0;
        }

        int result;
        if (o1 instanceof Comparable && o1.getClass().equals(o2.getClass())) {
            result = ((Comparable) o1).compareTo(o2);
        } else {
            result = compareTo(o1, o2);
        }
        return result == 0 ? 1 : result;
    };

    protected final TreeMap<PathPattern, PathEntry> paths = new TreeMap<>(COMPARATOR);

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
        return configurator(route);
    }

    @Override
    public SecuredPathConfigurator get(Route route) {
        PathEntry pe = paths.get(route.getPathTemplate());

        if (null == pe) {
            return null;
        } else {
            return pe.configurator;
        }
    }

    protected SecuredPathConfigurator configurator(Route route) {
        PathEntry pe = paths.get(route.getPathTemplate());

        if (null == pe) {
            return new DefaultSecuredPathConfigurator(this, route);
        }

        if (null == pe.configurator) {
            pe.configurator = new DefaultSecuredPathConfigurator(this, pe);
        }

        return pe.configurator;
    }

    protected SecuredPathConfigurator configurator(PathPattern pp) {
        PathEntry pe = paths.get(pp);

        if (null == pe) {
            return new DefaultSecuredPathConfigurator(this, pp);
        }

        if (null == pe.configurator) {
            pe.configurator = new DefaultSecuredPathConfigurator(this, pe);
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
        public Route getRoute() {
            return path.getRoute();
        }

        @Override
        public PathPattern getPattern() {
            return path.getPattern();
        }

        @Override
        public Boolean getAllowAnonymous() {
            return path.getAllowAnonymous();
        }

        @Override
        public Boolean getAllowClientOnly() {
            return path.getAllowClientOnly();
        }

        @Override
        public Boolean getAllowRememberMe() {
            return path.getAllowRememberMe();
        }

        @Override
        public SecurityFailureHandler getFailureHandler() {
            return path.getFailureHandler();
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
        public Boolean tryCheckAuthentication(SecurityContextHolder context) {
            return path.tryCheckAuthentication(context);
        }

        @Override
        public Boolean tryCheckAuthorization(SecurityContextHolder context) {
            return path.tryCheckAuthorization(context);
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

    protected static int compareTo(PathPattern p1, PathPattern p2) {
        String pattern1 = p1.pattern();
        String pattern2 = p2.pattern();

        if (pattern1 == null && pattern2 == null) {
            return 1;
        }

        if (pattern1 == null) {
            return 1;
        }

        if (pattern2 == null) {
            return -1;
        }

        if (pattern1 == pattern2) {
            return 0;
        }

        String[] parts1 = Strings.split(pattern1, '/');
        String[] parts2 = Strings.split(pattern2, '/');

        int min = Math.min(parts1.length, parts2.length);
        for(int i=0;i<min;i++) {
            String part1 = parts1.length > i ? parts1[i] : null;
            String part2 = parts2.length > i ? parts2[i] : null;

            if(part1 != null && part2 == null) {
                return -1;
            }

            if(part1 == null && part2 == null) {
                return 1;
            }

            if(part1.equals(part2)) {
                continue;
            }

            if(part1.contains("**")) {
                return 1;
            }

            if(part2.contains("**")) {
                return -1;
            }

            if(isDotStartVariable(part1)) {
                return 1;
            }

            if(isDotStartVariable(part2)) {
                return -1;
            }

            if(part1.contains("*")) {
                return 1;
            }

            if(part2.contains("*")) {
                return -1;
            }

            if(isVariable(part1)) {
                return 1;
            }

            if(isVariable(part2)) {
                return -1;
            }
        }

        return 0;
    }

    protected static boolean isVariable(String part) {
        return part.contains("{") && part.contains("}");
    }

    protected static boolean isDotStartVariable(String part) {
        return isVariable(part) && part.contains(".*");
    }
}