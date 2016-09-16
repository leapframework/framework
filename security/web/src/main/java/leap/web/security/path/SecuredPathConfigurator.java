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

import leap.web.security.SecurityFailureHandler;
import leap.web.security.annotation.Secured;

/**
 * The configurator of {@link SecuredPath}.
 */
public interface SecuredPathConfigurator {

    /**
     * Allow anonymous (not authenticated) user to access the path.
     */
    default SecuredPathConfigurator allowAnonymous() {
        return setAllowAnonymous(true);
    }

    /**
     * Deny anonymous access.
     */
    default SecuredPathConfigurator denyAnonymous() {
        return setAllowAnonymous(false);
    }

    /**
     * Allow remember-me user to access the path.
     */
    default SecuredPathConfigurator allowRememberMe() {
        return setAllowRememberMe(true);
    }

    /**
     * Deny remember-me user to access the path.
     */
    default SecuredPathConfigurator denyRememberMe() {
        return setAllowRememberMe(false);
    }

    /**
     * Allow client-only authentication.
     */
    default SecuredPathConfigurator allowClientOnly() {
        return setAllowClientOnly(true);
    }

    /**
     * Deny client-only authentication.
     */
    default SecuredPathConfigurator denyClientOnly() {
        return setAllowClientOnly(false);
    }

    /**
     * Allow the given permissions can access the path.
     */
    default SecuredPathConfigurator allowPermissions(String... permissions) {
        return setPermissionsAllowed(permissions);
    }

    /**
     * Allow the given roles can access the path.
     */
    default SecuredPathConfigurator allowRoles(String... roles) {
        return setRolesAllowed(roles);
    }

    /**
     * Sets allow or deny anonymous access.
     *
     * <p/>
     * Default is deny.
     */
    SecuredPathConfigurator setAllowAnonymous(Boolean allow);

    /**
     * Sets allow or deny remember-me authentication.
     *
     * <p/>
     * Default is allow.
     */
    SecuredPathConfigurator setAllowRememberMe(Boolean allow);

    /**
     * Sets allow or deny client-only authentication.
     *
     * <p/>
     * Default is deny.
     */
    SecuredPathConfigurator setAllowClientOnly(Boolean allow);

    /**
     * Sets the {@link SecurityFailureHandler}.
     */
    SecuredPathConfigurator setFailureHandler(SecurityFailureHandler handler);

    /**
     * Sets the allowed permissions.
     */
    SecuredPathConfigurator setPermissionsAllowed(String... permissions);

    /**
     * Sets the allowed roles.
     */
    SecuredPathConfigurator setRolesAllowed(String... roles);

    /**
     * Returns true if allow anonymous access.
     */
    Boolean getAllowAnonymous();

    /**
     * Returns true if allow remember-me authentication.
     */
    Boolean getAllowRememberMe();

    /**
     * Returns true if allow client-only authentication.
     */
    Boolean getAllowClientOnly();

    /**
     * Updates the configuration of secured path.
     */
    SecuredPath apply();

}