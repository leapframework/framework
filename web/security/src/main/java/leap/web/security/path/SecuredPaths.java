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

import leap.lang.Emptiable;
import leap.lang.path.PathPattern;
import leap.web.route.Route;

public interface SecuredPaths extends Iterable<SecuredPath>,Emptiable {

    /**
     * Returns an exists or creates a new {@link SecuredPathConfigurator} for the given path pattern
     */
    SecuredPathConfigurator of(String path);

    /**
     * Returns an exists or creates a new {@link SecuredPathConfigurator} for the given path pattern
     */
    SecuredPathConfigurator of(PathPattern pp);

    /**
     * Returns an exists or creates a new {@link SecuredPathConfigurator} for the given path pattern in route.
     */
    SecuredPathConfigurator of(Route route);

    /**
     * Returns the exists {@link SecuredPathConfigurator} for the route or null if not exists.
     */
    SecuredPathConfigurator get(Route route);

    /**
     * Apply the secured path.
     */
    SecuredPaths apply(SecuredPath p);

    /**
     * Apply a secured path.
     */
    SecuredPaths apply(String path, boolean allowAnonymous);

    /**
     * Removes the secured path and returns the removed {@link SecuredPath} object.
     *
     * <p/>
     * Returns null if not exists.
     */
    SecuredPath remove(String path);

    /**
     * Removes the secured path and returns the removed {@link SecuredPath} object.
     *
     * <p/>
     * Returns false if not exists.
     */
    boolean remove(SecuredPath path);

}