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

import leap.oauth2.server.OAuth2Error;
import leap.oauth2.server.OAuth2Params;
import leap.web.Request;
import leap.web.Response;

/**
 * Created by kael on 2017/2/28.
 */
public interface GrantTypeHandleFailHandler {
    /**
     * handle this fail in grant type handler
     * @return true if handle or false when not handle
     */
    boolean handle(Request request, Response response, OAuth2Params params, OAuth2Error error, GrantTypeHandler handler);
}
