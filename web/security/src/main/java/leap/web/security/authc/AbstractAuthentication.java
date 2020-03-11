/*
 * Copyright 2013 the original author or authors.
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
package leap.web.security.authc;

import leap.core.security.Authentication;
import leap.core.security.ClientPrincipal;
import leap.core.security.UserPrincipal;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAuthentication implements Authentication {

    protected String   token;
    protected boolean  rememberMe;
    protected String[] permissions;
    protected String[] roles;
    protected String[] rules;
    protected String   accessMode;
    protected Map<String, Object> attributes = new HashMap<String, Object>();

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public void setToken(String token) throws IllegalStateException {
        if(null != this.token) {
            throw new IllegalStateException("Token already exists");
        }
        this.token = token;
    }

    @Override
    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    @Override
    public String[] getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(String... permissions) {
        this.permissions = permissions;
    }

    @Override
    public String[] getRoles() {
        return roles;
    }

    @Override
    public void setRoles(String... roles) {
        this.roles = roles;
    }

    @Override
    public String[] getRules() {
        return rules;
    }

    @Override
    public void setRules(String[] rules) {
        this.rules = rules;
    }

    @Override
    public String getAccessMode() {
        return accessMode;
    }

    @Override
    public void setAccessMode(String accessMode) {
        this.accessMode = accessMode;
    }

    @Override
    public String toString() {
        UserPrincipal   user   = getUser();
        ClientPrincipal client = getClient();

        StringBuilder s = new StringBuilder();
        s.append("Authc[user=")
         .append(null == user ? "n/a" : user.getLoginName())
         .append(",client=")
         .append(null == client ? "n/a" : client.getIdAsString())
         .append("]") ;

        return s.toString();
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

}