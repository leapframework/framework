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

import leap.lang.Buildable;
import leap.lang.path.PathPattern;

public interface SecuredPathBuilder extends Buildable<SecuredPath> {

    default SecuredPathBuilder allowAnonymous() {
        return setAllowAnonymous(true);
    }

    default SecuredPathBuilder allowClientOnly() {
        return setAllowClientOnly(true);
    }

    SecuredPathBuilder setPattern(PathPattern pattern);

    SecuredPathBuilder setAllowAnonymous(boolean allow);

    SecuredPathBuilder setAllowClientOnly(boolean allow);

    SecuredPathBuilder setAllowRememberMe(boolean allow);

    SecuredPathBuilder setPermissionsAllowed(String... permissions);

    SecuredPathBuilder setRolesAllowed(String... roles);

    SecuredPathBuilder addPermissionsAllowed(String... permissions);

    SecuredPathBuilder addRolesAllowed(String... roles);

    PathPattern getPattern();

    boolean isAllowAnonymous();

    boolean isAllowRememberMe();

    boolean isAllowClientOnly();

}