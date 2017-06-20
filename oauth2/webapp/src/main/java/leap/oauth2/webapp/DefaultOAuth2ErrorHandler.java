/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package leap.oauth2.webapp;

import leap.lang.http.ContentTypes;
import leap.lang.http.HTTP;
import leap.lang.json.JSON;
import leap.lang.json.JsonWriter;
import leap.web.Request;
import leap.web.Response;

public class DefaultOAuth2ErrorHandler implements OAuth2ErrorHandler {
	
	@Override
    public void handleInvalidRequest(Request request, Response response, String desc) {
	    writeError(request, response, HTTP.SC_BAD_REQUEST, OAuth2Errors.ERROR_INVALID_REQUEST, desc);
    }

    @Override
    public void handleInvalidToken(Request request, Response response, String desc) {
        writeError(request, response, HTTP.SC_UNAUTHORIZED, OAuth2Errors.ERROR_INVALID_TOKEN, desc);        
    }

    @Override
    public void handleInsufficientScope(Request request, Response response, String desc) {
        writeError(request, response, HTTP.SC_FORBIDDEN, OAuth2Errors.ERROR_INSUFFICIENT_SCOPE, desc);
    }

    @Override
    public void handleServerError(Request request, Response response, Throwable e) {
        writeError(request, response, HTTP.SC_INTERNAL_SERVER_ERROR, OAuth2Errors.ERROR_SERVER_ERROR, e.getMessage());
    }

    @Override
    public void responseError(Request request, Response response, int status, String error, String message) {
        writeError(request, response, status, error, message);
    }

    protected void writeError(Request request, Response response, int status, String code, String desc) {
		
		response.setStatus(status);
		response.setContentType(ContentTypes.APPLICATION_JSON_UTF8);
		
		JsonWriter json = JSON.createWriter(response.getWriter());
		
		json.startObject()
		    .property("error", code)
		    .propertyOptional("error_description", desc)
		    .endObject();
		
	}

}