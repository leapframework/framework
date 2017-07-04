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
package tests.as.openid;

import app.Global;
import leap.core.annotation.Inject;
import leap.core.security.token.jwt.MacSigner;
import leap.oauth2.server.OAuth2AuthzServerConfig;
import org.junit.Test;
import tests.OAuth2TestBase;
import tests.TokenResponse;

import java.util.Map;

public class OpenIDLoginTest extends OAuth2TestBase {
    private @Inject OAuth2AuthzServerConfig asc;
    @Test
    public void testLogin() {
        TokenResponse token = obtainIdTokenImplicit();
        
        assertFalse(token.isError());
        
        String idToken = token.idToken;
        assertNotEmpty(idToken);
        
        MacSigner signer = new MacSigner(Global.TEST_CLIENT_SECRET);
        Map<String, Object> claims = signer.verify(idToken);
        
        assertEquals(Global.TEST_CLIENT_ID, claims.get("aud"));
        assertEquals(USER_ADMIN, claims.get("name"));
        assertEquals(USER_ADMIN, claims.get("login_name"));
    }
    
    @Test
    public void testLogout() {
        login();
        assertLogin();
        
        get(LOGOUT_ENDPOINT);
        
        assertLogout();
    }
    
}