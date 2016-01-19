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
package leap.oauth2.wac;

import java.io.PrintWriter;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.http.QueryStringBuilder;
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.net.Urls;
import leap.lang.servlet.Servlets;
import leap.oauth2.OAuth2Params;
import leap.oauth2.RequestOAuth2Params;
import leap.oauth2.wac.auth.WacResponseHandler;
import leap.web.App;
import leap.web.AppInitializable;
import leap.web.Request;
import leap.web.Response;
import leap.web.security.SecurityConfigurator;
import leap.web.security.SecurityContextHolder;
import leap.web.security.SecurityInterceptor;
import leap.web.security.authc.AuthenticationManager;
import leap.web.security.login.LoginManager;
import leap.web.security.logout.LogoutManager;
import leap.web.view.View;
import leap.web.view.ViewSource;

public class OAuth2WebAppSecurityInterceptor implements SecurityInterceptor, AppInitializable {

    private static final Log log = LogFactory.get(OAuth2WebAppSecurityInterceptor.class);

    protected @Inject OAuth2WebAppConfig    config;
    protected @Inject SecurityConfigurator  sc;
    protected @Inject ViewSource            vs;
    protected @Inject AuthenticationManager am;
    protected @Inject LogoutManager         lom;
    protected @Inject WacResponseHandler[]  handlers;
    
    protected String redirectPath;
    protected String logoutPath;
    protected View   defaultErrorView;
    
    @Override
    public void postAppInit(App app) throws Throwable {
        if(config.isEnabled()) {
            String redirectUri = config.getClientRedirectUri();
            if(redirectUri.startsWith("/")) {
                redirectPath = Servlets.getRequestPathFromUri(redirectUri);
            }else{
                redirectPath = Servlets.getRequestPathFromUri(redirectUri, app.getContextPath());
                // when redirectPath is empty,set it to contextPath
                if(Strings.isEmpty(redirectPath)){
                	redirectPath = "/";
                }
            }
            sc.ignore(redirectPath);
            app.routes().get(redirectPath, (req, resp) -> handleAuthzServerLoginResponse(req, resp));

            String logoutUri = config.getClientLogoutUri();
            if(!Strings.isEmpty(logoutUri)) {
                if(logoutUri.startsWith("/")){
                    logoutPath = Servlets.getRequestPathFromUri(logoutUri);
                }else{
                    logoutPath = Servlets.getRequestPathFromUri(logoutUri, app.getContextPath());
                    // when logoutPath is empty,set it to root contextPath
                    if(Strings.isEmpty(logoutPath)){
                    	logoutPath = "/";
                    }
                }
                sc.ignore(logoutPath);
                app.routes().get(logoutPath,(req,resp) -> handleAuthzServerLogoutNotification(req, resp));
            }

            if(!Strings.isEmpty(config.getErrorView())) {
                defaultErrorView = vs.getView(config.getErrorView());
            }
        }
    }

    @Override
    public State prePromoteLogin(Request request, Response response, SecurityContextHolder context) throws Throwable {
        if(config.isOAuth2LoginEnabled()) {
            //Check cyclic redirect.
            if(!Strings.isEmpty(request.getParameter("oauth2_redirect"))) {
                throw new IllegalStateException("Cannot promote login for oauth2 redirect request : " + request.getUri());
            }else{
                context.getLoginContext().setLoginUrl(buildRemoteLoginUrl(request));
            }
        }
        return State.CONTINUE;
    }

