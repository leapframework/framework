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
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.core.ds.DataSourceManager;
import leap.core.schedule.SchedulerManager;
import leap.core.store.JdbcStore;
import leap.lang.Args;
import leap.lang.Run;
import leap.lang.Strings;
import leap.lang.security.RSA;
import leap.oauth2.as.client.AuthzClientStore;
import leap.oauth2.as.code.AuthzCodeStore;
import leap.oauth2.as.store.AuthzInMemoryStore;
import leap.oauth2.as.token.AuthzTokenStore;
import leap.web.App;
import leap.web.AppInitializable;
import leap.web.security.SecurityConfigurator;

import java.security.PrivateKey;

@Configurable(prefix="oauth2.as")
public class DefaultOAuth2ServerConfig implements OAuth2ServerConfig, OAuth2ServerConfigurator, AppInitializable {
	
    protected @Inject AppConfig                       appConfig;
    protected @Inject SecurityConfigurator            sc;
    protected @Inject DataSourceManager               dsm;
    protected @Inject AuthzInMemoryStore              inMemoryStore;
    protected @Inject AuthzClientStore                clientStore;
    protected @Inject AuthzCodeStore                  codeStore;
    protected @Inject AuthzTokenStore                 tokenStore;
    protected @Inject SchedulerManager                schedulerManager;
    protected @Inject(name = "jdbc") AuthzClientStore jdbcClientStore;
    protected @Inject(name = "jdbc") AuthzCodeStore   jdbcCodeStore;
    protected @Inject(name = "jdbc") AuthzTokenStore  jdbcTokenStore;
    
    protected boolean enabled                    = false;
    protected boolean httpsOnly                  = true;
    protected boolean cleanupEnabled             = true;
    protected int     cleanupInterval            = 60 * 5; //5 minute
    protected boolean openIDConnectEnabled       = true;
    protected boolean singleLogoutEnabled        = true;
    protected boolean tokenEndpointEnabled       = true;
    protected boolean authzEndpointEnabled       = true;
    protected boolean tokenInfoEndpointEnabled   = true;
    protected boolean logoutEndpointEnabled      = true;
    protected boolean passwordCredentialsEnabled = true;
    protected boolean refreshTokenEnabled        = true;
    protected boolean authorizationCodeEnabled   = true;
    protected boolean                    implicitGrantEnabled            = true;
    protected boolean                    clientCredentialsEnabled        = true;
    protected String                     tokenEndpointPath               = DEFAULT_TOKEN_ENDPOINT_PATH;
    protected String                     authzEndpointPath               = DEFAULT_AUTHZ_ENDPOINT_PATH;
    protected String                     tokenInfoEndpointPath           = DEFAULT_TOKENINFO_ENDPOINT_PATH;
    protected String                     logoutEndpointPath              = DEFAULT_LOGOUT_ENDPOINT_PATH;
    protected String                     errorView                       = DEFAULT_ERROR_VIEW;
    protected String                     loginView                       = DEFAULT_LOGIN_VIEW;
    protected String                     logoutView                      = DEFAULT_LOGOUT_VIEW;
    protected int                        defaultAccessTokenExpires       = 3600;                          //1 hour
    protected int                        defaultRefreshTokenExpires      = 3600 * 24 * 30;                //30 days
    protected int                        defaultAuthorizationCodeExpires = 60 * 5;                        //5 minutes
    protected int                        defaultIdTokenExpires           = 60 * 5;                        //5 minutes
    protected String                     jdbcDataSourceName              = null;
    protected PrivateKey                 privateKey                      = null;

	private boolean hasDataSources;

	@Override
	public OAuth2ServerConfig config() {
		return this;
	}

	@Override
    public AuthzInMemoryStore inMemoryStore() {
        return inMemoryStore;
    }
	
    @Override
    public OAuth2ServerConfigurator useInMemoryStore() {
        clientStore = inMemoryStore;
        codeStore   = inMemoryStore;
        tokenStore  = inMemoryStore;
        return this;
    }

    @Override
    public OAuth2ServerConfigurator useJdbcStore() {
        clientStore = jdbcClientStore;
        codeStore   = jdbcCodeStore;
        tokenStore  = jdbcTokenStore;
        return this;
    }

