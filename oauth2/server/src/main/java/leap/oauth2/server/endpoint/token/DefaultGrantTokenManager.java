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

package leap.oauth2.server.endpoint.token;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.lang.Out;
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.time.StopWatch;
import leap.oauth2.server.OAuth2Params;
import leap.oauth2.server.token.AuthzAccessToken;
import leap.web.Request;
import leap.web.Response;

/**
 * @author kael.
 */
public class DefaultGrantTokenManager implements GrantTokenManager {
    private static final Log log = LogFactory.get(DefaultGrantTokenManager.class);
    
    protected @Inject BeanFactory factory;
    protected @Inject GrantTokenInterceptor[] interceptors;
    
    @Override
    public GrantTypeHandler getHandler(String grantType) {
        return factory.tryGetBean(GrantTypeHandler.class, grantType);
    }

    @Override
    public AuthzAccessToken grantAccessToken(Request request, Response response, OAuth2Params params, GrantTypeHandler handler) throws Throwable {
        Out<AuthzAccessToken> out = new Out<>();
        try {
            out.set(grantToken(request, response, params, handler));
        }finally {
            StopWatch sw = StopWatch.startNew();
            for(GrantTokenInterceptor interceptor : interceptors) {
                try {
                    sw.reset();
                    sw.start();
                    interceptor.grantTypeHandleComplete(request,response,params,handler,out);
                }catch (Exception e){
                    log.warn("complete grant type handle fail for class {}, error message: ",interceptor.getClass().getName(),e);
                    log.warn(e);
                }finally {
                    sw.stop();
                    log.debug("interceptor {} execute grantTypeHandleComplete use {}ms", interceptor.getClass(), sw.getElapsedMilliseconds());
                }
            }
        }
        return out.get();
    }

    protected AuthzAccessToken grantToken(Request request, Response response, OAuth2Params params, GrantTypeHandler handler) throws Throwable{
        Out<AuthzAccessToken> out = new Out<>();
        StopWatch sw = StopWatch.startNew();
        for(GrantTokenInterceptor interceptor:interceptors){
            try {
                sw.reset();
                sw.start();
                if(State.isIntercepted(interceptor.beforeGrantTypeHandle(request,response,params,handler,out))){
                    return out.get();
                }
            } finally {
                sw.stop();
                log.debug("interceptor {} execute beforeGrantTypeHandle use {}ms", interceptor.getClass(), sw.getElapsedMilliseconds());
            }
        }
        try {
            sw.reset();
            sw.start();
            handler.handleRequest(request,response,params, out::set);
        }finally {
            sw.stop();
            log.debug("handler {} execute handleRequest use {}ms", handler.getClass(), sw.getElapsedMilliseconds());
        }
        for(GrantTokenInterceptor interceptor:interceptors){
            try {
                sw.reset();
                sw.start();
                if(State.isIntercepted(interceptor.afterGrantTypeHandle(request,response,params,handler,out))){
                    return out.get();
                }
            } finally {
                sw.stop();
                log.debug("interceptor {} execute afterGrantTypeHandle use {}ms", interceptor.getClass(), sw.getElapsedMilliseconds());
            }

        }
        return out.get();
    }

}
