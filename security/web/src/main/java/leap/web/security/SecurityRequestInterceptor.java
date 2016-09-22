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
import leap.core.security.annotation.*;
import leap.core.web.RequestIgnore;
import leap.lang.Arrays2;
import leap.lang.Assert;
import leap.lang.Strings;
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.*;
import leap.web.action.Action;
import leap.web.action.ActionContext;
import leap.web.route.Route;
import leap.web.security.csrf.CSRF;
import leap.web.security.csrf.CsrfHandler;
import leap.web.security.path.*;
import leap.web.security.permission.PermissionManager;

public class SecurityRequestInterceptor implements RequestInterceptor,AppListener {

	private static final Log log = LogFactory.get(SecurityRequestInterceptor.class);

    protected @Inject @M SecurityConfig    config;
    protected @Inject @M PermissionManager perm;
    protected @Inject @M SecuredPathSource pathSource;
    protected @Inject @M SecurityHandler   handler;
    protected @Inject @M CsrfHandler       csrf;

    protected SecuredPathBuilder spb(Route route) {
        SecuredPathBuilder spb = route.getExtension(SecuredPathBuilder.class);
        if(null == spb) {
            spb = new DefaultSecuredPathBuilder(route);
            route.setExtension(SecuredPathBuilder.class, spb);
        }
        return spb;
    }

	@Override
    public void postAppStart(App app) throws Throwable {

	    for(Route route : app.routes()) {
            if(null != route.getAllowAnonymous()) {
                spb(route).setAllowAnonymous(route.getAllowAnonymous());
            }

            if(null != route.getAllowClientOnly()) {
                spb(route).setAllowClientOnly(route.getAllowClientOnly());
            }

            if(null != route.getAllowRememberMe()) {
                spb(route).setAllowRememberMe(route.getAllowRememberMe());
            }

            if(null != route.getPermissions()) {
                spb(route).setPermissionsAllowed(route.getPermissions());
            }

            if(null != route.getRoles()) {
                spb(route).setRolesAllowed(route.getRoles());
            }

            config.getPathPrefixFailureHandlers().forEach((prefix, handler) -> {
                if(Strings.startsWith(route.getPathTemplate().getTemplate(), prefix)) {
                    log.debug("Set failure handler for path prefix '{}'", route.getPathTemplate());
                    spb(route).setFailureHandler(handler);
                }
            });

            SecuredPathBuilder spb = route.removeExtension(SecuredPathBuilder.class);
            if(null != spb) {
                route.setExtension(SecuredPath.class, spb.build());
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
                new DefaultSecurityContextHolder(config, perm, request);

		return preHandleRequest(request, response, context);
    }

    protected State preHandleRequest(Request request, Response response, DefaultSecurityContextHolder context) throws Throwable {
        //Resolve authentication.
        State state = resolveAuthentication(request,response,context);
        if(state.isIntercepted()){
            return state;
        }

        //Disable csrf if anonymous access.
        if(!context.getAuthentication().isAuthenticated()) {
            CSRF.ignore(request);
        }

        //Handles request if login
        if(handleLoginRequest(request, response, context)){
            return State.INTERCEPTED;
        }

        //Handles request if logout.
        if(handleLogoutRequest(request, response, context)) {
            return State.INTERCEPTED;
        }

        return State.CONTINUE;
    }

    @Override
    public State handleRoute(Request request, Response response, Route route, ActionContext ac) throws Throwable {
        return handleSecurity(request, response, route);
    }

    @Override
    public State handleNoRoute(Request request, Response response) throws Throwable {
        return handleSecurity(request, response, null);
    }

    protected State handleSecurity(Request request, Response response, Route route) throws Throwable {
        DefaultSecurityContextHolder context = DefaultSecurityContextHolder.tryGet(request);
        if(null == context || context.isHandled()) {
            return State.CONTINUE;
        }
        context.markHandled();

        //Resolve secured path.
        context.setSecuredPath(resolveSecuredPath(request, response, context, route));

        //Check authentication
        State state = checkAuthentication(request, response, context);
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

    @Override
    public void completeHandleRequest(Request request, Response response, RequestExecution execution) throws Throwable {
		DefaultSecurityContextHolder.remove(request);
	}

	/**
	 * Returns <code>true</code> if the request already handled.
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

    protected SecuredPath resolveSecuredPath(Request request, Response response, DefaultSecurityContextHolder context, Route route) throws Throwable {
        SecuredPath p1 = null == route ? null : route.getExtension(SecuredPath.class);
        SecuredPath p2 = pathSource.getSecuredPath(context, request);

        if(null == p1) {
            return p2;
        }

        if(null == p2) {
            return p1;
        }

        return new MergedSecuredPath(route, p1, p2);
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