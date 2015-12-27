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
package leap.web.security;

import leap.lang.intercepting.Interceptor;
import leap.lang.intercepting.State;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.authc.Authentication;

public interface SecurityInterceptor extends Interceptor{

	default State preResolveAuthentication(Request request,Response response, SecurityContextHolder context) throws Throwable {
		return State.CONTINUE;
	}
	
	default State postResolveAuthentication(Request request, Response response, SecurityContextHolder context) throws Throwable {
		return State.CONTINUE;
	}
	
	default State prePromoteLogin(Request request, Response response, SecurityContextHolder context) throws Throwable {
	    return State.CONTINUE;
	}
	
	default State preLoginAuthentication(Request request, Response response, SecurityContextHolder context) throws Throwable {
	    return State.CONTINUE;
	}

	default State postLoginAuthentication(Request request, Response response, SecurityContextHolder context, Authentication authc) throws Throwable {
	    return State.CONTINUE;
	}
	
	default State preLogout(Request request, Response response, SecurityContextHolder context) throws Throwable {
	    return State.CONTINUE;
	}
}