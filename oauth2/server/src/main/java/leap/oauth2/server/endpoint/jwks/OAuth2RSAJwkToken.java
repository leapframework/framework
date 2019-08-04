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

package leap.oauth2.server.endpoint.jwks;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.core.security.token.jwt.JWT;
import leap.lang.codec.Base64;
import leap.lang.security.RSA;
import leap.oauth2.server.OAuth2AuthzServerConfig;

import java.security.interfaces.RSAPublicKey;

/**
 * @author kael.
 */
public class OAuth2RSAJwkToken implements JwkToken, PostCreateBean {
    
    public static final String KID = "oauth2_authorize_server_public_key";

    protected @Inject OAuth2AuthzServerConfig oac;
    
    protected RSAPublicKey publicKey;
    protected String       pk;
    protected String       n;
    protected String       e;

    @Override
    public String getAlg() {
        return JWT.ALG_RS256;
    }

    @Override
    public String getKty() {
        return "RSA";
    }

    @Override
    public String getValue() {
        return pk;
    }

    @Override
    public String getKid() {
        return KID;
    }

    @Override
    public String getN() {
        return n;
    }

    @Override
    public String getE() {
        return e;
    }

    @Override
    public String getUse() {
        return Use.ENC.name().toLowerCase();
    }

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        if (null != oac.getPublicKey()){
            pk = Base64.encode(oac.getPublicKey().getEncoded()).replace("\n","").replace(" ","");
            publicKey = RSA.decodePublicKey(pk);
            n = Base64.encode(publicKey.getModulus().toByteArray());
            e = Base64.encode(publicKey.getPublicExponent().toByteArray());
        }
    }
}
