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

import leap.core.security.ClientPrincipal;
import leap.core.security.UserPrincipal;

public abstract  class AbstractAuthentication implements Authentication{

    protected String token;

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
    public String toString() {
        UserPrincipal   user   = getUserPrincipal();
        ClientPrincipal client = getClientPrincipal();

        StringBuilder s = new StringBuilder();
        s.append("Authc[user=")
         .append(null == user ? "n/a" : user.getLoginName())
         .append(",client=")
         .append(null == client ? "n/a" : client.getIdAsString())
         .append("]") ;

        return s.toString();
    }
}