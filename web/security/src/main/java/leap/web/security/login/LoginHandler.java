/*
 * Copyright 2013 the original author or authors.
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

import leap.lang.Out;
import leap.lang.intercepting.State;
import leap.web.Request;
import leap.web.Response;

public interface LoginHandler {
    
    default State handleLoginView(Request request,Response response, LoginContext context) throws Throwable {
        return State.CONTINUE;
    }
	
	default State handleLoginAuthentication(Request request,Response response, LoginContext context) throws Throwable {
	    return State.CONTINUE;
	}
	
	default void parseHandleType(Request request, Response response, LoginContext context, Out<HandleType> out){
    	
	} 
	
	enum HandleType{
    	VIEW,
		AUTHENTICATION
	}
	
}