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

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import leap.lang.Strings;
import leap.lang.http.Headers;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.OAuth2Constants;
import leap.web.Request;

public class BearerAccessTokenExtractor implements AccessTokenExtractor {
	
	private static final Log log = LogFactory.get(BearerAccessTokenExtractor.class);

	@Override
	public AccessToken extractTokenFromRequest(Request request) {
		String v = extractToken(request.getServletRequest());
		return extractTokenFromString(v,request.getParameters());
	}

	@Override
	public AccessToken extractTokenFromString(String token, Map<String, Object> params) {
		if(null == token || token.length() == 0) {
			return null;
		}

		if(isJwt(token)){
			return new JwtAccessToken(OAuth2Constants.BEARER_TYPE,token);
		}
		return new SimpleAccessToken(OAuth2Constants.BEARER_TYPE, token);
	}


	protected boolean isJwt(String token){
		return Strings.contains(token,".");
	}

	protected String extractToken(HttpServletRequest request) {
		// first check the header...
		String token = extractHeaderToken(request);

		// bearer type allows a request parameter as well
		if (token == null) {
			log.debug("Token not found in headers. Trying request parameters.");
			token = request.getParameter(OAuth2Constants.ACCESS_TOKEN);
			if (token == null) {
				log.debug("Token not found in request parameters.  Not an OAuth2 request.");
			}
		}

		return token;
	}

	protected String extractHeaderToken(HttpServletRequest request) {
		Enumeration<String> headers = request.getHeaders(Headers.AUTHORIZATION);
		while (headers.hasMoreElements()) { // typically there is only one (most servers enforce that)
			String value = headers.nextElement();
			if ((value.toLowerCase().startsWith(OAuth2Constants.BEARER_TYPE.toLowerCase()))) {
				String authHeaderValue = value.substring(OAuth2Constants.BEARER_TYPE.length()).trim();
				int commaIndex = authHeaderValue.indexOf(',');
				if (commaIndex > 0) {
					authHeaderValue = authHeaderValue.substring(0, commaIndex);
				}
				return authHeaderValue;
			}
		}

		return null;
	}

}
