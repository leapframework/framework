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
package leap.oauth2.server.token;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import leap.lang.expirable.TimeExpirableSeconds;
import leap.oauth2.server.OAuth2Constants;

public class SimpleAuthzAccessToken extends TimeExpirableSeconds implements AuthzAccessToken {
	
    private static final long serialVersionUID = -4427485425591978410L;
    
    protected String              token;
    protected String			  tokenType = OAuth2Constants.BEARER_TYPE;
    protected String			  refreshToken;
    protected String              clientId;
    protected String              userId;
	protected String 			  username;
	protected String 			  scope;
	protected Boolean			  authenticated = false;
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
	public void addExtendedParameters(String key, Object value) {
		putExtendedParameter(key,value);
	}

	@Override
	public void forEachExtendParams(BiConsumer<String, Object> consumer) {
		if(hasExtendedParameters()){
			extendedParameters.forEach(consumer);
		}
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	@Override
	public String getTokenType() {
		return tokenType;
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
	
	public void putExtendedParameter(String name, Object value) {
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