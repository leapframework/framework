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
package leap.web.security.login;

import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.lang.Strings;
import leap.lang.net.Urls;
import leap.web.AppListener;
import leap.web.Request;
import leap.web.Response;
import leap.web.UrlHandler;
import leap.web.security.SecurityConfig;
import leap.web.security.SecurityContextHolder;

public class DefaultLoginViewHandler implements LoginViewHandler,AppListener {
	
	protected @Inject @M SecurityConfig config;
	protected @Inject @M UrlHandler     urlHandler;
	
    @Override
    public void promoteLogin(Request request, Response response, SecurityContextHolder context) throws Throwable {
        goLoginUrl(request, response, context);
    }

    @Override
    public void goLoginUrl(Request request, Response response, SecurityContextHolder context) throws Throwable {
        LoginContext sc = context.getLoginContext();
        
        String loginUrl  = getLoginUrl(sc, request);
        String returnUrl = getReturnUrl(sc, request, loginUrl);
        
        //goto login url
        goLoginUrl(sc, request, response, loginUrl, returnUrl);        
	}
    
	@Override
    public void handleLoginSuccess(Request request, Response response, SecurityContextHolder context) throws Throwable {
        response.sendRedirect(getReturnUrl(context.getLoginContext(), request, null));
    }
	
    @Override
    public void handleLoginFailure(Request request, Response response, SecurityContextHolder context) throws Throwable {
        goLoginUrl(request, response, context);
    }
	
	protected void goLoginUrl(LoginContext context,Request request,Response response,String loginUrl,String returnUrl) throws Throwable {
	    urlHandler.handleUrl(request, response, loginUrl, (url, info) -> {
	        if(info.isRedirect()) {
	            if(null == context.getLoginUrl() && null != returnUrl) {
	                return Urls.appendQueryParams(url, config.getReturnUrlParameterName(), returnUrl);
	            }
	        }else{
	            request.setAttribute(context.getSecurityConfig().getReturnUrlParameterName(), returnUrl);
	        }
	        
	        return url;
	    });
	}
	
    protected String getLoginUrl(LoginContext context, Request request) {
        String loginUrl = context.getLoginUrl();
        if (null == loginUrl) {
            loginUrl = context.getSecurityConfig().getLoginUrl();
        }
        return loginUrl;
    }
	
	protected String getReturnUrl(LoginContext context, Request request, String loginUrl){
        String returnUrl = context.getReturnUrl() ;
        
        if(null == returnUrl){
        	returnUrl = request.getParameter(config.getReturnUrlParameterName());
        }
        
        if(Strings.isEmpty(returnUrl)){
            if(null != loginUrl) {
                String loginPath = Urls.removeQueryString(urlHandler.removePrefix(loginUrl));
                if(!request.getPath().equals(loginPath)) {
                    return request.getUriWithQueryString();
                }
            }
            return "/";
        }
        
        return returnUrl;
	}
	
}