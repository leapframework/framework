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

import leap.oauth2.as.AuthzAuthentication;

public interface AuthzTokenManager {
    
    /**
     * Creates a new token.
     */
    AuthzAccessToken createAccessToken(AuthzAuthentication authc);

    /**
     * Creates a new token.
     */
	AuthzAccessToken createAccessToken(AuthzAuthentication authc, AuthzRefreshToken rt);
	
	/**
	 * Returns the {@link AuthzAccessToken} or <code>null</code> if not exists.
	 */
	AuthzAccessToken loadAccessToken(String accessToken);
	
	/**
	 * Returns the {@link AuthzRefreshToken} or <code>null</code> if not exists.
	 */
	AuthzRefreshToken loadRefreshToken(String refreshToken);
	
	/**
	 * Removes the acces token.
	 */
	void removeAccessToken(AuthzAccessToken token);
	
	/**
	 * Removes the refresh token.
	 */
	void removeRefreshToken(AuthzRefreshToken token);
	
}