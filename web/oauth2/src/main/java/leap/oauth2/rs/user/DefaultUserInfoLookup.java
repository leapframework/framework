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

package leap.oauth2.rs.user;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.codec.Base64;
import leap.lang.http.ContentTypes;
import leap.lang.http.Headers;
import leap.lang.http.client.HttpClient;
import leap.lang.http.client.HttpRequest;
import leap.lang.http.client.HttpResponse;
import leap.lang.json.JSON;
import leap.lang.json.JsonObject;
import leap.lang.json.JsonValue;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.OAuth2InternalServerException;
import leap.oauth2.rs.OAuth2ResServerConfig;
import leap.oauth2.rs.token.ResAccessToken;

public class DefaultUserInfoLookup implements UserInfoLookup {

    private static final Log log = LogFactory.get(DefaultUserInfoLookup.class);

    protected @Inject OAuth2ResServerConfig config;
    protected @Inject HttpClient            httpClient;

    @Override
    public UserInfo lookupUserInfo(ResAccessToken at) {
        if(null == config.getRemoteUserInfoEndpointUrl()) {
            throw new IllegalStateException("The userInfoEndpointUrl must be configured when use remote authz server");
        }

        HttpRequest request = httpClient.request(config.getRemoteUserInfoEndpointUrl())
                                        .addQueryParam("access_token", at.getToken());

        if(null != config.getResourceServerId()){
            request.addHeader(Headers.AUTHORIZATION, "Basic " +
                    Base64.encode(config.getResourceServerId()+":"+config.getResourceServerSecret()));
        }

        HttpResponse response = request.get();

        if(ContentTypes.APPLICATION_JSON_TYPE.isCompatible(response.getContentType())){
            String content = response.getString();

            log.debug("Received response : {}", content);

            try {
                JsonValue json = JSON.parse(content);

                if(!json.isMap()) {
                    throw new OAuth2InternalServerException("Invalid response from auth server : not a json map");
                }else{
                    JsonObject o = json.asJsonObject();

                    String error = o.getString ("error");
                    if(Strings.isEmpty(error)) {
                        return newUserInfo(o);
                    }else{
                        return null;
                    }
                }
            } catch (Exception e) {
                log.error(e);
                throw new OAuth2InternalServerException(e.getMessage());
            }
        }else{
            throw new OAuth2InternalServerException("Invalid response from auth server");
        }
    }

    protected UserInfo newUserInfo(JsonObject json) {
        UserInfo userInfo = new UserInfo();

        userInfo.setId(json.getString("sub"));
        userInfo.setName(json.getString("name"));
        userInfo.setLoginName(json.getString("login_name"));

        return userInfo;
    }

}