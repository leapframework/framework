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
package leap.web.api.config;

import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.Collections2;
import leap.lang.accessor.MapAttributeAccessor;
import leap.lang.naming.NamingStyle;
import leap.lang.path.Paths;
import leap.web.api.meta.model.MApiResponse;
import leap.web.api.meta.model.MApiResponseBuilder;
import leap.web.api.meta.model.MApiPermission;
import leap.web.api.permission.ResourcePermissionsSet;
import leap.web.api.spec.swagger.SwaggerConstants;
import leap.web.route.Route;

import java.util.*;

public class DefaultApiConfig extends MapAttributeAccessor implements ApiConfig, ApiConfigurator {
	
	protected final String name;
	protected final String basePath;

    protected String         basePackage;
    protected String         title;
    protected String         summary;
    protected String         description;
    protected String         version                     = DEFAULT_VERSION;
    protected String[]       protocols;
    protected String[]       produces;
    protected String[]       consumes;
    protected boolean        corsEnabled                 = true;
    protected OauthConfig    oauthConfig;
    protected NamingStyle    parameterNamingStyle;
    protected NamingStyle    propertyNamingStyle;
    protected int            maxPageSize                 = MAX_PAGE_SIZE;
    protected int            defaultPageSize             = DEFAULT_PAGE_SIZE;
    protected Set<String> 	 removalModelNamePrefixes    = new HashSet<String>();
    protected Set<String> 	 removalModelNamePrefixesImv = Collections.unmodifiableSet(removalModelNamePrefixes);

    protected Set<Route>  	 routes                      = new HashSet<>();
    protected Set<Route>  	 routesImv                   = Collections.unmodifiableSet(routes);

    protected Map<String, MApiPermission> permissions    = new LinkedHashMap<>();
    protected Map<String, MApiPermission> permissionsImv = Collections.unmodifiableMap(permissions);

    protected Map<String, MApiResponse> commonResponses    = new LinkedHashMap<>();
    protected Map<String, MApiResponse> commonResponsesImv = Collections.unmodifiableMap(commonResponses);

    protected Map<String, MApiResponseBuilder> commonResponseBuilders    = new LinkedHashMap<>();
    protected Map<String, MApiResponseBuilder> commonResponseBuildersImv = Collections.unmodifiableMap(commonResponseBuilders);

    protected Map<Route, Class<?>> resourceTypes    = new HashMap<>();
    protected Map<Route, Class<?>> resourceTypesImv = Collections.unmodifiableMap(resourceTypes);

    protected ResourcePermissionsSet resourcePermissionsSet = new ResourcePermissionsSet();
	
	public DefaultApiConfig(String name, String basePath) {
		Args.notEmpty(name, "name");
		Args.notEmpty(basePath, "basePath");
		Args.assertTrue(basePath.startsWith("/"), "The base path must be leading with a slash '/'");
		this.name     = name;
		this.title    = name;
		this.basePath = Paths.suffixWithoutSlash(basePath);
	}
	
	@Override
	public ApiConfig config() {
		return this;
	}

