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

import leap.core.annotation.Inject;
import leap.lang.Arrays2;
import leap.lang.Strings;

import java.util.Arrays;

public class DefaultPermissionManager implements PermissionManager {

    protected @Inject PermissionChecker[] checkers;

    @Override
    public boolean checkPermissionImplies(String[] checkingPermission, String impliedByPermission) {
        for(PermissionChecker checker : checkers) {
            if(checker.checkPermissionImplies(checkingPermission, impliedByPermission)) {
                return true;
            }
        }
        return Arrays2.containsAny(checkingPermission,impliedByPermission);
    }

    @Override
    public boolean checkPermissionImpliesAll(String[] checkingPermissions, String[] impliedByPermissions) {
        if(null == checkingPermissions || checkingPermissions.length == 0) {
            return true;
        }
        if(null == impliedByPermissions || impliedByPermissions.length == 0) {
            return false;
        }
        for(String by : impliedByPermissions) {
            if(!checkPermissionImplies(checkingPermissions,by)){
                return false;
            }
        }
        return true;
    }

}