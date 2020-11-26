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

package leap.oauth2.server.client;

import leap.core.validation.Errors;
import leap.core.validation.SimpleErrors;
import leap.lang.NamedError;
import leap.web.Request;
import leap.web.Response;

/**
 * Created by kael on 2017/1/3.
 */
public class DefaultAuthzClientAuthenticationContext implements AuthzClientAuthenticationContext {

    private Request request;
    private Response response;

    public DefaultAuthzClientAuthenticationContext(Request request, Response response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public Errors errors() {
        return request.getValidation().errors();
    }

    @Override
    public void addError(String name, String code, String message) {
        NamedError error = new NamedError(name,code,message);
        request.getValidation().errors().add(error);
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public Response response() {
        return response;
    }
}