	@Override
	public String getBasePath() {
		return basePath;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
    public String getSummary() {
	    return summary;
    }

	@Override
    public String getDescription() {
	    return description;
    }

	@Override
    public String getTitle() {
	    return title;
    }
	
	@Override
    public String getVersion() {
	    return version;
    }

	public String[] getProtocols() {
		return protocols;
	}

	@Override
    public String[] getProduces() {
	    return produces;
    }

	@Override
    public String[] getConsumes() {
	    return consumes;
    }
	
	public boolean isCorsDisabled() {
		return !corsEnabled;
	}

	@Override
    public Set<Route> getRoutes() {
	    return routesImv;
    }

    @Override
    public Map<String, MApiResponse> getCommonResponses() {
        return commonResponsesImv;
    }

    public NamingStyle getParameterNamingStyle() {
		return parameterNamingStyle;
	}
	
	public NamingStyle getPropertyNamingStyle() {
		return propertyNamingStyle;
	}
	
	public Set<String> getRemovalModelNamePrefixes() {
		return removalModelNamePrefixesImv;
	}
	
	public ApiConfigurator setTitle(String title) {
		Args.notEmpty(title, "title");
		this.title = title;
		return this;
	}
	
	@Override
    public ApiConfigurator setSummary(String s) {
		this.summary = s;
	    return this;
    }

	@Override
    public ApiConfigurator setDescription(String s) {
		this.description = s;
	    return this;
    }

	public ApiConfigurator setVersion(String v) {
		Args.notEmpty(v, "version");
		this.version = v;
		return this;
	}
	
	public ApiConfigurator setProtocols(String... protocols) {
		this.protocols = null == protocols ? Arrays2.EMPTY_STRING_ARRAY : protocols;
		return this;
	}

    @Override
    public ApiConfigurator putCommonResponseBuilder(String name, MApiResponseBuilder response) {
        commonResponseBuilders.put(name,response);
        return this;
    }

    @Override
    public ApiConfigurator putCommonResponse(String name, MApiResponse response) {
        commonResponses.put(name, response);
        return this;
    }

    @Override
    public ApiConfigurator setProduces(String... produces) {
		this.produces = null == produces ? Arrays2.EMPTY_STRING_ARRAY : produces;
	    return this;
    }

	@Override
    public ApiConfigurator setConsumes(String... consumes) {
		this.consumes = null == consumes ? Arrays2.EMPTY_STRING_ARRAY : consumes;
		return this;
    }
	
	public ApiConfigurator setParameterNamingStyle(NamingStyle ns) {
		this.parameterNamingStyle = ns;
		return this;
	}
	
	public ApiConfigurator setPropertyNamingStyle(NamingStyle namingStyle) {
		this.propertyNamingStyle = namingStyle;
		return this;
	}

	@Override
    public ApiConfigurator removeModelNamePrefixes(String... prefixes) {
		Collections2.addAll(removalModelNamePrefixes, prefixes);
	    return this;
    }
	
	@Override
    public ApiConfigurator setCorsEnabled(boolean enabled) {
		this.corsEnabled = enabled;
	    return this;
    }

    @Override
    public ApiConfigurator setOAuthConfig(OauthConfig oauth) {
        this.oauthConfig = oauth;
        return this;
    }

    @Override
    public ApiConfigurator enableOAuth() {
        if(oauthConfig == null){
            oauthConfig = new OauthConfig(true,null,null);
        }else{
            oauthConfig.setOauthEnabled(true);
        }
        return this;
    }

    @Override
    public OauthConfig getOauthConfig() {
        return oauthConfig;
    }

    @Override
    public Map<String, MApiPermission> getPermissions() {
        return permissionsImv;
    }

    @Override
    public ApiConfigurator setPermission(MApiPermission p) {
        permissions.put(p.getValue(), p);
        return this;
    }

    @Override
    public ApiConfigurator tryAddPermission(MApiPermission p) {
        if(!permissions.containsKey(p.getValue())) {
            setPermission(p);
        }
        return this;
    }

    @Override
    public int getMaxPageSize() {
        return maxPageSize;
    }

    @Override
    public int getDefaultPageSize() {
        return defaultPageSize;
    }

    @Override
    public ApiConfigurator setMaxPageSize(int size) {
        this.maxPageSize = size;
        return this;
    }

    @Override
    public ApiConfigurator setDefaultPageSize(int size) {
        this.defaultPageSize = size;
        return this;
    }

    @Override
    public ApiConfigurator addRoute(Route route) {
		routes.add(route);

        if(null != route.getPermissions()) {
            for(String p : route.getPermissions()) {
                if(!permissions.containsKey(p)) {
                    permissions.put(p, new MApiPermission(p, ""));
                }
            }
        }

	    return this;
    }

    @Override
    public Map<Route, Class<?>> getResourceTypes() {
        return resourceTypesImv;
    }

    @Override
    public ApiConfigurator setResourceType(Route route, Class<?> resourceType) {
        resourceTypes.put(route, resourceType);
        return this;
    }

    @Override
    public ResourcePermissionsSet getResourcePermissionsSet() {
        return resourcePermissionsSet;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[api=" + name + "]";
    }

    @Override
    public String getBasePackage() {
        return basePackage;
    }

    @Override
    public ApiConfigurator setBasePackage(String basePackage) {
        this.basePackage = basePackage;
        return this;
    }
}