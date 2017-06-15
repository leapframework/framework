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
package leap.oauth2.as;

import leap.core.AppConfig;
import leap.core.AppConfigException;
import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.core.ds.DataSourceManager;
import leap.core.schedule.SchedulerManager;
import leap.core.security.token.jwt.JwtVerifier;
import leap.core.security.token.jwt.RsaVerifier;
import leap.core.store.JdbcStore;
import leap.lang.Args;
import leap.lang.Try;
import leap.lang.Strings;
import leap.lang.security.RSA;
import leap.oauth2.as.client.AuthzClientStore;
import leap.oauth2.as.code.AuthzCodeStore;
import leap.oauth2.as.sso.AuthzSSOStore;
import leap.oauth2.as.store.AuthzInMemoryStore;
import leap.oauth2.as.token.AuthzTokenStore;
import leap.web.App;
import leap.web.AppInitializable;
import leap.web.security.SecurityConfigurator;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

@Configurable(prefix="oauth2.as")
public class DefaultOAuth2AuthzServerConfig implements OAuth2AuthzServerConfig, OAuth2AuthzServerConfigurator, AppInitializable {

    protected @Inject                AppConfig            appConfig;
    protected @Inject                SecurityConfigurator sc;
    protected @Inject                DataSourceManager    dsm;
    protected @Inject                AuthzInMemoryStore   inMemoryStore;
    protected @Inject                AuthzClientStore     clientStore;
    protected @Inject                AuthzCodeStore       codeStore;
    protected @Inject                AuthzTokenStore      tokenStore;
    protected @Inject                AuthzSSOStore        ssoStore;
    protected @Inject                SchedulerManager     schedulerManager;
    protected @Inject(name = "jdbc") AuthzClientStore     jdbcClientStore;
    protected @Inject(name = "jdbc") AuthzCodeStore       jdbcCodeStore;
    protected @Inject(name = "jdbc") AuthzTokenStore      jdbcTokenStore;
    protected @Inject(name = "jdbc") AuthzSSOStore        jdbcSSOStore;

    protected boolean    enabled                         = false;
    protected boolean    httpsOnly                       = true;
    protected boolean    cleanupEnabled                  = true;
    protected int        cleanupInterval                 = DEFAULT_CLEANUP_INTERVAL;
    protected boolean    singleLoginEnabled              = true;
    protected boolean    singleLogoutEnabled             = true;
    protected boolean    passwordCredentialsEnabled      = true;
    protected boolean    loginTokenEnabled               = true;
    protected boolean    userInfoEnabled                 = true;
    protected boolean    authorizationCodeEnabled        = true;
    protected boolean    implicitGrantEnabled            = true;
    protected boolean    clientCredentialsEnabled        = true;
    protected boolean    tokenClientEnabled              = true;
    protected boolean    requestLevelScopeEnabled        = false;
    protected String     tokenEndpointPath               = DEFAULT_TOKEN_ENDPOINT_PATH;
    protected String     authzEndpointPath               = DEFAULT_AUTHZ_ENDPOINT_PATH;
    protected String     tokenInfoEndpointPath           = DEFAULT_TOKENINFO_ENDPOINT_PATH;
    protected String     userInfoEndpointPath            = DEFAULT_USERINFO_ENDPOINT_PATH;
    protected String     logoutEndpointPath              = DEFAULT_LOGOUT_ENDPOINT_PATH;
    protected String     errorView                       = DEFAULT_ERROR_VIEW;
    protected String     loginView                       = DEFAULT_LOGIN_VIEW;
    protected String     logoutView                      = DEFAULT_LOGOUT_VIEW;
    protected int        defaultAccessTokenExpires       = DEFAULT_ACCESS_TOKEN_EXPIRES;
    protected int        defaultRefreshTokenExpires      = DEFAULT_REFRESH_TOKEN_EXPIRES;
    protected int        defaultLoginTokenExpires        = DEFAULT_LOGIN_TOKEN_EXPIRES;
    protected int        defaultAuthorizationCodeExpires = DEFAULT_AUTHORIZATION_CODE_EXPIRES;
    protected int        defaultIdTokenExpires           = DEFAULT_ID_TOKEN_EXPIRES;
    protected int        defaultLoginSessionExpires      = DEFAULT_LOGIN_SESSION_EXPIRES;
    protected String     jdbcDataSourceName              = null;
    protected PrivateKey privateKey                      = null;
    protected PublicKey  publicKey                       = null;
    protected JwtVerifier jwtVerifier                    = null;

	private boolean hasDataSources;

