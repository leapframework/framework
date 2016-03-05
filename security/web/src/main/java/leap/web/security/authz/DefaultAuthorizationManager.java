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

import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.security.Authorization;
import leap.lang.Result;
import leap.web.Request;
import leap.web.RequestIntercepted;
import leap.web.Response;
import leap.web.security.SecurityConfig;

public class DefaultAuthorizationManager implements AuthorizationManager {

    protected static final Authorization EMPTY_AUTHZ = new Authorization() {};

    protected @Inject @M SecurityConfig          config;
    protected @Inject @M AuthorizationResolver[] resolvers;

	@Override
    public Authorization resolveAuthorization(Request request, Response response, AuthorizationContext context) throws Throwable {

		for(AuthorizationResolver resolver : resolvers){
			Result<Authorization> r =
                    resolver.resolveAuthorization(request, response, context);

            if(null == r || r.isEmpty()) {
                continue;
            }

            if(r.isIntercepted()) {
                RequestIntercepted.throwIt();
            }

            if(r.isPresent()) {
                return r.get();
            }

		}
		
		return EMPTY_AUTHZ;
    }
}