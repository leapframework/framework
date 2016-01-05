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
package leap.oauth2.server.endpoint.tokeninfo;

import java.util.Map.Entry;

import leap.core.annotation.Inject;
import leap.lang.http.ContentTypes;
import leap.lang.json.JsonWriter;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.OAuth2Params;
import leap.oauth2.server.token.OAuth2AccessToken;
import leap.oauth2.server.token.OAuth2TokenManager;
import leap.web.Request;
import leap.web.Response;

public class AccessTokenInfoHandler implements TokenInfoHandler {
    
    protected @Inject OAuth2TokenManager tokenManager;

    @Override
    public boolean handleTokenInfoRequest(Request request, Response response) throws Throwable {
        String accessToken = request.getParameter(OAuth2Params.ACCESS_TOKEN);
        if(null != accessToken) {
            if(accessToken.isEmpty()) {
                OAuth2Errors.invalidRequest(response, "token required");
                return true;
            }            

            OAuth2AccessToken at = tokenManager.loadAccessToken(accessToken);
            if(null == at) {
                OAuth2Errors.invalidRequest(response, "invalid token");
            }else if(at.isExpired()) {
                OAuth2Errors.invalidRequest(response, "invalid token");
                tokenManager.removeAccessToken(at);
            }else{
                writeTokenInfo(request, response, at);    
            }
            
            return true;
        }
        
        return false;
    }
    
    protected void writeTokenInfo(Request request, Response response, OAuth2AccessToken at) {
        response.setContentType(ContentTypes.APPLICATION_JSON_UTF8);
        
        JsonWriter w = response.getJsonWriter();
        
        w.startObject()
         
         .property("client_id",     at.getClientId())
         .property("user_id",       at.getUserId())
         .property("created",       at.getCreated())
         .property("expires_in",    at.getExpiresIn())
         .propertyOptional("scope", at.getScope());
         
         if(at.hasExtendedParameters()) {
             for(Entry<String, Object> entry : at.getExtendedParameters().entrySet()) {
                 w.propertyOptional(entry.getKey(), entry.getValue());
             }
         }
         
        w.endObject();
    }

}
