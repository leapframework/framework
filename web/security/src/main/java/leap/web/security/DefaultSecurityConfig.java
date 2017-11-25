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

import leap.core.AppConfig;
import leap.core.BeanFactory;
import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.core.ioc.BeanList;
import leap.core.ioc.PostConfigureBean;
import leap.core.security.crypto.PasswordEncoder;
import leap.core.web.RequestIgnore;
import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.path.AntPathPattern;
import leap.web.Renderable;
import leap.web.security.csrf.CsrfStore;
import leap.web.security.path.SecuredPaths;
import leap.web.security.user.UserStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Configurable(prefix = "websecurity")
public class DefaultSecurityConfig implements SecurityConfig, SecurityConfigurator, PostConfigureBean {

    protected BeanFactory              factory                        = null;
    protected boolean                  enabled                        = false;
    protected boolean                  crossContext                   = false;
    protected Boolean                  csrfEnabled                    = null;
    protected boolean                  loginEnabled                   = true;
    protected boolean                  logoutEnabled                  = true;
    protected boolean                  authenticateAnyRequests        = true;
    protected boolean                  authorizeAnyRequests           = false;
    protected int                      defaultAuthenticationExpires   = SecurityConstants.DEFAULT_AUTHENTICATION_EXPIRES;
    protected String                   returnUrlParameterName         = SecurityConstants.DEFAULT_RETURN_URL_PARAMETER;
    protected boolean                  rememberMeEnabled              = true;
    protected String                   rememberMeSecret               = null;
    protected String                   rememberMeCookieName           = SecurityConstants.DEFAULT_REMEMBERME_COOKIE;
    protected String                   rememberMeParameterName        = SecurityConstants.DEFAULT_REMEMBERME_PARAMETER;
    protected String                   rememberMeExpiresParameterName = SecurityConstants.DEFAULT_REMEMBERME_EXPIRES_PARAMETER;
    protected String                   loginUrl                       = null;
    protected String                   loginAction                    = SecurityConstants.DEFAULT_LOGIN_ACTION;
    protected String                   logoutAction                   = SecurityConstants.DEFAULT_LOGOUT_ACTION;
    protected String                   logoutSuccessUrl               = SecurityConstants.DEFAULT_LOGOUT_SUCCESS_URL;
    protected int                      defaultRememberMeExpires       = SecurityConstants.DEFAULT_REMEMBERME_EXPIRES;
    protected String                   csrfHeaderName                 = SecurityConstants.DEFAULT_CSRF_HEADER;
    protected String                   csrfParameterName              = SecurityConstants.DEFAULT_CSRF_PARAMETER;
    protected boolean                  authenticationTokenEnabled     = true;
    protected String                   authenticationTokenCookieName  = SecurityConstants.DEFAULT_TOKEN_AUTHENTICATION_COOKIE;
    protected String                   authenticationTokenHeaderName  = SecurityConstants.DEFAULT_TOKEN_AUTHENTICATION_HEADER;
    protected String                   authenticationTokenType        = SecurityConstants.DEFAULT_TOKEN_TYPE;
    protected String                   tokenSecret                    = null;
    protected String                   cookieDomain                   = null;
    protected List<RequestIgnore>      ignores                        = new ArrayList<>();

    protected Map<String,SecurityFailureHandler> pathPrefixFailureHandlers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    protected @Inject SecuredPaths                  securedPaths;
    protected @Inject PasswordEncoder               passwordEncoder;
    protected @Inject UserStore                     userStore;
    protected @Inject CsrfStore                     csrfStore;
    protected @Inject BeanList<SecurityInterceptor> interceptors;

    private RequestIgnore[]       ignoresArray       = new RequestIgnore[] {};
    private SecurityInterceptor[] interceptorArray   = new SecurityInterceptor[]{};
    private final Object          interceptorLock    = new Object();
    
    public DefaultSecurityConfig() {
        super();
    }
    
    @Override
    public SecurityConfig config() {
        return this;
    }

