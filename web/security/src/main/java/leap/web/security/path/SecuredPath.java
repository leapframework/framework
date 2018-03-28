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

import leap.core.web.RequestBase;
import leap.core.web.RequestMatcher;
import leap.lang.path.PathPattern;
import leap.web.route.Route;
import leap.web.security.SecuredObject;
import leap.web.security.SecurityFailureHandler;

import java.util.Comparator;

public interface SecuredPath extends SecuredObject, RequestMatcher, Comparable<SecuredPath> {

    Comparator<SecuredPath> COMPARATOR = (p1, p2) -> {
        if (p1 == null && p2 == null) {
            return 0;
        }
        if (p1 == null) {
            return 1;
        }
        if (p2 == null) {
            return -1;
        }

        int i = p1.getPattern().pattern().length() - p2.getPattern().pattern().length();
        if(i != 0){
            return i;
        }

        return -1;
    };

    /**
     * Optional. Returns the route.
     */
    default Route getRoute() {
        return null;
    }

    /**
     * Required. Returns the pattern of path.
     */
    PathPattern getPattern();

    /**
     * Returns the allow-anonymous status, may be null.
     */
    default Boolean getAllowAnonymous() {
        return null;
    }

    /**
     * Returns true if allows anonymous access to the path.
     *
     * <p/>
     * Same as {@link #getAllowAnonymous()} == Boolean.TRUE .
     */
    default boolean isAllowAnonymous() {
        return getAllowAnonymous() == Boolean.TRUE;
    }

    /**
     * Returns true if denys anonymous access to the path.
     *
     * <p/>
     * Same as {@link #getAllowAnonymous()} == Boolean.FALSE.
     */
    default boolean isDenyAnonymous() {
        return getAllowAnonymous() == Boolean.FALSE;
    }

    /**
     * Returns the allow-clientOnly status, may be null.
     */
    default Boolean getAllowClientOnly() {
        return null;
    }

    /**
     * Return true if allows client-only authentication access to the path.
     *
     * <p/>
     * Same as {@link #getAllowClientOnly()} == Boolean.TRUE.
     */
    default boolean isAllowClientOnly() {
        return getAllowClientOnly() == Boolean.TRUE;
    }

    /**
     * Returns true if denys client-only authentication access to the path.
     *
     * <P/>
     * Same as {@link #getAllowClientOnly()} == Boolean.FALSE.
     */
    default boolean isDenyClientOnly() {
        return getAllowClientOnly() == Boolean.FALSE;
    }

    /**
     * Returns the allow-rememberMe status, may be null.
     */
    default Boolean getAllowRememberMe() {
        return null;
    }

    /**
     * Returns true if allows remember-me authentication access to the path.
     *
     * <p/>
     * Same as {@link #getAllowRememberMe()} == Boolean.TRUE.
     */
    default boolean isAllowRememberMe() {
        return getAllowRememberMe() == Boolean.TRUE;
    }

    /**
     * Returns true if denys remember-me authentication access to the path.
     *
     * <p/>
     * Same as {@link #getAllowRememberMe()} == Boolean.FALSE.
     */
    default boolean isDenyRememberMe() {
        return getAllowRememberMe() == Boolean.FALSE;
    }

    /**
     * Optional. Returns the permissions allowed to access the path.
     */
    default String[] getPermissions() {
        return null;
    }

    /**
     * Optional. Returns the roles allowed to access the path.
     */
    default String[] getRoles() {
        return null;
    }

    /**
     * Optional. Returns the {@link SecurityFailureHandler}.
     */
    default SecurityFailureHandler getFailureHandler() {
        return null;
    }

    /**
     * Returns true if the path pattern matches the request.
     */
    default boolean matches(RequestBase request) {
       return getPattern().matches(request.getPath());
    }

    /**
     * Returns true if the path pattern matches the given path.
     */
    default boolean matches(String path) {
        return getPattern().matches(path);
    }

    @Override
    default int compareTo(SecuredPath o) {
        return COMPARATOR.compare(this, o);
    }
}
