/*
 * Copyright 2015 the original author or authors.
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
package leap.oauth2.server.entity;

import leap.lang.expirable.TimeExpirable;
import leap.orm.annotation.Id;
import leap.orm.annotation.Table;

import java.sql.Timestamp;

@Table("oauth2_sso_login")
public class OAuth2SSOLoginEntity implements OAuth2Entity {

    @Id
    protected String sessionId;

    @Id
    @Created
    protected Timestamp loginTime;

    @Uri
    protected String logoutUri;

    @ClientId
    protected String clientId;

    protected boolean initial;

    @Expiration
    protected Timestamp expiration;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Timestamp getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Timestamp loginTime) {
        this.loginTime = loginTime;
    }

    public String getLogoutUri() {
        return logoutUri;
    }

    public void setLogoutUri(String logoutUri) {
        this.logoutUri = logoutUri;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isInitial() {
        return initial;
    }

    public void setInitial(boolean initial) {
        this.initial = initial;
    }

    public Timestamp getExpiration() {
        return expiration;
    }

    public void setExpiration(Timestamp expiration) {
        this.expiration = expiration;
    }

    public void setExpiration(TimeExpirable expirable) {
        this.expiration = new Timestamp(expirable.getCreated() + expirable.getExpiresIn() * 1000l);
    }
}