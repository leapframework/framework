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

package tests.resource;

import org.junit.Test;
import tests.OAuth2TestBase;
import tests.TokenResponse;

/**
 * @author kael.
 */
public class HomeControllerTest extends OAuth2TestBase {
    @Test
    public void testIgnoreAccessTokenResolved(){
        TokenResponse token = obtainAccessTokenByPassword(USER_XIAOMING, PASS_XIAOMING,TEST_CLIENT_ID,TEST_CLIENT_SECRET);
        useGet("/ignore_access_token_resolved").addHeader("Authorization","Bearer " + token.accessToken)
                .send().assertOk();
        useGet("/no_ignore_access_token_resolved").addHeader("Authorization","Bearer " + token.accessToken)
                .send().assertOk();
    }
    
    
}
