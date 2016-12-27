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
package leap.oauth2.wac.token;

import leap.core.Session;
import leap.core.annotation.Inject;
import leap.core.security.Authentication;
import leap.core.security.UserPrincipal;
import leap.lang.http.client.HttpClient;
import leap.lang.http.client.HttpRequest;
import leap.lang.http.client.HttpResponse;
import leap.lang.json.JSON;
import leap.oauth2.AuthorizationCodeInvalidException;
import leap.oauth2.ObtainAccessTokenFailedException;
import leap.oauth2.RefreshAccessTokenFailedException;
import leap.oauth2.RefreshTokenInvalidException;
import leap.oauth2.wac.OAuth2AccessToken;
import leap.oauth2.wac.OAuth2WebAppConfig;
import leap.web.Request;

import java.util.Map;
import java.util.UUID;

public class DefaultWacTokenManager implements WacTokenManager {
    
    private static final String KEY = "AccessToken_" + UUID.randomUUID().toString();
    
    protected @Inject OAuth2WebAppConfig config;
    protected @Inject HttpClient         hc;
    
    @Override
    public OAuth2AccessToken fetchAndSaveAccessToken(Request request, Authentication authc, String code) {
        HttpRequest req = 
                hc.request(config.getServerTokenEndpointUrl())
                  .addQueryParam("grant_type", "authorization_code")
                  .addQueryParam("code", code)
                  .addQueryParam("client_id", config.getClientId())
                  .addQueryParam("client_secret", config.getClientSecret());
        
        HttpResponse resp = req.post();
        if(resp.isOk()) {
            Map<String, Object> map = JSON.decode(resp.getString());

            if(!map.containsKey("error")) {
                SimpleWacAccessToken at = new SimpleWacAccessToken();

                at.setCreated(System.currentTimeMillis());
                at.setToken((String)map.get("access_token"));
                at.setRefreshToken((String)map.get("refresh_token"));
                at.setExpiresIn((Integer)map.get("expires_in"));
                at.setUserId(authc.getUser().getIdAsString());

                saveAccessToken(request, at);

                return at;
            }else{
                throw new AuthorizationCodeInvalidException("Cannot obtain access token, authorization code may be invalid : " +
                                                            map.get("error"));
            }
        }else {
            throw new ObtainAccessTokenFailedException("Obtain access token failed, " +
                    resp.getStatus() + " -> " + resp.getString());
        }
    }
    
    @Override
    public OAuth2AccessToken refreshAndSaveAccessToken(Request request) {
        OAuth2AccessToken old = resolveAccessToken(request, false);
        
        if(null == old) {
            throw new IllegalStateException("No current access token, cannot refresh");
        }
        
        return refreshAndSaveAccessToken(request, old);
    }

    @Override
    public OAuth2AccessToken refreshAndSaveAccessToken(Request request, OAuth2AccessToken old) {
        if(null != config.getTokenStore()) {
            config.getTokenStore().removeAccessToken(request, old);
        }
        
        HttpRequest req = 
                hc.request(config.getServerTokenEndpointUrl())
                  .addQueryParam("grant_type", "refresh_token")
                  .addQueryParam("refresh_token", old.getRefreshToken());
        
        HttpResponse resp = req.post();
        if(resp.isOk()) {
            String content = resp.getString();

            Map<String, Object> map = JSON.decode(content);

            if(!map.containsKey("error")) {
                SimpleWacAccessToken at = new SimpleWacAccessToken();

                at.setCreated(System.currentTimeMillis());
                at.setToken((String)map.get("access_token"));
                at.setRefreshToken((String)map.get("refresh_token"));
                at.setExpiresIn((Integer)map.get("expires_in"));
                at.setUserId(old.getUserId());

                saveAccessToken(request, at);

                return at;
            }else{
                if(config.getTokenStore() != null) {
                    config.getTokenStore().removeAccessToken(request, old);
                }
                throw new RefreshTokenInvalidException("Refresh access token failed : " + map.get("error"));
            }
        }else{
            throw new RefreshAccessTokenFailedException("Refresh access token failed : " + 
                    resp.getStatus() + " -> " + resp.getString());
        }

        
    }
    
    @Override
    public OAuth2AccessToken resolveAccessToken(Request request, boolean refreshIfExpired) {
        Session session = request.getSession(false);
        if(null == session) {
            return null;
        }
        
        OAuth2AccessToken at = (OAuth2AccessToken)session.getAttribute(KEY);

        if(null == at) {
            if(config.getTokenStore() != null) {
                at = config.getTokenStore().loadAccessToken(request);
                if(null != at) {
                    session.setAttribute(KEY, at);
                }
            }
        }

        if(null != at){
            UserPrincipal user = request.getUser();
            if(null != user && !user.getIdAsString().equals(at.getUserId())) {
                removeAccessToken(request);
                if(null != config.getTokenStore()) {
                    config.getTokenStore().removeAccessToken(request, at);
                }
                return null;
            }
            
            if(at.isExpired() && refreshIfExpired) {
                return refreshAndSaveAccessToken(request, at);
            }
        }
        
        return at;
    }

    public void saveAccessToken(Request request, OAuth2AccessToken at) {
        if(null != config.getTokenStore()) {
            config.getTokenStore().saveAccessToken(request, request.response(), at);
        }
        request.getSession(true).setAttribute(KEY, at);
    }

    public void removeAccessToken(Request request) {
        Session session = request.getSession(false);
        
        if(null != session) {
            session.removeAttribute(KEY);
        }
    }
}
