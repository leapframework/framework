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
package leap.core;

import leap.core.ds.DataSourceConfig;
import leap.core.sys.SysPermissionDefinition;
import leap.lang.Args;
import leap.lang.Factory;
import leap.lang.accessor.MapPropertyAccessor;
import leap.lang.accessor.PropertyGetter;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.text.DefaultPlaceholderResolver;
import leap.lang.text.PlaceholderResolver;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

class DefaultAppConfigLoader {
	
	private static final Log log = LogFactory.get(DefaultAppConfigLoader.class);
	
    private static final List<AppConfigReader>    readers    = Factory.newInstances(AppConfigReader.class);

	protected String  basePackage;
	protected Boolean debug;
	protected Locale  defaultLocale;
	protected Charset defaultCharset;
	protected Object  externalContext;

    protected final Set<String> additionalPackages = new LinkedHashSet<>();
    protected final Map<String, String> externalProperties;
    protected final Map<String, String>           properties  = new LinkedHashMap<>();
    protected final Map<String, List<String>>     arrayProperties = new LinkedHashMap<>();
    protected final Map<Class<?>, Object>         extensions  = new HashMap<>();
    protected final Set<Resource>                 resources   = new HashSet<>();
    protected final List<SysPermissionDefinition> permissions = new ArrayList<>();
    protected final Map<String, DataSourceConfig> dataSourceConfigs;

	protected final Map<Class<?>, Map<String, SysPermissionDefinition>> typedPermissions = new HashMap<>();

	protected final DefaultPlaceholderResolver placeholderResolver;
	protected final AppPropertyProcessor       propertyProcessor;
	
	protected DefaultAppConfigLoader(){
		this.externalContext     = null;
		this.externalProperties  = null;
		this.placeholderResolver = null;
		this.propertyProcessor   = null;
		this.dataSourceConfigs   = new HashMap<>();
	}

	protected DefaultAppConfigLoader(Object externalContext,
									 Map<String, String> externalProperties,
									 Map<String, DataSourceConfig> dataSourceConfigs,
									 AppPropertyProcessor propertyProcessor){

		this.externalContext     = externalContext;
		this.externalProperties  = externalProperties;
		this.placeholderResolver = new DefaultPlaceholderResolver(new PropertyGetter() {
			@Override
			public String getProperty(String name) {
				if(properties.containsKey(name)) {
					return properties.get(name);
				}
				return null != externalProperties ? externalProperties.get(name) : null;
			}
		});

		this.placeholderResolver.setEmptyUnresolvablePlaceholders(false);
		this.placeholderResolver.setIgnoreUnresolvablePlaceholders(true);
		this.dataSourceConfigs = dataSourceConfigs;
		this.propertyProcessor = propertyProcessor;
	}
	
	protected DefaultAppConfigLoader load(Resource... resources){
		loadConfigs(new LoadContext(null, false), resources);
		return this;
	}
	
	protected DefaultAppConfigLoader load(String profile, Resource... resources){
		loadConfigs(new LoadContext(profile,false), resources);
		return this;
	}
	
	protected Map<String, String> getProperties() {
		return properties;
	}

    protected Map<String, List<String>> getArrayProperties() {
        return arrayProperties;
    }
	
	protected Set<Resource> getResources(){
		return resources;
	}
	
	protected List<SysPermissionDefinition> getPermissions(){
		return permissions;
	}
	
	protected void loadBaseProperties(String profile, Resource resource) {
        readConfigs(new LoadContext(profile, false), true, resource);
	}

	private void loadConfigs(LoadContext context,Resource... resources){
        readConfigs(context, false, resources);
	}

    private void readConfigs(LoadContext context, boolean base, Resource... resources){
        for(Resource resource : resources){
            try{
                String resourceUrl = resource.getURL().toString();

                if(log.isDebugEnabled()){
                    if(AppResources.isFrameworkResource(resourceUrl)) {
                        log.trace("Load config from resource : {}",resourceUrl);
                    }else{
                        log.debug("Load config from resource : {}",resourceUrl);
                    }
                }

                if(context.resources.contains(resourceUrl)){
                    throw new AppConfigException("cycle importing detected, please check your config : " + resourceUrl);
                }

                context.resources.add(resourceUrl);

                for(AppConfigReader reader : readers) {
                    if(base) {
                        if(reader.readBase(context, resource)) {
                            break;
                        }
                    }else{
                        if(reader.readFully(context, resource)) {
                            break;
                        }
                    }
                }

            }catch(IOException e) {
                throw new AppConfigException("I/O Exception",e);
            }
        }
    }
	
	protected void addPermissions(List<SysPermissionDefinition> permissions,boolean override){
		for(SysPermissionDefinition permission : permissions){
			addPermission(permission, override);
		}
	}