    @Override
    public State preLogout(Request request, Response response, SecurityContextHolder context) throws Throwable {
        if(config.isEnabled() && config.isOAuth2LogoutEnabled()) {
            Boolean reqeustedLogout = (Boolean)request.getAttribute("oauth2_logout");
            if(null != reqeustedLogout) {
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

    protected void handleAuthzServerLogoutNotification(Request request, Response response) throws Throwable {
        log.debug("Logout by oauth2 authorization server");
        am.logoutImmediately(request, response);
    }

    protected void handleAuthzServerLoginResponse(Request request, Response response) throws Throwable {
        String logoutParam = request.getParameter("oauth2_logout");
        if(!Strings.isEmpty(logoutParam)) {
            request.setAttribute("oauth2_logout", Boolean.TRUE);
            lom.logout(request, response);
        }else{
            OAuth2Params params = new RequestOAuth2Params(request);
            if(params.isError()) {
                handleOAuth2ServerError(request, response, params);
            }else{
                handleOAuth2ServerSuccess(request, response, params);
            }
        }
    }
    
    protected void handleOAuth2ServerError(Request request, Response response, OAuth2Params params) throws Throwable {
        if(null != defaultErrorView) {
            View view = request.getViewSource().getView(config.getErrorView(), request.getLocale());
            if(null == view) {
                view = defaultErrorView;
            }
            
            view.render(request, response);
        }else{
            printError(response, params.getError(), params.getErrorDescription());
        }
    }
    
    protected void printError(Response response, String error, String desc) throws Throwable {
        PrintWriter out = response.getWriter();
        out.write(error);
        
        if(!Strings.isEmpty(desc)) {
            out.write(":");
            out.write(desc);
        }
    }
    
    protected void handleOAuth2ServerSuccess(Request request, Response response, OAuth2Params params) throws Throwable {
        boolean processed = false;
        
        for (WacResponseHandler handler : handlers) {
            State state = handler.handleSuccessResponse(request, response, params);
            if (State.isProcessed(state)) {
                processed = true;
            }
            if (State.isIntercepted(state)) {
                return;
            }
        }
        
        if(!processed) {
            printError(response, "invalid_redirect", "cannot handle the response from oauth2 server");
        }
    }
    
    protected String buildRemoteLoginUrl(Request request){
        QueryStringBuilder qs = new QueryStringBuilder();
        
        qs.add(OAuth2Params.RESPONSE_TYPE, config.isAccessTokenEnabled() ? "code id_token" : "id_token");
        qs.add(OAuth2Params.CLIENT_ID,     config.getClientId());
        qs.add(OAuth2Params.REDIRECT_URI,  buildClientRedirectUri(request));
        qs.add(OAuth2Params.LOGOUT_URI,    buildClientLogoutUri(request));

        String login_token = request.getParameter(OAuth2Params.LOGIN_TOKEN);
        if(!Strings.isEmpty(login_token)) {
            qs.add(OAuth2Params.LOGIN_TOKEN, Urls.encode(login_token));
        }
        
        return "redirect:" + Urls.appendQueryString(config.getServerAuthorizationEndpointUrl(), qs.build());
    }
    
    protected String buildClientRedirectUri(Request request) {
        String url = null;
        
        if(!config.getClientRedirectUri().startsWith("/")) {
            url = config.getClientRedirectUri();
        }else{
            url = request.getContextUrl() + config.getClientRedirectUri();
        }
        
        url = Urls.appendQueryString(url, "oauth2_redirect=1&" + sc.config().getReturnUrlParameterName() + "=" + Urls.encode(request.getUriWithQueryString()));
        
        return url;
    }

    protected String buildClientLogoutUri(Request request) {
        String url = config.getClientLogoutUri();

        if(Strings.isEmpty(url)) {
            return request.getContextUrl() + "/logout";
        }else if(url.startsWith("/")) {
            url = request.getContextUrl() + url;
        }
        return url;
    }

    protected String buildRemoteLogoutUrl(Request request) {
        QueryStringBuilder qs = new QueryStringBuilder();
        
        qs.add(OAuth2Params.CLIENT_ID,                config.getClientId());
        qs.add(OAuth2Params.POST_LOGOUT_REDIRECT_URI, buildLogoutRedirectUri(request));
        
        return Urls.appendQueryString(config.getServerLogoutEndpointUrl(), qs.build());
    }
    
    protected String buildLogoutRedirectUri(Request request) {
        String url = null;
        
        if(!config.getClientRedirectUri().startsWith("/")) {
            url = config.getClientRedirectUri();
        }else{
            url = request.getContextUrl() + config.getClientRedirectUri();
        }
        
        url = Urls.appendQueryString(url, "oauth2_logout=1");
        
        return url;
    }
}