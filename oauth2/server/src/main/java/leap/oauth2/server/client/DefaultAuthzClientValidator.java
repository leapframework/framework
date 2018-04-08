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
package leap.oauth2.server.client;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.oauth2.server.OAuth2Params;
import leap.oauth2.server.OAuth2Errors;
import leap.oauth2.server.Oauth2MessageKey;
import leap.web.Request;
import leap.web.Response;

import static leap.oauth2.server.Oauth2MessageKey.INVALID_REQUEST_CLIENT_ID_REQUIRED;
import static leap.oauth2.server.Oauth2MessageKey.INVALID_REQUEST_INVALID_CLIENT;

public class DefaultAuthzClientValidator implements AuthzClientValidator {
    
    protected @Inject AuthzClientManager clientManager;

    @Override
    public AuthzClient validatePasswordGrantRequest(Request request, Response response, OAuth2Params params) throws Throwable {
        String clientId = params.getClientId();
        if(Strings.isEmpty(clientId)) {
            OAuth2Errors.invalidRequest(request, response, Oauth2MessageKey.getMessageKey(INVALID_REQUEST_CLIENT_ID_REQUIRED),"client_id required");
            return null;
        }
        
        AuthzClient client = clientManager.loadClientById(clientId);
        if(null == client) {
            OAuth2Errors.invalidClient(request , response, Oauth2MessageKey.getMessageKey(INVALID_REQUEST_INVALID_CLIENT),"invalid client id");
            return null;
        }
        return client;
    }

}