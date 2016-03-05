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
package leap.oauth2.rs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import leap.core.BeanFactory;
import leap.core.annotation.ConfigProperty;
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.core.cache.Cache;
import leap.core.cache.CacheManager;
import leap.core.ioc.PostCreateBean;
import leap.web.App;
import leap.web.AppInitializable;
import leap.web.security.SecurityConfigurator;

@Configurable(prefix="oauth2.rs")
public class DefaultOAuth2ResServerConfig implements OAuth2ResServerConfig, OAuth2ResServerConfigurator, PostCreateBean, AppInitializable {

	protected @Inject SecurityConfigurator sc;
    protected @Inject CacheManager         cm;

	protected boolean               enabled;
	protected AuthzServerMode		authzServerMode = AuthzServerMode.NONE;
	protected String                remoteTokenInfoEndpointUrl;
	protected Cache<String, Object> cachedInterceptUrls;
	protected Map<String, ResScope> scopes        = new HashMap<>();
	protected List<ResPath>         interceptUrls = new CopyOnWriteArrayList<>();
	
	@Override
	public OAuth2ResServerConfig config() {
		return this;
	}

    public boolean isEnabled() {
	    return enabled;
    }

	@ConfigProperty
    public OAuth2ResServerConfigurator setEnabled(boolean enabled) {
		this.enabled = enabled;
	    return this;
    }

	@Override
	public boolean isUseLocalAuthorizationServer() {
		return authzServerMode == AuthzServerMode.LOCAL;
	}

	@Override
	public boolean isUseRemoteAuthorizationServer() {
		return authzServerMode == AuthzServerMode.REMOTE;
	}

	@Override
	public OAuth2ResServerConfigurator useLocalAuthorizationServer() {
		authzServerMode = AuthzServerMode.LOCAL;
		return this;
	}

	@Override
	public OAuth2ResServerConfigurator useRemoteAuthorizationServer() {
		authzServerMode = AuthzServerMode.REMOTE;
		return this;
	}

	@Override
	public OAuth2ResServerConfigurator useRemoteAuthorizationServer(String tokenInfoEndpointUrl) {
		authzServerMode = AuthzServerMode.REMOTE;
		this.setRemoteTokenInfoEndpointUrl(tokenInfoEndpointUrl);
		return this;
	}

	@ConfigProperty
	public OAuth2ResServerConfigurator setAuthorizationServerMode(AuthzServerMode mode) {
		return this;
	}

	@Override
    public String getRemoteTokenInfoEndpointUrl() {
        return remoteTokenInfoEndpointUrl;
    }
	
    @ConfigProperty
	public OAuth2ResServerConfigurator setRemoteTokenInfoEndpointUrl(String url) {
	    this.remoteTokenInfoEndpointUrl = url;
	    return this;
	}

    public OAuth2ResServerConfigurator addScope(String path, ResScope scope) {
		scopes.put(path, scope);
		return this;
	}

	@Override
    public ResScope resolveResourceScope(String path) {
	    return scopes.get(path);
    }

    public OAuth2ResServerConfigurator intercept(ResPath url) {
		if(null != url) {
			interceptUrls.add(url);
		}
	    return this;
    }

	@Override
    public ResPath resolveResourcePath(String path) {
		Object cachedUrl = cachedInterceptUrls.get(path);
		if(null != cachedUrl) {
			return cachedUrl instanceof ResPath ? (ResPath)cachedUrl : null;
		}
		
		for(ResPath url : interceptUrls) {
			if(url.matches(path)){
				cachedInterceptUrls.put(path, url);
				return url;
			}
		}
		
		cachedInterceptUrls.put(path, Boolean.FALSE);
	    return null;
    }

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        this.cachedInterceptUrls = cm.createSimpleLRUCache(1024);
    }

    @Override
    public void postAppInit(App app) throws Throwable {
        if(enabled) {
            if(!sc.config().isEnabled()) {
                sc.enable(true);
            }
        }
    }
}