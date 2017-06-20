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
import leap.core.security.Authentication;
import leap.core.security.ClientPrincipal;
import leap.core.security.UserPrincipal;
import leap.core.security.token.TokenVerifyException;
import leap.lang.Strings;
import leap.lang.http.HTTP;
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.OAuth2Params;
import leap.oauth2.RequestOAuth2Params;
import leap.oauth2.webapp.OAuth2Config;
import leap.oauth2.webapp.OAuth2ErrorHandler;
import leap.oauth2.webapp.client.OAuth2Client;
import leap.oauth2.webapp.code.CodeVerifier;
import leap.oauth2.webapp.token.id.IdToken;
import leap.oauth2.webapp.token.id.IdTokenVerifier;
import leap.oauth2.webapp.token.*;
import leap.oauth2.webapp.user.UserDetailsLookup;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.authc.AuthenticationContext;
import leap.web.security.authc.AuthenticationManager;
import leap.web.security.authc.SimpleAuthentication;
import leap.web.security.login.LoginManager;
import leap.web.view.View;

public class DefaultOAuth2LoginHandler implements OAuth2LoginHandler {

    private static final Log log = LogFactory.get(DefaultOAuth2LoginHandler.class);

    protected @Inject OAuth2Config          config;
    protected @Inject OAuth2ErrorHandler    errorHandler;
    protected @Inject AuthenticationManager am;
    protected @Inject LoginManager          lm;
    protected @Inject TokenStore            tokenStore;
    protected @Inject IdTokenVerifier       idTokenVerifier;
    protected @Inject CodeVerifier          codeVerifier;
    protected @Inject UserDetailsLookup     userDetailsLookup;

    @Override
    public State handleServerRedirectRequest( Request request, Response response, AuthenticationContext context) throws Throwable{
        OAuth2Params params = new RequestOAuth2Params(request);

        if(params.isError()) {
            return handleOAuth2ServerError(request, response, params);
        }else{
            return handleOAuth2ServerSuccess(request, response, params);
        }
    }

    @Override
    public State handleAuthenticationResolved(Request request, Response response, AuthenticationContext context) throws Throwable {
        Authentication authc = context.getAuthentication();

        if(null != authc) {
            TokenDetails at;
            if(authc instanceof OAuth2LoginAuthentication) {
                at = ((OAuth2LoginAuthentication) authc).getAccessToken();
                if(null != at) {
                    tokenStore.saveAccessToken(request, context, at);
                }
            }else{
                at = tokenStore.loadAccessToken(request, context);
            }

            if(null != at) {

                if(at.isExpired()) {
                    log.info("AT '{}' expired, refresh it", at.getAccessToken());
                    at = tokenStore.refreshAndSaveAccessToken(request, context, at);
                }

                TokenContext.setAccessToken(request, at);
            }
        }

        return State.CONTINUE;
    }

    protected State handleOAuth2ServerError(Request request, Response response, OAuth2Params params) throws Throwable {
        if(Strings.isEmpty(config.getErrorView())) {
            View view = request.getView(config.getErrorView());

            //todo : handle null view

            if(null != view) {
                view.render(request, response);
            }

            return State.INTERCEPTED;
        }

        return error(request, response, params.getError(), params.getErrorDescription());
    }

    protected State error(Request request, Response response, String code, String message) {
        errorHandler.responseError(request,response, HTTP.Status.INTERNAL_SERVER_ERROR.value(), code, message);
        return State.INTERCEPTED;
    }

    protected State handleOAuth2ServerSuccess(Request request, Response response, OAuth2Params params) throws Throwable {
        TokenDetails at = null;

        if(config.isLoginWithAccessToken()) {
            String code = params.getCode();
            if(Strings.isEmpty(code)) {
                return error(request, response, "illegal_state", "code required from oauth2 server");
            }

            at = codeVerifier.verifyCode(code);
            if(null == at) {
                return error(request, response, "illegal_state", "invalid authorization code");
            }
        }

        String idToken = params.getIdToken();
        if(Strings.isEmpty(idToken)) {
            return error(request, response, "illegal_state", "id_token required from oauth2 server");
        }

        try{
            IdToken credentials = idTokenVerifier.verifyIdToken(params, idToken);

            Authentication authc = authenticate(params, credentials, at);

            login(request, response, authc);

            return State.CONTINUE;
        }catch (TokenVerifyException e) {
            return error(request, response, e.getErrorCode().name(), e.getMessage());
        }
    }

    protected Authentication authenticate(OAuth2Params params, IdToken idtoken, TokenDetails at) {
        String clientId = idtoken.getClientId();
        String userId   = idtoken.getUserId();

        UserPrincipal   user   = idtoken.getUserInfo();
        ClientPrincipal client = idtoken.getClientInfo();

        if(null != userDetailsLookup && !Strings.isEmpty(userId)) {
            user = userDetailsLookup.lookupUserDetails(at.getAccessToken(), userId);
        }

        if(null == client && !Strings.isEmpty(clientId)) {
            client = new OAuth2Client(clientId);
        }

        OAuth2LoginAuthentication authc = new OAuth2LoginAuthentication(user, idtoken);
        if(null != client) {
            authc.setClientPrincipal(client);
        }
        if(null != at) {
            authc.setAccessToken(at);
        }

        return authc;
    }

    protected void login(Request request, Response response, Authentication authc) throws Throwable {
        am.loginImmediately(request, response, authc);
        lm.handleLoginSuccess(request, response, authc);
    }

}