	@Override
	public OAuth2AuthzServerConfig config() {
		return this;
	}

	@Override
    public AuthzInMemoryStore inMemoryStore() {
        return inMemoryStore;
    }

    @Override
    public OAuth2AuthzServerConfigurator useInMemoryStore() {
        clientStore = inMemoryStore;
        codeStore   = inMemoryStore;
        tokenStore  = inMemoryStore;
        ssoStore    = inMemoryStore;
        return this;
    }

    @Override
    public OAuth2AuthzServerConfigurator useJdbcStore() {
        clientStore = jdbcClientStore;
        codeStore   = jdbcCodeStore;
        tokenStore  = jdbcTokenStore;
        ssoStore    = jdbcSSOStore;
        return this;
    }

    @Override
	public boolean isEnabled() {
		return enabled;
	}

    @ConfigProperty
    public OAuth2AuthzServerConfigurator setSingleLoginEnabled(boolean enabled) {
        this.singleLoginEnabled = enabled;
        return this;
    }

    @ConfigProperty
    public OAuth2AuthzServerConfigurator setSingleLogoutEnabled(boolean enabled) {
        this.singleLogoutEnabled = enabled;
        return this;
    }

    @ConfigProperty
    public OAuth2AuthzServerConfigurator setCleanupEnabled(boolean cleanup) {
        this.cleanupEnabled = cleanup;
        return this;
    }

    @ConfigProperty
    public OAuth2AuthzServerConfigurator setCleanupInterval(int seconds) {
        this.cleanupInterval = seconds;
        return this;
    }

    @Override
    public boolean isCleanupEnabled() {
        return cleanupEnabled;
    }

    @Override
    public int getCleanupInterval() {
        return cleanupInterval;
    }

    @Override
    public boolean isHttpsOnly() {
        return httpsOnly;
    }

    @Override
    public boolean isSingleLoginEnabled() {
        return singleLoginEnabled;
    }

    @Override
    public boolean isSingleLogoutEnabled() {
        return singleLogoutEnabled;
    }

    @Override
    public boolean isPasswordCredentialsEnabled() {
	    return passwordCredentialsEnabled;
    }

    @Override
    public boolean isRequestLevelScopeEnabled() {
        return requestLevelScopeEnabled;
    }

    @Override
    public boolean isUserInfoEnabled() {
        return userInfoEnabled;
    }

    @Override
    public boolean isClientCredentialsEnabled() {
	    return clientCredentialsEnabled;
    }

    @Override
    public boolean isTokenClientEnabled() {
        return tokenClientEnabled;
    }

    @Override
    public boolean isAuthorizationCodeEnabled() {
	    return authorizationCodeEnabled;
    }

	@Override
    public boolean isImplicitGrantEnabled() {
	    return implicitGrantEnabled;
    }

	@Override
    public String getTokenEndpointPath() {
	    return tokenEndpointPath;
    }

	@Override
    public String getAuthzEndpointPath() {
	    return authzEndpointPath;
    }

    @Override
    public String getTokenInfoEndpointPath() {
        return tokenInfoEndpointPath;
    }

    public String getUserInfoEndpointPath() {
        return userInfoEndpointPath;
    }

    @Override
    public String getLogoutEndpointPath() {
        return logoutEndpointPath;
    }

    @Override
    public String getErrorView() {
	    return errorView;
    }

	@Override
    public String getLoginView() {
        return loginView;
    }

    @Override
    public String getLogoutView() {
        return logoutView;
    }

    public int getDefaultAccessTokenExpires() {
		return defaultAccessTokenExpires;
	}

	@Override
    public int getDefaultRefreshTokenExpires() {
	    return defaultRefreshTokenExpires;
    }

    @Override
    public int getDefaultLoginTokenExpires() {
        return defaultLoginTokenExpires;
    }

    @Override
    public int getDefaultAuthorizationCodeExpires() {
        return defaultAuthorizationCodeExpires;
    }

	@Override
    public int getDefaultIdTokenExpires() {
        return defaultIdTokenExpires;
    }

    @Override
    public int getDefaultLoginSessionExpires() {
        return defaultLoginSessionExpires;
    }

    @Override
    public String getJdbcDataSourceName() {
        return jdbcDataSourceName;
    }

    @ConfigProperty
	public OAuth2AuthzServerConfigurator setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	@ConfigProperty
	public OAuth2AuthzServerConfigurator setHttpsOnly(boolean httpsOnly) {
	    this.httpsOnly = httpsOnly;
	    return this;
	}

