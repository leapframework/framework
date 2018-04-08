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
package leap.oauth2.server.endpoint;

import leap.core.annotation.Inject;
import leap.core.security.Authentication;
import leap.lang.Result;
import leap.lang.Strings;
import leap.lang.http.QueryString;
import leap.lang.http.QueryStringParser;
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.net.Urls;
import leap.oauth2.server.OAuth2Error;
import leap.oauth2.server.OAuth2Errors;
import leap.oauth2.server.OAuth2Params;
import leap.oauth2.server.OAuth2ResponseException;
import leap.oauth2.server.Oauth2MessageKey;
import leap.oauth2.server.QueryOAuth2Params;
import leap.oauth2.server.RequestOAuth2Params;
import leap.oauth2.server.authc.SimpleAuthzAuthentication;
import leap.oauth2.server.client.AuthzClient;
import leap.oauth2.server.endpoint.authorize.ResponseTypeHandler;
import leap.oauth2.server.token.AuthzTokenManager;
import leap.web.App;
import leap.web.Request;
import leap.web.Response;
import leap.web.exception.ResponseException;
import leap.web.route.Routes;
import leap.web.security.SecurityInterceptor;
import leap.web.security.authc.AuthenticationContext;
import leap.web.security.login.LoginContext;
import leap.web.security.user.UserManager;
import leap.web.view.ViewSource;

public class AuthorizationEndpoint extends AbstractAuthzEndpoint implements SecurityInterceptor {

    private static final Log log = LogFactory.get(AuthorizationEndpoint.class);

    public static final String CLIENT_ATTRIBUTE  = "oauth2.client";
    public static final String STATE_ATTRIBUTE   = "oauth2.state";
    public static final String PARAMS_ATTRIBUTE  = "oauth2.params";

    protected @Inject AuthzTokenManager tokenManager;
    protected @Inject ViewSource        viewSource;
    protected @Inject UserManager       um;
    protected String loginUrl;

	@Override
    public void startEndpoint(App app, Routes routes) throws Throwable {
	    if(config.isEnabled()) {
            sc.interceptors().add(this);

	        if (!Strings.isEmpty(config.getLoginView()) && null != viewSource.getView(config.getLoginView(), null)) {
	            loginUrl = "view:" + config.getLoginView();
	        }
	    }
    }

    @Override
    public State postResolveAuthentication(Request request, Response response, AuthenticationContext context) throws Throwable {
        if(!request.getPath().equals(config.getAuthzEndpointPath())) {
            return State.CONTINUE;
        }

        OAuth2Params params = new RequestOAuth2Params(request);

        ResponseTypeHandler handler = getResponseTypeHandler(request, response, params);
        if(null == handler) {
            return State.INTERCEPTED;
        }

        Result<AuthzClient> result = handler.validateRequest(request, response, params);
        if(result.isIntercepted()) {
            return State.INTERCEPTED;
        }

        AuthzClient client = result.get();

        //If user not authenticated, redirect to login url.
        Authentication authc = context.getAuthentication();
        //todo: if(null == authc || !authc.isAuthenticated() || (authc instanceof ResAuthentication)) {
        if(null == authc || !authc.isAuthenticated()) {
            //Expose view data.
            exposeViewData(request, params, client);

            return State.CONTINUE;
        }

        //Handle authentication.
        handleAuthenticated(request, response,
                            new SimpleAuthzAuthentication(params, client, um.getUserDetails(authc.getUser()), authc),
                            handler);

        //Intercepted.
        return State.INTERCEPTED;
    }

    @Override
    public State prePromoteLogin(Request request, Response response, LoginContext context) throws Throwable {
    	//set login url
        if(null != loginUrl) {
            context.setLoginUrl(loginUrl);
        }
    	return State.CONTINUE;
    }

    @Override
    public State preLoginAuthentication(Request request, Response response, LoginContext context) throws Throwable {
        String savedQueryString = request.getParameter(STATE_ATTRIBUTE);
        if(Strings.isEmpty(savedQueryString)) {
            return State.CONTINUE;
        }

        if(null != loginUrl) {
            context.setLoginUrl(loginUrl);
        }

        String savedQueryStringDecoded = Urls.decode(savedQueryString);

        QueryString qs = QueryStringParser.parse(savedQueryStringDecoded);
        if(qs.isEmpty()) {
            return State.CONTINUE;
        }

        OAuth2Params params = new QueryOAuth2Params(qs);
        ResponseTypeHandler handler = getResponseTypeHandler(request, response, params);
        if(null == handler) {
            return State.INTERCEPTED;
        }

        Result<AuthzClient> result = handler.validateRequest(request, response, params);
        if(result.isIntercepted()) {
            return State.INTERCEPTED;
        }

        exposeViewData(request, params, result.get(), savedQueryStringDecoded);

        return State.CONTINUE;
    }

