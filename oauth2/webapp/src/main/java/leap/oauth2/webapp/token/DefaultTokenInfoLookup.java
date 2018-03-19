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
                    return createTokenInfo(map);
                }else{
                    if(response.getStatus() == HTTP.SC_UNAUTHORIZED){
                        throw new Oauth2InvalidTokenException(response.getStatus(),error, Objects.toString(map.get("error_description")));
                    }else if(!response.is2xx()){
                        throw new OAuth2ResponseException(response.getStatus(),error, Objects.toString(map.get("error_description")));
                    }else {
                        throw new OAuth2InternalServerException("Auth server response error '" + error + "' : " + map.get("error_description"));
                    }
                }
            }
        }else{
            throw new OAuth2InternalServerException("Invalid response from auth server");
        }
    }

    protected TokenInfo createTokenInfo(Map<String, Object> map) {
        SimpleTokenInfo details = new SimpleTokenInfo();

        details.setClientId((String)map.remove("client_id"));
        details.setUserId((String)map.remove("user_id"));
        details.setCreated(System.currentTimeMillis());
        details.setExpiresIn(((Integer)map.remove("expires_in")));
        details.setScope((String)map.remove("scope"));

        return details;
    }
}