    @Override
    public SecuredPaths paths() {
        return securedPaths;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @ConfigProperty
    public SecurityConfigurator setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public BeanList<SecurityInterceptor> interceptors() {
        return interceptors;
    }

    @Override
    public SecurityInterceptor[] getInterceptors() {
        if(interceptorArray.length != interceptors.size()) {
            synchronized (interceptorLock) {
                if(interceptorArray.length != interceptors.size()){
                    interceptorArray = interceptors.toArray(new SecurityInterceptor[interceptors.size()]);        
                }
            }
        }
        return interceptorArray;
    }

    public boolean isCrossContext() {
        return crossContext;
    }

    @ConfigProperty
    public DefaultSecurityConfig setCrossContext(boolean crossContext) {
        this.crossContext = crossContext;
        return this;
    }

    public boolean isCsrfEnabled() {
        return csrfEnabled == null ? this.enabled : csrfEnabled;
    }

    @ConfigProperty
    public DefaultSecurityConfig setCsrfEnabled(boolean csrfEnabled) {
        this.csrfEnabled = csrfEnabled;
        return this;
    }

    public boolean isAuthenticateAnyRequests() {
        return authenticateAnyRequests;
    }

    @ConfigProperty
    public DefaultSecurityConfig setAuthenticateAnyRequests(boolean authenticateAnyRequests) {
        this.authenticateAnyRequests = authenticateAnyRequests;
        return this;
    }

    public boolean isAuthorizeAnyRequests() {
        return authorizeAnyRequests;
    }

    @ConfigProperty
    public DefaultSecurityConfig setAuthorizeAnyRequests(boolean authorizeAnyRequests) {
        this.authorizeAnyRequests = authorizeAnyRequests;
        return this;
    }
    
    @Override
    public UserStore getUserStore() {
        return userStore;
    }

    @Override
    public CsrfStore getCsrfStore() {
        return csrfStore;
    }

    @Override
    public SecurityConfigurator setUserStore(UserStore userStore) {
        Args.notNull(userStore, "userStore");
        this.userStore = userStore;
        factory.setPrimaryBean(UserStore.class, userStore);
        return this;
    }

    @Override
    public SecurityConfigurator setCsrfStore(CsrfStore csrfStore) {
        Args.notNull(csrfStore, "csrfStore");
        this.csrfStore = csrfStore;
        return this;
    }

    @Override
    public String getReturnUrlParameterName() {
        return returnUrlParameterName;
    }

    @ConfigProperty
    public DefaultSecurityConfig setReturnUrlParameterName(String returnUrlParameterName) {
        Args.notEmpty(returnUrlParameterName);
        this.returnUrlParameterName = returnUrlParameterName;
        return this;
    }

    @Override
    public boolean isRememberMeEnabled() {
        return rememberMeEnabled;
    }

    @ConfigProperty
    public DefaultSecurityConfig setRememberMeEnabled(boolean rememberMeEnabled) {
        this.rememberMeEnabled = rememberMeEnabled;
        return this;
    }

    @Override
    public String getRememberMeSecret() {
        return rememberMeSecret;
    }

    @ConfigProperty
    public DefaultSecurityConfig setRememberMeSecret(String rememberMeSecret) {
        Args.notEmpty(rememberMeSecret);
        this.rememberMeSecret = rememberMeSecret;
        return this;
    }

    public String getRememberMeCookieName() {
        return rememberMeCookieName;
    }

    @ConfigProperty
    public DefaultSecurityConfig setRememberMeCookieName(String rememberMeCookieName) {
        Args.notEmpty(rememberMeCookieName);
        this.rememberMeCookieName = rememberMeCookieName;
        return this;
    }

    public String getRememberMeParameterName() {
        return rememberMeParameterName;
    }

    @ConfigProperty
    public DefaultSecurityConfig setRememberMeParameterName(String rememberMeParameterName) {
        Args.notEmpty(rememberMeParameterName);
        this.rememberMeParameterName = rememberMeParameterName;
        return this;
    }

    public String getRememberMeExpiresParameterName() {
        return rememberMeExpiresParameterName;
    }

    @ConfigProperty
    public DefaultSecurityConfig setRememberMeExpiresParameterName(String rememberMeExpiresParameterName) {
        Args.notEmpty(rememberMeExpiresParameterName);
        this.rememberMeExpiresParameterName = rememberMeExpiresParameterName;
        return this;
    }

    public int getDefaultRememberMeExpires() {
        return defaultRememberMeExpires;
    }

    @ConfigProperty
    public DefaultSecurityConfig setDefaultRememberMeExpires(int defaultRememberExpires) {
        Args.assertTrue(defaultRememberExpires > 0, "Expires must be > 0");
        this.defaultRememberMeExpires = defaultRememberExpires;
        return this;
    }

    @Override
    public String getCsrfHeaderName() {
        return csrfHeaderName;
    }

    @Override
    public String getCsrfParameterName() {
        return csrfParameterName;
    }

    public DefaultSecurityConfig setCsrfHeaderName(String csrfHeaderName) {
        this.csrfHeaderName = csrfHeaderName;
        return this;
    }

    @ConfigProperty
    public DefaultSecurityConfig setCsrfParameterName(String csrfParameterName) {
        this.csrfParameterName = csrfParameterName;
        return this;
    }

    @Override
    public boolean isAuthenticationTokenEnabled() {
        return authenticationTokenEnabled;
    }

    @Override
    public int  getDefaultAuthenticationExpires() {
        return defaultAuthenticationExpires;
    }

    @Override
    public String getAuthenticationTokenCookieName() {
        return authenticationTokenCookieName;
    }

    public String getAuthenticationTokenHeaderName() {
        return authenticationTokenHeaderName;
    }

    @ConfigProperty
    public void setAuthenticationTokenHeaderName(String authenticationTokenHeaderName) {
        this.authenticationTokenHeaderName = authenticationTokenHeaderName;
    }

    @ConfigProperty
    public DefaultSecurityConfig setAuthenticationTokenEnabled(boolean authenticationTokenEnabled) {
        this.authenticationTokenEnabled = authenticationTokenEnabled;
        return this;
    }

    @ConfigProperty
    public void setAuthenticationTokenCookieName(String authenticationTokenCookieName) {
        this.authenticationTokenCookieName = authenticationTokenCookieName;
    }

    public String getAuthenticationTokenType() {
        return authenticationTokenType;
    }

    @ConfigProperty
    public void setAuthenticationTokenType(String authenticationTokenType) {
        this.authenticationTokenType = authenticationTokenType;
    }

    public String getSecret() {
        return tokenSecret;
    }

    @ConfigProperty
    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    public String getCookieDomain() {
        return cookieDomain;
    }

    @ConfigProperty
    public void setCookieDomain(String cookieDomain) {
        this.cookieDomain = cookieDomain;
    }

    @Override
    public boolean isLoginEnabled() {
        return loginEnabled;
    }

    @ConfigProperty
    public SecurityConfigurator setLoginEnabled(boolean loginEnabled) {
        this.loginEnabled = loginEnabled;
        return this;
    }

    @Override
    public boolean isLogoutEnabled() {
        return logoutEnabled;
    }

    @ConfigProperty
    public SecurityConfigurator setLogoutEnabled(boolean logoutEnabled) {
        this.logoutEnabled = logoutEnabled;
        return this;
    }

    public String getLoginUrl() {
        if(null == loginUrl) {
            return Renderable.ACTION_PREFIX + loginAction;
        }
        return loginUrl;
    }

    @ConfigProperty
    public DefaultSecurityConfig setLoginUrl(String url) {
        this.loginUrl = url;
        return this;
    }
    
    @Override
    public String getLoginAction() {
        return loginAction;
    }

    @Override
    public String getLogoutAction() {
        return logoutAction;
    }

    public String getLogoutSuccessUrl() {
        return logoutSuccessUrl;
    }
    
    @ConfigProperty
    public SecurityConfigurator setLoginAction(String path) {
        this.loginAction = path;
        return this;
    }

    @Override
    public SecurityConfigurator setLogoutAction(String path) {
        this.logoutAction = path;
        return this;
    }

    @ConfigProperty
    public DefaultSecurityConfig setLogoutSuccessUrl(String url) {
        Args.notEmpty(url);
        this.logoutSuccessUrl = url;
        return this;
    }

    @Override
    public SecurityConfigurator setPathPrefixFailureHandler(String pathPrefix, SecurityFailureHandler failureHandler) {
        pathPrefixFailureHandlers.put(pathPrefix, failureHandler);
        return this;
    }

    @Override
    public RequestIgnore[] getIgnores() {
        return ignoresArray;
    }

    @Override
    public Map<String, SecurityFailureHandler> getPathPrefixFailureHandlers() {
        return pathPrefixFailureHandlers;
    }

    @Override
    public SecuredPaths getSecuredPaths() {
        return securedPaths;
    }

    @Override
    public SecurityConfigurator ignore(String path) {
        AntPathPattern pattern = new AntPathPattern(path);
        ignores.add((req) -> pattern.matches(req.getPath()));
        ignoresArray = ignores.toArray(new RequestIgnore[ignores.size()]);
        return this;
    }

    public SecurityConfigurator setPasswordEncoder(PasswordEncoder encoder) {
        Args.notNull(encoder, "password encoder");
        this.passwordEncoder = encoder;
        return this;
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    @Override
    public void postConfigure(BeanFactory factory, AppConfig config) throws Throwable {
        this.factory = factory;

        if (Strings.isEmpty(tokenSecret)) {
            tokenSecret = config.ensureGetSecret();
        }
        
        if (Strings.isEmpty(rememberMeSecret)) {
            rememberMeSecret = tokenSecret;
        }

        if (Strings.isEmpty(rememberMeSecret)) {
            rememberMeSecret = config.ensureGetSecret();
        }

    }
}