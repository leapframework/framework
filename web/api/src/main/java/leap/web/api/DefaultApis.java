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
package leap.web.api;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import leap.core.annotation.Inject;
import leap.lang.Args;
import leap.lang.beans.BeanType;
import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.http.QueryStringBuilder;
import leap.lang.net.Urls;
import leap.lang.path.Paths;
import leap.lang.reflect.ReflectClass;
import leap.lang.reflect.ReflectField;
import leap.web.App;
import leap.web.AppInitializable;
import leap.web.api.config.*;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.ApiMetadataFactory;
import leap.web.route.Route;

public class DefaultApis implements Apis, AppInitializable  {
	
	protected @Inject ApiConfiguratorFactory configuratorFactory;
    protected @Inject ApiConfigSource        configSource;
	protected @Inject ApiConfigProcessor[]   configProcessors;
	protected @Inject ApiMetadataFactory     metadataFactory;
	
	protected Map<String, ApiConfigurator> configurators  = new ConcurrentHashMap<String, ApiConfigurator>();
	protected Map<String, ApiConfig>       configurations = new ConcurrentHashMap<String, ApiConfig>();
	protected Map<String, ApiMetadata>     metadatas      = new ConcurrentHashMap<String, ApiMetadata>();
	
	protected Map<String, ApiConfigurator> configuratorsImmutableView  = Collections.unmodifiableMap(configurators);
	protected Map<String, ApiConfig>       configurationsImmutableView = Collections.unmodifiableMap(configurations);
	protected Map<String, ApiMetadata>     metadatasImmutableView      = Collections.unmodifiableMap(metadatas);
	
	protected boolean defaultOAuthEnabled;
	protected String  defaultOAuthAuthorizationUrl;
	protected String  defaultOAuthTokenUrl;
	
	@Override
    public Map<String, ApiConfigurator> configurators() {
	    return configuratorsImmutableView;
    }
	
	@Override
    public Map<String, ApiConfig> configurations() {
	    return configurationsImmutableView;
    }
	
	@Override
    public Map<String, ApiMetadata> metadatas() {
	    return metadatasImmutableView;
    }

	@Override
    public ApiConfigurator add(String name, String basePath) throws ObjectExistsException {
		Args.notEmpty(name,     "name");
		Args.notEmpty(basePath, "basePath");
		
		String key = name.toLowerCase();
		if(configurators.containsKey(key)) {
			throw new ObjectExistsException("The api '" + name + "' already exists");
		}
 
		ApiConfigurator configurator = configuratorFactory.createConfigurator(this, name, basePath);
		configurators.put(key, configurator);
		configurations.put(key, configurator.config());
		return configurator;
    }

	@Override
    public ApiConfigurator of(String name) throws ObjectNotFoundException {
		Args.notEmpty(name, "name");
		
		ApiConfigurator c = configurators.get(name.toLowerCase());
		if(null == c) {
			throw new ObjectNotFoundException("The api '" + name + "' not found");
		}
	    return c;
    }
	
	@Override
    public boolean isDefaultOAuthEnabled() {
        return defaultOAuthEnabled;
    }

    @Override
    public String getDefaultOAuthAuthorizationUrl() {
        return defaultOAuthAuthorizationUrl;
    }

    @Override
    public String getDefaultOAuthTokenUrl() {
        return defaultOAuthTokenUrl;
    }

    @Override
    public Apis setDefaultOAuthEnabled(boolean enabled) {
        this.defaultOAuthEnabled = enabled;
        return this;
    }

    @Override
    public Apis setDefaultOAuthAuthorizationUrl(String url) {
        this.defaultOAuthAuthorizationUrl = url;
        return this;
    }
    
    @Override
    public Apis setDefaultOAuthAuthorizationUrl(String endpoint, String clientId, String redirectUri) {
        Args.notEmpty(endpoint, "endpoint");
        
        QueryStringBuilder qs = new QueryStringBuilder();
        qs.add("client_id", clientId)
          .add("redirect_uri", redirectUri)
          .add("response_type", "token");
        
        this.defaultOAuthAuthorizationUrl = Urls.appendQueryString(endpoint, qs.build());
        return this;
    }

    @Override
    public Apis setDefaultOAuthTokenUrl(String url) {
        this.defaultOAuthTokenUrl = url;
        return this;
    }

    @Override
    public void postAppInit(App app) throws Throwable {
        configSource.loadConfiguration(app, this);

		for(Entry<String, ApiConfigurator> entry : configurators.entrySet()) {
			
			String key = entry.getKey();
			ApiConfigurator c = entry.getValue();
			
			//do configuration
			doConfiguration(app, c);
			
			//create metadata
			ApiMetadata m = createMetadata(c);
			metadatas.put(key, m);	
		}
	}
	
	protected void doConfiguration(App app, ApiConfigurator c) {
		//resolve routes of api.
		resolveRoutes(app, c);
		
		//configure by processors.
		for(ApiConfigProcessor p : configProcessors) {
			p.preProcess(c);
		}
		
		for(ApiConfigProcessor p : configProcessors) {
			p.postProcess(c.config());
		}

        configureRoutes(app, c.config());
	}
	
    protected void resolveRoutes(App app, ApiConfigurator c) {
		String basePath				   = c.config().getBasePath();
		String basePathSuffixWithSlash = Paths.suffixWithSlash(basePath);
		for(Route route : app.routes()) {
			String pathTemplate = route.getPathTemplate().getTemplate();
			if(pathTemplate.equals(basePath) || pathTemplate.startsWith(basePathSuffixWithSlash)) {
				
				if(!c.config().isCorsDisabled() && !route.isCorsDisabled()) {
					route.setCorsEnabled(true);
				}
				
				c.addRoute(route);
			}
		}
	}

    protected void configureRoutes(App app, ApiConfig c) {
        for(Route route : c.getRoutes()) {
            Object controller = route.getAction().getController();
            if(null != controller) {

                ReflectClass rc = ReflectClass.of(controller.getClass());

                for(ReflectField rf : rc.getFields()) {
                    if(rf.getType().equals(ApiConfig.class)) {
                        rf.setValue(controller, c);
                        break;
                    }
                }

            }
        }
    }

	protected ApiMetadata createMetadata(ApiConfigurator c) {
		return metadataFactory.createMetadata(c.config());
	}
}