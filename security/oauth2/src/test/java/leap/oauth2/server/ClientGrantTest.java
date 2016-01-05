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
package leap.oauth2.server;

import leap.oauth2.TokenResponse;
import leap.oauth2.OAuth2TestBase;

import org.junit.Test;

import app.Global;

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
	
}