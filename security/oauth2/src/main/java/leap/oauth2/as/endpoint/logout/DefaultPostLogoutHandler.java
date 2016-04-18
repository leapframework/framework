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
package leap.oauth2.as.endpoint.logout;

import leap.core.annotation.Inject;
import leap.oauth2.OAuth2Params;
import leap.oauth2.RequestOAuth2Params;
import leap.oauth2.as.OAuth2AuthzServerConfig;
import leap.oauth2.as.sso.AuthzSSOManager;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.logout.LogoutContext;
import leap.web.view.View;

public class DefaultPostLogoutHandler implements PostLogoutHandler {

    protected @Inject OAuth2AuthzServerConfig config;
    protected @Inject AuthzSSOManager         ssom;

    @Override
    public void handlePostLogout(Request request, Response response, LogoutContext context, View defaultLogoutView) throws Throwable {
        //Render logout view.
        OAuth2Params params = new RequestOAuth2Params(request);

        exposeViewAttributes(request, response, context, params);

        defaultLogoutView.render(request, response);
    }

    protected void exposeViewAttributes(Request request, Response response, LogoutContext context, OAuth2Params params) throws Throwable {
        request.setAttribute("redirect_uri",   params.getPostLogoutRedirectUri());
        request.setAttribute("authentication", context.getAuthentication());

        if(config.isSingleLoginEnabled() && config.isSingleLogoutEnabled()) {
            request.setAttribute("logout_urls",ssom.resolveLogoutUrls(request, response, context));
        }
    }

}