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
package leap.oauth2.as.store;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import leap.lang.Args;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.as.client.AuthzClient;
import leap.oauth2.as.client.SimpleAuthzClient;
import leap.oauth2.as.code.AuthzCode;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.oauth2.as.token.AuthzRefreshToken;

public class DefaultAuthzInMemoryStore implements AuthzInMemoryStore {
    
    private static final Log log = LogFactory.get(DefaultAuthzInMemoryStore.class);
    
    protected boolean                        enabled;
    protected Map<String, AuthzClient>       clients       = new ConcurrentHashMap<>();
    protected Map<String, AuthzCode>         codes         = new ConcurrentHashMap<>();
    protected Map<String, AuthzAccessToken>  accessTokens  = new ConcurrentHashMap<>();
    protected Map<String, AuthzRefreshToken> refreshTokens = new ConcurrentHashMap<>();

    @Override
    public AuthzClient loadClient(String clientId) {
        return clients.get(clientId);
    }

    @Override
    public AuthzInMemoryStore addClient(AuthzClient client) {
        clients.put(client.getId(), client);
        return this;
    }
    
    @Override
    public AuthzInMemoryStore addClient(String clientId, String clientSecret, String redirectUri) {
        Args.notEmpty(clientId, "client id");
        Args.notEmpty(clientSecret, "client secret");
        Args.notEmpty(redirectUri, "redirect uri");
        
        SimpleAuthzClient client = new SimpleAuthzClient();
        client.setId(clientId);
        client.setSecret(clientSecret);
        client.setRedirectUri(redirectUri);
        
        return addClient(client);
    }

    @Override
    public AuthzClient removeClient(String clientId) {
        return clients.remove(clientId);
    }

    @Override
    public void saveAuthorizationCode(AuthzCode code) {
        codes.put(code.getCode(), code);
    }

    @Override
    public AuthzCode loadAuthorizationCode(String code) {
        return codes.get(code);
    }

    @Override
    public AuthzCode removeAuthorizationCode(String code) {
        return codes.remove(code);
    }

    @Override
    public void removeAuthorizationCode(AuthzCode code) {
        if(null == code){
            return;
        }
        codes.remove(code.getCode());
    }
    
    @Override
    public void saveAccessToken(AuthzAccessToken token) {
        accessTokens.put(token.getToken(), token);
    }
    
    @Override
    public void saveRefreshToken(AuthzRefreshToken token) {
        refreshTokens.put(token.getToken(), token);
    }

    @Override
    public AuthzAccessToken loadAccessToken(String accessToken) {
        return accessTokens.get(accessToken);
    }

    @Override
    public AuthzRefreshToken loadRefreshToken(String refreshToken) {
        return refreshTokens.get(refreshToken);
    }
    
    @Override
    public void removeAccessToken(String accessToken) {
        accessTokens.remove(accessToken);
    }

    @Override
    public void removeRefreshToken(String refreshToken) {
        refreshTokens.remove(refreshToken);
    }

    @Override
    public void cleanupTokens() {
        for(Entry<String, AuthzAccessToken> entry : accessTokens.entrySet()) {
            AuthzAccessToken at = entry.getValue();
            if(at.isExpired()) {
                log.debug("Removing the expired access token : {}", at.getToken());
                accessTokens.remove(entry.getKey());
            }
        }
        
        for(Entry<String, AuthzRefreshToken> entry : refreshTokens.entrySet()) {
            AuthzRefreshToken rt = entry.getValue();
            if(rt.isExpired()) {
                log.debug("Removing the expired refresh token : {}", rt.getToken());
                refreshTokens.remove(entry.getKey());
            }
        }
    }

    @Override
    public void cleanupAuthorizationCodes() {
        for(Entry<String, AuthzCode> entry : codes.entrySet()) {
            AuthzCode code = entry.getValue();
            if(code.isExpired()) {
                log.debug("Removing the expired authorization code : {}", code.getCode());
                codes.remove(entry.getKey());
            }
        }
    }
    
}