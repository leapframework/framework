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
package leap.oauth2.as.token;

import java.util.LinkedHashMap;
import java.util.Map;

import leap.lang.expirable.TimeExpirableSeconds;

public class SimpleAuthzAccessToken extends TimeExpirableSeconds implements AuthzAccessToken {
	
    private static final long serialVersionUID = -4427485425591978410L;
    
    protected String              token;
    protected String              clientId;
    protected String              userId;
	protected String 			   username;
	protected String 			  refreshToken;
	protected int                 refreshTokenExpiresIn;
	protected String 			  scope;
	protected Boolean				authenticated = false;
	protected Map<String, Object> extendedParameters;

	public String getToken() {
		return token;
	}

	public void setToken(String tokenString) {
		this.token = tokenString;
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

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	
	public int getRefreshTokenExpiresIn() {
        return refreshTokenExpiresIn;
    }

    public void setRefreshTokenExpiresIn(int refreshTokenExpiresIn) {
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

    public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

    public Map<String, Object> getExtendedParameters() {
		return extendedParameters;
	}

	@Override
	public boolean isAuthenticated() {
		return authenticated;
	}

	public Boolean getAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(Boolean authenticated) {
		this.authenticated = authenticated;
	}

	public void setExtendedParameters(Map<String, Object> extendedParameters) {
		this.extendedParameters = extendedParameters;
	}
	
	public void putExtendedParameter(String name, String value) {
		if(null == extendedParameters) {
			extendedParameters = new LinkedHashMap<>();
		}
		extendedParameters.put(name, value);
	}

	@Override
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}