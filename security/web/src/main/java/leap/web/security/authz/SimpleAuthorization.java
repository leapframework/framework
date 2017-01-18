/*
 * Copyright 2014 the original author or authors.
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
package leap.web.security.authz;

import leap.core.security.Authorization;
import leap.lang.Arrays2;

public class SimpleAuthorization implements Authorization {

	protected String[] roles;
    protected String[] permissions;

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean hasRole(String role) {
        return Arrays2.contains(roles, role);
    }

    @Override
    public boolean hasAnyRole(String... roles) {
        return Arrays2.containsAny(this.roles, roles);
    }

    @Override
    public boolean hasPermission(String permission) {
        return Arrays2.contains(permissions, permission);
    }

    @Override
    public boolean hasAllPermission(String... permissions) {
        if(permissions == null){
            return true;
        }
        for(String permission:permissions){
            if(!Arrays2.contains(this.permissions, permission)){
                return false;
            }
        }
        return true;
    }
}