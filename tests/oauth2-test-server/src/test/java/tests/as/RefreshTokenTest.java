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
import leap.lang.http.Headers;
import leap.lang.net.Urls;
import org.junit.Test;
import tests.OAuth2TestBase;
import tests.TokenResponse;

import static app.Global.TEST_CLIENT_ID;
import static app.Global.TEST_CLIENT_SECRET;

public class RefreshTokenTest extends OAuth2TestBase {
    
    @Test
    public void testInvalidClientCredentials() {
        // user and client token
    	TokenResponse token = obtainAccessTokenByRefreshToken("bad refresh token");
        assertTrue(token.isError());
        assertNotEmpty(token.error);
        
        // client only token
		token = obtainAccessTokenByClient(TEST_CLIENT_ID,TEST_CLIENT_SECRET);
		token = obtainAccessTokenByRefreshToken(token.refreshToken,TEST_CLIENT_ID,TEST_CLIENT_SECRET);
		assertNotEmpty(token.accessToken);
		
		// refresh token not issue to client
		token = obtainAccessTokenByClient(TEST_CLIENT_ID,TEST_CLIENT_SECRET);
		String tokenUri = serverContextPath + TOKEN_ENDPOINT +
				"?grant_type=refresh_token&refresh_token=" + Urls.encode(token.refreshToken);
		usePost(tokenUri).addHeader(Headers.AUTHORIZATION,encodeToBasicAuthcHeader("app1","app1_secret"))
				.send().assert401();
    }
    
	@Test
	public void testSuccessRequest() {
	    logout();
	    
	    TokenResponse token = obtainAccessTokenByPassword(USER_XIAOMING, PASS_XIAOMING);
	    assertFalse(token.isError());
	    
	    TokenResponse newToken = obtainAccessTokenByRefreshToken(token.refreshToken);
	    testAccessTokenInfo(newToken);
	}
	
}