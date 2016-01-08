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


public interface AuthzTokenStore {
    
    /**
     * Saves the {@link AuthzAccessToken} in store.
     */
    void saveAccessToken(AuthzAccessToken token);
    
    /**
     * Saves the {@link AuthzRefreshToken} in store.
     */
    void saveRefreshToken(AuthzRefreshToken token);

    /**
     * Saves the {@link AuthzLoginToken} in store.
     */
    void saveLoginToken(AuthzLoginToken token);

    /**
     * Returns the {@link AuthzAccessToken} or <code>null</code>
     */
    AuthzAccessToken loadAccessToken(String accessToken);
    
    /**
     * Returns the {@link AuthzRefreshToken} or <code>null</code>
     */
    AuthzRefreshToken loadRefreshToken(String refreshToken);

    /**
     * Returns the {@link AuthzLoginToken} or <code>null</code>
     */
    AuthzLoginToken loadLoginToken(String loginToken);

    /**
     * Removes the access token.
     */
    void removeAccessToken(String accessToken);
    
    /**
     * Removes the refresh token.
     */
    void removeRefreshToken(String refreshToken);

    /**
     * Removes the login token.
     */
    void removeLoginToken(String loginToken);

    /**
     * Returns the removed {@link AuthzLoginToken} in store or returns <code>null</code> if not exists.
     */
    AuthzLoginToken removeAndLoadLoginToken(String loginToken);
    
    /**
     * Cleanup expired tokens (access token & refresh token).
     */
    void cleanupTokens();
    
}