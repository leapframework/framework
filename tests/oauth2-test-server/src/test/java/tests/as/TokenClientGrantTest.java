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
import org.junit.Test;
import tests.OAuth2TestBase;
import tests.TokenInfoResponse;
import tests.TokenResponse;

/**
 * Created by kael on 2016/6/14.
 */
public class TokenClientGrantTest extends OAuth2TestBase {
    @Test
    public void testAuthenticateTokenClient(){
        if(isLogin()) {
            logoutAuthzServer();
        }
        TokenResponse token = obtainAccessTokenByPassword(USER_XIAOMING, PASS_XIAOMING,Global.TEST_CLIENT_ID,Global.TEST_CLIENT_SECRET);
        assertFalse(token.isError());
        TokenInfoResponse userToken = testAccessTokenInfo(token);
        assertNotEmpty(userToken.clientId);
        assertNotEmpty(userToken.scope);
    }

}
