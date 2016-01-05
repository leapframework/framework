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


public interface OAuth2TokenStore {
    
    /**
     * Saves the {@link OAuth2AccessToken} in store.
     */
    void saveAccessToken(OAuth2AccessToken token);
    
    /**
     * Saves the {@link OAuth2RefreshToken} in store.
     */
    void saveRefreshToken(OAuth2RefreshToken token);

    /**
     * Returns the {@link OAuth2AccessToken} or <code>null</code>
     */
    OAuth2AccessToken loadAccessToken(String accessToken);
    
    /**
     * Returns the {@link OAuth2RefreshToken} or <code>null</code>
     */
    OAuth2RefreshToken loadRefreshToken(String refreshToken);
    
    /**
     * Removes the access token.
     */
    void removeAccessToken(String accessToken);
    
    /**
     * Removes the refresh token.
     */
    void removeRefreshToken(String refreshToken);
    
    /**
     * Cleanup expired tokens (access token & refresh token).
     */
    void cleanupTokens();
    
}