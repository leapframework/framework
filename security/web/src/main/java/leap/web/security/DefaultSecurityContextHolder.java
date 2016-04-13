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

import leap.core.security.*;
import leap.core.validation.Validation;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.Request;
import leap.web.security.authc.AuthenticationContext;
import leap.web.security.login.LoginContext;
import leap.web.security.logout.LogoutContext;
import leap.web.security.path.SecuredPath;
import leap.web.security.permission.PermissionManager;

public class DefaultSecurityContextHolder extends SecurityContext implements SecurityContextHolder {

	private static final Log log = LogFactory.get(DefaultSecurityContextHolder.class);

	protected final SecurityConfig    config;
	protected final PermissionManager permissionManager;
	protected final Request           request;

	protected SecuredPath   path;
    protected LoginContext  loginContext;
    protected LogoutContext logoutContext;
	protected String        authenticationToken;
	protected boolean		error;
	protected Object		errorObj;

	public DefaultSecurityContextHolder(SecurityConfig config, PermissionManager permissionManager, Request request){
		this.config            = config;
        this.permissionManager = permissionManager;
		this.request           = request;
	}

	void initContext() {
        request.setAttribute(CONTEXT_ATTRIBUTE_NAME, this);
    }

    static void removeContext(Request request) {
        request.removeAttribute(CONTEXT_ATTRIBUTE_NAME);
    }

	@Override
    public Validation validation() {
	    return request.getValidation();
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
	public SecuredPath getSecurityPath() {
		return path;
	}

	public void setSecurityPath(SecuredPath path) {
		this.path = path;
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
}