    @ConfigProperty
	public OAuth2AuthzServerConfigurator setAuthorizationCodeEnabled(boolean authorazationCodeEnabled) {
		this.authorizationCodeEnabled = authorazationCodeEnabled;
		return this;
	}

    @ConfigProperty
    public OAuth2AuthzServerConfigurator setImplicitGrantEnabled(boolean enabled) {
		this.implicitGrantEnabled = enabled;
		return this;
    }

	@ConfigProperty
	public OAuth2AuthzServerConfigurator setPasswordCredentialsEnabled(boolean passwordCredentialsEnabled) {
		this.passwordCredentialsEnabled = passwordCredentialsEnabled;
		return this;
	}

    @ConfigProperty
    public OAuth2AuthzServerConfigurator setLoginTokenEnabled(boolean enabled) {
        this.loginTokenEnabled = enabled;
        return this;
    }

    @ConfigProperty
    public OAuth2AuthzServerConfig setUserInfoEnabled(boolean enabled) {
        this.userInfoEnabled = enabled;
        return this;
    }

	@ConfigProperty
	public OAuth2AuthzServerConfigurator setClientCredentialsEnabled(boolean clientCredentialsEnabled) {
		this.clientCredentialsEnabled = clientCredentialsEnabled;
		return this;
	}
    @ConfigProperty
    public OAuth2AuthzServerConfigurator setTokenClientEnabled(boolean tokenClientEnabled){
        this.tokenClientEnabled = tokenClientEnabled;
        return this;
    }

    @ConfigProperty
    public OAuth2AuthzServerConfigurator setRequestLevelScopeEnabled(boolean enabled) {
        this.requestLevelScopeEnabled = enabled;
        return this;
    }

    @ConfigProperty
    public OAuth2AuthzServerConfigurator setTokenEndpointPath(String path) {
		tokenEndpointPath = path;
	    return this;
    }

	@ConfigProperty
    public OAuth2AuthzServerConfigurator setAuthzEndpointPath(String path) {
		this.authzEndpointPath = path;
	    return this;
    }

	@ConfigProperty
    public OAuth2AuthzServerConfigurator setTokenInfoEndpointPath(String path) {
        this.tokenInfoEndpointPath = path;
        return this;
    }

    @ConfigProperty
    public OAuth2AuthzServerConfig setUserInfoEndpointPath(String path) {
        this.userInfoEndpointPath = path;
        return this;
    }

    @ConfigProperty
    public OAuth2AuthzServerConfigurator setLogoutEndpointPath(String path) {
        this.logoutEndpointPath = path;
        return this;
    }

	@ConfigProperty
    public OAuth2AuthzServerConfigurator setErrorView(String view) {
		this.errorView = view;
	    return this;
    }

	@ConfigProperty
	public OAuth2AuthzServerConfigurator setLoginView(String view) {
	    this.loginView = view;
	    return this;
	}

	@ConfigProperty
	public OAuth2AuthzServerConfigurator setLogoutView(String view) {
	    this.loginView = view;
	    return this;
	}

	@ConfigProperty
	public OAuth2AuthzServerConfigurator setDefaultAccessTokenExpires(int defaultExpiresIn) {
		this.defaultAccessTokenExpires = defaultExpiresIn;
		return this;
	}

	@ConfigProperty
    public OAuth2AuthzServerConfigurator setDefaultRefreshTokenExpires(int seconds) {
		this.defaultRefreshTokenExpires = seconds;
	    return this;
    }

    @ConfigProperty
    public OAuth2AuthzServerConfigurator setDefaultLoginTokenExpires(int seconds) {
        this.defaultLoginTokenExpires = seconds;
        return this;
    }

	@ConfigProperty
	public OAuth2AuthzServerConfigurator setDefaultAuthorizationCodeExpires(int seconds) {
	    this.defaultAuthorizationCodeExpires = seconds;
	    return this;
	}

	@ConfigProperty
	public OAuth2AuthzServerConfigurator setDefaultIdTokenExpires(int seconds) {
	    this.defaultIdTokenExpires = seconds;
	    return this;
	}

    @ConfigProperty
    public OAuth2AuthzServerConfigurator setDefaultLoginSessionExpires(int seconds){
        this.defaultLoginSessionExpires = seconds;
        return this;
    }

	@ConfigProperty
	public OAuth2AuthzServerConfigurator setJdbcDataSourceName(String name) {
	    this.jdbcDataSourceName = name;
	    return this;
	}

    @Override
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    @Override
    public PublicKey getPublicKey() {
        return publicKey;
    }
    @Override
    public PrivateKey ensureGetPrivateKey() {
        if(null == privateKey) {
            return appConfig.ensureGetPrivateKey();
        }
        return null;
    }

