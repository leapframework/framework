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

public interface RouteManager {

    /**
     * Loads the {@link RouteBuilder} into the given {@link Routes}.
     */
    void loadRoute(Routes routes, RouteBuilder route);

    /**
     * Loads all the routes defined in the controller class into the given {@link Routes}.
     */
    default void loadRoutesFromController(Routes routes, Class<?> controllerClass) {
        loadRoutesFromController(routes, controllerClass, "/");
    }

    /**
     * Loads all the routes defined in the controller class into the given {@link Routes}.
     */
    void loadRoutesFromController(Routes routes, Class<?> controllerClass,String basePath);

    /**
     * Loads all the routes defined in the controller object into the given {@link Routes}.
     */
    default void loadRoutesFromController(Routes routes, Object controller) {
        loadRoutesFromController(routes, controller, "/");
    }

    /**
     * Loads all the routes defined in the controller object into the given {@link Routes}.
     */
    void loadRoutesFromController(Routes routes, Object controller,String basePath);

}