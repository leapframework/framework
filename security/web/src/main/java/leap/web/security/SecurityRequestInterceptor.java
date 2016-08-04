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
import leap.core.security.Authentication;
import leap.core.security.Authorization;
import leap.core.web.RequestIgnore;
import leap.lang.Arrays2;
import leap.lang.Assert;
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.path.Paths;
import leap.web.*;
import leap.web.action.Action;
import leap.web.config.WebConfig;
import leap.web.cors.CorsHandler;
import leap.web.route.Route;
import leap.web.security.annotation.*;
import leap.web.security.csrf.CsrfHandler;
import leap.web.security.path.SecuredPath;
import leap.web.security.path.SecuredPathConfigurator;
import leap.web.security.path.SecuredPathSource;
import leap.web.security.path.SecuredPaths;
import leap.web.security.permission.PermissionManager;

public class SecurityRequestInterceptor implements RequestInterceptor,AppListener {

	private static final Log log = LogFactory.get(SecurityRequestInterceptor.class);

    protected @Inject @M SecurityConfig       config;
    protected @Inject @M SecurityConfigurator configurator;
    protected @Inject @M WebConfig            webConfig;
    protected @Inject @M PermissionManager    permissionManager;
    protected @Inject @M SecuredPathSource    pathSource;
    protected @Inject @M SecurityHandler      handler;
    protected @Inject @M CsrfHandler          csrf;
    protected @Inject @M CorsHandler          cors;

	@Override
    public void postAppStart(App app) throws Throwable {
        SecuredPaths paths = configurator.paths();

	    for(Route route : app.routes()) {
	        Action action = route.getAction();

            if(route.isAllowAnonymous()) {
                paths.of(route).setAllowAnonymous(true).apply();
            }else{
                AllowAnonymous aa = action.searchAnnotation(AllowAnonymous.class);
                if(null != aa) {
                    paths.of(route).setAllowAnonymous(aa.value()).apply();
                }
            }

            if(route.isAllowClientOnly()) {
                paths.of(route).setAllowClientOnly(true).apply();
            }else{
                AllowClientOnly ac = action.searchAnnotation(AllowClientOnly.class);
                if(null != ac) {
                    paths.of(route).setAllowClientOnly(ac.value()).apply();
                }
            }

			AllowRememberMe ar = action.searchAnnotation(AllowRememberMe.class);
			if(null != ar) {
				paths.of(route).setAllowRememberMe(ar.value()).apply();
			}

            if(route.isCorsEnabled() || (!route.isCorsDisabled() && webConfig.isCorsEnabled())) {
                paths.of(route).setAllowCors(true).apply();
            }

            Permissions permissions = action.searchAnnotation(Permissions.class);
            if(null != permissions) {
                paths.of(route).setPermissionsAllowed(permissions.value()).apply();
            }
	        
	        Secured secured = action.searchAnnotation(Secured.class);
	        if(null != secured){
	            boolean isAction = action.getAnnotation(Secured.class) != null;

                SecuredPathConfigurator p;
	            
	            if(isAction){
	                 p = paths.of(route);
	            }else{
	                 p = paths.of(Paths.suffixWithSlash(route.getControllerPath()) + "**");
	            }
	            
	            p.setAllowRememberMe(secured.allowRememberMe());

	            if(!Arrays2.isEmpty(secured.roles())){
                    p.setRolesAllowed(secured.roles());
	            }

	            if(!Arrays2.isEmpty(secured.permissions())) {
                    p.setPermissionsAllowed(secured.permissions());
	            }

                p.apply();
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
		
		DefaultSecurityContextHolder context = new DefaultSecurityContextHolder(config, permissionManager, request);
		context.initContext();

		return handleRequest(request, response, context);
    }
	
	@Override
    public void completeHandleRequest(Request request, Response response, RequestExecution execution) throws Throwable {
		DefaultSecurityContextHolder.removeContext(request);
	}

	protected State handleRequest(Request request, Response response,DefaultSecurityContextHolder context) throws Throwable {
		//Resolve authentication.
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

        //Resolve security path.
        SecuredPath sp = resolveSecurityPath(request, response, context);
        context.setSecurityPath(sp);

        //Handles cors request.
        if(sp.isAllowCors()) {
            state = cors.handle(request, response);
            if(State.isIntercepted(state)) {
                return state;
            }
        }

        //Check authentication
		state = checkAuthentication(request, response, context);
        if(state.isIntercepted()) {
            return state;
        }

        //Resolve authorization.
        state = resolveAuthorization(request, response, context);
        if(state.isIntercepted()) {
            return state;
        }

        //Check authorization.
		return checkAuthorization(request, response, context);
	}
	
	/**
	 * Returns <code>true</code> if the request aleady handled.
	 */
	protected State resolveAuthentication(Request request, Response response, DefaultSecurityContextHolder context) throws Throwable {
	    SecurityInterceptor[] interceptors = config.getInterceptors();
		for(SecurityInterceptor interceptor : config.getInterceptors()) {
			if(interceptor.preResolveAuthentication(request, response, context).isIntercepted()){
				log.debug("Intercepted by interceptor : {}", interceptor.getClass());
				return State.INTERCEPTED;
			}
		}

		Authentication authc = context.getAuthentication();
        if(null == authc) {
            log.debug("Resolving authentication...");
            authc = handler.resolveAuthentication(request, response, context);

            Assert.notNull(authc,"'Authentication' must not be null");
            context.setAuthentication(authc);
        }else{
            log.debug("Authentication already resolved by interceptor -> {}", authc);
        }

		if(log.isDebugEnabled()) {
			if(authc.isAuthenticated()) {
				log.debug("Request authenticated to : {}", authc);
			}else{
				log.debug("Request not authenticated!");
			}
		}

		request.setUser(authc.getUser());

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

    protected SecuredPath resolveSecurityPath(Request request, Response response, DefaultSecurityContextHolder context) throws Throwable {
        return pathSource.getSecuredPath(context, request);
    }

    protected State checkAuthentication(Request request, Response response, DefaultSecurityContextHolder context) throws Throwable {
		if(!handler.checkAuthentication(request, response, context)){
			handler.handleAuthenticationDenied(request, response, context);
			return State.INTERCEPTED;
		}
		return State.CONTINUE;
    }

    protected State resolveAuthorization(Request request,Response response,DefaultSecurityContextHolder context) throws Throwable {
        SecurityInterceptor[] interceptors = config.getInterceptors();
        for(SecurityInterceptor si : interceptors) {
            if(State.isIntercepted(si.preResolveAuthorization(request, response, context))) {
                return State.INTERCEPTED;
            }
        }

        Authorization authz = context.getAuthorization();
        if(null == authz) {
            log.debug("Resolving authorization...");
            authz = handler.resolveAuthorization(request,response,context);
            Assert.notNull(authz,"The authorization must not be null");
            context.setAuthorization(authz);
        }else{
            log.debug("Authorization already resolved by interceptor -> {}", authz);
        }


        for(SecurityInterceptor si : interceptors) {
            if(State.isIntercepted(si.postResolveAuthorization(request, response, context))) {
                return State.INTERCEPTED;
            }
        }

        return State.CONTINUE;
    }
	
	/**
	 * Returns <code>true</code> if current request handled by this interceptor.
	 */
	protected State checkAuthorization(Request request, Response response, DefaultSecurityContextHolder context) throws Throwable {
        if(!handler.checkAuthorization(request, response, context)) {
            handler.handleAuthorizationDenied(request, response, context);
            return State.INTERCEPTED;
        }
		return State.CONTINUE;
	}
}