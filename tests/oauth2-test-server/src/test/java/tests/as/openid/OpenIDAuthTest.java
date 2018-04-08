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
import leap.lang.http.QueryString;
import leap.lang.http.QueryStringParser;
import leap.lang.net.Urls;
import org.junit.Test;
import tests.TokenResponse;

public class OpenIDAuthTest extends OpenIdTestBase {
    
    @Test
    public void testCodeIdTokenResponseType() {
        String uri = AUTHZ_ENDPOINT + 
                "?client_id=test&redirect_uri=" + Global.TEST_CLIENT_REDIRECT_URI_ENCODED + "&response_type=" + Urls.encode("code id_token");

        login();
        String redirectUrl = get(uri).assertRedirect().getRedirectUrl();

        QueryString qs = QueryStringParser.parse(Urls.getQueryString(redirectUrl));

        TokenResponse resp = new TokenResponse();

        if(!setErrorResponse(qs.getParameters(), resp)) {
            setSuccessResponse(qs.getParameters(), resp);
        }
        
        assertNotEmpty(resp.code);
        assertNotEmpty(resp.idToken);
        
        TokenResponse at = obtainAccessTokenByCode(resp.code);
        testAccessTokenInfo(at);
        
        IdTokenInfo info = getIdTokenInfo(resp.idToken);
        assertEquals(Global.TEST_CLIENT_ID, info.clientId);
        assertNotEmpty(info.userId);
        assertNotEmpty(info.userName);
        assertNotEmpty(info.loginName);
        
    }
}