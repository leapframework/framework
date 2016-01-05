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

public class PasswordGrantTest extends OAuth2TestBase {
    
    @Test
    public void testInvalidUsernamePassword() {
        TokenResponse token = obtainAccessTokenByPassword("bad username", "bad password");
        assertTrue(token.isError());
        assertNotEmpty(token.error);
        
        token = obtainAccessTokenByPassword(USERNAME, "bad password");
        assertTrue(token.isError());
        assertNotEmpty(token.error);
    }
    
	@Test
	public void testSuccessAccessTokenRequest() {
	    logout();
	    
	    TokenResponse token = obtainAccessTokenByPassword(USERNAME1, PASSWORD1);
	    assertFalse(token.isError());
	    
	    testAccessTokenInfo(token);
	}
	
}