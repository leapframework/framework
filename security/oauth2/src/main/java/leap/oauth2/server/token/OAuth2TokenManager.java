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

import leap.oauth2.server.authc.OAuth2Authentication;

public interface OAuth2TokenManager {
    
    /**
     * Creates a new token.
     */
    OAuth2AccessToken createAccessToken(OAuth2Authentication authc);

    /**
     * Creates a new token.
     */
	OAuth2AccessToken createAccessToken(OAuth2Authentication authc, OAuth2RefreshToken rt);
	
	/**
	 * Returns the {@link OAuth2AccessToken} or <code>null</code> if not exists.
	 */
	OAuth2AccessToken loadAccessToken(String accessToken);
	
	/**
	 * Returns the {@link OAuth2RefreshToken} or <code>null</code> if not exists.
	 */
	OAuth2RefreshToken loadRefreshToken(String refreshToken);
	
	/**
	 * Removes the acces token.
	 */
	void removeAccessToken(OAuth2AccessToken token);
	
	/**
	 * Removes the refresh token.
	 */
	void removeRefreshToken(OAuth2RefreshToken token);
	
}