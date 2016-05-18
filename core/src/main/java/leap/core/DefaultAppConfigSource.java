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
import leap.core.ds.DataSourceManager;
import leap.core.instrument.AppInstrumentation;
import leap.core.sys.SysPermissionDef;
import leap.lang.*;
import leap.lang.accessor.MapPropertyAccessor;
import leap.lang.accessor.PropertyGetter;
import leap.lang.accessor.SystemPropertyAccessor;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.convert.Converts;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.reflect.Reflection;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;
import leap.lang.resource.SimpleResourceSet;
import leap.lang.text.DefaultPlaceholderResolver;
import leap.lang.text.PlaceholderResolver;
import leap.lang.tools.DEV;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static leap.core.AppConfig.*;
import static leap.core.AppResources.CP_APP_PREFIX;

public class DefaultAppConfigSource implements AppConfigSource {

	private static final Log log = LogFactory.get(DefaultAppConfigSource.class);

    protected static final String APP_PROFILE_CONFIG_RESOURCE = CP_APP_PREFIX + "/profile";

    protected static final String[] BASE_CONFIG_LOCATIONS = new String[]{
            CP_APP_PREFIX + "/config.properties",
            CP_APP_PREFIX + "/config.xml"
    };

    protected static final String[] APP_CONFIG_LOCATIONS  = new String[]{
            CP_APP_PREFIX + "/config.properties",
            CP_APP_PREFIX + "/config.xml",
            CP_APP_PREFIX + "/config/**/*",
            CP_APP_PREFIX + "/profiles/{profile}/config.properties",
            CP_APP_PREFIX + "/profiles/{profile}/config.xml",
            CP_APP_PREFIX + "/profiles/{profile}/config/**/*"
    };

    //all init properties
    protected static Set<String> INIT_PROPERTIES = new HashSet<>();
    static {
        INIT_PROPERTIES.add(AppConfig.INIT_PROPERTY_PROFILE);
        INIT_PROPERTIES.add(AppConfig.INIT_PROPERTY_BASE_PACKAGE);
        INIT_PROPERTIES.add(AppConfig.INIT_PROPERTY_DEBUG);
        INIT_PROPERTIES.add(AppConfig.INIT_PROPERTY_DEFAULT_CHARSET);
        INIT_PROPERTIES.add(AppConfig.INIT_PROPERTY_DEFAULT_LOCALE);
    }

    private static final List<AppConfigReader> readers = Factory.newInstances(AppConfigReader.class);

    protected AppPropertyProcessor propertyProcessor = new PropertyProcessorWrapper();
    protected AppInstrumentation   instrumentation   = Factory.getInstance(AppInstrumentation.class);

    @Override
    public AppConfig loadConfiguration(Object externalContext, Map<String, String> initProperties) {
        if(null == initProperties) {
            initProperties = new LinkedHashMap<>();
        }

        //load init properties from system environment.
        loadInitPropertiesFromSystem(externalContext, initProperties);

        //init profile
        String profile = initProfile(externalContext, initProperties);

        //Create loader for the configuration.
        Loader loader = createLoader(externalContext, initProperties, profile);

        //Load config
        DefaultAppConfig config = loader.load();

        //post loading.
        postLoad(config);

        //instrument
        instrumentClasses(config);

        return config;
    }

    protected void postLoad(DefaultAppConfig config) {
        //load datasource form properties
        new DataSourceConfigPropertiesLoader(config.properties, config.dataSourceConfigs).load();

        config.postLoad();
    }

    protected void instrumentClasses(DefaultAppConfig config) {
        instrumentation.init(config);
        instrumentation.instrument(config.resources);
    }

    protected void loadInitPropertiesFromSystem(Object externalContext, Map<String, String> initProperties) {
        if(!initProperties.isEmpty()) {
            Maps.resolveValues(initProperties, new DefaultPlaceholderResolver(SystemPropertyAccessor.INSTANCE));
        }

        Properties props = System.getProperties();
        for(Object key : props.keySet()) {
            String name = key.toString();
            initProperties.put(name, System.getProperty(name));
        }

        for(String p : INIT_PROPERTIES){
            if(!initProperties.containsKey(p)){
                String v = System.getProperty(p);
                if(!Strings.isEmpty(v)){
                    initProperties.put(p, v);
                }
            }
        }
    }

