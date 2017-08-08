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

import leap.oauth2.server.OAuth2Constants;
import org.junit.Test;
import tests.OAuth2TestBase;
import tests.TokenResponse;

public class AuthorizeEndpointTest extends OAuth2TestBase {

    @Test
    public void testRequestAuthzWithAt(){
        logout();
        TokenResponse at = obtainAccessTokenByPassword(USER_XIAOMING, PASS_XIAOMING,"app2","app2_secret");
        useGet("/server"+AUTHZ_ENDPOINT).addHeader(OAuth2Constants.TOKEN_HEADER,"Bearer " + at.accessToken)
                .addQueryParam("response_type","code")
                .addQueryParam("redirect_uri","http://localhost:8080/clientapp2/auth_redirect")
                .addQueryParam("client_id","app2")
                .send().assertOk().getContent()
                .contains("/login");
        
        usePost("/server/login").addHeader(OAuth2Constants.TOKEN_HEADER,"Bearer " + at.accessToken)
                .addFormParam("return_url","http://localhost:8080/clientapp2/auth_redirect")
                .send().assertOk().getContent()
                .contains("/login");
    }
    
}
