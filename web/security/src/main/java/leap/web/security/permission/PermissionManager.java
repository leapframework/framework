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
package leap.web.security.permission;

import java.security.Permission;

public interface PermissionManager extends PermissionChecker {

    /**
     * Checks if the specified permissions are implied all of the "impliedBy" permissions.
     *
     * see {@link java.security.Permission#implies(Permission)}.
     *
     * @deprecated use {@link #checkPermissionImplies(String[], String[])} instead.
     */
    @Deprecated
    boolean checkPermissionImpliesAll(String[] checkingPermissions,String[] impliedByPermissions);

    /**
     * Checks if the expected permissions are all implied by the actual permissions.
     */
    boolean checkPermissionImplies(String[] expected, String[] actual);
}