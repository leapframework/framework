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

package leap.oauth2.server.endpoint;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.core.security.token.jwt.JWT;
import leap.core.security.token.jwt.JwtSigner;
import leap.core.security.token.jwt.JwtVerifier;
import leap.core.security.token.jwt.MacSigner;
import leap.web.App;
import leap.web.Handler;
import leap.web.Request;
import leap.web.Response;
import leap.web.config.WebConfig;
import leap.web.cookie.AbstractCookieBean;
import leap.web.route.Routes;

import javax.servlet.http.Cookie;
import java.util.Map;

/**
 * @author kael.
 */
public class SessionRefreshEndpoint extends AbstractAuthzEndpoint implements Handler,PostCreateBean {

    protected @Inject WebConfig webConfig;
    protected JwtSigner signer;
    protected JwtVerifier verifier;
    protected AbstractCookieBean cookieBean;
    
    
    @Override
    public void startEndpoint(App app, Routes routes) throws Throwable {
        if(config.isSessionRefreshEnabled()) {
            sc.ignore(config.getSessionRefreshEndpointPath());

            routes.create()
                    .handle(config.getSessionRefreshEndpointPath(), this)
                    .disableCsrf().enableCors()
                    .apply();
        }
    }

    @Override
    public void handle(Request request, Response response) throws Throwable {
        String token = null;
        Cookie cookie = cookieBean.getCookie(request);
        if(null != cookie) {
            token = cookie.getValue();
        }
        if(null == token){
            return;
        }

        Map<String, Object> claims = verifier.verify(token);
        claims.remove(JWT.CLAIM_EXPIRATION_TIME);
        token = signer.sign(claims);

        cookieBean.setCookie(request,response,token);
    }

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        if(null == signer) {
            signer   = new MacSigner(sc.config().getSecret(), sc.config().getDefaultAuthenticationExpires());
            verifier = (JwtVerifier)signer;
        }
        if(null == cookieBean){
            cookieBean = new AbstractCookieBean() {
                @Override
                public String getCookieName() {
                    return sc.config().getAuthenticationTokenCookieName();
                }

                @Override
                public String getCookieDomain() {
                    return SessionRefreshEndpoint.this.webConfig.getCookieDomain();
                }
            };
            
        }
    }
}
