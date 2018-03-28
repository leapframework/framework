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

import leap.core.security.Authentication;
import leap.core.security.Authorization;
import leap.core.security.Credentials;
import leap.core.security.SecurityContext;
import leap.core.security.UserPrincipal;
import leap.core.validation.Validation;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.Request;
import leap.web.action.ActionContext;
import leap.web.route.Route;
import leap.web.security.authc.AuthenticationContext;
import leap.web.security.login.LoginContext;
import leap.web.security.logout.LogoutContext;
import leap.web.security.path.SecuredPath;
import leap.web.security.permission.PermissionManager;

public class DefaultSecurityContextHolder extends SecurityContext implements SecurityContextHolder {

	private static final Log log = LogFactory.get(DefaultSecurityContextHolder.class);

    static DefaultSecurityContextHolder tryGet(Request request) {
        return (DefaultSecurityContextHolder)request.getAttribute(CONTEXT_HOLDER_ATTRIBUTE_NAME);
    }

    static void remove(Request request) {
        request.removeAttribute(CONTEXT_ATTRIBUTE_NAME);
    }

	protected final SecurityConfig    config;
	protected final PermissionManager permissionManager;
	protected final Request           request;
    protected final ActionContext     actionContext;

	protected SecuredPath   securedPath;
    protected LoginContext  loginContext;
    protected LogoutContext logoutContext;
	protected String        authenticationToken;
	protected boolean       error;
	protected Object        errorObj;
	protected String        identity;
    protected String        denyMessage;

    private boolean handled;

	public DefaultSecurityContextHolder(SecurityConfig config, PermissionManager permissionManager, Request request){
        this(config, permissionManager, request, null);
    }

    public DefaultSecurityContextHolder(SecurityConfig config, PermissionManager permissionManager, Request request, ActionContext actionContext){
        this.config            = config;
        this.permissionManager = permissionManager;
        this.request           = request;
        this.actionContext     = actionContext;
        request.setAttribute(CONTEXT_ATTRIBUTE_NAME, this);
        request.setAttribute(CONTEXT_HOLDER_ATTRIBUTE_NAME, this);
    }

	@Override
    public Validation validation() {
	    return request.getValidation();
    }

    @Override
    public ActionContext getActionContext() {
        return actionContext;
    }

    @Override
    public SecurityConfig getSecurityConfig() {
	    return config;
    }

    @Override
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    @Override
    public SecurityContext getSecurityContext() {
	    return this;
    }

	@Override
	public SecuredPath getSecuredPath() {
		return securedPath;
	}

	public void setSecuredPath(SecuredPath path) {
		this.securedPath = path;
	}

    @Override
    public String getDenyMessage() {
        return denyMessage;
    }

    @Override
    public void setDenyMessage(String message) {
        this.denyMessage = denyMessage;
    }

    @Override
	public String getAuthenticationToken() {
		return authenticationToken;
	}

	@Override
	public void setAuthenticationToken(String token) {
        log.debug("Set authentication token : {}", token);
		this.authenticationToken = token;
	}

    public void setAuthentication(Authentication authc) {
        log.debug("Set authentication : {}", authc);
        this.authentication = authc;
    }

    public void setAuthorization(Authorization authz) {
        log.debug("Set authorization : {}", authz);
        this.authorization = authz;
    }

    public LoginContext getLoginContext() {
		if(null == loginContext){
			loginContext = new DefaultLoginContext();
		}
	    return loginContext;
    }

    public LogoutContext getLogoutContext() {
		if(null == logoutContext){
			logoutContext = new DefaultLogoutContext();
		}
	    return logoutContext;
    }

    protected abstract class AbstractContext implements AuthenticationContext {
    	private boolean       error;
        private Object		  errorObj;	
        
		@Override
        public SecurityConfig getSecurityConfig() {
	        return config;
        }

		@Override
        public SecurityContext getSecurityContext() {
	        return DefaultSecurityContextHolder.this;
        }

        @Override
        public Validation validation() {
	        return DefaultSecurityContextHolder.this.validation();
        }
        @Override
		public Object getErrorObj() {
			return this.errorObj;
		}

		@Override
		public void setErrorObj(Object obj) {
			this.errorObj = obj;
		}
		@Override
        public boolean isError() {
	        return error;
        }

		@Override
        public void setError(boolean error) {
			this.error = error;
        }

		@Override
		public String getIdentity() {
			return DefaultSecurityContextHolder.this.identity;
		}

		@Override
		public void setIdentity(String identity) {
			DefaultSecurityContextHolder.this.identity = identity;
		}
	}

	protected final class DefaultLoginContext extends AbstractContext implements LoginContext {
		
        private String        returnUrl;
        private String        loginUrl;
        private Credentials   credentials;
        private UserPrincipal user;

		@Override
		public String getAuthenticationToken() {
			return DefaultSecurityContextHolder.this.getAuthenticationToken();
		}

		@Override
		public void setAuthenticationToken(String token) {
			DefaultSecurityContextHolder.this.setAuthenticationToken(token);
		}

		@Override
        public Authentication getAuthentication() {
	        return DefaultSecurityContextHolder.this.authentication;
        }

        public void setAuthentication(Authentication auth) {
			DefaultSecurityContextHolder.this.setAuthentication(auth);
		}

		@Override
        public String getReturnUrl() {
	        return returnUrl;
        }

		@Override
        public void setReturnUrl(String returnUrl) {
		    this.returnUrl = returnUrl;
		}

		@Override
        public String getLoginUrl() {
	        return loginUrl;
        }

		@Override
        public void setLoginUrl(String url) {
		    this.loginUrl = url;
		}
		    

		@Override
        public boolean isCredentialsResolved() {
	        return null != credentials;
        }

		@Override
        public Credentials getCredentials() {
	        return credentials;
        }

		@Override
        public void setCredentials(Credentials credentials) {
			this.credentials = credentials;
        }

		@Override
        public boolean isAuthenticated() {
	        return null != user && !user.isAnonymous();
        }

		@Override
        public UserPrincipal getUser() {
	        return user;
        }

		@Override
        public void setUser(UserPrincipal user) {
			this.user = user;
        }

		@Override
		public SecuredPath getSecuredPath() {
			return securedPath;
		}
	}
	
	protected final class DefaultLogoutContext extends AbstractContext implements LogoutContext {

		private String 	returnUrl;

		@Override
		public String getAuthenticationToken() {
			return DefaultSecurityContextHolder.this.getAuthenticationToken();
		}

		@Override
		public void setAuthenticationToken(String token) {
			DefaultSecurityContextHolder.this.setAuthenticationToken(token);
		}

		@Override
        public Authentication getAuthentication() {
	        return DefaultSecurityContextHolder.this.authentication;
        }

        public void setAuthentication(Authentication auth) {
			DefaultSecurityContextHolder.this.setAuthentication(auth);
		}

		public String getReturnUrl() {
			return returnUrl;
		}

		public void setReturnUrl(String returnUrl) {
			this.returnUrl = returnUrl;
		}
		@Override
		public SecuredPath getSecuredPath() {
			return securedPath;
		}
	}

	@Override
	public boolean isError() {
		return error;
	}

	@Override
	public void setError(boolean error) {
		this.error = error;
	}

	@Override
	public Object getErrorObj() {
		return errorObj;
	}

	@Override
	public void setErrorObj(Object obj) {
		this.errorObj = obj;
	}

	@Override
	public String getIdentity() {
		return this.identity;
	}

	@Override
	public void setIdentity(String identity) {
		this.identity = identity;
	}

    boolean isHandled() {
        return handled;
    }

    void markHandled() {
        handled = true;
    }
}