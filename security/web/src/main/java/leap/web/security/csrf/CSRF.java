/*
 * Copyright 2014 the original author or authors.
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
package leap.web.security.csrf;

import javax.servlet.http.HttpServletRequest;

import leap.core.web.RequestBase;

public class CSRF {
	static final String GENERATED_TOKEN_KEY = CsrfToken.class.getName();
	static final String REQUEST_TOKEN_KEY   = CsrfToken.class.getName() + "$REQ";
	static final String CSRF_IGNROED_KEY    = CsrfToken.class.getName() + "$IGNORED";

	public static CsrfToken getGeneratedToken(HttpServletRequest request) {
		return (CsrfToken)request.getAttribute(GENERATED_TOKEN_KEY);
	}
	
	public static CsrfToken getGeneratedToken(RequestBase request) {
		return (CsrfToken)request.getAttribute(GENERATED_TOKEN_KEY);
	}
	
	static void setGeneratedToken(RequestBase request, CsrfToken token) {
		request.setAttribute(GENERATED_TOKEN_KEY, token);
	}
	
	public static String getRequestToken(RequestBase request) {
		return (String)request.getAttribute(REQUEST_TOKEN_KEY);
	}
	
	public static void setRequestToken(RequestBase request, String token) {
		request.setAttribute(REQUEST_TOKEN_KEY, token);
	}
	
	public static void setRequestToken(HttpServletRequest request, String token) {
		request.setAttribute(REQUEST_TOKEN_KEY, token);
	}
	
	/**
	 * Ignores the csrf checking in the http request.
	 */
	public static void ignore(HttpServletRequest request) {
		request.setAttribute(CSRF_IGNROED_KEY, Boolean.TRUE);
	}
	
	public static boolean isIgnored(HttpServletRequest request) {
		Object v = request.getAttribute(CSRF_IGNROED_KEY);
		return Boolean.TRUE == v;
	}
	
	protected CSRF() {
		
	}
}
