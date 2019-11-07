/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.route;

import leap.core.web.path.PathTemplate;

import java.util.Map;

public class SimpleNestedRoute extends AbstractNestedRoute implements NestedRoute {

    private final Object       source;
    private final String       method;
    private final String       basePath;
    private final PathTemplate pathTemplate;
    private final Routes       routes;

    private boolean enabled = true;

    public SimpleNestedRoute(Object source, PathTemplate pathTemplate, Routes routes) {
        this(source, "*", null, pathTemplate, routes);
    }

    public SimpleNestedRoute(Object source, String method, PathTemplate pathTemplate, Routes routes) {
        this(source, method, null, pathTemplate, routes);
    }

    public SimpleNestedRoute(Object source, String method, String basePath, PathTemplate pathTemplate, Routes routes) {
        this.source = source;
        this.method = method;
        this.basePath = basePath;
        this.pathTemplate = pathTemplate;
        this.routes = routes;
    }

    @Override
    public boolean match(String path, Map<String, String> variables) {
        if (null != basePath && path.equalsIgnoreCase(basePath)) {
            return true;
        } else {
            return getPathTemplate().match(path, variables);
        }
    }

    @Override
    public Route matchNested(String method, String path, Map<String, Object> in, Map<String, String> outVariables) {
        return routes.match(method, path, in, outVariables);
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public PathTemplate getPathTemplate() {
        return pathTemplate;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + method + " " + pathTemplate + ")";
    }
}
