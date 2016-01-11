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
package leap.oauth2.as.endpoint;

import leap.web.*;
import leap.web.route.Routes;

/**
 * Open ID Connect defined endpoint, see <a href="https://openid.net/specs/openid-connect-basic-1_0.html#UserInfo">UserInfo Endpoint</a>
 */
public class UserInfoEndpoint extends AbstractAuthzEndpoint implements Endpoint,Handler {
    
	@Override
    public void startEndpoint(App app, Routes routes) {
		//TODO :
    }

	@Override
    public void handle(Request request, Response response) throws Throwable {

		//TODO :

    }
	
}