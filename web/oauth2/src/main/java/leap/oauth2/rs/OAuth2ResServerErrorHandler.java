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
package leap.oauth2.rs;

import leap.web.Request;
import leap.web.Response;

/**
 * OAuth2.0 Error Handler in Resource Server.
 */
public interface OAuth2ResServerErrorHandler {

	void handleInvalidRequest(Request request, Response response, String desc);
	
	void handleInvalidToken(Request request, Response response, String desc);
	
	void handleInsufficientScope(Request request, Response response, String desc);
	
	void handleServerError(Request request, Response response, Throwable e);

	void responseError(Request request, Response response, int status, String error, String message);
}