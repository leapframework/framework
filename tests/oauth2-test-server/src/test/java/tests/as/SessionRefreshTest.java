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

import leap.core.annotation.Inject;
import leap.core.security.token.jwt.JWT;
import leap.core.security.token.jwt.JwtVerifier;
import leap.core.security.token.jwt.MacSigner;
import leap.lang.Strings;
import leap.lang.Threads;
import leap.lang.http.Header;
import leap.oauth2.server.endpoint.SessionRefreshEndpoint;
import leap.web.config.WebConfig;
import leap.web.cookie.AbstractCookieBean;
import leap.web.security.SecurityConfig;
import leap.web.security.user.JwtTokenAuthenticator;
import org.junit.Test;
import tests.OAuth2TestBase;

import java.util.Map;

/**
 * @author kael.
 */
public class SessionRefreshTest extends OAuth2TestBase {
    
    protected @Inject SecurityConfig sc;
    protected @Inject WebConfig webConfig;
    
    @Test
    public void testSessionRefresh(){
        JwtVerifier verifier = new MacSigner(sc.getSecret(), sc.getDefaultAuthenticationExpires());
        AbstractCookieBean cookieBean = new AbstractCookieBean() {
            @Override
            public String getCookieName() {
                return sc.getAuthenticationTokenCookieName();
            }

            @Override
            public String getCookieDomain() {
                return SessionRefreshTest.this.webConfig.getCookieDomain();
            }
        };

        Header[] headers1 = forLogin().send().getHeaders("Set-Cookie");
        String token1 = getCookieValue(cookieBean.getCookieName()+"_root",headers1);
        assertNotEmpty(token1);
        Map<String,Object> claims1 = verifier.verify(token1);
        Threads.sleep(1000);
        Header[] headers2 = useGet(SESSION_REFRESH_ENDPOINT).send().assertOk().getHeaders("Set-Cookie");
        String token2 = getCookieValue(cookieBean.getCookieName()+"_root",headers2);
        assertNotEmpty(token2);
        Map<String,Object> claims2 = verifier.verify(token2);
        assertNotEquals(token1,token2);
        
        assertEquals(claims1.get(JWT.CLAIM_JWT_ID),claims2.get(JWT.CLAIM_JWT_ID));
        assertEquals(claims1.get(JwtTokenAuthenticator.CLAIM_NAME),claims2.get(JwtTokenAuthenticator.CLAIM_NAME));
        assertNotEquals(claims1.get(JWT.CLAIM_EXPIRATION_TIME),claims2.get(JWT.CLAIM_EXPIRATION_TIME));
        
    }
    
    protected String getCookieValue(String name, Header[] headers){
        for(Header h : headers){
            String value = h.getValue();
            if(Strings.startsWith(value,name)){
                String[] strs = Strings.split(value,";");
                for(String str : strs){
                    String[] kv = Strings.split(str,"=");
                    if(Strings.equals(name,kv[0])){
                        return kv[1];
                    }
                }
                break;
            }
        }
        return null;
    }
    
}
