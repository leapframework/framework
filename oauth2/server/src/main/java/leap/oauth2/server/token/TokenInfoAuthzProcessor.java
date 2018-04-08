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

package leap.oauth2.server.token;

import leap.oauth2.server.OAuth2Params;
import leap.web.Request;
import leap.web.Response;

/**
 * Created by kael on 2016/7/12.
 */
public interface TokenInfoAuthzProcessor {

    /**
     * process the scope of this access token, the return string where be set to this access token
     * @param at the access token
     * @return  is continue to process access token,if true it will continue and return access token at last,otherwise will not return access token
     */
    boolean process(Request request, Response response, OAuth2Params params, AuthzAccessToken at);
}
