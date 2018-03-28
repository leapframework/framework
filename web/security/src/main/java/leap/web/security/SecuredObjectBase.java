/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.web.security;

import leap.lang.Arrays2;
import leap.web.security.authz.AuthorizationContext;
import leap.web.security.permission.PermissionManager;

public abstract class SecuredObjectBase implements SecuredObject {

    protected final boolean checkRoles(AuthorizationContext context, String[] roles) {
        if(null != roles && roles.length > 0) {
            boolean allow = false;
            String[] grantedRoles = context.getAuthentication().getRoles();
            if(null != grantedRoles && grantedRoles.length > 0) {
                allow = Arrays2.containsAny(grantedRoles,roles);
            }

            if(!allow) {
                allow = context.getAuthorization().hasAnyRole(roles);
            }

            if(!allow) {
                return false;
            }
        }
        return true;
    }

    protected final boolean checkPermissions(AuthorizationContext context, String[] permissions) {
        if(null != permissions && permissions.length > 0) {
            PermissionManager pm = context.getPermissionManager();

            boolean allow = false;
            String[] grantedPermissions = context.getAuthentication().getPermissions();
            if(null != grantedPermissions && grantedPermissions.length > 0) {
                allow = pm.checkPermissionImpliesAll(grantedPermissions,permissions);
            }

            if(!allow) {
                allow = context.getAuthorization().hasAllPermission(permissions);
            }

            if(!allow) {
                return false;
            }
        }
        return true;
    }

}
