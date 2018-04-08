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
import leap.core.security.token.jwt.MacSigner;
import tests.OAuth2TestBase;

import java.util.Map;

public abstract class OpenIdTestBase extends OAuth2TestBase {
    
    protected IdTokenInfo getIdTokenInfo(String idToken) {
        return getIdTokenInfo(idToken, Global.TEST_CLIENT_SECRET);
    }

    protected IdTokenInfo getIdTokenInfo(String idToken, String secret) {
        MacSigner signer = new MacSigner(secret);
        
        Map<String, Object> claims = signer.verify(idToken);
        
        IdTokenInfo info = new IdTokenInfo();
        
        info.clientId  = (String)claims.remove("aud");
        info.userId    = (String)claims.remove("sub");
        info.userName  = (String)claims.remove("name");
        info.loginName = (String)claims.remove("username");
        
        return info;
    }
    
}