    @Override
    public State onLoginAuthenticationSuccess(Request request, Response response, LoginContext context, Authentication authc) throws Throwable {
        String savedQueryString = request.getParameter(STATE_ATTRIBUTE);
        if(Strings.isEmpty(savedQueryString)) {
            return State.CONTINUE;
        }

        OAuth2Params params = (OAuth2Params)request.getAttribute(PARAMS_ATTRIBUTE);
        AuthzClient client = (AuthzClient)request.getAttribute(CLIENT_ATTRIBUTE);

        ResponseTypeHandler handler = null;
        if(null != params) {
            handler = getResponseTypeHandler(request, response, params);
        }

        if(null == client || null == params) {
            QueryString qs = QueryStringParser.parse(Urls.decode(savedQueryString));
            if(qs.isEmpty()) {
                return State.CONTINUE;
            }

            params = new QueryOAuth2Params(qs);
            handler = getResponseTypeHandler(request, response, params);
            if(null == handler){
                return State.INTERCEPTED;
            }

            Result<AuthzClient> result = handler.validateRequest(request, response, params);
            if(result.isIntercepted()) {
                return State.INTERCEPTED;
            }

            client = result.get();
        }

        //Handle authentication.
        handleAuthenticated(request, response, new SimpleAuthzAuthentication(params, client, um.getUserDetails(authc.getUser()), authc), handler);

        //Intercepted.
        return State.INTERCEPTED;
    }

    protected ResponseTypeHandler getResponseTypeHandler(Request request, Response response, OAuth2Params params) throws Throwable {
        //String redirectUri  = params.getRedirectUri();
        String responseType = params.getResponseType();

        if(Strings.isEmpty(responseType)) {
            //if(Strings.isEmpty(redirectUri)) {
            log.debug("error : response_type required");
            request.getValidation().addError(OAuth2Errors.ERROR_INVALID_REQUEST, "response_type required");
            request.forwardToView(config.getErrorView());
            //}else{
            //    OAuth2Errors.redirectInvalidRequest(response, redirectUri, "response_type required");
            //}
            return null;
        }

        ResponseTypeHandler handler = factory.tryGetBean(ResponseTypeHandler.class, responseType);
        if(null == handler) {
            log.info("error : invalid response type {}", responseType);
            //if(Strings.isEmpty(redirectUri)) {
                request.getValidation().addError(OAuth2Errors.ERROR_INVALID_REQUEST, "unsupported or invalid response type");
                request.forwardToView(config.getErrorView());
            //}else{
            //    OAuth2Errors.redirectUnsupportedResponseType(response, redirectUri, "unsupported or invalid response type");
            //}
            return null;
        }

        return handler;
    }

    protected void handleAuthenticated(Request request, Response response, SimpleAuthzAuthentication authc, ResponseTypeHandler handler) throws Throwable {
        String redirectUri = authc.getRedirectUri();

        try{
            handler.handleResponseType(request, response, authc);
        }catch(OAuth2ResponseException e) {
            OAuth2Error error = OAuth2Errors.oauth2Error(request,e.getStatus(),e.getError(),null,e.getMessage());
            OAuth2Errors.redirect(response, redirectUri, error);
        }catch(ResponseException e) {
            throw e;
        }catch(Throwable e) {
            log.error("Internal server error : {}", e.getMessage(), e);
            OAuth2Error error = OAuth2Errors.redirectServerErrorError(request, Oauth2MessageKey.createRandomKey(),e.getMessage());
            OAuth2Errors.redirect(response, redirectUri, error);
        }
    }

    protected void exposeViewData(Request request, OAuth2Params params, AuthzClient client) {
        request.setAttribute(CLIENT_ATTRIBUTE, client);
        request.setAttribute(PARAMS_ATTRIBUTE, params);
        request.setAttribute(STATE_ATTRIBUTE,  Urls.encode(request.getQueryString()));
    }

    protected void exposeViewData(Request request, OAuth2Params params, AuthzClient client, String state) {
        request.setAttribute(CLIENT_ATTRIBUTE, client);
        request.setAttribute(PARAMS_ATTRIBUTE, params);
        request.setAttribute(STATE_ATTRIBUTE,  Urls.encode(state));
    }
}