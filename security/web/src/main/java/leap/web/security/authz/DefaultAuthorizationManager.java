/*
 * Copyright 2014 the original author or authors.
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
package leap.web.security.authz;

import java.io.IOException;

import javax.servlet.ServletException;

import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.lang.Out;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityConfig;

public class DefaultAuthorizationManager implements AuthorizationManager {

    protected @Inject @M SecurityConfig         config;
    protected @Inject @M AuthorizationHandler[] handlers;

	@Override
    public Authorization resolveAuthorization(Request request, Response response, AuthorizationContext context) throws ServletException, IOException {
		Out<Authorization> out = new Out<Authorization>();
		
		for(AuthorizationHandler h : handlers){
			if(h.authorize(request, response, context, out)) {
				return out.getValue();
			}
		}
		
		return new SimpleAuthorization(!config.isAuthorizeAnyRequests());
    }
}
