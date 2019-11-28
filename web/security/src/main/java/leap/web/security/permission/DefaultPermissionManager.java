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
import leap.core.el.EL;
import leap.lang.Arrays2;
import leap.lang.expression.Expression;
import leap.web.security.SecurityConfig;

import java.util.HashMap;
import java.util.Map;

public class DefaultPermissionManager implements PermissionManager {

    protected @Inject SecurityConfig      config;
    protected @Inject PermissionChecker[] checkers;

    @Override
    public boolean checkPermissionImplies(String[] checkingPermission, String impliedByPermission) {
        for (PermissionChecker checker : checkers) {
            if (checker.checkPermissionImplies(checkingPermission, impliedByPermission)) {
                return true;
            }
        }
        return Arrays2.containsAny(checkingPermission, impliedByPermission);
    }

    @Override
    public boolean checkPermissionImplies(String expected, String[] actual) {
        boolean b = doCheckPermissionImplies(expected, actual);
        if(!b && !config.getScopeExpressions().isEmpty()) {
            Expression expr = config.getScopeExpressions().get(expected);
            if(null != expr) {
                b = EL.test(expr.getValue(new ExprContext(actual)));
            }
        }
        return b;
    }

    @Override
    public boolean checkPermissionImpliesAll(String[] checkingPermissions, String[] impliedByPermissions) {
        if (null == checkingPermissions || checkingPermissions.length == 0) {
            return true;
        }
        if (null == impliedByPermissions || impliedByPermissions.length == 0) {
            return false;
        }
        for (String by : impliedByPermissions) {
            if (!checkPermissionImplies(checkingPermissions, by)) {
                return false;
            }
        }
        return true;
    }

    protected boolean doCheckPermissionImplies(String expected, String[] actual) {
        if (null == expected) {
            return true;
        }
        if (null == actual) {
            return false;
        }
        for (PermissionChecker checker : checkers) {
            if (checker.checkPermissionImplies(expected, actual)) {
                return true;
            }
        }
        return Arrays2.contains(actual, expected);
    }

    @Override
    public boolean checkPermissionImplies(String[] expected, String[] actual) {
        if (null == expected || expected.length == 0) {
            return true;
        }
        if (null == actual || actual.length == 0) {
            return false;
        }

        ExprContext exprContext = null;
        Map<String, Expression> exprs = config.getScopeExpressions();

        for (String expectedOne : expected) {
            if (!doCheckPermissionImplies(expectedOne, actual)) {
                if(!exprs.isEmpty()) {
                    Expression expr = exprs.get(expectedOne);
                    if(null != expr) {
                        if(null == exprContext) {
                            exprContext = new ExprContext(actual);
                        }
                        if(EL.test(expr.getValue(exprContext))) {
                            continue;
                        }
                    }
                }
                return false;
            }
        }

        return true;
    }

    protected static class ExprContext extends HashMap {
        private final String[] scopes;

        public ExprContext(String[] scopes) {
            this.scopes = scopes;
        }

        public boolean has(String s) {
            return Arrays2.contains(scopes, s);
        }

        public boolean hasAny(String... a) {
            if (null == a) {
                return false;
            }
            for (String e : a) {
                if (Arrays2.contains(scopes, e)) {
                    return true;
                }
            }
            return false;
        }

        public boolean hasAll(String... a) {
            if (null == a) {
                return false;
            }
            for (String e : a) {
                if (!Arrays2.contains(scopes, e)) {
                    return false;
                }
            }
            return true;
        }
    }
}