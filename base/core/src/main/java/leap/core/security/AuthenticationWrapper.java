/*
 *  Copyright 2018 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.core.security;

public class AuthenticationWrapper implements Authentication {

    protected final Authentication wrapped;

    public AuthenticationWrapper(Authentication wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean isAuthenticated() {
        return wrapped.isAuthenticated();
    }

    @Override
    public boolean isRememberMe() {
        return wrapped.isRememberMe();
    }

    @Override
    public String getToken() {
        return wrapped.getToken();
    }

    @Override
    public void setToken(String token) throws IllegalStateException {
        wrapped.setToken(token);
    }

    @Override
    public Object getCredentials() {
        return wrapped.getCredentials();
    }

    @Override
    public UserPrincipal getUser() {
        return wrapped.getUser();
    }

    @Override
    public ClientPrincipal getClient() {
        return wrapped.getClient();
    }

    @Override
    public String[] getPermissions() {
        return wrapped.getPermissions();
    }

    @Override
    public void setPermissions(String... permissions) {
        wrapped.setPermissions(permissions);
    }

    @Override
    public String[] getRoles() {
        return wrapped.getRoles();
    }

    @Override
    public void setRoles(String... roles) {
        wrapped.setRoles(roles);
    }

    @Override
    public String[] getRules() {
        return wrapped.getRules();
    }

    @Override
    public void setRules(String... rules) {
        wrapped.setRules(rules);
    }

    @Override
    public String getAccessMode() {
        return wrapped.getAccessMode();
    }

    @Override
    public void setAccessMode(String accessMode) {
        wrapped.setAccessMode(accessMode);
    }

    @Override
    public boolean hasClient() {
        return wrapped.hasClient();
    }

    @Override
    public boolean isUserAuthenticated() {
        return wrapped.isUserAuthenticated();
    }

    @Override
    public boolean isClientAuthenticated() {
        return wrapped.isClientAuthenticated();
    }

    @Override
    public boolean isClientOnly() {
        return wrapped.isClientOnly();
    }

    @Override
    public boolean isFullyAuthenticated() {
        return wrapped.isFullyAuthenticated();
    }
}