    public OAuth2AuthzServerConfigurator setClientStore(AuthzClientStore store) {
        Args.notNull(store);
        this.clientStore = store;
        return this;
    }

    public OAuth2AuthzServerConfigurator setCodeStore(AuthzCodeStore store) {
        Args.notNull(store);
        this.codeStore = store;
        return this;
    }

    public OAuth2AuthzServerConfigurator setTokenStore(AuthzTokenStore store) {
        Args.notNull(store);
        this.tokenStore = store;
        return this;
    }

    public OAuth2AuthzServerConfigurator setSSOStore(AuthzSSOStore store) {
        Args.notNull(store);
        this.ssoStore = store;
        return this;
    }

    @Override
    public AuthzClientStore getClientStore() {
        if(null == clientStore) {
            return hasDataSources ? jdbcClientStore : inMemoryStore;
        }
        return clientStore;
    }

    @Override
    public AuthzCodeStore getCodeStore() {
        if(null == codeStore) {
            return hasDataSources ? jdbcCodeStore : inMemoryStore;
        }
        return codeStore;
    }

    @Override
    public AuthzTokenStore getTokenStore() {
        if(null == tokenStore) {
            return hasDataSources ? jdbcTokenStore : inMemoryStore;
        }
        return tokenStore;
    }

    @Override
    public AuthzSSOStore getSSOStore() {
        if(null == ssoStore) {
            return hasDataSources ? jdbcSSOStore : inMemoryStore;
        }
        return ssoStore;
    }

    public OAuth2AuthzServerConfigurator setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
        return this;
    }
    @Override
    public OAuth2AuthzServerConfigurator setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    @ConfigProperty
    public void setPrivateKeyStr(String privateKey) {
        if(Strings.isEmpty(privateKey)) {
            this.privateKey = null;
        }else{
            this.privateKey = RSA.decodePrivateKey(privateKey);
        }
    }

    @ConfigProperty
    public void setPublicKeyStr(String publicKey) {
        if(Strings.isEmpty(publicKey)) {
            this.publicKey = null;
        }else{
            this.publicKey = RSA.decodePublicKey(publicKey);
        }
    }

    @Override
    public JwtVerifier getJwtVerifier() {
        return jwtVerifier;
    }

    @Override
    public OAuth2AuthzServerConfigurator useRsaJwtVerifier() {
        if(this.publicKey == null){
            throw new NullPointerException("public key is null! please use setPublicKeyStr(String publicKey) " +
                    "or setPublicKey to set the public key.");
        }
        if(this.publicKey instanceof RSAPublicKey){
            this.jwtVerifier = new RsaVerifier((RSAPublicKey)this.getPublicKey());
        }else{
            throw new AppConfigException("this public key is not a rsa public key!");
        }
        return this;
    }

    @Override
    public OAuth2AuthzServerConfigurator useJwtVerifier(JwtVerifier verifier) {
        return null;
    }

    @Override
    public void postAppInit(App app) throws Throwable {
        if(enabled) {
            if(!sc.config().isEnabled()) {
                sc.enable(true);
            }

            this.hasDataSources = dsm.hasDataSources();

            if(hasDataSources) {
                String dataSourceName = Strings.firstNotEmpty(jdbcDataSourceName, DataSourceManager.DEFAULT_DATASOURCE_NAME);

                if(getTokenStore() instanceof JdbcStore) {
                    ((JdbcStore) getTokenStore()).setDataSourceName(dataSourceName);
                }

                if(getCodeStore() instanceof JdbcStore) {
                    ((JdbcStore)getCodeStore()).setDataSourceName(dataSourceName);
                }

                if(getClientStore() instanceof JdbcStore) {
                    ((JdbcStore)getClientStore()).setDataSourceName(dataSourceName);
                }

                if(getSSOStore() instanceof  JdbcStore) {
                    ((JdbcStore) getSSOStore()).setDataSourceName(dataSourceName);
                }
            }

            if(isCleanupEnabled()) {
                schedulerManager
                    .newFixedThreadPoolScheduler("auth-cleanup")
                    .scheduleAtFixedRate(() -> cleanup(), getCleanupInterval() * 1000l);
            }
        }
    }

    protected void cleanup() {
        Try.catchAll(() -> getCodeStore().cleanupAuthorizationCodes());
        Try.catchAll(() -> getTokenStore().cleanupTokens());
        Try.catchAll(() -> getSSOStore().cleanupSSO());
    }
}