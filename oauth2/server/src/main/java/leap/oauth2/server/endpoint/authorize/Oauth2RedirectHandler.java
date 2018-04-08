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

package leap.oauth2.server.endpoint.authorize;

import java.util.Map;

import leap.oauth2.server.authc.AuthzAuthentication;
import leap.web.Request;
import leap.web.Response;

/**
 * Created by kael on 2017/1/5.
 */
public interface Oauth2RedirectHandler {
    /**
     * before redirect on oauth2 login success
     * @param request
     * @param response
     * @param authc
     * @param qs
     * @return true if want to go to redirect, otherwise false
     */
    boolean onOauth2LoginSuccessRedirect(Request request, Response response, AuthzAuthentication authc, Map<String,String> qs);
}
