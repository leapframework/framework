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
package leap.web.security.user;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.core.security.Authentication;
import leap.core.security.Credentials;
import leap.core.security.UserPrincipal;
import leap.core.security.token.TokenCredentials;
import leap.core.security.token.jwt.JWT;
import leap.core.security.token.jwt.JwtSigner;
import leap.core.security.token.jwt.JwtVerifier;
import leap.core.security.token.jwt.MacSigner;
import leap.lang.Out;
import leap.lang.Strings;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityConfig;
import leap.web.security.authc.AuthenticationException;
import leap.web.security.authc.TokenAuthenticator;
import leap.web.security.authc.credentials.CredentialsAuthenticationContext;

public class JwtTokenAuthenticator extends UsernameBasedTokenAuthenticator implements TokenAuthenticator, PostCreateBean {
	
	public static final String CLAIM_NAME = "name"; //username
	
    @Inject
    protected SecurityConfig   config;
    protected JwtSigner        signer;
    protected JwtVerifier      verifier;
	
	@Override
	public String generateAuthenticationToken(Request request, Response response, Authentication authc) {
		return signer.sign(createClaims(authc));
	}

	@Override
    public boolean authenticate(CredentialsAuthenticationContext context, Credentials credentials, Out<UserPrincipal> user) throws AuthenticationException {
		if(credentials instanceof TokenCredentials) {
			String token = ((TokenCredentials) credentials).getToken();
			
			Map<String, Object> claims = verifier.verify(token);
			
			String username = (String)claims.get(CLAIM_NAME);
			String jti = (String)claims.get(JWT.CLAIM_JWT_ID);
			
			if(Strings.isEmpty(username)||Strings.isEmpty(jti)) {
				return false;
			}

			if(sc.getUserStore() != null) {
				UserDetails details = resolveUserDetails(context, username, claims);
				if (null == details) {
					return false;
				}else {
					user.set(details);
					return true;
				}
			}else {
                SimpleUserDetails details = new SimpleUserDetails();
                details.setId(jti);
                details.setLoginName(username);
                details.setName(username);
                user.set(details);
                return true;
            }
		}
		
		return false;
    }

	@Override
	protected Map<String, Object> createDefaultClaims(Authentication auth) {
		UserPrincipal user = auth.getUser();
		
		Map<String, Object> claims = new HashMap<>();

		claims.put(JWT.CLAIM_JWT_ID, UUID.randomUUID().toString());
		claims.put(CLAIM_NAME, user.getLoginName());
		
		return claims;
	}
	
	@Override
    public void postCreate(BeanFactory factory) throws Throwable {
	    if(null == signer) {
	        signer   = new MacSigner(config.getSecret(), config.getDefaultAuthenticationExpires());
	        verifier = (JwtVerifier)signer;
	    }
    }

	public JwtSigner getSigner() {
		return signer;
	}

	public void setSigner(JwtSigner tokenSigner) {
		this.signer = tokenSigner;
	}

	public JwtVerifier getVerifier() {
		return verifier;
	}

	public void setVerifier(JwtVerifier tokenVerifier) {
		this.verifier = tokenVerifier;
	}
	
}