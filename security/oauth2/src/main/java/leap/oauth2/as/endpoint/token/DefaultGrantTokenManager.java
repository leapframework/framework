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

package leap.oauth2.as.endpoint.token;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.lang.Out;
import leap.lang.intercepting.State;
import leap.oauth2.OAuth2Params;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.web.Request;
import leap.web.Response;

/**
 * @author kael.
 */
public class DefaultGrantTokenManager implements GrantTokenManager {
    protected @Inject BeanFactory factory;
    protected @Inject GrantTokenInterceptor[] interceptors;
    
    @Override
    public GrantTypeHandler getHandler(String grantType) {
        return factory.tryGetBean(GrantTypeHandler.class, grantType);
    }

    @Override
    public AuthzAccessToken grantAccessToken(Request request, Response response, OAuth2Params params, GrantTypeHandler handler) throws Throwable {
        Out<AuthzAccessToken> out = new Out<>();
        for(GrantTokenInterceptor interceptor:interceptors){
            if(State.isIntercepted(interceptor.beforeGrantTypeHandle(request,response,params,handler,out))){
                return out.get();
            }
        }
        handler.handleRequest(request,response,params,accessToken -> out.set(accessToken));
        for(GrantTokenInterceptor interceptor:interceptors){
            if(State.isIntercepted(interceptor.afterGrantTypeHandle(request,response,params,handler,out))){
                return out.get();
            }
        }
        return out.get();
    }
}
