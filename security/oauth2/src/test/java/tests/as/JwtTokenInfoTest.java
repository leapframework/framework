/*
 * Copyright 2016 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.as;

import app.Global;
import org.junit.Test;
import tests.JwtTokenResponse;
import tests.OAuth2TestBase;
import tests.TokenResponse;

import java.util.Map;

/**
 * Created by KAEL on 2016/6/12.
 */
public class JwtTokenInfoTest extends OAuth2TestBase {
    @Test
    public void testJwtTokenInfo(){
        logout();

        TokenResponse token = obtainAccessTokenByPassword(USER_XIAOMING, PASS_XIAOMING);
        assertFalse(token.isError());

        JwtTokenResponse jwtTokenResponse = testJwtResponseAccessTokenInfo(token);
        Map<String, Object> map = verifier.verify(jwtTokenResponse.jwtToken);
        assertEquals(USER_XIAOMING,map.get("username"));
        assertEquals(USER_XIAOMING,map.get("username"));
    }


}