    protected String initProfile(Object externalContext, Map<String, String> initProperties){
        String profile = null;

        //read from config file
        Resource r = Resources.getResource(APP_PROFILE_CONFIG_RESOURCE);
        if(null != r && r.exists()){
            profile = Strings.trim(r.getContent());
        }

        //read from init properties
        if(Strings.isEmpty(profile)){
            profile = initProperties.get(AppConfig.INIT_PROPERTY_PROFILE);
        }

        //auto detect profile name
        if(Strings.isEmpty(profile)){
            profile = autoDetectProfileName(externalContext);
        }

        return profile;
    }

    protected String autoDetectProfileName(Object externalContext){
        //Auto detect development environment (maven environment)
        if(DEV.isDevProject(externalContext)){
            return AppProfile.DEVELOPMENT.getName();
        }else{
            return AppConfig.DEFAULT_PROFILE;
        }
    }

    protected Loader createLoader(Object externalContext, Map<String,String> initProperties, String profile) {
        return new Loader(externalContext, initProperties, profile);
    }

    protected void loadBaseConfig(ConfigContext context, Resource... resources) {
        loadConfig(context, true, resources);
    }

    protected void loadFullConfig(ConfigContext context, Resource... resources) {
        loadConfig(context, false, resources);
    }

    private void loadConfig(ConfigContext context, boolean base, Resource... resources){
        for(Resource resource : resources){
            try{
                String resourceUrl = resource.getURL().toString();

                if(log.isDebugEnabled()){
                    if(AppResources.isFrameworkResource(resourceUrl)) {
                        log.trace("Loading {} config from : {}", base ? "base" : "full", resourceUrl);
                    }else{
                        log.debug("Loading {} config from : {}", base ? "base" : "full", resourceUrl);
                    }
                }

                if(context.resources.contains(resourceUrl)){
                    throw new AppConfigException("Cycle importing detected of '" + resourceUrl + "', please check your config : " + resourceUrl);
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

    protected static class PropertyProcessorWrapper implements AppPropertyProcessor {

        private final AppPropertyProcessor[] processors =
                Factory.newInstances(AppPropertyProcessor.class).toArray(new AppPropertyProcessor[]{});

        @Override
        public boolean process(String name, String value, Out<String> newValue) {
            if(processors.length > 0) {
                for(int i=0;i<processors.length;i++) {
                    AppPropertyProcessor p = processors[i];
                    if(p.process(name, value, newValue)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    protected static class DataSourceConfigPropertiesLoader {
        protected static final String DB_DEFAULT_PREFIX = "db.";
        protected static final String DB_NAMED_PREFIX   = "db_";

        protected final Map<String, String>           properties;
        protected final Map<String, DataSourceConfig> dataSourceConfigs;

        public DataSourceConfigPropertiesLoader(Map<String, String> properties, Map<String, DataSourceConfig> dataSourceConfigs) {
            this.properties = properties;
            this.dataSourceConfigs = dataSourceConfigs;
        }

        protected void load() {
            Map<String, DataSourceConfig.Builder> dsMap = new HashMap<>();

            for(Map.Entry<String, String> entry : properties.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue();

                if(key.startsWith(DB_DEFAULT_PREFIX)) {
                    if(dataSourceConfigs.containsKey(DataSourceManager.DEFAULT_DATASOURCE_NAME)) {
                        throw new AppConfigException("DataSource '" + DataSourceManager.DEFAULT_DATASOURCE_NAME + "' already configured, check property '" + key + "'");
                    }

                    DataSourceConfig.Builder conf = dsMap.get(DataSourceManager.DEFAULT_DATASOURCE_NAME);
                    if(null == conf) {
                        conf = new DataSourceConfig.Builder();
                        dsMap.put(DataSourceManager.DEFAULT_DATASOURCE_NAME, conf);
                    }

                    conf.setProperty(key.substring(DB_DEFAULT_PREFIX.length()), val);
                    continue;
                }

                if(key.startsWith(DB_NAMED_PREFIX)) {
                    int dotIndex = key.indexOf(DB_NAMED_PREFIX.length(), '.');
                    if(dotIndex > 0) {

                        String dataSourceName = key.substring(DB_NAMED_PREFIX.length(), dotIndex);
                        String dataSourceProp = key.substring(dotIndex + 1);

                        if(dataSourceConfigs.containsKey(dataSourceName)) {
                            throw new AppConfigException("DataSource '" + dataSourceName + "' already configured, check property '" + key + "'");
                        }

                        DataSourceConfig.Builder conf = dsMap.get(dataSourceName);
                        if(null == conf) {
                            conf = new DataSourceConfig.Builder();
                            dsMap.put(dataSourceName, conf);
                        }

                        conf.setProperty(dataSourceProp, val);
                        continue;
                    }
                }
            }

            if(!dsMap.isEmpty()) {
                for(Map.Entry<String, DataSourceConfig.Builder> entry : dsMap.entrySet()) {
                    DataSourceConfig c = entry.getValue().build();

                    if(c.isValid()) {
                        dataSourceConfigs.put(entry.getKey(), c);
                    }
                }
            }
        }
    }

    protected class Loader {

        final DefaultAppConfigSource parent = DefaultAppConfigSource.this;

        final Object                     externalContext;
        final Map<String, String>        initProperties;
        final DefaultAppConfig           config;
        final DefaultPlaceholderResolver placeholderResolver;

        protected final Set<String>                                  additionalPackages = new LinkedHashSet<>();
        protected final Map<String, String>                          properties         = new LinkedHashMap<>();
        protected final Map<String, List<String>>                    arrayProperties    = new LinkedHashMap<>();
        protected final Set<Resource>                                resources          = new HashSet<>();
        protected final List<SysPermissionDef>                       permissions        = new ArrayList<>();
        protected final Map<Class<?>, Map<String, SysPermissionDef>> typedPermissions   = new HashMap<>();
        protected final List<AppConfigLoaderDef>                      externalLoaders    = new ArrayList<>();

        Loader(Object externalContext, Map<String,String> initProperties, String profile) {
            this.externalContext = externalContext;
            this.initProperties  = initProperties;
            this.config          = new DefaultAppConfig(profile);

            this.placeholderResolver = new DefaultPlaceholderResolver(new PropertyGetter() {
                @Override
                public String getProperty(String name) {
                    if(properties.containsKey(name)) {
                        return properties.get(name);
                    }
                    return config.properties.get(name);
                }
            });

            this.placeholderResolver.setEmptyUnresolvablePlaceholders(false);
            this.placeholderResolver.setIgnoreUnresolvablePlaceholders(true);
        }

        protected DefaultAppConfig load() {
            init();

            loadBase(new ConfigContext(this, false));

            loadDefault(new ConfigContext(this, false));

            loadExternal(new ConfigContext(this, false));

            complete();

            return config;
        }

        protected void init() {
            Maps.accept(initProperties, AppConfig.INIT_PROPERTY_BASE_PACKAGE,    String.class,  (p) -> config.basePackage = p);
            Maps.accept(initProperties, AppConfig.INIT_PROPERTY_DEBUG,           Boolean.class, (d) -> config.debug = d);
            Maps.accept(initProperties, AppConfig.INIT_PROPERTY_DEFAULT_CHARSET, Charset.class, (c) -> config.defaultCharset = c);
            Maps.accept(initProperties, AppConfig.INIT_PROPERTY_DEFAULT_LOCALE,  Locale.class,  (l) -> config.defaultLocale = l);
            config.loadProperties(initProperties);
        }

        protected void loadBase(ConfigContext context){
            Resource[] appBaseConfigFiles = AppResources.get().searchClasspaths(BASE_CONFIG_LOCATIONS);
            if(appBaseConfigFiles.length > 0) {
                parent.loadBaseConfig(context, appBaseConfigFiles);
            }

            //base package
            if(Strings.isEmpty(config.basePackage)){
                config.basePackage = DEFAULT_BASE_PACKAGE;
            }

            //debug
            if(null == config.debug){
                config.debug = AppProfile.DEVELOPMENT.matches(config.getProfile()) ? true : false;
            }

            //default locale
            if(null == config.defaultLocale){
                config.defaultLocale = DEFAULT_LOCALE;
            }

            //default charset
            if(null == config.defaultCharset){
                config.defaultCharset = DEFAULT_CHARSET;
            }

            log.info("{}:{}, {}:{}, {}:{}, {}:{}",
                    INIT_PROPERTY_PROFILE,config.profile,
                    INIT_PROPERTY_BASE_PACKAGE,config.basePackage,
                    INIT_PROPERTY_DEFAULT_LOCALE,config.defaultLocale.toString(),
                    INIT_PROPERTY_DEFAULT_CHARSET,config.defaultCharset.name());

            config.properties.put(INIT_PROPERTY_PROFILE,config.profile);
            config.properties.put(INIT_PROPERTY_DEBUG,String.valueOf(config.debug));
            config.properties.put(INIT_PROPERTY_BASE_PACKAGE,config.basePackage);
            config.properties.put(INIT_PROPERTY_DEFAULT_LOCALE,config.defaultLocale.toString());
            config.properties.put(INIT_PROPERTY_DEFAULT_CHARSET,config.defaultCharset.name());
        }

        protected void loadDefault(ConfigContext context) {
            Resource[] fmmResources = AppResources.getFMMClasspathResourcesForXml("config");
            Resource[] appResources = AppResources.getLocClasspathResources(config.getProfiled(APP_CONFIG_LOCATIONS));
            parent.loadFullConfig(context, Arrays2.concat(fmmResources, appResources));
        }

        protected void loadExternal(ConfigContext context) {

            for(AppConfigLoaderDef def : externalLoaders) {
                Class<?> cls = Classes.tryForName(def.getClassName());
                if(null == cls) {
                    throw new AppConfigException("The config loader class '" + def.getClassName() + "' not found, check your config");
                }

                if(!AppConfigLoader.class.isAssignableFrom(cls)) {
                    throw new AppConfigException("The config loader class '" + def.getClassName() + "' must implements interface '" + AppConfigLoader.class.getName() + "'");
                }

                AppConfigLoader obj = (AppConfigLoader)Reflection.newInstance(cls);

                BeanType bt = BeanType.of(cls);

                for(Map.Entry<String,String> prop : def.getProperties().entrySet()) {
                    String name  = prop.getKey();
                    String value = placeholderResolver.resolveString(prop.getValue());

                    if(!Strings.isEmpty(value)) {
                        BeanProperty bp = bt.getProperty(name);
                        bp.setValue(obj, Converts.convert(value, bp.getType(), bp.getGenericType()));
                    }
                }

                log.debug("Load config by loader : {}", cls.getSimpleName());
                obj.loadConfig(context);
            }
        }

        protected void complete() {
            //properties
            config.loadProperties(this.properties);
            config.loadArrayProperties(this.arrayProperties);

            //resources
            try {
                Map<String,Resource> urlResourceMap = new HashMap<>();

                loadBasePackageResources(urlResourceMap,config.basePackage);
                loadResources(urlResourceMap);

                config.resources = new SimpleResourceSet(urlResourceMap.values().toArray(new Resource[]{}));
            } catch (IOException e) {
                throw new AppConfigException("Unexpected IOException : " + e.getMessage(), e);
            }

            //permissions
            config.permissions.addAll(permissions);

            log.info("Load {} properties",config.properties.size());
        }

        protected void loadResources(Map<String,Resource> urlResourceMap) throws IOException{
            for(String basePackage : additionalPackages) {
                loadBasePackageResources(urlResourceMap, basePackage);
            }
            for(Resource resource : resources){
                urlResourceMap.put(resource.getURL().toExternalForm(), resource);
            }
        }

        protected void loadBasePackageResources(Map<String, Resource> urlResourceMap, String basePackage) throws IOException {
            if(!Strings.isEmpty(basePackage)){
                ResourceSet rs = Resources.scanPackage(basePackage);

                log.debug("Scan {} resource(s) in base-package location '{}'",rs.size(),basePackage);

                for(Resource resource : rs){
                    urlResourceMap.put(resource.getURL().toExternalForm(), resource);
                }
            }
        }

        protected void addPermissions(List<SysPermissionDef> permissions, boolean override){
            for(SysPermissionDef permission : permissions){
                addPermission(permission, override);
            }
        }

        protected void addPermission(SysPermissionDef permission, boolean override){
            Map<String,SysPermissionDef> typesPermissionsMap = typedPermissions.get(permission.getPermType());

            SysPermissionDef exists = null;

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
    }

    protected class ConfigContext extends MapPropertyAccessor implements AppConfigContext {

        protected final Loader           loader;
        protected final DefaultAppConfig config;
        protected final boolean          defaultOverrided;

        protected boolean     hasDefaultDataSource = false;
        protected Set<String> resources            = new HashSet<>();

        ConfigContext(Loader loader, boolean defaultOverried){
            super(loader.properties);
            this.loader = loader;
            this.config = loader.config;
            this.defaultOverrided = defaultOverried;
        }

        @Override
        public String getProfile() {
            return config.profile;
        }

        @Override
        public boolean isDefaultOverride() {
            return defaultOverrided;
        }

        @Override
        public Boolean getDebug() {
            return config.debug;
        }

        @Override
        public void setDebug(boolean debug) {
            config.debug = debug;
        }

        @Override
        public String getBasePackage() {
            return config.basePackage;
        }

        @Override
        public void setBasePackage(String bp) {
            config.basePackage = bp;
        }

        @Override
        public Locale getDefaultLocale() {
            return config.defaultLocale;
        }

        @Override
        public void setDefaultLocale(Locale locale) {
            config.defaultLocale = locale;
        }

        @Override
        public Charset getDefaultCharset() {
            return config.defaultCharset;
        }

        @Override
        public void setDefaultCharset(Charset charset) {
            config.defaultCharset = charset;
        }

        @Override
        public Set<String> getAdditionalPackages() {
            return loader.additionalPackages;
        }

        @Override
        public Map<String, String> getProperties() {
            return properties;
        }

        @Override
        public Map<String, List<String>> getArrayProperties() {
            return loader.arrayProperties;
        }

        @Override
        public boolean hasArrayProperty(String name) {
            return loader.arrayProperties.containsKey(name);
        }

        @Override
        public List<SysPermissionDef> getPermissions() {
            return loader.permissions;
        }

        @Override
        public void addPermission(SysPermissionDef p, boolean override) {
            loader.addPermission(p, override);
        }

        @Override
        public void importResource(Resource resource, boolean override) {
            loadFullConfig(new ConfigContext(loader, override), resource);
        }

        @Override
        public PlaceholderResolver getPlaceholderResolver() {
            return loader.placeholderResolver;
        }

        @Override
        public AppPropertyProcessor getPropertyProcessor() {
            return DefaultAppConfigSource.this.propertyProcessor;
        }

        @Override
        public void putProperties(Map<String, String> props) {
            if(null != props) {
                loader.properties.putAll(props);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getExtension(Class<T> type) {
            return (T)config.extensions.get(type);
        }

        @Override
        public <T> void setExtension(T extension) {
            Args.notNull(extension);
            config.extensions.put(extension.getClass(), extension);
        }

        @Override
        public <T> void setExtension(Class<T> type, T extension) {
            Args.notNull(type);
            Args.notNull(extension);
            config.extensions.put(type, extension);
        }

        @Override
        public void addResource(Resource r) {
            if(null != r) {
                loader.resources.add(r);
            }
        }

        @Override
        public void addResources(ResourceSet rs) {
            if(null != rs) {
                for(Resource r : rs.toResourceArray()) {
                    loader.resources.add(r);
                }
            }
        }

        @Override
        public boolean hasDefaultDataSourceConfig() {
            return hasDefaultDataSource;
        }

        @Override
        public boolean hasDataSourceConfig(String name) {
            return config.dataSourceConfigs.containsKey(name);
        }

        @Override
        public void setDataSourceConfig(String name, DataSourceConfig conf) {
            config.dataSourceConfigs.put(name, conf);

            if(conf.isDefault()) {

                if (hasDefaultDataSource) {
                    throw new AppConfigException("Default DataSource already exists");
                }

                this.hasDefaultDataSource = true;
            }
        }

        @Override
        public Map<String, DataSourceConfig> getDataSourceConfigs() {
            return config.dataSourceConfigs;
        }

        @Override
        public void addConfigLoader(AppConfigLoaderDef cl) {
            loader.externalLoaders.add(cl);
        }
    }

}