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
package leap.oauth2.server.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import leap.lang.Args;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.server.client.OAuth2Client;
import leap.oauth2.server.client.SimpleOAuth2Client;
import leap.oauth2.server.code.OAuth2AuthzCode;
import leap.oauth2.server.sso.OAuth2SSOLogin;
import leap.oauth2.server.sso.OAuth2SSOSession;
import leap.oauth2.server.token.OAuth2AccessToken;
import leap.oauth2.server.token.OAuth2RefreshToken;

public class DefaultOAuth2InMemoryStore implements OAuth2InMemoryStore {
    
    private static final Log log = LogFactory.get(DefaultOAuth2InMemoryStore.class);
    
    protected boolean                        enabled;
    protected Map<String, OAuth2Client>         clients       = new ConcurrentHashMap<>();
    protected Map<String, OAuth2AuthzCode>      codes         = new ConcurrentHashMap<>();
    protected Map<String, OAuth2AccessToken>    accessTokens  = new ConcurrentHashMap<>();
    protected Map<String, OAuth2RefreshToken>   refreshTokens = new ConcurrentHashMap<>();
    protected Map<String, OAuth2SSOSession>     ssoSessions   = new ConcurrentHashMap<>();
    protected Map<String, List<OAuth2SSOLogin>> ssoLogins     = new ConcurrentHashMap<>();

    @Override
    public OAuth2Client loadClient(String clientId) {
        return clients.get(clientId);
    }

    @Override
    public OAuth2InMemoryStore addClient(OAuth2Client client) {
        clients.put(client.getId(), client);
        return this;
    }
    
    @Override
    public OAuth2InMemoryStore addClient(String clientId, String clientSecret, String redirectUri) {
        Args.notEmpty(clientId, "client id");
        Args.notEmpty(clientSecret, "client secret");
        Args.notEmpty(redirectUri, "redirect uri");
        
        SimpleOAuth2Client client = new SimpleOAuth2Client();
        client.setId(clientId);
        client.setSecret(clientSecret);
        client.setRedirectUri(redirectUri);
        
        return addClient(client);
    }

    @Override
    public OAuth2Client removeClient(String clientId) {
        return clients.remove(clientId);
    }

    @Override
    public void saveAuthorizationCode(OAuth2AuthzCode code) {
        codes.put(code.getCode(), code);
    }

    @Override
    public OAuth2AuthzCode loadAuthorizationCode(String code) {
        return codes.get(code);
    }

    @Override
    public OAuth2AuthzCode removeAuthorizationCode(String code) {
        return codes.remove(code);
    }

    @Override
    public void removeAuthorizationCode(OAuth2AuthzCode code) {
        if(null == code){
            return;
        }
        codes.remove(code.getCode());
    }
    
    @Override
    public void saveAccessToken(OAuth2AccessToken token) {
        accessTokens.put(token.getToken(), token);
    }
    
    @Override
    public void saveRefreshToken(OAuth2RefreshToken token) {
        refreshTokens.put(token.getToken(), token);
    }

    @Override
    public OAuth2AccessToken loadAccessToken(String accessToken) {
        return accessTokens.get(accessToken);
    }

    @Override
    public OAuth2RefreshToken loadRefreshToken(String refreshToken) {
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
        for(Entry<String, OAuth2AccessToken> entry : accessTokens.entrySet()) {
            OAuth2AccessToken at = entry.getValue();
            if(at.isExpired()) {
                log.debug("Removing the expired access token : {}", at.getToken());
                accessTokens.remove(entry.getKey());
            }
        }
        
        for(Entry<String, OAuth2RefreshToken> entry : refreshTokens.entrySet()) {
            OAuth2RefreshToken rt = entry.getValue();
            if(rt.isExpired()) {
                log.debug("Removing the expired refresh token : {}", rt.getToken());
                refreshTokens.remove(entry.getKey());
            }
        }
    }

    @Override
    public void cleanupAuthorizationCodes() {
        for(Entry<String, OAuth2AuthzCode> entry : codes.entrySet()) {
            OAuth2AuthzCode code = entry.getValue();
            if(code.isExpired()) {
                log.debug("Removing the expired authorization code : {}", code.getCode());
                codes.remove(entry.getKey());
            }
        }
    }

    @Override
    public OAuth2SSOSession loadSessionByToken(String username, String token) {
        return ssoSessions.get(token);
    }

    @Override
    public List<OAuth2SSOLogin> loadLoginsInSession(OAuth2SSOSession session) {
        List<OAuth2SSOLogin> logins = ssoLogins.get(session.getId());
        if(null == logins) {
            return new ArrayList<>();
        }else{
            return logins;
        }
    }

    @Override
    public void saveSession(OAuth2SSOSession session, OAuth2SSOLogin initialLogin) {
        if(ssoSessions.containsKey(session.getToken())) {
            throw new IllegalStateException("Duplicated sso token '" + session.getToken() + "'");
        }
        ssoSessions.put(session.getToken(), session);

        List<OAuth2SSOLogin> logins = new ArrayList<>();
        logins.add(initialLogin);
        ssoLogins.put(session.getId(), logins);
    }

    @Override
    public void saveLogin(OAuth2SSOSession session, OAuth2SSOLogin newlogin) {
        List<OAuth2SSOLogin> logins = ssoLogins.get(session.getId());
        if(null == logins) {
            throw new IllegalStateException("Session not exists, cannot save new login");
        }
        logins.add(newlogin);
    }

    @Override
    public void cleanupSSO() {
        for(Entry<String, OAuth2SSOSession> entry : ssoSessions.entrySet()) {
            OAuth2SSOSession session = entry.getValue();
            if(session.isExpired()) {
                log.debug("Removing the expired sso session of user '{}", session.getUsername(), session.getUsername());
                ssoSessions.remove(entry.getKey());
                ssoLogins.remove(session.getId());
            }
        }
    }


}