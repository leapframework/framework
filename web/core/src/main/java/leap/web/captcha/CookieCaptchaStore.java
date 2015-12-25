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
package leap.web.captcha;

import javax.servlet.http.Cookie;

import leap.core.AppConfig;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.security.token.TokenExpiredException;
import leap.lang.Strings;
import leap.lang.codec.Base64;
import leap.lang.codec.MD5;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.Request;
import leap.web.Response;
import leap.web.cookie.AbstractCookieBean;

public class CookieCaptchaStore extends AbstractCookieBean implements CaptchaStore {
	
	private static final Log log = LogFactory.get(CookieCaptchaStore.class);
	
    protected @Inject @M CaptchaConfig captchaConfig;
    protected @Inject @M AppConfig     appConfig;
	
	protected String cookieName;
	
	@Override
	public void saveCaptchaToken(Request request, Response response, String token) {
		setCookie(request, response, encodeCaptchaToken(token));
	}
	
	@Override
    public boolean verifyCaptchaToken(Request request, Response response, String input) throws TokenExpiredException {
		Cookie cookie = getCookie(request);
		if(null == cookie || cookie.getValue().isEmpty()) {
			return false;
		}else{
			try{
				return verifyCaptchaToken(cookie.getValue(), input);	
			}finally{
				removeCookie(request, response, cookie);
			}
		}
    }
	
	protected boolean verifyCaptchaToken(String encodedTokenInCookie,String input) throws TokenExpiredException {
		
        for (int j = 0; j < encodedTokenInCookie.length() % 4; j++) {
        	encodedTokenInCookie = encodedTokenInCookie + "=";
        }
		
		if(!Base64.isBase64(encodedTokenInCookie)) {
			log.debug("The captcha cookie is not a valid base64 string");
			return false;
		}
		
		String   data  = Base64.decode(encodedTokenInCookie);
		String[] parts = Strings.split(data,':');
		if(parts.length != 2) {
			log.debug("The captcha cookie is invalid");
			return false;
		}
		
		String expires              = parts[0];
		String encodedTokenForInput = encodeCaptchaToken(input, expires);
		
		return data.equals(encodedTokenForInput);
	}

	@Override
    public String getCookieName() {
		if(null == cookieName) {
			cookieName = captchaConfig.getDefaultCookieName();
		}
	    return cookieName;
    }
	
    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }
	
	/**
	 * expirationTime + ":" + md5Hex(token + ":" + expirationTime + ":" + key)
	 */
	protected String encodeCaptchaToken(String token) {
		String expires = String.valueOf(System.currentTimeMillis() + (captchaConfig.getDefaultTokenExpires() * 1000));
		String encoded = encodeCaptchaToken(token, expires);
		
		//removes all the '=' characters
		StringBuilder sb = new StringBuilder(Base64.encode(encoded));
        while (sb.charAt(sb.length() - 1) == '=') {
            sb.deleteCharAt(sb.length() - 1);
        }
		
		return sb.toString();
	}
	
	protected String encodeCaptchaToken(String token, String expires) {
		if(captchaConfig.isTokenIgnoreCase()) {
			token = token.toLowerCase();
		}
		
		String key     = appConfig.ensureGetSecret();
		String content = token + ":" + expires +  ":" + key;
		String signed  = MD5.hex(Strings.getBytesUtf8(content));
		
		return expires + ":" + signed;
	}

}