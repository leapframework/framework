/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.web.security.login;

import leap.lang.http.HTTP;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityContextHolder;

public class DefaultLoginAjaxHandler implements LoginAjaxHandler {

    @Override
    public void promoteLogin(Request request, Response response, SecurityContextHolder context) throws Throwable {
        response.setStatus(HTTP.SC_UNAUTHORIZED);
    }

    @Override
    public void handleLoginSuccess(Request request, Response response, SecurityContextHolder context) throws Throwable {
        response.setStatus(HTTP.SC_OK);
    }

    @Override
    public void handleLoginFailure(Request request, Response response, SecurityContextHolder context) throws Throwable {
        response.setStatus(HTTP.SC_UNAUTHORIZED);
        
        //TODO : error message
    }
    
}