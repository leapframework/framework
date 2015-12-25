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
package leap.web.cors;

import leap.lang.intercepting.State;
import leap.web.Request;
import leap.web.Response;

public interface CorsHandler {

	String REQUEST_HEADER_ORIGIN 						 = "Origin";
    String REQUEST_HEADER_ACCESS_CONTROL_REQUEST_METHOD  = "Access-Control-Request-Method";
    String REQUEST_HEADER_ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";    
    
    String RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_ORIGIN      = "Access-Control-Allow-Origin"; 
    String RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";  
    String RESPONSE_HEADER_ACCESS_CONTROL_EXPOSE_HEADERS    = "Access-Control-Expose-Headers";
    String RESPONSE_HEADER_ACCESS_CONTROL_MAX_AGE           = "Access-Control-Max-Age";  
    String RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_METHODS     = "Access-Control-Allow-Methods";  
    String RESPONSE_HEADER_ACCESS_CONTROL_ALLOW_HEADERS     = "Access-Control-Allow-Headers";    
	
	State handle(Request request,Response response, CorsConfig conf) throws Throwable;
	
}
