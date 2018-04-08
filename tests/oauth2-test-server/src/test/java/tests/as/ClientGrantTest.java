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
import leap.lang.codec.Base64;
import leap.webunit.client.THttpRequest;
import org.junit.Test;
import tests.OAuth2TestBase;
import tests.TokenResponse;

public class ClientGrantTest extends OAuth2TestBase {
    
    @Test
    public void testInvalidClientIdSecret() {
        TokenResponse token = obtainAccessTokenByClient("bad clientId", "bad clientSecret");
        assertTrue(token.isError());
        assertNotEmpty(token.error);
        
        token = obtainAccessTokenByClient(Global.TEST_CLIENT_ID, "bad clientSecret");
        assertTrue(token.isError());
        assertNotEmpty(token.error);
    }
    
	@Test
	public void testSuccessAccessTokenRequest() {
	    logout();
	    
	    TokenResponse token = obtainAccessTokenByClient(Global.TEST_CLIENT_ID, Global.TEST_CLIENT_SECRET);
	    assertFalse(token.isError());
	    
	    testClientOnlyAccessTokenInfo(token);
	}
	@Test
	public void testClientSecretBasic(){
        String tokenUri = serverContextPath + TOKEN_ENDPOINT;
        String token = "Basic " + Base64.encode(Global.TEST_CLIENT_ID + ":" + Global.TEST_CLIENT_SECRET);

        THttpRequest request = usePost(tokenUri).addHeader("Authorization",token)
                .addFormParam("grant_type","client_secret_basic");

        TokenResponse response = resp(request.send(), new TokenResponse());

        assertFalse(response.isError());

        testClientOnlyAccessTokenInfo(response);
        
    }
    @Test
    public void testClientCredentials(){
        String tokenUri = serverContextPath + TOKEN_ENDPOINT;
        String token = "Basic " + Base64.encode(Global.TEST_CLIENT_ID + ":" + Global.TEST_CLIENT_SECRET);

        THttpRequest request = usePost(tokenUri).addHeader("Authorization",token)
                .addFormParam("grant_type","client_credentials");

        TokenResponse response = resp(request.send(), new TokenResponse());

        assertFalse(response.isError());

        testClientOnlyAccessTokenInfo(response);

        request = usePost(tokenUri).addHeader("Authorization",token)
                .addFormParam("grant_type","client_secret_basic")
                .addFormParam("client_id",Global.TEST_CLIENT_ID)
                .addFormParam("client_secret",Global.TEST_CLIENT_SECRET);
        response = resp(request.send(), new TokenResponse());

        assertFalse(response.isError());

        testClientOnlyAccessTokenInfo(response);
    }

    @Test
    public void testClientSecretPost(){
        String tokenUri = serverContextPath + TOKEN_ENDPOINT;

        THttpRequest request = usePost(tokenUri).addFormParam("grant_type","client_secret_post")
                .addFormParam("client_id",Global.TEST_CLIENT_ID)
                .addFormParam("client_secret",Global.TEST_CLIENT_SECRET);

        TokenResponse response = resp(request.send(), new TokenResponse());

        assertFalse(response.isError());

        testClientOnlyAccessTokenInfo(response);
        
    }
	
}