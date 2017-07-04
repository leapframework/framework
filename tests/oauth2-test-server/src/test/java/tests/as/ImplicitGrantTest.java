/*
 *
 *  * Copyright 2013 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */
package tests.as;

import app.Global;
import leap.core.annotation.Inject;
import leap.core.security.token.jwt.JWT;
import leap.core.security.token.jwt.MacSigner;
import leap.lang.Threads;
import leap.oauth2.server.OAuth2AuthzServerConfig;
import org.junit.Test;
import tests.OAuth2TestBase;
import tests.TokenInfoResponse;
import tests.TokenResponse;

import java.util.Map;

public class ImplicitGrantTest extends OAuth2TestBase {

	protected @Inject OAuth2AuthzServerConfig config;
	
	@Test
	public void testSuccessAuthorizationRequest() {
	    String uri = AUTHZ_ENDPOINT + "?client_id=test&redirect_uri=" + Global.TEST_CLIENT_REDIRECT_URI_ENCODED + "&response_type=token";

	    logout();
	    get(uri).assertOk().assertContentContains("Login with your Account");
	    
	    login();
	    String redirectUrl = get(uri).assertRedirect().getRedirectUrl();
	    
	    assertTrue(redirectUrl.contains(Global.TEST_CLIENT_REDIRECT_URI));
	}
	
	@Test
	public void testSuccessAccessTokenRequest() {
	    TokenResponse token = obtainAccessTokenImplicit();
	    
	    testAccessTokenInfo(token);
	}

	@Test
	public void testSuccessIdTokenTokenRequest(){
		TokenResponse response = obtainIdTokenTokenImplicit();
		assertNotEmpty(response.accessToken);
		assertNotEmpty(response.idToken);

		MacSigner signer = new MacSigner(Global.TEST_CLIENT_SECRET);
		Map<String, Object> claim = signer.verify(response.idToken);

        long exp = Long.parseLong(claim.get(JWT.CLAIM_EXPIRATION_TIME).toString());
		long expect = System.currentTimeMillis()/1000+config.getDefaultIdTokenExpires();
		assertTrue(expect >= exp && expect <= exp + 100L);

		assertNotEmpty(claim.get(JWT.CLAIM_SUBJECT).toString());

		Threads.sleep(1000);
		TokenInfoResponse tokeninfo = obtainAccessTokenInfo(response.accessToken);
		assertNotEmpty(tokeninfo.userId);
		assertEquals(claim.get(JWT.CLAIM_SUBJECT),tokeninfo.userId);
		assertTrue(tokeninfo.expiresIn < config.getDefaultAccessTokenExpires());
	}
}