    @Override
	public boolean isEnabled() {
		return enabled;
	}

    @Override
    public OAuth2ServerConfigurator setSingleLogoutEnabled(boolean enabled) {
        this.singleLogoutEnabled = enabled;
        return this;
    }

    @Configurable.Property
    public OAuth2ServerConfigurator setCleanupEnabled(boolean cleanup) {
        this.cleanupEnabled = cleanup;
        return this;
    }

    @Configurable.Property
    public OAuth2ServerConfigurator setCleanupInterval(int seconds) {
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
    public boolean isOpenIDConnectEnabled() {
        return openIDConnectEnabled;
    }

    @Override
    public boolean isSingleLogoutEnabled() {
        return singleLogoutEnabled;
    }

    @Override
	public boolean isTokenEndpointEnabled() {
		return tokenEndpointEnabled;
	}

	@Override
	public boolean isAuthzEndpointEnabled() {
		return authzEndpointEnabled;
	}
	
    @Override
    public boolean isTokenInfoEndpointEnabled() {
        return tokenInfoEndpointEnabled;
    }
    
	@Override
    public boolean isLogoutEndpointEnabled() {
        return logoutEndpointEnabled;
    }

    @Override
    public boolean isPasswordCredentialsEnabled() {
	    return passwordCredentialsEnabled;
    }

	@Override
    public boolean isRefreshTokenEnabled() {
	    return refreshTokenEnabled;
    }

	@Override
    public boolean isClientCredentialsEnabled() {
	    return clientCredentialsEnabled;
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
    public int getDefaultAuthorizationCodeExpires() {
        return defaultAuthorizationCodeExpires;
    }
	
	@Override
    public int getDefaultIdTokenExpires() {
        return defaultIdTokenExpires;
    }
	
    @Override
    public String getJdbcDataSourceName() {
        return jdbcDataSourceName;
    }

    @Configurable.Property
	public OAuth2ServerConfigurator setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}
	
	@Configurable.Property
	public OAuth2ServerConfigurator setHttpsOnly(boolean httpsOnly) {
	    this.httpsOnly = httpsOnly;
	    return this;
	}
	
	@Configurable.Property
	public OAuth2ServerConfigurator setOpenIDConnectEnabled(boolean enabled) {
	    this.openIDConnectEnabled = enabled;
	    return this;
	}

    @Configurable.Property
    public OAuth2ServerConfigurator setSingleLogoutenabled(boolean enabled) {
        this.singleLogoutEnabled = enabled;
        return this;
    }

	@Configurable.Property
	public OAuth2ServerConfigurator setAuthzEndpointEnabled(boolean enabled) {
		this.authzEndpointEnabled = enabled;
		return this;
	}
	
	@Configurable.Property
	public OAuth2ServerConfigurator setTokenEndpointEnabled(boolean enabled) {
		this.tokenEndpointEnabled = enabled;
		return this;
	}

	@Configurable.Property
    public OAuth2ServerConfigurator setTokenInfoEndpointEnabled(boolean enabled) {
        this.tokenInfoEndpointEnabled = enabled;
        return this;
    }
    
    @Configurable.Property
    public OAuth2ServerConfigurator setLogoutEndpointEnabled(boolean enabled) {
        this.logoutEndpointEnabled = enabled;
        return this;
    }

    @Configurable.Property
	public OAuth2ServerConfigurator setAuthorizationCodeEnabled(boolean authorazationCodeEnabled) {
		this.authorizationCodeEnabled = authorazationCodeEnabled;
		return this;
	}
	
    @Configurable.Property
    public OAuth2ServerConfigurator setImplicitGrantEnabled(boolean enabled) {
		this.implicitGrantEnabled = enabled;
		return this;
    }

	@Configurable.Property
	public OAuth2ServerConfigurator setPasswordCredentialsEnabled(boolean passwordCredentialsEnabled) {
		this.passwordCredentialsEnabled = passwordCredentialsEnabled;
		return this;
	}

	@Configurable.Property
	public OAuth2ServerConfigurator setRefreshTokenEnabled(boolean refreshTokenEnabled) {
		this.refreshTokenEnabled = refreshTokenEnabled;
		return this;
	}

	@Configurable.Property
	public OAuth2ServerConfigurator setClientCredentialsEnabled(boolean clientCredentialsEnabled) {
		this.clientCredentialsEnabled = clientCredentialsEnabled;
		return this;
	}
	
	@Configurable.Property
    public OAuth2ServerConfigurator setTokenEndpointPath(String path) {
		tokenEndpointPath = path;
	    return this;
    }

	@Configurable.Property
    public OAuth2ServerConfigurator setAuthzEndpointPath(String path) {
		this.authzEndpointPath = path;
	    return this;
    }

	@Configurable.Property
    public OAuth2ServerConfigurator setTokenInfoEndpointPath(String path) {
        this.tokenInfoEndpointPath = path;
        return this;
    }
    
    @Configurable.Property
    public OAuth2ServerConfigurator setLogoutEndpointPath(String path) {
        this.logoutEndpointPath = path;
        return this;
    }
	
	@Configurable.Property
    public OAuth2ServerConfigurator setErrorView(String view) {
		this.errorView = view;
	    return this;
    }
	
	@Configurable.Property
	public OAuth2ServerConfigurator setLoginView(String view) {
	    this.loginView = view;
	    return this;
	}
	
	@Configurable.Property
	public OAuth2ServerConfigurator setLogoutView(String view) {
	    this.loginView = view;
	    return this;
	}

	@Configurable.Property
	public OAuth2ServerConfigurator setDefaultAccessTokenExpires(int defaultExpiresIn) {
		this.defaultAccessTokenExpires = defaultExpiresIn;
		return this;
	}

	@Configurable.Property
    public OAuth2ServerConfigurator setDefaultRefreshTokenExpires(int seconds) {
		this.defaultRefreshTokenExpires = seconds;
	    return this;
    }
	
	@Configurable.Property
	public OAuth2ServerConfigurator setDefaultAuthorizationCodeExpires(int seconds) {
	    this.defaultAuthorizationCodeExpires = seconds;
	    return this;
	}
	
	@Configurable.Property
	public OAuth2ServerConfigurator setDefaultIdTokenExpires(int seconds) {
	    this.defaultIdTokenExpires = seconds;
	    return this;
	}
	
	@Configurable.Property
	public OAuth2ServerConfigurator setJdbcDataSourceName(String name) {
	    this.jdbcDataSourceName = name;
	    return this;
	}
	
    @Override
    public PrivateKey getPrivateKey() {
        return privateKey;
    }
    
    @Override
    public PrivateKey ensureGetPrivateKey() {
        if(null == privateKey) {
            return appConfig.ensureGetPrivateKey();
        }
        return null;
    }

    @Override
    public OAuth2ServerConfigurator setClientStore(AuthzClientStore store) {
        Args.notNull(store);
        this.clientStore = store;
        return this;
    }
    
    public OAuth2ServerConfigurator setCodeStore(AuthzCodeStore store) {
        Args.notNull(store);
        this.codeStore = store;
        return this;
    }
    
    public OAuth2ServerConfigurator setTokenStore(AuthzTokenStore store) {
        Args.notNull(store);
        this.tokenStore = store;
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
    public OAuth2ServerConfigurator setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    @Override
    public OAuth2ServerConfigurator setPrivateKey(String privateKey) {
        if(Strings.isEmpty(privateKey)) {
            this.privateKey = null;
        }else{
            this.privateKey = RSA.decodePrivateKey(privateKey);
        }
        return this;
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
            }
            
            if(cleanupEnabled) {
                schedulerManager
                    .newFixedThreadPoolScheduler("auth-cleanup")
                    .scheduleAtFixedRate(() -> cleanup(), cleanupInterval * 1000l);
            }
        }
    }
    
    protected void cleanup() {
        Run.catchThrowable(() -> getCodeStore().cleanupAuthorizationCodes()); 
        Run.catchThrowable(() -> getTokenStore().cleanupTokens()); 
    }
}