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
package leap.web.security;

import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.security.SecurityContext;
import leap.core.web.RequestIgnore;
import leap.lang.Arrays2;
import leap.lang.Assert;
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.web.*;
import leap.web.action.Action;
import leap.web.route.Route;
import leap.web.security.annotation.AllowAnonymous;
import leap.web.security.annotation.AllowClientOnly;
import leap.web.security.annotation.Secured;
import leap.web.security.authc.Authentication;
import leap.web.security.authz.Authorization;
import leap.web.security.csrf.CsrfHandler;

public class SecurityRequestInterceptor implements RequestInterceptor,AppListener {

	private static final Log log = LogFactory.get(SecurityRequestInterceptor.class);
	
    protected @Inject @M SecurityConfig       config;
    protected @Inject @M SecurityConfigurator configurator;
    protected @Inject @M SecuredPathSource    pathSource;
    protected @Inject @M SecurityHandler      handler;
    protected @Inject @M CsrfHandler          csrf;

	@Override
    public void postAppStart(App app) throws Throwable {
	    for(Route route : app.routes()) {
	        Action action = route.getAction();
	        
	        AllowAnonymous a = action.searchAnnotation(AllowAnonymous.class);
	        if(route.isAllowAnonymous() || (null != a && a.value())) {
	            configurator.secured(new SecuredPathBuilder()
	                                        .allowAnonymous()
	                                        .setPathPattern(route.getPathTemplate())
	                                        .build());
	            continue;
	        }
	        
	        AllowClientOnly aa = action.searchAnnotation(AllowClientOnly.class);
	        if(route.isAllowClientOnly() || (null != aa && aa.value())) {
	            configurator.secured(new SecuredPathBuilder()
	                                .allowClientOnly()
	                                .setPathPattern(route.getPathTemplate())
	                                .build());
	            continue;
	        }
	        
	        Secured secured = action.searchAnnotation(Secured.class);
	        if(null != secured){
	            boolean isAction = action.getAnnotation(Secured.class) != null;
	            
	            SecuredPathBuilder url = new SecuredPathBuilder();
	            
	            if(isAction){
	                url.setPathPattern(route.getPathTemplate());
	            }else{
	                url.path(Paths.suffixWithSlash(route.getControllerPath()) + "**");
	            }
	            
	            url.setAllowRememberMe(secured.allowRememberMe());

	            if(!Arrays2.isEmpty(secured.roles())){
	                //TODO : 
	            }
	            
	            if(!Arrays2.isEmpty(secured.permissions())) {
	                //TODO : 
	            }
	            
	            configurator.secured(url.build());
	        }
	    }
    }
	
	@Override
    public State preHandleRequest(Request request, Response response) throws Throwable {
		//Web security do not enabled.
		if(!config.isEnabled()){
			log.debug("Web security not enabled, ignore the interceptor");
			return State.CONTINUE;
		}
		
		if(State.isIntercepted(csrf.handleRequest(request, response))){
		    return State.INTERCEPTED;
		}
		
		//TODO : cache 
		//Check is the request ignored.
		for(RequestIgnore ignore : config.getIgnores()) {
			if(ignore.matches(request)) {
				return State.CONTINUE;
			}
		}
		
		DefaultSecurityContextHolder context = 
		        new DefaultSecurityContextHolder(config,request);;
		
		SecurityContext.setCurrent(context.getSecurityContext());
		
		return handleRequest(request, response, context);
    }
	
	@Override
    public void completeHandleRequest(Request request, Response response, RequestExecution execution) throws Throwable {
		SecurityContext.removeCurrent();
	}

	/**
	 * Returns <code>true</code> if current request handled.
	 */
	protected State handleRequest(Request request, Response response,DefaultSecurityContextHolder context) throws Throwable {
		//Resolves authentication.
		State state = resolveAuthentication(request,response,context);
		if(state.isIntercepted()){
			return state;
		}
		
		//Handles request if login
		if(handleLoginRequest(request, response, context)){
			return State.INTERCEPTED;
		}
		
		//Handles request if logout.
		if(handleLogoutRequest(request, response, context)) {
			return State.INTERCEPTED;
		}
		
		//Check security.
		return checkSecurity(request, response, context);
	}
	
	/**
	 * Returns <code>true</code> if the request aleady handled.
	 */
	protected State resolveAuthentication(Request request, Response response, DefaultSecurityContextHolder context) throws Throwable {
	    SecurityInterceptor[] interceptors = config.getInterceptors();
		for(SecurityInterceptor interceptor : interceptors) {
			if(interceptor.preResolveAuthentication(request, response, context).isIntercepted()){
				log.debug("Intercepted by interceptor : {}", interceptor.getClass());
				return State.INTERCEPTED;
			}
		}
		
        Authentication authentication = context.getAuthentication();
        if (null == authentication) {
			log.debug("Resolving authentication...");
            authentication = handler.resolveAuthentication(request, response, context);
            Assert.notNull(authentication,"'Authentication' must not be null");
            context.setAuthentication(authentication);
        }

		if(log.isDebugEnabled()) {
			if(authentication.isAuthenticated()) {
				log.debug("Request authenticated to : {}", authentication);
			}else{
				log.debug("Request not authenticated!");
			}
		}

        context.setUser(authentication.getUserPrincipal());
		request.setUser(authentication.getUserPrincipal());
		
		for(SecurityInterceptor interceptor : interceptors) {
			if(interceptor.postResolveAuthentication(request, response, context).isIntercepted()){
				return State.INTERCEPTED;
			}
		}
		
		return State.CONTINUE;
	}
	
    protected boolean handleLoginRequest(Request request, Response response, DefaultSecurityContextHolder context) throws Throwable {
        return handler.handleLoginRequest(request, response, context);
    }

    protected boolean handleLogoutRequest(Request request, Response response, DefaultSecurityContextHolder context) throws Throwable {
        return handler.handleLogoutRequest(request, response, context);
    }
	
	/**
	 * Returns <code>true</code> if current request handled by this interceptor.
	 */
	protected State checkSecurity(Request request, Response response, DefaultSecurityContextHolder context) throws Throwable {
		Authentication authentication = context.getAuthentication();
		
		//Check current request path is secured :
		//1. if secured, checks is allow to access
		//2. if not, return false directly.
		SecuredPath securedPath = pathSource.getSecuredPath(context, request);
		if(null != securedPath && !securedPath.isAllow(request, context, authentication)){
			handler.handleAuthenticationDenied(request, response, context);
			return State.INTERCEPTED;
		}
		
		//Authorizes current request
		Authorization authorization = resolveAuthorization(request, response, context);
		if(null != authorization && !authorization.isAuthorized()){
		    handler.handleAuthorizationDenied(request, response, context);
			return State.INTERCEPTED;
		}
		
		return State.CONTINUE;
	}
	
	protected Authorization resolveAuthorization(Request request,Response response,DefaultSecurityContextHolder context) throws Throwable {
		return handler.resolveAuthorization(request,response,context);
	}
}