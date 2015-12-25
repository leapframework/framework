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
package leap.web.cookie;

import javax.servlet.http.Cookie;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.web.Request;
import leap.web.Response;
import leap.web.config.WebConfig;

public abstract class AbstractCookieBean {
	
	protected @Inject WebConfig webConfig;

	public AbstractCookieBean() {
		super();
	}

	protected int getCookieMaxAge(Request request) {
		String expiresParameter = getCookieExpiresParameter();
		if(null != expiresParameter) {
			String expires = request.getParameter(expiresParameter);
			if(!Strings.isEmpty(expires)){
				return Converts.toInt(expires);
			}
		}
		return getCookieExpires();
	}

	protected String getCookieName(Request request) {
		if(isCookieCrossContext()) {
			return getCookieName();
		}else{
			String contextPath = request.getContextPath();
			if(contextPath.length() == 0) {
				return getCookieName() + "_root";
			}else{
				return getCookieName() + "_" + contextPath.substring(1);
			}
		}
	}

	protected String getCookiePath(Request request) {
		if(isCookieCrossContext()) {
			return "/";
		}else{
	        return request.getContextPath() + "/";
		}
	}

	protected String getCookieDomain(Request request) {
		String domain = getCookieDomain();
		if(Strings.isEmpty(domain)) {
			return null;
		}
		
		String host = "." + request.getServletRequest().getServerName();
		if(Strings.endsWith(host, domain)) {
			return domain;
		}
		
		return null;
	}

	public Cookie getCookie(Request request) {
		return request.getCookie(getCookieName(request));
	}

	public void setCookie(Request request, Response response, String value) {
		setCookie(request, response, value, getCookieMaxAge(request));
	}

	public void setCookie(Request request, Response response, String value, int maxAge) {
		Cookie cookie = new Cookie(getCookieName(request), value);
	
		cookie.setPath(getCookiePath(request));
		cookie.setMaxAge(maxAge);
		cookie.setHttpOnly(isCookieHttpOnly());
		cookie.setSecure(request.isSecure());
		
		String domain = getCookieDomain(request);
		if(null != domain) {
			cookie.setDomain(getCookieDomain(request));	
		}
		
		response.addCookie(cookie);
	}

	public boolean removeCookie(Request request, Response response) {
		Cookie cookie = getCookie(request);
		if(null != cookie){
			removeCookie(request, response, cookie);
			return true;
		}
		return false;
	}

	protected void removeCookie(Request request, Response response, Cookie cookie) {
		cookie.setPath(getCookiePath(request));
		response.removeCookie(cookie);
	}

	public boolean isCookieHttpOnly() {
		return true;
	}

	public String getCookieDomain() {
		return webConfig.getCookieDomain();
	}

	public boolean isCookieCrossContext() {
		return false;
	}
	
	public String getCookieExpiresParameter() {
		return null;
	}
	
	public int getCookieExpires() {
		return -1;
	}

	public abstract String getCookieName();
}