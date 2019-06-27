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

import leap.lang.Strings;
import leap.lang.codec.Base64;
import leap.lang.http.Headers;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.webapp.OAuth2Constants;
import leap.web.Request;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;

public class DefaultTokenExtractor implements TokenExtractor {
	
	private static final Log log = LogFactory.get(DefaultTokenExtractor.class);

	@Override
	public Token extractTokenFromRequest(Request request) {
		String v = extractToken(request.getServletRequest());
		return extractTokenFromString(v,request.getParameters());
	}

	protected Token extractTokenFromString(String token, Map<String, Object> params) {
		if(null == token || token.length() == 0) {
			return null;
		}

		if(isJwt(token)){
			return new SimpleToken(OAuth2Constants.JWT_TYPE, token);
		}else{
            return new SimpleToken(token);
        }
	}

	protected boolean isJwt(String token){
        //todo :
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
			if ((value.toLowerCase().startsWith(OAuth2Constants.BEARER.toLowerCase()))) {
				String authHeaderValue = value.substring(OAuth2Constants.BEARER.length()).trim();
				int commaIndex = authHeaderValue.indexOf(',');
				if (commaIndex > 0) {
					authHeaderValue = authHeaderValue.substring(0, commaIndex);
				}
				return authHeaderValue;
			}else if(value.startsWith("Basic ")) {
				String usernameAndPassword = Base64.decode(value.substring(6));
				int index = usernameAndPassword.indexOf(':');
				if(index > 0) {
					String username = usernameAndPassword.substring(0, index);
					if(username.equals("oauth2")) {
						return usernameAndPassword.substring(index+1);
					}
				}
			}
		}

		return null;
	}

}
