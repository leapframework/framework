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
package leap.oauth2.resource.token;

import java.util.Map;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
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
import leap.oauth2.resource.OAuth2ResourceConfig;

public class RemoteBearerResAccessTokenStore implements ResBearerAccessTokenStore, PostCreateBean {
    
    private static final Log log = LogFactory.get(RemoteBearerResAccessTokenStore.class);;

    protected @Inject OAuth2ResourceConfig config;
    protected @Inject HttpClient           httpClient;
    protected         String               remoteTokenInfoUrl;
    
    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    
    public String getRemoteTokenInfoUrl() {
        return remoteTokenInfoUrl;
    }

    public void setRemoteTokenInfoUrl(String remoteTokenInfoUrl) {
        this.remoteTokenInfoUrl = remoteTokenInfoUrl;
    }
    
    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        this.remoteTokenInfoUrl = Strings.trimToNull(config.getRemoteTokenInfoUrl());
    }

    @Override
    public Result<ResAccessTokenDetails> loadAccessTokenDetails(ResAccessToken credentials) {
        if(null == remoteTokenInfoUrl) {
            throw new IllegalStateException("The tokenInfoEndpoint must not be empty when using RemoteAccessTokenStore");
        }
        
        HttpRequest request =  httpClient.request(remoteTokenInfoUrl)
                                         .addQueryParam("access_token", credentials.getToken());

        HttpResponse response = request.get();
        
        if(ContentTypes.APPLICATION_JSON_TYPE.isCompatible(response.getContentType())){
            String content = response.getString();
            
            log.debug("Recevied response : {}", content);
            
            try {
                JsonValue json = JSON.decodeToJsonValue(content);
                
                if(!json.isMap()) {
                    return Result.err("Not a json object");
                }else{
                    Map<String, Object> map = json.asMap();
                    String error = (String)map.get("error");
                    if(Strings.isEmpty(error)) {
                        return Result.of(createAccessTokenDetails(map));
                    }else{
                        return Result.err(error + " : " + (String)map.get("error_description"));
                    }
                }
            } catch (Exception e) {
                log.error(e);
                return Result.err(e);
            }
        }else{
            return Result.err("Not a json response");
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
        
        //TODO : scope
        
        return details;
    }
}