	protected void addPermission(SysPermissionDefinition permission,boolean override){
		Map<String,SysPermissionDefinition> typesPermissionsMap = typedPermissions.get(permission.getPermType());

		SysPermissionDefinition exists = null;

		if(null == typesPermissionsMap){
			typesPermissionsMap = new HashMap<>();
		}else{
			exists = typesPermissionsMap.get(permission.getPermObject().getName());
		}

		if(!override && null != exists){
			throw new AppConfigException("Found duplicated permission '" + permission.toString() + "', source : " + permission.getSource() + "," + exists.getSource());
		}

		if(null != exists){
			permissions.remove(exists);
		}

		typesPermissionsMap.put(permission.getPermObject().getName(), permission);
		permissions.add(permission);
	}

	private final class LoadContext extends MapPropertyAccessor implements AppConfigReaderContext {
        protected DefaultAppConfigLoader parent    = DefaultAppConfigLoader.this;
		protected String      profile              = null;
		protected boolean     defaultOverrided     = false;
		protected boolean     hasDefaultDataSource = false;
		protected Set<String> resources            = new HashSet<>();
		
		LoadContext(String profile,boolean defaultOverried){
			super(DefaultAppConfigLoader.this.properties);
			this.profile = profile;
			this.defaultOverrided = defaultOverried;
		}

		@Override
        public String getProfile() {
	        return profile;
        }

		@Override
        public boolean isDefaultOverrided() {
	        return defaultOverrided;
        }

        public Object getExternalContext() {
	        return externalContext;
        }

        @Override
        public Boolean getDebug() {
            return debug;
        }

        @Override
        public void setDebug(boolean debug) {
            parent.debug = debug;
        }

        @Override
        public String getBasePackage() {
            return basePackage;
        }

        @Override
        public void setBasePackage(String bp) {
            parent.basePackage = bp;
        }

        @Override
        public Locale getDefaultLocale() {
            return defaultLocale;
        }

        @Override
        public void setDefaultLocale(Locale locale) {
            parent.defaultLocale = locale;
        }

        @Override
        public Charset getDefaultCharset() {
            return defaultCharset;
        }

        @Override
        public void setDefaultCharset(Charset charset) {
            parent.defaultCharset = charset;
        }

        @Override
        public Set<String> getAdditionalPackages() {
            return parent.additionalPackages;
        }

        @Override
        public Map<String, String> getProperties() {
            return parent.properties;
        }

        @Override
        public Map<String, List<String>> getArrayProperties() {
            return parent.arrayProperties;
        }

        @Override
        public boolean hasArrayProperty(String name) {
            return parent.arrayProperties.containsKey(name);
        }

        @Override
        public List<SysPermissionDefinition> getPermissions() {
            return parent.permissions;
        }

        @Override
        public void addPermission(SysPermissionDefinition p, boolean override) {
            parent.addPermission(p, override);
        }

        @Override
        public void readImportedResource(Resource resource, boolean override) {
            loadConfigs(new LoadContext(profile, override), resource);
        }

        @Override
        public PlaceholderResolver getPlaceholderResolver() {
            return parent.placeholderResolver;
        }

        @Override
        public AppPropertyProcessor getPropertyProcessor() {
            return parent.propertyProcessor;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getExtension(Class<T> type) {
	        return (T)extensions.get(type);
        }

		@Override
        public <T> void setExtension(T extension) {
			Args.notNull(extension);
			extensions.put(extension.getClass(), extension);
        }

		@Override
        public <T> void setExtension(Class<T> type, T extension) {
	        Args.notNull(type);
	        Args.notNull(extension);
	        extensions.put(type, extension);
        }
		
		@Override
        public void addResource(Resource r) {
			if(null != r) {
				DefaultAppConfigLoader.this.resources.add(r);
			}
        }

		@Override
        public void addResources(ResourceSet rs) {
			if(null != rs) {
				for(Resource r : rs.toResourceArray()) {
					DefaultAppConfigLoader.this.resources.add(r);	
				}
			}
		}
		
		@Override
        public boolean hasDefaultDataSourceConfig() {
            return hasDefaultDataSource;
        }

        @Override
        public boolean hasDataSourceConfig(String name) {
	        return dataSourceConfigs.containsKey(name);
        }

		@Override
        public void setDataSourceConfig(String name, DataSourceConfig conf) {
			dataSourceConfigs.put(name, conf);
			
			if(conf.isDefault()) {
		         
	            if (hasDefaultDataSource) {
	                throw new AppConfigException("Default DataSource already exists");
	            }
	            
			    this.hasDefaultDataSource = true;
			}
        }

		@Override
        public Map<String, DataSourceConfig> getDataSourceConfigs() {
	        return dataSourceConfigs;
        }
	}
}