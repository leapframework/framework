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
import leap.oauth2.OAuth2Params;
import leap.oauth2.webapp.OAuth2Config;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityConfig;
import leap.web.security.SecurityInterceptor;
import leap.web.security.authz.AuthorizationContext;
import leap.web.security.login.LoginContext;

public class OAuth2LoginInterceptor implements SecurityInterceptor {
    
    private static final String OAUTH2_REDIRECT = "oauth2_redirect";

    protected @Inject OAuth2Config   config;
    protected @Inject SecurityConfig sc;

    @Override
    public State prePromoteLogin(Request request, Response response, LoginContext context) throws Throwable {
        if(config.isEnabled() && config.isLogin()) {
            //Check cyclic redirect.
            if(!Strings.isEmpty(request.getParameter(OAUTH2_REDIRECT))) {
                //todo:
                throw new IllegalStateException("Cannot promote login for oauth2 redirect request : " + request.getUri());
            }else{
                context.setLoginUrl(buildLoginUrl(request));
            }
        }
        return State.CONTINUE;
    }

    @Override
    public State preResolveAuthorization(Request request, Response response, AuthorizationContext context) throws Throwable {
        if("1".equals(request.getParameter(OAUTH2_REDIRECT))) {
            
        }
        return State.CONTINUE;
    }

    protected String buildLoginUrl(Request request) {
        QueryStringBuilder qs = new QueryStringBuilder();

        qs.add(OAuth2Params.RESPONSE_TYPE, "code id_token");
        qs.add(OAuth2Params.CLIENT_ID,     config.getClientId());
        qs.add(OAuth2Params.REDIRECT_URI,  buildClientRedirectUri(request));
        qs.add(OAuth2Params.LOGOUT_URI,    buildClientLogoutUri(request));

        return "redirect:" + Urls.appendQueryString(config.getAuthorizationUrl(), qs.build());
    }

    protected String buildClientRedirectUri(Request request) {
        String url;

        //todo: reverse proxy

        String redirectUri = config.getRedirectUri();
        if (Strings.isEmpty(redirectUri)) {

            url = request.getServletRequest().getRequestURL().toString();
        }else{
            if(Strings.startsWithIgnoreCase(redirectUri,"http")) {
                url = redirectUri;
            }else{
                url = request.getContextUrl() + redirectUri;
            }
        }

        String qs = OAUTH2_REDIRECT + "=1&" + sc.getReturnUrlParameterName() + "=" + Urls.encode(request.getUriWithQueryString());

        return Urls.appendQueryString(url, qs);
    }

    protected String buildClientLogoutUri(Request request) {
        return request.getContextUrl() + sc.getLogoutAction();
    }
}
