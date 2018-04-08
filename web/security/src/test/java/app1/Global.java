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
package app1;

import leap.lang.intercepting.State;
import leap.web.App;
import leap.web.Request;
import leap.web.RequestInterceptor;
import leap.web.Response;

public class Global extends App {

	@Override
    protected void init() throws Throwable {
		interceptors().add(new RequestInterceptor(){

			@Override
            public State preHandleRequest(Request request, Response response) throws Throwable {
	            return State.CONTINUE;
            }
			
		});
	}
	
}
