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
package leap.web.security.authc;

import leap.core.security.Authentication;
import leap.core.security.UserPrincipal;
import leap.lang.Result;
import leap.lang.Strings;
import leap.lang.codec.Base64;
import leap.lang.codec.MD5;
import leap.lang.convert.Converts;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.user.*;

import javax.servlet.http.Cookie;

public class DefaultRememberMeManager extends CookieBasedAuthenticationResolver implements RememberMeManager {
	
	private static final Log log = LogFactory.get(DefaultRememberMeManager.class);
	
	@Override
    public Result<Authentication> resolveAuthentication(Request request, Response response, AuthenticationContext context) {
		Cookie cookie = getCookie(request);
		
		if(null == cookie){
			return Result.empty();
		}
		
		if(Strings.isEmpty(cookie.getValue())){
			//invalid cookie
			return Result.empty();
		}
		
		String[] tokens = decodeRememberMeTokens(cookie);
		if(null == tokens){
			return Result.empty();
		}
		
		log.debug("A valid remember-me cookie detected, authenticates it");
		Authentication authc = authenticateRememberMeTokens(request, response, context, tokens);
		if(null == authc){
			log.debug("Failed authenticating the remember-me cookie, removes it");
			removeCookie(request, response, cookie);
			return Result.empty();
		}
		
		log.debug("Successful to authenticating the remember-me cookie");
		return Result.of(authc);
	}
	
	@Override
    public void forgetRememberedUser(Request request, Response response) {
		removeCookie(request, response);
    }

	@Override
    public void onLoginSuccess(Request request, Response response, Authentication authentication) {
		UserPrincipal user = authentication.getUser();
		
		if(user instanceof UserDetails){
			String rememberMe = request.getParameter(securityConfig.getRememberMeParameterName());
		
			if(Converts.toBoolean(rememberMe, false)){
                setRememberMeCookie(request,response,user.getLoginName(),((UserDetails) user).getPassword());
			}else{
				removeCookie(request, response);
			}
		}
    }

	@Override
    public void onLogoutSuccess(Request request, Response response) {
		forgetRememberedUser(request, response);
    }
	
	protected void setRememberMeCookie(Request request,Response response,String username,String password) {
		int    maxAge  = getCookieMaxAge(request);
		long   expires = System.currentTimeMillis() + maxAge * 1000L;
		String tokens  = encodeRememberMeTokens(username, password, expires);
		
		setCookie(request, response, tokens, maxAge);
	}
	
	/*
	 * From spring security :
	 * <pre>
	 * base64(username + ":" + expirationTime + ":" + md5Hex(username + ":" + expirationTime + ":" password + ":" + key))
	 *
	 * username:          As identifiable to the `UserDetailsService`
	 * password:          That matches the one in the retrieved UserDetails
	 * expirationTime:    The date and time when the remember-me token expires, expressed in milliseconds
	 * key:               A private key to prevent modification of the remember-me token 
	 * </pre>
	 */
	protected String encodeRememberMeTokens(String username,String password,long expires) {
		String key = securityConfig.getRememberMeSecret();
		if(Strings.isEmpty(key)) {
			throw new RememberMeException("Cannot sign the remember-me tokens, secret must be provided");
		}
		
		String signed  = sign(username, password, expires);
		String data    = username + ":" + String.valueOf(expires) + ":" + signed;
		
		//removes all the '=' characters
		StringBuilder sb = new StringBuilder(Base64.encode(data));
        while (sb.charAt(sb.length() - 1) == '=') {
            sb.deleteCharAt(sb.length() - 1);
        }
        
        return sb.toString();
	}
	
	protected String[] decodeRememberMeTokens(Cookie cookie) {
		String encodedTokenString = cookie.getValue();
		
        for (int j = 0; j < encodedTokenString.length() % 4; j++) {
        	encodedTokenString = encodedTokenString + "=";
        }
		
		if(!Base64.isBase64(encodedTokenString)) {
			log.debug("The remember-me cookie is not a valid base64 string");
			return null;
		}
		
		String decodedTokenString = Base64.decode(encodedTokenString);
		String[] tokens = Strings.split(decodedTokenString, ':');
		if(tokens.length != 3){
			return null;
		}
		
		return tokens;
	}
	
	protected Authentication authenticateRememberMeTokens(Request request, Response response, AuthenticationContext context, String[] tokens) {
        long expires;

        try {
            expires = new Long(tokens[1]).longValue();
        } catch (NumberFormatException nfe) {
            log.debug("Remember-me token[1] did not contain a valid expires number, actual is : {}",tokens[1]);
            return null;
        }

        if (isTokenExpired(expires)) {
        	log.debug("Remember-me token has expired");
        	return null;
        }
        
        UserStore userStore = securityConfig.getUserStore();
        
        String username = tokens[0];
        UserDetails user = userStore.loadUserDetailsByLoginName(username);
        if(null == user){
        	log.debug("The remembered user '{}' not found",username);
        	return null;
        }
        
        String signed = sign(username, user.getPassword(), expires);
        if(null == signed){
        	return null;
        }
        
        if(!signed.equals(tokens[2])){
        	log.debug("The remembered user's signed is invalid, may be the user's password was changed");
        	return null;
        }
        
		SimpleAuthentication authc = new SimpleAuthentication(user);
		authc.setRememberMe(true);

		return authc;
	}
	
	protected String sign(String username, String password, long expires) {
		String key = securityConfig.getRememberMeSecret();
		if(Strings.isEmpty(key)){
			log.debug("Remember-me secret not exists, cannot sign user tokens");
			return null;
		}
		
		String data = username + ":" + expires + ":" + password + ":" + key;
		return MD5.hex(Strings.getBytesUtf8(data));
	}
	
	@Override
    public String getCookieExpiresParameter() {
		return securityConfig.getRememberMeExpiresParameterName();
	}

	@Override
	public int getCookieExpires() {
		return securityConfig.getDefaultRememberMeExpires();
	}

	@Override
	public String getCookieName() {
	    return securityConfig.getRememberMeCookieName();
    }

	protected boolean isTokenExpired(long expires) {
		return expires < System.currentTimeMillis();
	}
}