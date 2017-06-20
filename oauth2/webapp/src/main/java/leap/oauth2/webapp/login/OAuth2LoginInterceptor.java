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

package leap.oauth2.webapp.login;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.http.QueryStringBuilder;
import leap.lang.intercepting.State;
import leap.lang.net.Urls;
import leap.oauth2.webapp.OAuth2Params;
import leap.oauth2.webapp.OAuth2Config;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityConfig;
import leap.web.security.SecurityInterceptor;
import leap.web.security.authc.AuthenticationContext;
import leap.web.security.login.LoginContext;

public class OAuth2LoginInterceptor implements SecurityInterceptor {
    
    private static final String REDIRECT_BACK_PARAM = "oauth2_redirect";

    protected @Inject OAuth2Config       config;
    protected @Inject SecurityConfig     sc;
    protected @Inject OAuth2LoginHandler handler;

    @Override
    public State preResolveAuthentication(Request request, Response response, AuthenticationContext context) throws Throwable {
        if(config.isEnabled() && config.isLogin()) {
            if(isRedirectBackFromServer(request)) {
                return handler.handleServerRedirectRequest(request, response, context);
            }
        }
        return State.CONTINUE;
    }

    @Override
    public State postResolveAuthentication(Request request, Response response, AuthenticationContext context) throws Throwable {
        return handler.handleAuthenticationResolved(request, response, context);
    }

    @Override
    public State prePromoteLogin(Request request, Response response, LoginContext context) throws Throwable {
        if(config.isEnabled() && config.isLogin()) {
            if(!isRedirectBackFromServer(request)) {
                context.setLoginUrl(buildLoginUrl(request));
            }
        }
        return State.CONTINUE;
    }

    protected boolean isRedirectBackFromServer(Request request) {
        String v = request.getParameter(REDIRECT_BACK_PARAM);
        return "1".equals(v);
    }

    protected String buildLoginUrl(Request request) {
        QueryStringBuilder qs = new QueryStringBuilder();

        String responseType = config.isLoginWithAccessToken() ? "code id_token" : "id_token";

        qs.add(OAuth2Params.RESPONSE_TYPE, responseType);
        qs.add(OAuth2Params.CLIENT_ID,     config.getClientId());
        qs.add(OAuth2Params.REDIRECT_URI,  buildClientRedirectUri(request));
        qs.add(OAuth2Params.LOGOUT_URI,    buildClientLogoutUri(request));

        return "redirect:" + Urls.appendQueryString(config.getAuthorizeUrl(), qs.build());
    }

    protected String buildClientRedirectUri(Request request) {
        String uri;

        //todo: reverse proxy

        String redirectUri = config.getRedirectUri();
        if (Strings.isEmpty(redirectUri)) {
            uri = request.getServletRequest().getRequestURL().toString();
        }else{
            if(Strings.startsWithIgnoreCase(redirectUri,"http")) {
                uri = redirectUri;
            }else{
                uri = request.getContextUrl() + redirectUri;
            }

            String returnUrl = sc.getReturnUrlParameterName() + "=" + Urls.encode(request.getUri());

            uri = Urls.appendQueryString(uri, returnUrl);
        }

        String redirectBack = REDIRECT_BACK_PARAM + "=1";

        return Urls.appendQueryString(uri, redirectBack);
    }

    protected String buildClientLogoutUri(Request request) {
        return request.getContextUrl() + sc.getLogoutAction();
    }

}
