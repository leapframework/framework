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

import leap.lang.Out;
import leap.lang.intercepting.State;
import leap.oauth2.server.OAuth2Params;
import leap.oauth2.server.token.AuthzAccessToken;
import leap.web.Request;
import leap.web.Response;

/**
 * @author kael.
 */
public interface GrantTokenInterceptor {
    /**
     * Returns <code>true</code> if handle.
     */
    State beforeGrantTypeHandle(Request request, Response response, OAuth2Params params, GrantTypeHandler handler,Out<AuthzAccessToken> at);

    /**
     * Returns <code>true</code> if handle.
     */
    State afterGrantTypeHandle(Request request, Response response, OAuth2Params params, GrantTypeHandler handler,Out<AuthzAccessToken> at);
}
