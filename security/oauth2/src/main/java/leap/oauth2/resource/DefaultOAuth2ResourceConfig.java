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
package leap.oauth2.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import leap.core.BeanFactory;
import leap.core.annotation.Configurable;
import leap.core.annotation.Inject;
import leap.core.cache.Cache;
import leap.core.cache.CacheManager;
import leap.core.ioc.PostCreateBean;
import leap.lang.path.Paths;
import leap.web.App;
import leap.web.AppInitializable;
import leap.web.security.SecurityConfigurator;

@Configurable(prefix="oauth2.resource")
public class DefaultOAuth2ResourceConfig implements OAuth2ResourceConfig, OAuth2ResourceConfigurator, PostCreateBean, AppInitializable {

    protected @Inject SecurityConfigurator sc;
    protected @Inject CacheManager         cm;
    
    protected boolean                    enabled;
    protected boolean                    interceptAnyRequests;
    protected String                     remoteServerUrl;
    protected String                     remoteTokenInfoUrl;
    protected Cache<String, Object>      cachedInterceptUrls;
    protected Map<String, OAuth2ResourceScope> scopes        = new HashMap<>();
    protected List<OAuth2ResourcePath>         interceptUrls = new CopyOnWriteArrayList<OAuth2ResourcePath>();
	
	@Override
	public OAuth2ResourceConfig config() {
		return this;
	}

    public boolean isEnabled() {
	    return enabled;
    }

	@Configurable.Property
    public OAuth2ResourceConfigurator setEnabled(boolean enabled) {
		this.enabled = enabled;
	    return this;
    }
	
	public boolean isInterceptAnyRequests() {
		return interceptAnyRequests;
	}

	public OAuth2ResourceConfigurator setInterceptAnyRequests(boolean interceptAnyRequests) {
		this.interceptAnyRequests = interceptAnyRequests;
		return this;
	}

	@Override
    public String getRemoteTokenInfoUrl() {
        return remoteTokenInfoUrl;
    }
	
	@Configurable.Property
    public OAuth2ResourceConfigurator setRemoteServerUrl(String url) {
	    this.remoteServerUrl = url;
	    this.remoteTokenInfoUrl = Paths.suffixWithoutSlash(url) + "/oauth2/tokeninfo";
        return this;
    }

    @Configurable.Property
	public OAuth2ResourceConfigurator setRemoteTokenInfoUrl(String url) {
	    this.remoteTokenInfoUrl = url;
	    return this;
	}

    public OAuth2ResourceConfigurator addScope(String path, OAuth2ResourceScope scope) {
		scopes.put(path, scope);
		return this;
	}

	@Override
    public OAuth2ResourceScope getScope(String path) {
	    return scopes.get(path);
    }

    public OAuth2ResourceConfigurator intercept(OAuth2ResourcePath url) {
		if(null != url) {
			interceptUrls.add(url);
		}
	    return this;
    }

	@Override
    public OAuth2ResourcePath getResourcePath(String path) {
		Object cachedUrl = cachedInterceptUrls.get(path);
		if(null != cachedUrl) {
			return cachedUrl instanceof OAuth2ResourcePath ? (OAuth2ResourcePath)cachedUrl : null;
		}
		
		for(OAuth2ResourcePath url : interceptUrls) {
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