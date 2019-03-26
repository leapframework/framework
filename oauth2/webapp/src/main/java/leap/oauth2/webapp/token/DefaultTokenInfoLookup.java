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

package leap.oauth2.webapp.token;

import leap.core.annotation.Inject;
import leap.core.security.SimpleUserPrincipal;
import leap.lang.Strings;
import leap.lang.codec.Base64;
import leap.lang.http.ContentTypes;
import leap.lang.http.HTTP;
import leap.lang.http.Headers;
import leap.lang.http.client.HttpClient;
import leap.lang.http.client.HttpRequest;
import leap.lang.http.client.HttpResponse;
import leap.lang.json.JSON;
import leap.lang.json.JsonValue;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.time.StopWatch;
import leap.oauth2.webapp.OAuth2InternalServerException;
import leap.oauth2.webapp.OAuth2Config;
import leap.oauth2.webapp.OAuth2ResponseException;
import leap.oauth2.webapp.Oauth2InvalidTokenException;

import java.util.Map;
import java.util.Objects;

public class DefaultTokenInfoLookup implements TokenInfoLookup {

    private static final Log log = LogFactory.get(DefaultTokenInfoLookup.class);

    protected @Inject OAuth2Config config;
    protected @Inject HttpClient   httpClient;

    @Override
    public TokenInfo lookupByAccessToken(String at) {
        if(null == config.getTokenInfoUrl()) {
            throw new IllegalStateException("The tokenInfoUrl must be configured");
        }

        HttpRequest request = httpClient.request(config.getTokenInfoUrl())
                .addQueryParam("access_token", at)
                .setMethod(HTTP.Method.GET);

        if(null != config.getClientId()){
            request.addHeader(Headers.AUTHORIZATION, "Basic " +
                    Base64.encode(config.getClientId()+":"+config.getClientSecret()));
        }

        StopWatch sw = StopWatch.startNew();
        HttpResponse response = request.send();
        
        if(ContentTypes.APPLICATION_JSON_TYPE.isCompatible(response.getContentType())){
            String content = response.getString();

            log.debug("Received response : {}", content);

            JsonValue json = JSON.parse(content);

            if(!json.isMap()) {
                throw new OAuth2InternalServerException("Invalid response from auth server : not a json map");
            }else{
                Map<String, Object> map = json.asMap();
                String error = (String)map.get("error");
                if(Strings.isEmpty(error)) {
                    log.info("Request token info from oauth2 server use {}ms", sw.getElapsedMilliseconds());
                    return createTokenInfo(map);
                }else{
                    String desc  = Objects.toString(map.get("error_description"));
                    if(Strings.isEmpty(desc)) {
                        log.error("Err get token info from '{}' : {} - {}", config.getTokenUrl(), response.getStatus(), content);
                    }else {
                        log.error("Err get token info from '{}' : {} - {}", config.getTokenUrl(), response.getStatus(), desc);
                    }
                    if(response.getStatus() == HTTP.SC_UNAUTHORIZED){
                        throw new Oauth2InvalidTokenException(response.getStatus(),error, desc);
                    }else if(!response.is2xx()){
                        throw new OAuth2ResponseException(response.getStatus(),error, desc);
                    }else {
                        throw new OAuth2InternalServerException("Auth server response error '" + error + "' : " + desc);
                    }
                }
            }
        }else{
            throw new OAuth2InternalServerException("Invalid response from auth server");
        }
    }

    protected TokenInfo createTokenInfo(Map<String, Object> map) {
        SimpleTokenInfo info = new SimpleTokenInfo();

        info.setClientId((String)map.get("client_id"));
        info.setUserId((String)map.get("user_id"));
        info.setCreated(System.currentTimeMillis());
        info.setExpiresIn(((Integer)map.get("expires_in")));
        info.setScope((String)map.get("scope"));
        info.setClaims(map);

        String username = (String)map.get("username");
        if(null != username) {
            SimpleUserPrincipal user = new SimpleUserPrincipal();
            user.setId(info.getUserId());
            user.setLoginName(username);
            user.setName(username);
            info.setUserInfo(user);
        }

        return info;
    }
}
