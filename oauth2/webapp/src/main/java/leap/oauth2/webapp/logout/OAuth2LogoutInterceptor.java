/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package leap.oauth2.webapp.logout;

import leap.core.annotation.Inject;
import leap.lang.http.QueryStringBuilder;
import leap.lang.intercepting.State;
import leap.lang.net.Urls;
import leap.oauth2.OAuth2Params;
import leap.oauth2.webapp.OAuth2Config;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityConfig;
import leap.web.security.SecurityInterceptor;
import leap.web.security.authc.AuthenticationContext;
import leap.web.security.logout.LogoutContext;

public class OAuth2LogoutInterceptor implements SecurityInterceptor {

    protected static final String OAUTH2_LOGOUT = "oauth2_logout";

    protected @Inject OAuth2Config        config;
    protected @Inject SecurityConfig      sc;
    protected @Inject OAuth2LogoutHandler handler;

    @Override
    public State preLogout(Request request, Response response, LogoutContext context) throws Throwable {
        if(config.isEnabled() && config.isLogout()) {

            if(null != request.getAttribute("oauth2_logout")) {
                return State.CONTINUE;
            }

            String remoteLogoutParam = request.getParameter("remote_logout");
            if("0".equals(remoteLogoutParam)) {
                return State.CONTINUE;
            }else{
                response.sendRedirect(buildRemoteLogoutUrl(request));
                return State.INTERCEPTED;
            }
        }

        return State.CONTINUE;
    }

    @Override
    public State preResolveAuthentication(Request request, Response response, AuthenticationContext context) throws Throwable {

        if("1".equals(request.getParameter(OAUTH2_LOGOUT))) {
            return handler.handleServerLogoutRequest(config, request, response, context);
        }

        return State.CONTINUE;
    }

    protected String buildRemoteLogoutUrl(Request request) {
        QueryStringBuilder qs = new QueryStringBuilder();

        qs.add(OAuth2Params.CLIENT_ID,                config.getClientId());
        qs.add(OAuth2Params.POST_LOGOUT_REDIRECT_URI, buildLogoutRedirectUri(request));

        return Urls.appendQueryString(config.getLogoutUrl(), qs.build());
    }

    protected String buildLogoutRedirectUri(Request request) {
        String url = request.getContextUrl() + sc.getLogoutAction();

        url = Urls.appendQueryString(url, OAUTH2_LOGOUT + "=1");

        return url;
    }
}
