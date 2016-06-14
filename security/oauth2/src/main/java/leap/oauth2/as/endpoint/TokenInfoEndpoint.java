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
package leap.oauth2.as.endpoint;

import leap.core.annotation.Inject;
import leap.core.security.token.jwt.JwtSigner;
import leap.core.security.token.jwt.RsaSigner;
import leap.lang.Out;
import leap.lang.Strings;
import leap.lang.http.ContentTypes;
import leap.lang.json.JsonWriter;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.OAuth2Params;
import leap.oauth2.RequestOAuth2Params;
import leap.oauth2.as.endpoint.tokeninfo.TokenInfoHandler;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.web.App;
import leap.web.Handler;
import leap.web.Endpoint;
import leap.web.Request;
import leap.web.Response;
import leap.web.route.Routes;

import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Not a standard endpoint, to obtain the access token's info issued by authorization server.
 * 
 * <p>
 * 
 * see <a href="https://developers.google.com/identity/protocols/OAuth2UserAgent#validatetoken">Google OAuth2.0 protocols</a>
 */
public class TokenInfoEndpoint extends AbstractAuthzEndpoint implements Endpoint,Handler {
    
    protected @Inject TokenInfoHandler[] handlers;

	protected JwtSigner signer;
    
	@Override
    public void startEndpoint(App app, Routes routes) {
	    if(config.isEnabled()) {
	        sc.ignore(config.getTokenInfoEndpointPath());
			signer = genSigner();
	        routes.create()
	              .get(config.getTokenInfoEndpointPath(), this)
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
			if(Strings.equalsIgnoreCase("jwt",responseType)){
				writeJwtTokenInfo(request,response,at);
			}else{
				writeTokenInfo(request,response,at);
			}
		}
    }

	protected void writeTokenInfo(Request request, Response response, AuthzAccessToken at) {
		response.setContentType(ContentTypes.APPLICATION_JSON_UTF8);

		JsonWriter w = response.getJsonWriter();

		w.startObject()

				.property("user_id",       at.getUserId())
				.property("username",      at.getUsername())
				.property("created",       at.getCreated())
				.property("expires_in",    at.getExpiresIn())
				.propertyOptional("scope", at.getScope());

		if(at.isAuthenticated()){
			w.property("client_id",     at.getClientId());
		}

		if(at.hasExtendedParameters()) {
			for(Entry<String, Object> entry : at.getExtendedParameters().entrySet()) {
				w.propertyOptional(entry.getKey(), entry.getValue());
			}
		}

		w.endObject();
	}

	protected void writeJwtTokenInfo(Request request, Response response, AuthzAccessToken at){
		if(signer == null){
			OAuth2Errors.invalidRequest(response, "not support jwt response type, server may not configure rsa private key!");
			return;
		}
		response.setContentType(ContentTypes.APPLICATION_JSON_UTF8);
		String jwtToken = signer.sign(createClaims(request,response,at),at.getExpiresIn());
		JsonWriter w = response.getJsonWriter();

		w.startObject()

				.property("jwt_token",     jwtToken);

		w.endObject();
	}

	protected Map<String,Object> createClaims(Request request, Response response, AuthzAccessToken at) {
		Map<String,Object> map = new LinkedHashMap<>();

		//todo :
		if(at.isAuthenticated()){
			map.put("client_id",at.getClientId());
		}
		map.put("username",at.getUsername());
		map.put("scope", at.getScope());
		map.put("expires_in", at.getExpiresIn());
		map.put("expires",at.getCreated()+at.getExpiresIn()*1000L);

		return map;
	}

	protected JwtSigner genSigner(){
		PrivateKey privateKey = config.getPrivateKey();
		if(privateKey instanceof RSAPrivateKey){
			return new RsaSigner((RSAPrivateKey)privateKey);
		}
		return null;
	}
}