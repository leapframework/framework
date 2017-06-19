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
import leap.core.security.token.jwt.MacSigner;
import leap.lang.Strings;
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.oauth2.OAuth2Params;
import leap.oauth2.RequestOAuth2Params;
import leap.oauth2.webapp.OAuth2Config;
import leap.oauth2.webapp.authc.OAuth2ClientPrincipal;
import leap.oauth2.webapp.token.*;
import leap.oauth2.webapp.user.UserInfoLookup;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.authc.AuthenticationContext;
import leap.web.security.authc.AuthenticationManager;
import leap.web.security.authc.SimpleAuthentication;
import leap.web.security.login.LoginManager;

import java.io.PrintWriter;
import java.util.Map;

public class DefaultOAuth2LoginHandler implements OAuth2LoginHandler {

    private static final Log log = LogFactory.get(DefaultOAuth2LoginHandler.class);

    protected @Inject OAuth2Config          config;
    protected @Inject AuthenticationManager am;
    protected @Inject LoginManager          lm;
    protected @Inject TokenInfoLookup       tokenInfoLookup;
    protected @Inject UserInfoLookup        userInfoLookup;

    @Override
    public State handleServerRedirectRequest(OAuth2Config config, Request request, Response response, AuthenticationContext context) throws Throwable{
        OAuth2Params params = new RequestOAuth2Params(request);

        if(params.isError()) {
            return handleOAuth2ServerError(request, response, params);
        }else{
            return handleOAuth2ServerSuccess(request, response, params);
        }
    }

    protected State handleOAuth2ServerError(Request request, Response response, OAuth2Params params) {
//        if(null != defaultErrorView) {
//            View view = request.getViewSource().getView(config.getErrorView(), request.getLocale());
//            if(null == view) {
//                view = defaultErrorView;
//            }
//
//            view.render(request, response);
//        }else{
//            printError(response, params.getError(), params.getErrorDescription());
//        }



        return State.INTERCEPTED;
    }

    protected void printError(Response response, String error, String desc)  {
        PrintWriter out = response.getWriter();
        out.write(error);

        if(!Strings.isEmpty(desc)) {
            out.write(":");
            out.write(desc);
        }
    }

    protected State error(Request request, Response response, String error, String desc) {
        printError(response, error, desc);
        return State.INTERCEPTED;
    }

    protected State handleOAuth2ServerSuccess(Request request, Response response, OAuth2Params params) throws Throwable {
        String code = params.getCode();
        if(Strings.isEmpty(code)) {
            return error(request, response, "illegal_state", "code required");
        }

        AccessTokenDetails at =
                tokenInfoLookup.lookupByAuthorizationCode(code);

        String idToken = params.getIdToken();
        if(Strings.isEmpty(idToken)) {
            return error(request, response, "illegal_state", "id_token required");
        }

        try{
            IdToken credentials = verifyIdToken(params, idToken);

            Authentication authc = authenticate(params, credentials, at);

            return login(request,response,authc);
        }catch (TokenVerifyException e) {
            return error(request, response, e.getErrorCode().name(), e.getMessage());
        }
    }

    protected IdToken verifyIdToken(OAuth2Params params, String token) throws TokenVerifyException {
        //todo: RSA or MAC ? more details userinfo ?

        MacSigner signer = new MacSigner(config.getClientSecret());

        Map<String, Object> claims = signer.verify(token);
        SimpleIdToken idToken = new SimpleIdToken(token);

        idToken.setClientId((String)claims.remove("aud"));
        idToken.setUserId((String)claims.remove("sub"));

        return idToken;
    }

    protected Authentication authenticate(OAuth2Params params, IdToken idToken, AccessTokenDetails at) {
        String clientId = idToken.getClientId();
        String userId   = idToken.getUserId();

        UserPrincipal user   = null;
        ClientPrincipal client = null;

        if(!Strings.isEmpty(userId)) {
            user = userInfoLookup.lookupUserDetails(new SimpleAccessToken(null, at.getAccessToken()), userId);
        }

        if(!Strings.isEmpty(clientId)) {
            client = new OAuth2ClientPrincipal(clientId);
        }

        SimpleAuthentication authc = new SimpleAuthentication(user, idToken);
        if(null != client) {
            authc.setClientPrincipal(client);
        }

        return authc;
    }

    protected State login(Request request, Response response, Authentication authc) throws Throwable {
        am.loginImmediately(request, response, authc);
        lm.handleLoginSuccess(request, response, authc);
        return State.CONTINUE;
    }

}
