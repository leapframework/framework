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

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.core.meta.MTypeManager;
import leap.lang.Args;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.Types;
import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.http.QueryStringBuilder;
import leap.lang.net.Urls;
import leap.lang.reflect.ReflectClass;
import leap.lang.reflect.ReflectField;
import leap.web.App;
import leap.web.AppInitializable;
import leap.web.api.annotation.Resource;
import leap.web.api.annotation.ResourceWrapper;
import leap.web.api.config.*;
import leap.web.api.config.model.OAuthConfigImpl;
import leap.web.api.meta.ApiMetadata;
import leap.web.api.meta.ApiMetadataFactory;
import leap.web.api.meta.model.MApiPermission;
import leap.web.api.meta.model.MApiResponse;
import leap.web.api.mvc.ApiInitializable;
import leap.web.api.permission.ResourcePermissions;
import leap.web.api.route.ApiRoute;
import leap.web.route.Route;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultApis implements Apis, AppInitializable,PostCreateBean {
	
	protected @Inject ApiConfigProcessor[]   configProcessors;
	protected @Inject ApiMetadataFactory     metadataFactory;
	protected @Inject MTypeManager           typeManager;
    protected @Inject App                    app;

    protected Map<String, MApiResponse> commonResponses = new LinkedHashMap<>();
    protected Map<String, Api>          apis            = new ConcurrentHashMap<>();

    private ApiConfigs      configs;
    private OAuthConfigImpl oauthConfig = new OAuthConfigImpl();
    private Set<String>     created = new CopyOnWriteArraySet<>();

    @Override
    public Api get(String name) throws ObjectNotFoundException {
        Api api = tryGet(name);
        if(null == api) {
            throw new ObjectNotFoundException("Api '" + name + "' not exists!");
        }
        return api;
    }

    @Override
    public Api tryGet(String name) {
        return apis.get(name.toLowerCase());
    }

    @Override
    public boolean remove(Api api) {
        AtomicBoolean removed = new AtomicBoolean();

        apis.forEach((k,v) -> {
            if(v == api) {
                apis.remove(k);
                removed.set(true);
                return;
            }
        });

        return removed.get();
    }

    @Override
    public boolean remove(String name) {
        String key = name.toLowerCase();
        if(null == apis.remove(key)) {
            return false;
        }
        created.remove(key);
        return true;
    }

    @Override
    public ApiConfigurator add(String name, String basePath) throws ObjectExistsException {
		Args.notEmpty(name,     "name");
		Args.notEmpty(basePath, "basePath");
        
		DefaultApiConfig c = new DefaultApiConfig(name,basePath,DefaultApis.class);
        doAdd(c);
		return c;
    }

    @Override
    public void add(Api api) throws ObjectExistsException {
        checkDuplicate(api.getConfig());
        apis.put(api.getName().toLowerCase(), api);
    }

    @Override
    public Api newDynamic(String name, String basePath) {
        DefaultApiConfig c = new DefaultApiConfig(name,basePath,DefaultApis.class);
        return doNewApi(c, true);
    }

	@Override
    public boolean isDefaultOAuthEnabled() {
        return oauthConfig.isEnabled();
    }

    @Override
    public String getDefaultOAuthAuthorizationUrl() {
        return oauthConfig.getAuthorizationUrl();
    }

    @Override
    public String getDefaultOAuthTokenUrl() {
        return oauthConfig.getTokenUrl();
    }

    @Override
    public Apis setDefaultOAuthEnabled(boolean enabled) {
        oauthConfig.setEnabled(enabled);
        return this;
    }

    @Override
    public Apis setDefaultOAuthAuthorizationUrl(String url) {
        this.oauthConfig.setAuthorizationUrl(url);
        return this;
    }
    
    @Override
    public Apis setDefaultOAuthAuthorizationUrl(String endpoint, String clientId, String redirectUri) {
        Args.notEmpty(endpoint, "endpoint");
        
        QueryStringBuilder qs = new QueryStringBuilder();
        qs.add("client_id", clientId)
          .add("redirect_uri", redirectUri)
          .add("response_type", "token");
        
        this.oauthConfig.setAuthorizationUrl(Urls.appendQueryString(endpoint, qs.build()));
        return this;
    }

    @Override
    public Apis setDefaultOAuthTokenUrl(String url) {
        this.oauthConfig.setTokenUrl(url);
        return this;
    }

    @Override
    public void postCreate(BeanFactory factory) throws Throwable {
        configs = factory.getAppConfig().getExtension(ApiConfigs.class);
        if(configs == null){
            return;
        }

        //oauth
        this.oauthConfig.updateFrom(configs.getOAuthConfig());

        //common responses.
        configs.getCommonResponses().forEach((key, builder)->{
            builder.setTypeManager(typeManager);
            commonResponses.put(key,builder.build());
        });

        //check model class name
        configs.getCommonModels().forEach(m -> {
            if(!Strings.isEmpty(m.getClassName()) && null == Classes.tryForName(m.getClassName())) {
                throw new ApiConfigException("The model class '" + m.getClassName() + "' not found");
            }
        });

        //check param class name
        configs.getCommonParams().forEach(m -> {
            if(!Strings.isEmpty(m.getClassName()) && null == Classes.tryForName(m.getClassName())) {
                throw new ApiConfigException("The param class '" + m.getClassName() + "' not found");
            }
        });

        //auto register configured api(s).
        configs.getApis().values().forEach(this::doAdd);
    }

    protected void checkDuplicate(ApiConfig c) {
        String key = c.getName().toLowerCase();
        if(apis.containsKey(key)) {
            throw new ObjectExistsException("The api '" + c.getName() + "' already exists");
        }

        apis.values().forEach(api -> {
            if(Strings.equalsIgnoreCase(api.getBasePath(),c.getBasePath())){
                throw new ApiConfigException("Found duplicated api config with base path: " + c.getBasePath()
                        + " in " + api.getConfig().getSource() + " and " + c.getSource());
            }
        });
    }

    protected void doAdd(ApiConfigurator c) {
        checkDuplicate(c.config());

        Api api = doNewApi(c, false);

        apis.put(api.getName().toLowerCase(),api);
    }

    protected Api doNewApi(ApiConfigurator configurator, boolean dynamic) {
        configurator.setContainerRoutes(app.routes());

        if(null != configs) {
            ApiConfig c = configurator.config();

            if(c.getOAuthConfig() == null){
                configurator.setOAuthConfig(oauthConfig);
            }else{
                configurator.setOAuthConfig(new OAuthConfigImpl().updateFrom(oauthConfig));
            }

            configs.getCommonModels().forEach(model -> {

                if(null != c.getModelByClassName(model.getClassName())) {
                    return;
                }

                if(null != c.getModel(model.getName())) {
                    return;
                }

                configurator.addModel(model);
            });

            configs.getCommonParams().forEach(param -> {

                if(null != c.getParam(param.getClassName(),param.getName())) {
                    return;
                }

                configurator.addParam(param);
            });
        }

        return new DefaultApi(this::doCreate, configurator, dynamic);
    }

    @Override
    public void postAppInit(App app) throws Throwable {
        //auto create apis.
        apis.values().forEach(api -> {
            if(!api.isCreated()) {
                api.create();
            }
        });
	}

    protected void doCreate(Api api) {
        ApiConfigurator c = api.getConfigurator();

        doConfiguration(app, c);

        if(!api.isDynamic()) {
            resolveAppRoutes(app, c);
        }

        //configure by processors.
        for(ApiConfigProcessor p : configProcessors) {
            p.preProcess(api);
        }

        for(ApiConfigProcessor p : configProcessors) {
            p.postProcess(api);
        }

        for(ApiConfigProcessor p : configProcessors) {
            p.completeProcess(api);
        }

        //post config
        postConfigApi(app, c.config());

        //create metadata
        ApiMetadata metadata = createMetadata(api);
        api.setMetadata(metadata);
        api.markCreated();

        //inject api beans.
        injectApiBeans(app, api);
    }

    protected void doConfiguration(App app, ApiConfigurator c) {
        //todo : oauth

        //todo : cors

        commonResponses.forEach(c::putCommonResponse);
    }
	
    protected void resolveAppRoutes(App app, ApiConfigurator c) {
		String basePath	= c.config().getBasePath();
		for(Route route : app.routes()) {
			String pathTemplate = route.getPathTemplate().getTemplate();
			if(pathTemplate.equals(basePath) || pathTemplate.startsWith(basePath)) {
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

        MApiPermission[] permissions = rps.resolvePermissions(route, resourceType);
        if(null != permissions && permissions.length > 0) {
            String[] values = new String[permissions.length];
            for(int i=0;i<values.length;i++) {
                values[i] = permissions[i].getValue();
            }

            route.setPermissions(values);

            for(MApiPermission p : permissions) {
                c.tryAddPermission(p);
            }
        }
    }

    protected void postConfigApi(App app, ApiConfig c) {
        c.getApiRoutes().forEach(ar -> {
            Route route = ar.getRoute();
            if(c.isDefaultAnonymous() && null == route.getAllowAnonymous()) {
                route.setAllowAnonymous(true);
            }
        });
    }

    protected void injectApiBeans(App app, Api api) {
        Set<Object> controllers = new HashSet<>();

        for(ApiRoute ar : api.getConfig().getApiRoutes()) {
            Route route = ar.getRoute();

            //Inject ApiConfig & ApiMetadata.
            Object controller = route.getAction().getController();
            if(null != controller && !controllers.contains(controller)) {
                controllers.add(controller);

                ReflectClass rc = ReflectClass.of(controller.getClass());

                for(ReflectField rf : rc.getFields()) {
                    if(rf.getType().equals(ApiConfig.class)) {
                        rf.setValue(controller, api.getConfig());
                        break;
                    }

                    if(rf.getType().equals(ApiMetadata.class)) {
                        rf.setValue(controller, api.getMetadata());
                    }
                }

                if(controller instanceof ApiInitializable) {
                    ((ApiInitializable) controller).postApiInitialized(api);
                }

            }
        }

        controllers.clear();
    }

	protected ApiMetadata createMetadata(Api api) {
		return metadataFactory.createMetadata(api);
	}
}