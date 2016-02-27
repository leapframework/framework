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

import leap.core.web.RequestBase;
import leap.web.Request;

import javax.servlet.http.HttpServletRequest;

public class CSRF {
	static final String GENERATED_TOKEN_KEY = CsrfToken.class.getName();
	static final String REQUEST_TOKEN_KEY   = CsrfToken.class.getName() + "$req";
	static final String CSRF_IGNORED_KEY    = CsrfToken.class.getName() + "$ignored";

    /**
     * Returns the generated csrf token stored in http request.
     */
	public static CsrfToken getGeneratedToken(HttpServletRequest request) {
		return (CsrfToken)request.getAttribute(GENERATED_TOKEN_KEY);
	}

    /**
     * Returns the generated csrf token stored in http request.
     */
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
		request.setAttribute(CSRF_IGNORED_KEY, Boolean.TRUE);
	}

    /**
     * Returns true if csrf checking is ignored in the http request.
     */
	public static boolean isIgnored(HttpServletRequest request) {
		Object v = request.getAttribute(CSRF_IGNORED_KEY);
		return Boolean.TRUE == v;
	}

	/**
	 * Ignores the csrf checking in the http request.
	 */
	public static void ignore(Request request) {
		request.setAttribute(CSRF_IGNORED_KEY, Boolean.TRUE);
	}

    /**
     * Returns true if csrf checking is ignored in the http request.
     */
    public static boolean isIgnored(Request request) {
		Object v = request.getAttribute(CSRF_IGNORED_KEY);
		return Boolean.TRUE == v;
	}

	protected CSRF() {
		
	}
}