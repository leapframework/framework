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

package leap.oauth2.as.token;

import leap.oauth2.OAuth2Params;
import leap.oauth2.as.endpoint.token.GrantTypeHandler;
import leap.web.Request;
import leap.web.Response;

/**
 * Created by kael on 2017/2/19.
 */
public interface TokenAuthzProcessor {
    /**
     * processor before handler handle success
     * @param request request
     * @param response response
     * @param params oauth2 params
     * @param handler the handler for this grant type
     * @param token access token
     * @return true if want continue to process, otherwise false will stop to continue
     */
    boolean process(Request request, Response response, OAuth2Params params, GrantTypeHandler handler, AuthzAccessToken token);
    
}
