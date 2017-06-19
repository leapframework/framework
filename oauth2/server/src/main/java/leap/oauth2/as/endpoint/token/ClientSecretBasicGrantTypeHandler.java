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

import leap.core.annotation.Inject;
import leap.oauth2.OAuth2Params;
import leap.oauth2.as.token.AuthzAccessToken;
import leap.web.Request;
import leap.web.Response;

import java.util.function.Consumer;

/**
 * grant_type=client_secret_basic
 */
public class ClientSecretBasicGrantTypeHandler extends AbstractGrantTypeHandler {
    @Inject(name = "client_credentials")
    private GrantTypeHandler handler;
    
    @Override
    public void handleRequest(Request request, Response response, OAuth2Params params,
                              Consumer<AuthzAccessToken> callback) throws Throwable {
        handler.handleRequest(request,response,params,callback);
    }

    @Override
    public boolean handleSuccess(Request request, Response response, OAuth2Params params, AuthzAccessToken token) {
        return handler.handleSuccess(request,response,params,token);
    }
}
