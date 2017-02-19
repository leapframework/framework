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

import java.io.Serializable;
import java.util.Map;
import java.util.function.BiConsumer;

import leap.lang.expirable.TimeExpirable;

/**
 * The token(s) issued by authorization server.
 */
public interface AuthzAccessToken extends TimeExpirable, Serializable {
	
	/**
	 * Required. Returns the token value of access token.
	 */
	String getToken();
	
	/**
	 * Returns the type of access token.
	 */
	default String getTokenType() {
	    return "Bearer";
	}
	
	/**
	 * Returns the client id.
	 */
	String getClientId();
	
	/**
	 * Returns the user id.
	 */
	String getUserId();

	/**
	 * Returns the user login name.
	 */
	String getUsername();
	
	/**
	 * Optional. 
	 */
	String getRefreshToken();
	
	/**
	 * Returns the expires in of refresh token.
	 */
	int getRefreshTokenExpiresIn();
	
	/**
	 * Returns <code>true</code> if the refresh token was expired.
	 */
	default boolean isRefreshTokenExpired() {
	    return getRefreshTokenExpiresIn() > 0 &&
	           (System.currentTimeMillis() - getCreated() >= getRefreshTokenExpiresIn() * 1000l);
	}
	
	/**
	 * Optional.
	 */
	String getScope();

	/**
	 * Optional.
     */
	void setScope(String scope);
	
	/**
	 * Optional.
	 */
	Map<String, Object> getExtendedParameters();

	/**
	 * Optional, add an extend parameter to this token.
	 */
	void addExtendedParameters(String key, Object value);

	/**
	 * Optional, ergodic all extend parameters of this token, if extended parameters is null, do nothing.
	 * @param consumer
	 */
	void forEachExtendParams(BiConsumer<String,Object> consumer);
	
	/**
	 * Returns <code>true</code> if the extended parameters is not empty.
	 */
	default boolean hasExtendedParameters() {
	    return null != getExtendedParameters() && !getExtendedParameters().isEmpty();
	}
	
	/**
	 * Returns <code>true</code> if the token has been issued to client only.
	 */
	default boolean isClientOnly() {
	    return null != getClientId() && null == getUserId();
	}

	/**
	 * Returns <code>true</code> if the client is authenticated.
	 * @return
	 */
	boolean isAuthenticated();
}