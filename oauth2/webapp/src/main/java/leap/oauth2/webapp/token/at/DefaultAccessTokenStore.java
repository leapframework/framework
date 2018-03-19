/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.oauth2.webapp.token.at;

import leap.core.Session;
import leap.core.annotation.Inject;
import leap.web.Request;
import leap.web.security.authc.AuthenticationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple save the access token in session.
 */
public class DefaultAccessTokenStore implements AccessTokenStore {

    private static final String KEY = AccessToken.class.getName();

    protected @Inject AccessTokenRefresher refresher;
    protected @Inject AccessTokenFetcher fetcher;

    protected Map<String, AccessToken> accessTokenPool = new ConcurrentHashMap<>();
    
    @Override
    public AccessToken loadAccessToken(Request request, AuthenticationContext context) {
        Session session = request.getSession(false);
        if(null == session) {
            return null;
        }

        return (AccessToken)session.getAttribute(KEY);
    }

    @Override
    public void saveAccessToken(Request request, AuthenticationContext context, AccessToken at) {
        request.getSession(true).setAttribute(KEY, at);
    }

    @Override
    public AccessToken refreshAndSaveAccessToken(Request request, AuthenticationContext context, AccessToken old) {
        AccessToken theNew = refresher.refreshAccessToken(old);

        saveAccessToken(request, context, theNew);

        return theNew;
    }

    @Override
    public AccessToken loadAccessTokenByClientCredentials(String clientId, String clientSecret) {
        String key = clientId+":"+clientSecret;
        AccessToken token = getAccessToken(key);
        if(token == null){
            token = fetcher.fetchTokenByClientCredentials(clientId,clientSecret);
            saveAccessToken(key,token);
        }
        if(token.isExpired()){
            token = refreshAccessToken(token);
            saveAccessToken(key,token);
        }
        return token;
    }

    @Override
    public AccessToken loadAccessTokenByPassword(String clientId, String clientSecret, String username,
                                                 String password) {
        String key = username+":"+password+":"+clientId+":"+clientSecret;
        AccessToken token = getAccessToken(key);
        if(token == null){
            token = fetcher.fetchTokenByPassword(clientId,clientSecret,username,password);
            saveAccessToken(key,token);
        }
        if(token.isExpired()){
            token = refreshAccessToken(token);
            saveAccessToken(key,token);
        }
        return token;
    }

    protected AccessToken getAccessToken(String key){
        AccessToken token = accessTokenPool.get(key);
        return token;
    }
    
    protected void saveAccessToken(String key, AccessToken accessToken){
        accessTokenPool.put(key,accessToken);
    }
    
    @Override
    public AccessToken refreshAccessToken(AccessToken old) {
        AccessToken theNew = refresher.refreshAccessToken(old);
        return theNew;
    }
}