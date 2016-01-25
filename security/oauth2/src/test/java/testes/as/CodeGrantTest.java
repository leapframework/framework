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
package testes.as;

import testes.TokenResponse;
import testes.OAuth2TestBase;

import org.junit.Test;

import app.Global;

public class CodeGrantTest extends OAuth2TestBase {
    
	@Test
	public void testInvalidAuthorizationRequest() {
		get(AUTHZ_ENDPOINT).assertContentContains("invalid_request");
		
		get(AUTHZ_ENDPOINT + "?client_id=1").assertContentContains("invalid_request");
		
		get(AUTHZ_ENDPOINT + "?redirect_uri=/auth_return").assertContentContains("invalid_request");
		
		get(AUTHZ_ENDPOINT + "?client_id=1&redirect_uri=/auth_return").assertContentContains("invalid_request");
	}
	
	@Test
	public void testSuccessAuthorizationRequest() {
	    String uri = AUTHZ_ENDPOINT + "?client_id=test&redirect_uri=" + Global.TEST_CLIENT_REDIRECT_URI_ENCODED + "&response_type=code";

	    logout();
	    get(uri).assertOk().assertContentContains("Login with your Account");
	    
	    login();
	    String redirectUrl = get(uri).assertRedirect().getRedirectUrl();
	    
	    assertTrue(redirectUrl.contains(Global.TEST_CLIENT_REDIRECT_URI));
	}
	
	@Test
	public void testSuccessAccessTokenRequest() {
	    String code = obtainAuthorizationCode();
	    assertNotEmpty(code);
	    
	    TokenResponse token = obtainAccessTokenByCode(code);
	    assertNotEmpty(token.accessToken);
	    assertNotEmpty(token.refreshToken);
	    assertEquals(new Integer(3600), token.expiresIn);
	    
	    testAccessTokenInfo(token);
	}
}