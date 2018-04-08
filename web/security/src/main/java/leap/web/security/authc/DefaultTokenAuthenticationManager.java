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
package leap.web.security.authc;

import leap.core.AppConfigException;
import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.core.security.Authentication;
import leap.core.security.Credentials;
import leap.core.security.UserPrincipal;
import leap.core.security.token.SimpleTokenCredentials;
import leap.core.security.token.TokenCredentials;
import leap.core.security.token.TokenVerifyException;
import leap.lang.Out;
import leap.lang.Result;
import leap.lang.Strings;
import leap.lang.codec.Base64;
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityConfig;
import leap.web.security.SecuritySessionManager;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.IOException;

public class DefaultTokenAuthenticationManager extends CookieBasedAuthenticationResolver implements TokenAuthenticationManager,PostCreateBean {
	
	private static final Log log = LogFactory.get(DefaultTokenAuthenticationManager.class);
	
    protected @Inject SecurityConfig         securityConfig;
    protected @Inject SecuritySessionManager sessionManager;

	protected TokenAuthenticator tokenAuthenticator;
	protected String  			 logoutToken;
	
	public String getLogoutToken() {
		if(null == logoutToken) {
			logoutToken = Base64.urlEncode("logout");
		}
		return logoutToken;
	}

	public void setLogoutToken(String token) {
		this.logoutToken = token;
	}

    @Override
    public State preResolveAuthentication(Request request, Response response, AuthenticationContext context) throws Throwable {
        if(!securityConfig.isAuthenticationTokenEnabled()) {
            return State.CONTINUE;
        }

        String token = getToken(request);

        if(getLogoutToken().equals(token)){
            sessionManager.removeAuthentication(request);
        }

        context.setAuthenticationToken(token);

        return State.CONTINUE;
    }

    @Override
	public Result<Authentication> resolveAuthentication(Request request, Response response, AuthenticationContext context) throws ServletException, IOException {
		if(!securityConfig.isAuthenticationTokenEnabled()) {
            return Result.empty();
        }

		String token = context.getAuthenticationToken();
		if(Strings.isEmpty(token)) {
			return Result.empty();
		}

        if(getLogoutToken().equals(token)){
            return Result.empty();
        }

		TokenCredentials   credentials  = new SimpleTokenCredentials(token);
		Out<UserPrincipal> outPrincipal = new Out<>();
		try {
		    if(log.isDebugEnabled()) {
		        log.debug("Authenticates the auth token : {}", Strings.abbreviate(token, 10) + "******");
		    }
		    
	        if(!tokenAuthenticator.authenticate(context, credentials, outPrincipal)) {
	        	return Result.empty();
	        }
        } catch (TokenVerifyException e) {
        	log.info("Token verify error, " + e.getMessage(), e);
        	removeCookie(request, response);
        	return Result.empty();
        }
		
		UserPrincipal principal = outPrincipal.getValue();
		if(null == principal){
			throw new IllegalStateException("Credentials '" + credentials + "' authenticated but no principal was returned");
		}
		
        Authentication authc = sessionManager.getAuthentication(request);
		if(null != authc && !principal.getId().equals(authc.getUser().getId())) {
			sessionManager.removeAuthentication(request);
		}

        authc = new TokenAuthentication(principal, credentials);
        authc.setToken(token);

		return Result.of(authc);
	}

	@Override
	public void onLoginSuccess(Request request, Response response, Authentication authc) {
        if(authc instanceof TokenAuthentication) {
            return;
        }
		if(null == authc.getToken()){
			String token = tokenAuthenticator.generateAuthenticationToken(request, response, authc);
			authc.setToken(token);
		}
		setCookie(request, response, authc.getToken());
	}

	@Override
    public void onLogoutSuccess(Request request, Response response) {
		setCookie(request, response, getLogoutToken());
	}

	protected String getToken(Request request) {
		String token = request.getHeader(securityConfig.getAuthenticationTokenHeaderName());
		if(Strings.isEmpty(token)) {
			Cookie cookie = request.getCookie(getCookieName(request));
			if(null != cookie) {
				token = cookie.getValue();
			}
		}
		
		return token;
	}
	
	@Override
	public int getCookieExpires() {
	    return -1;
    }

	@Override
	public String getCookieName() {
	    return securityConfig.getAuthenticationTokenCookieName();
    }

	@Override
	public String getCookieExpiresParameter() {
	    return null;
    }

	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
	    String tokenType = securityConfig.getAuthenticationTokenType();
	    if(Strings.isEmpty(tokenType)) {
	    	throw new AppConfigException("Default token type must be configured for token based authentication");
	    }
	    
	    this.tokenAuthenticator = factory.tryGetBean(TokenAuthenticator.class, tokenType);
	    if(null == this.tokenAuthenticator) {
	    	throw new AppConfigException("Bean of type '" + TokenAuthenticator.class.getSimpleName() + "' and named '" + tokenType + "' does not exists");
	    }
    }

    protected static final class TokenAuthentication extends SimpleAuthentication {
        public TokenAuthentication(UserPrincipal user, Credentials credentials) {
            super(user, credentials);
        }
    }
}
