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

package leap.oauth2.as.endpoint.tokeninfo;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.core.security.token.jwt.JwtSigner;
import leap.core.security.token.jwt.RsaSigner;
import leap.lang.http.ContentTypes;
import leap.lang.json.JsonWriter;
import leap.oauth2.OAuth2Errors;
import leap.oauth2.as.OAuth2AuthzServerConfig;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.web.Request;
import leap.web.Response;

import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by kael on 2016/7/28.
 * response_type=jwt
 */
public class JwtTokenInfoResponseHandler implements TokenInfoResponseHandler,PostCreateBean {
    @Inject
    protected OAuth2AuthzServerConfig config;
    protected JwtSigner signer;
    @Override
    public void writeTokenInfo(Request request, Response response, AuthzAccessToken at) {
        if(signer == null){
            OAuth2Errors.invalidRequest(response, "not support jwt response type, server may not configure rsa private key!");
            return;
        }
        response.setContentType(ContentTypes.APPLICATION_JSON_UTF8);
        String jwtToken = signer.sign(createClaims(request,response,at),at.getExpiresIn());
        JsonWriter w = response.getJsonWriter();

        w.startObject()

                .property("jwt_token",     jwtToken);

        w.endObject();
    }
    protected Map<String,Object> createClaims(Request request, Response response, AuthzAccessToken at) {
        Map<String,Object> map = new LinkedHashMap<>();

        //todo :
        if(at.isAuthenticated()){
            map.put("client_id",at.getClientId());
        }
        map.put("username",at.getUsername());
        map.put("scope", at.getScope());
        map.put("expires_in", at.getExpiresIn());
        map.put("expires",at.getCreated()+at.getExpiresIn()*1000L);

        if(at.hasExtendedParameters()) {
            for(Map.Entry<String, Object> entry : at.getExtendedParameters().entrySet()) {
                map.put(entry.getKey(), entry.getValue());
            }
        }

        return map;
    }

    protected JwtSigner genSigner(){
        PrivateKey privateKey = config.getPrivateKey();
        if(privateKey instanceof RSAPrivateKey){
            return new RsaSigner((RSAPrivateKey)privateKey);
        }
        return null;
    }

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        signer = genSigner();
    }
}
