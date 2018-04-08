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
package leap.oauth2.server.endpoint;

import leap.core.annotation.Inject;
import leap.lang.Out;
import leap.lang.Strings;
import leap.oauth2.server.OAuth2Params;
import leap.oauth2.server.RequestOAuth2Params;
import leap.oauth2.server.endpoint.tokeninfo.TokenInfoHandler;
import leap.oauth2.server.endpoint.tokeninfo.TokenInfoResponseHandler;
import leap.oauth2.server.token.AuthzAccessToken;
import leap.web.App;
import leap.web.Handler;
import leap.web.Endpoint;
import leap.web.Request;
import leap.web.Response;
import leap.web.route.Routes;

/**
 * Not a standard endpoint, to obtain the access token's info issued by authorization server.
 * 
 * <p>
 * 
 * see <a href="https://developers.google.com/identity/protocols/OAuth2UserAgent#validatetoken">Google OAuth2.0 protocols</a>
 */
public class TokenInfoEndpoint extends AbstractAuthzEndpoint implements Endpoint,Handler {
    
    protected @Inject TokenInfoHandler[] handlers;

	@Override
    public void startEndpoint(App app, Routes routes) {
	    if(config.isEnabled()) {
	        sc.ignore(config.getTokenInfoEndpointPath());
	        routes.create()
	              .handle(config.getTokenInfoEndpointPath(), this).disableCsrf().enableCors()
	              .apply();
	    }
    }

	@Override
    public void handle(Request request, Response response) throws Throwable {

		OAuth2Params params = new RequestOAuth2Params(request);
		AuthzAccessToken at = null;
		Out<AuthzAccessToken> result = new Out<>();
	    for(TokenInfoHandler h : handlers) {

			if(h.handleTokenInfoRequest(request, response, params,result)) {
				if(result.isPresent()){
					at = result.get();
				}
	            break;
	        }
	        
	    }
		if(at != null){
			String responseType = params.getResponseType();
			if(Strings.isEmpty(responseType)){
				responseType = "default";
			}
			TokenInfoResponseHandler handler = factory.tryGetBean(TokenInfoResponseHandler.class,responseType);
			handler.writeTokenInfo(request,response,at);
		}
    }

}