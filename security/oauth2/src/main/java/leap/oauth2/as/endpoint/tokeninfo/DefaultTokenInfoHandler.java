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
package leap.oauth2.as.endpoint.tokeninfo;

import leap.core.annotation.Inject;
import leap.lang.Out;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.OAuth2Params;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.oauth2.as.token.AuthzTokenManager;
import leap.oauth2.as.token.TokenInfoAuthzProcessor;
import leap.web.Request;
import leap.web.Response;

public class DefaultTokenInfoHandler implements TokenInfoHandler {
    
    protected @Inject AuthzTokenManager tokenManager;
    protected @Inject TokenInfoAuthzProcessor[] processors;

    @Override
    public boolean handleTokenInfoRequest(Request request, Response response, OAuth2Params params, Out<AuthzAccessToken> out) throws Throwable {
        String accessToken = params.getAccessToken();
        if(null != accessToken) {
            if(accessToken.isEmpty()) {
                OAuth2Errors.invalidRequest(request,response,null,"token required");
                return true;
            }            

            AuthzAccessToken at = tokenManager.loadAccessToken(accessToken);
            if(null == at) {
                OAuth2Errors.invalidRequest(request,response,null,"invalid token");
            }else if(at.isExpired()) {
                OAuth2Errors.invalidRequest(request,response,null,"invalid token");
                tokenManager.removeAccessToken(at);
            }else{
                if(processors != null && processors.length > 0){
                    for(TokenInfoAuthzProcessor processor : processors){
                        if(!processor.process(request, response, params, at)){
                            return true;
                        }
                    }
                }
                out.set(at);
            }
            
            return true;
        }
        
        return false;
    }
}
