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

package leap.core.security;

import leap.lang.Buildable;

public class SimpleSecurity {

    protected final boolean  userRequired;
    protected final boolean  clientRequired;
    protected final String[] permissions;
    protected final String[] roles;

    public SimpleSecurity(boolean userRequired, boolean clientRequired, String[] permissions, String[] roles) {
        this.userRequired = userRequired;
        this.clientRequired = clientRequired;
        this.permissions = permissions;
        this.roles = roles;
    }

    public boolean isUserRequired() {
        return userRequired;
    }

    public boolean isClientRequired() {
        return clientRequired;
    }

    public boolean hasPermissions() {
        return null != permissions && permissions.length > 0;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public boolean hasRoles() {
        return null != roles && roles.length > 0;
    }

    public String[] getRoles() {
        return roles;
    }

    public boolean matchAuthentication(Authentication authc) {
        if(!userRequired) {
            return !clientRequired ? true : authc.isClientAuthenticated();
        }

        if(!clientRequired) {
            return authc.isUserAuthenticated();
        }

        return authc.isUserAuthenticated() && authc.isClientAuthenticated();
    }

    public static class Builder implements Buildable<SimpleSecurity> {
        protected boolean  userRequired;
        protected boolean  clientRequired;
        protected String[] permissions;
        protected String[] roles;

        public boolean isUserRequired() {
            return userRequired;
        }

        public void setUserRequired(boolean userRequired) {
            this.userRequired = userRequired;
        }

        public boolean isClientRequired() {
            return clientRequired;
        }

        public void setClientRequired(boolean clientRequired) {
            this.clientRequired = clientRequired;
        }

        public String[] getPermissions() {
            return permissions;
        }

        public void setPermissions(String[] permissions) {
            this.permissions = permissions;
        }

        public String[] getRoles() {
            return roles;
        }

        public void setRoles(String[] roles) {
            this.roles = roles;
        }

        @Override
        public SimpleSecurity build() {
            return new SimpleSecurity(userRequired, clientRequired, permissions, roles);
        }
    }
}