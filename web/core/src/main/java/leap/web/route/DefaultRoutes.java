/*
 * Copyright 2013 the original author or authors.
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
package leap.web.route;

import leap.core.annotation.Inject;
import leap.core.web.path.JerseyUriTemplate;
import leap.core.web.path.PathTemplate;
import leap.core.web.path.PathTemplateFactory;
import leap.lang.Args;
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.http.HTTP.Method;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.web.Handler;
import leap.web.action.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class DefaultRoutes implements Routes {

    private static final Log log = LogFactory.get(DefaultRoute.class);

    protected @Inject RouteManager        routeManager;
    protected @Inject PathTemplateFactory pathTemplateFactory;
    protected @Inject ActionManager       actionManager;
    protected @Inject RoutesPrinter       routesPrinter;

    protected final List<Route> list = new CopyOnWriteArrayList<>();

    protected String pathPrefix = "";

    public String getPathPrefix() {
        return pathPrefix;
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = null == pathPrefix ? "" : pathPrefix;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public Iterator<Route> iterator() {
        return list.iterator();
    }

    @Override
    public RouteConfigurator create() {
        return new DefaultRouteConfigurator(this);
    }

    @Override
    public Routes addNested(Object source, String pathPrefix) {
        final String basePath = getPathPrefix() + Paths.prefixWithAndSuffixWithoutSlash(pathPrefix);
        final Routes routes   = routeManager.createRoutes(basePath);
        final PathTemplate pathTemplate = pathTemplateFactory.createPathTemplate(basePath + "/{path:.*}");

        SimpleNestedRoute route = new SimpleNestedRoute(source, "*", basePath, pathTemplate, routes);
        add(route);
        return routes;
    }

    @Override
    public Routes add(Method method, String path, Runnable handler) {
        Args.notNull(handler, "handler");
        return doAdd(method, path, () -> new RunnableAction(handler));
    }

    @Override
    public <T> Routes add(Method method, String path, Supplier<T> handler) {
        Args.notNull(handler, "handler");
        return doAdd(method, path, () -> new SupplierAction(handler));
    }

    @Override
    public Routes add(Method method, String path, Handler handler) {
        Args.notNull(handler, "handler");
        return doAdd(method, path, () -> new HandlerAction(handler));
    }

    protected Routes doAdd(Method method, String path, Supplier<Action> action) {
        return add(createRoute(method, path, action).build());
    }

    protected RouteBuilder createRoute(Method method, String path, Supplier<Action> action) {
        return createRoute(method, path, action.get());
    }

    protected RouteBuilder createRoute(Method method, String path, Action action) {
        Args.notEmpty(path, "path");

        RouteBuilder r = new RouteBuilder();

        r.setPathTemplate(pathTemplateFactory.createPathTemplate(pathPrefix + path));
        r.setAction(action);

        if (null != method) {
            r.setMethod(method.name());
        } else {
            r.setMethod("*");
        }

        actionManager.prepareAction(r);

        return r;
    }

    @Override
    public synchronized Routes add(Route route) {
        Args.notNull(route, "route");
        list.add(route);
        Collections.sort(list, Route.COMPARATOR);
        return this;
    }

    @Override
    public boolean exists(Route route) {
        return list.contains(route);
    }

    @Override
    public boolean remove(Route route) {
        return list.remove(route);
    }

    @Override
    public synchronized Routes addAll(Iterable<Route> routes) {
        Args.notNull(routes, "routes");
        for (Route route : routes) {
            add(route);
        }
        return this;
    }

    @Override
    public Route match(String method, String path) {
        return match(method, path, New.hashMap(), New.hashMap());
    }

    @Override
    public Route match(String method, String path, Map<String, Object> in, Map<String, String> out) {
        if(!Strings.isEmpty(pathPrefix) && !path.startsWith(pathPrefix)) {
            path = pathPrefix + Paths.prefixWithSlash(path);
        }

        List<Route> matchedRoutes = new ArrayList<>();
        for (Route route : list) {
            if (!route.isEnabled()) {
                continue;
            }

            if (null == method || route.getMethod().equals("*") || route.getMethod().equals(method)) {

                if (!matchRequiredParameters(route.getRequiredParameters(), in)) {
                    continue;
                }

                if (route.match(path, out)) {

                    if (route instanceof NestedRoute) {
                        NestedRoute nestedRoute = (NestedRoute) route;
                        route = nestedRoute.matchNested(method, path, in, out);
                        if (null != route) {
                            if(!matchedRoutes.isEmpty()) {
                                boolean ignore = false;
                                for(Route matchedRoute : matchedRoutes) {
                                    int re = matchedRoute.getPathTemplate().compareTo(route.getPathTemplate());
                                    if(re < 0) {
                                        ignore = true;
                                        break;
                                    }
                                }
                                if(ignore) {
                                    continue;
                                }
                            }

                            if(!nestedRoute.isCheckAmbiguity()) {
                                return route;
                            }
                        }
                    }

                    if (null != route) {
                        matchedRoutes.add(route);
                    }
                }
            }
        }

        if (matchedRoutes.isEmpty()) {
            return null;
        }

        Route route;
        if (matchedRoutes.size() == 1) {
            route = matchedRoutes.get(0);
        } else {
            route = rematch(matchedRoutes);
            // get the right path template variables.
            out.clear();
            route.getPathTemplate().match(path, out);
        }

        return route;
    }

    private Route rematch(List<Route> matchedRoutes) {
        // find the route of the highest priority
        return matchedRoutes.stream().min((r1, r2) -> {
            int re = r1.getPathTemplate().compareTo(r2.getPathTemplate());
            if (0 == re && r1.isExecutable() && r2.isExecutable()) {
                log.error("Found multi matched routes -> \n{}", routesPrinter.print(matchedRoutes));
                throw new IllegalStateException("Ambiguous handler methods mapped for path " + "'"
                        + r1.getPathTemplate()
                        + "' and '"
                        + r2.getPathTemplate() + "'");
            }
            return re;
        }).get();
    }

    protected boolean matchRequiredParameters(Map<String, String> requiredParameters, Map<String, Object> inParameters) {
        if (requiredParameters.isEmpty()) {
            return true;
        }

        for (Entry<String, String> entry : requiredParameters.entrySet()) {

            Object v = inParameters.get(entry.getKey());

            if (null == v || !(v instanceof String)) {
                return false;
            }

            if (!((String) v).equals(entry.getValue())) {
                return false;
            }
        }

        return true;
    }
}