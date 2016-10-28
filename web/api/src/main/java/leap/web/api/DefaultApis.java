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

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.lang.Args;
import leap.lang.Types;
import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.http.QueryStringBuilder;
import leap.lang.net.Urls;
import leap.lang.path.Paths;
import leap.lang.reflect.ReflectClass;
import leap.lang.reflect.ReflectField;
import leap.web.App;
import leap.web.AppInitializable;
import leap.web.api.annotation.Resource;
import leap.web.api.annotation.ResourceWrapper;
import leap.web.api.config.*;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.ApiMetadataFactory;
import leap.web.api.meta.model.MApiResponse;
import leap.web.api.meta.model.MPermission;
import leap.web.api.mvc.ApiInitializable;
import leap.web.api.permission.ResourcePermissions;
import leap.web.api.permission.ResourcePermissionsSet;
import leap.web.config.WebConfigurator;
import leap.web.route.Route;

public class DefaultApis implements Apis, AppInitializable,PostCreateBean {
	
	protected @Inject ApiConfiguratorFactory configuratorFactory;
    protected @Inject ApiConfigSource        configSource;
	protected @Inject ApiConfigProcessor[]   configProcessors;
	protected @Inject ApiMetadataFactory     metadataFactory;
	
	protected Map<String, ApiConfigurator> configurators  = new ConcurrentHashMap<String, ApiConfigurator>();
	protected Map<String, ApiConfig>       configurations = new ConcurrentHashMap<String, ApiConfig>();
	protected Map<String, ApiMetadata>     metadatas      = new ConcurrentHashMap<String, ApiMetadata>();
	
	protected boolean defaultOAuthEnabled;
	protected String  defaultOAuthAuthorizationUrl;
	protected String  defaultOAuthTokenUrl;
    protected Map<String, MApiResponse> commonResponses = new LinkedHashMap<>();

    @Override
    public ApiConfigurator tryGetConfigurator(String name) {
        return configurators.get(name.toLowerCase());
    }

    @Override
    public Set<ApiConfigurator> getConfigurators() {
	    return new LinkedHashSet<>(configurators.values());
    }
	
	@Override
    public Set<ApiConfig> getConfigurations() {
	    return new LinkedHashSet<>(configurations.values());
    }

    @Override
    public ApiMetadata tryGetMetadata(String name) {
        return metadatas.get(name.toLowerCase());
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
    public ApiConfigurator getConfigurator(String name) throws ObjectNotFoundException {
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
    public Map<String, MApiResponse> getCommonResponses() {
        return commonResponses;
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
    public void postCreate(BeanFactory factory) throws Throwable {
        configSource.loadConfiguration(factory.getAppConfig(), factory.getBean(WebConfigurator.class), this);
    }

    @Override
    public void postAppInit(App app) throws Throwable {


		for(Entry<String, ApiConfigurator> entry : configurators.entrySet()) {
			String key = entry.getKey();
			ApiConfigurator c = entry.getValue();
			
			//do configuration
			doConfiguration(app, c);
			
			//create metadata
			ApiMetadata m = createMetadata(c);
			metadatas.put(key, m);

            postLoadApi(app, c.config(), m);
		}
	}
	
	protected void doConfiguration(App app, ApiConfigurator c) {
        //common
        doCommonConfiguration(app, c);

		//resolve routes of api.
		resolveRoutes(app, c);

		//configure by processors.
		for(ApiConfigProcessor p : configProcessors) {
			p.preProcess(c);
		}
		
		for(ApiConfigProcessor p : configProcessors) {
			p.postProcess(c.config());
		}

	}

    protected void doCommonConfiguration(App app, ApiConfigurator c) {
        //todo : oauth

        //todo : cors

        commonResponses.forEach(c::putCommonResponse);
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

                resolveResourceType(app, c, route);

				c.addRoute(route);
			}
		}
	}

    protected void resolveResourceType(App app, ApiConfigurator c , Route route) {
        Class<?> resourceType = null;

        Resource resource = route.getAction().searchAnnotation(Resource.class);
        if(null != resource) {
            resourceType = resource.value();
        }

        if(null != route.getAction().getController()) {
            ResourceWrapper rw = route.getAction().getControllerAnnotation(ResourceWrapper.class);
            if(null != rw) {
                Class<?> cls = route.getAction().getController().getClass();

                if(cls.getTypeParameters().length == 1) {
                    resourceType = cls.getTypeParameters()[0].getGenericDeclaration();
                }else {
                    Class<?>[] types = Types.getActualTypeArguments(cls.getGenericSuperclass());
                    if(types.length == 1) {
                        resourceType = types[0];
                    }
                }
            }
        }

        if(null != resourceType) {
            c.setResourceType(route, resourceType);

            resolveResourcePermissions(app, c, route, resourceType);
        }
    }

    protected void resolveResourcePermissions(App app, ApiConfigurator c, Route route, Class<?> resourceType) {
        if(null != route.getPermissions()) {
            return;
        }

        ResourcePermissions rps =
                c.config().getResourcePermissionsSet().tryGetResourcePermissions(resourceType);

        if(null == rps) {
            return;
        }

        MPermission[] permissions = rps.resolvePermissions(route, resourceType);
        if(null != permissions && permissions.length > 0) {
            String[] values = new String[permissions.length];
            for(int i=0;i<values.length;i++) {
                values[i] = permissions[i].getValue();
            }

            route.setPermissions(values);

            for(MPermission p : permissions) {
                c.tryAddPermission(p);
            }
        }
    }

    protected void postLoadApi(App app, ApiConfig c, ApiMetadata m) {

        Set<Object> controllers = new HashSet<>();

        for(Route route : c.getRoutes()) {
            Object controller = route.getAction().getController();
            if(null != controller && !controllers.contains(controller)) {
                controllers.add(controller);

                ReflectClass rc = ReflectClass.of(controller.getClass());

                for(ReflectField rf : rc.getFields()) {
                    if(rf.getType().equals(ApiConfig.class)) {
                        rf.setValue(controller, c);
                        break;
                    }

                    if(rf.getType().equals(ApiMetadata.class)) {
                        rf.setValue(controller, m);
                    }
                }

                if(controller instanceof ApiInitializable) {
                    ((ApiInitializable) controller).postApiInitialized(c, m);
                }

            }
        }
    }

	protected ApiMetadata createMetadata(ApiConfigurator c) {
		return metadataFactory.createMetadata(c.config());
	}
}