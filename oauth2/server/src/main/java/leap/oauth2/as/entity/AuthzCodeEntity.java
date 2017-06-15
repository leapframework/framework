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
package leap.oauth2.as.entity;

import java.util.HashMap;
import java.util.Map;

import leap.oauth2.OAuth2ExpirableEntity;
import leap.orm.annotation.Column;
import leap.orm.annotation.Entity;
import leap.orm.annotation.Id;
import leap.orm.annotation.Table;

@Entity(name = "OAuth2AuthzCode", table = "oauth2_authz_code")
public class AuthzCodeEntity extends OAuth2ExpirableEntity {

    @Id
    @Token
    protected String code;

    @ClientId
    protected String clientId;

    @UserId
    protected String userId;
    
    protected String sessionId;
    
    @Column("ex_data")
	private Map<String,Object> exData=new HashMap<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

	public Map<String,Object> getExData() {
		return exData;
	}

	public void setExData(Map<String,Object> exData) {
		this.exData = exData;
	}

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}