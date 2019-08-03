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

package tests.as.jwks;

import leap.core.annotation.Inject;
import leap.lang.New;
import leap.lang.codec.Base64;
import leap.lang.json.JSON;
import leap.oauth2.server.OAuth2AuthzServerConfig;
import leap.oauth2.server.endpoint.jwks.OAuth2RSAJwkToken;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.lang.JoseException;
import org.junit.Test;
import tests.OAuth2TestBase;

import java.util.List;

/**
 * @author kael.
 */
public class JwksTest extends OAuth2TestBase {
    
    protected @Inject OAuth2AuthzServerConfig oac;
    
    @Test
    public void testJwkEndpoint() throws JoseException {
        String json = useGet("/oauth2/token_key").send().getContent();
        json =JSON.encode(New.hashMap("keys",New.arrayList(JSON.decodeMap(json))));
        final List<JsonWebKey> jwks = new JsonWebKeySet(json).getJsonWebKeys();
        assertEquals(1, jwks.size());
        assertEquals(OAuth2RSAJwkToken.KID, jwks.get(0).getKeyId());
        assertEquals(PUBLIC_KEY.replaceAll("\n",""), Base64.encode( jwks.get(0).getKey().getEncoded()));
    }
    @Test
    public void testJwksEndpoint() throws JoseException {
        String json = useGet("/oauth2/token_keys").send().getContent();
        final List<JsonWebKey> jwks = new JsonWebKeySet(json).getJsonWebKeys();
        assertEquals(1, jwks.size());
        assertEquals(OAuth2RSAJwkToken.KID, jwks.get(0).getKeyId());
        assertEquals(PUBLIC_KEY.replaceAll("\n",""), Base64.encode( jwks.get(0).getKey().getEncoded()));
    }
    
}
