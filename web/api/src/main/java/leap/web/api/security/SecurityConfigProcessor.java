/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.web.api.security;

import leap.core.annotation.Inject;
import leap.lang.path.Paths;
import leap.web.Request;
import leap.web.Response;
import leap.web.api.config.ApiConfigProcessor;
import leap.web.api.config.ApiConfigurator;
import leap.web.api.mvc.ApiErrorHandler;
import leap.web.route.Route;
import leap.web.security.SecurityConfigurator;
import leap.web.security.SecurityContextHolder;
import leap.web.security.SecurityFailureHandler;
import leap.web.security.authz.AuthorizationContext;

public class SecurityConfigProcessor implements ApiConfigProcessor {

    protected @Inject ApiErrorHandler      eh;
    protected @Inject SecurityConfigurator sc;

    @Override
    public void preProcess(ApiConfigurator c) {
        final String prefix = Paths.suffixWithSlash(c.config().getBasePath());

        //security failure handler.
        final ApiSecurityFailureHandler failureHandler = new ApiSecurityFailureHandler();
        sc.setPathPrefixFailureHandler(prefix, failureHandler);
    }

    protected final class ApiSecurityFailureHandler implements SecurityFailureHandler{
        @Override
        public boolean handleAuthenticationDenied(Request request, Response response, SecurityContextHolder context) throws Throwable {
            eh.unauthorized(response);
            return true;
        }

        @Override
        public boolean handleAuthorizationDenied(Request request, Response response, AuthorizationContext context) throws Throwable {
            eh.forbidden(response);
            return true;
        }
    }

}
