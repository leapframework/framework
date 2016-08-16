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
package leap.oauth2.rs.token;

import leap.core.annotation.Inject;
import leap.lang.Result;
import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.http.client.HttpClient;
import leap.lang.http.client.HttpRequest;
import leap.lang.http.client.HttpResponse;
import leap.lang.json.JSON;
import leap.lang.json.JsonValue;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.OAuth2InternalServerException;
import leap.oauth2.rs.OAuth2ResServerConfig;
import leap.oauth2.wac.OAuth2WebAppConfig;

import java.util.Map;

public class RemoteBearerResAccessTokenStore implements ResBearerAccessTokenStore {
    
    private static final Log log = LogFactory.get(RemoteBearerResAccessTokenStore.class);;

    protected @Inject OAuth2ResServerConfig config;
    protected @Inject HttpClient            httpClient;

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Result<ResAccessTokenDetails> loadAccessTokenDetails(ResAccessToken credentials) {
        if(null == config.getRemoteTokenInfoEndpointUrl()) {
            throw new IllegalStateException("The tokenInfoEndpointUrl must not be configured when use remote authz server");
        }
        HttpRequest request = httpClient.request(config.getRemoteTokenInfoEndpointUrl())
                                         .addQueryParam("access_token", credentials.getToken());
        if(null != config.getResourceServerId()){
            request.addHeader("rs_id",config.getResourceServerId());
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
                    Map<String, Object> map = json.asMap();
                    String error = (String)map.get("error");
                    if(Strings.isEmpty(error)) {
                        return Result.of(createAccessTokenDetails(map));
                    }else{
                        log.info("{} : {}", error, map.get("error_description"));
                        return Result.empty();
                    }
                }
            } catch (Exception e) {
                log.error(e);
                return Result.empty();
            }
        }else{
            throw new OAuth2InternalServerException("Invalid response from auth server");
        }
    }

    @Override
    public void removeAccessToken(ResAccessToken token) {
        //Do nothing.
    }

    protected ResAccessTokenDetails createAccessTokenDetails(Map<String, Object> map) {
        SimpleResAccessTokenDetails details = new SimpleResAccessTokenDetails();
        
        details.setClientId((String)map.remove("client_id"));
        details.setUserId((String)map.remove("user_id"));
        details.setCreated((Long)map.remove("created"));
        details.setExpiresIn(((Integer)map.remove("expires_in")) * 1000);
        details.setScope((String)map.remove("scope"));

        return details;
    }
}