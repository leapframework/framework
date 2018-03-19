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

import leap.core.config.*;
import leap.core.ds.DataSourceProps;
import leap.core.ioc.ConfigBean;
import leap.core.sys.SysPermissionDef;
import leap.lang.*;
import leap.lang.Comparators;
import leap.lang.accessor.MapAttributeAccessor;
import leap.lang.accessor.SystemPropertyAccessor;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.convert.Converts;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.logging.LogUtils;
import leap.lang.reflect.Reflection;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;
import leap.lang.resource.SimpleResourceSet;
import leap.lang.text.DefaultPlaceholderResolver;
import leap.lang.text.PlaceholderResolver;
import leap.lang.xml.XmlReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static leap.core.AppConfig.*;

public class DefaultAppConfigSource implements AppConfigSource {

	private static final Log log = LogFactory.get(DefaultAppConfigSource.class);

    //all init properties
    protected static Set<String> INIT_PROPERTIES = new HashSet<>();
    static {
        INIT_PROPERTIES.add(AppConfig.INIT_PROPERTY_PROFILE);
        INIT_PROPERTIES.add(AppConfig.INIT_PROPERTY_BASE_PACKAGE);
        INIT_PROPERTIES.add(AppConfig.INIT_PROPERTY_DEBUG);
        INIT_PROPERTIES.add(AppConfig.INIT_PROPERTY_LAZY_TEMPLATE);
        INIT_PROPERTIES.add(AppConfig.INIT_PROPERTY_DEFAULT_CHARSET);
        INIT_PROPERTIES.add(AppConfig.INIT_PROPERTY_DEFAULT_LOCALE);
    }

    private static final AppProfileResolver      profileResolver = Factory.newInstance(AppProfileResolver.class);
    private static final AppPropertyPrinter      propertyPrinter = Factory.newInstance(AppPropertyPrinter.class);
    private static final List<AppPropertyReader> propertyReaders = Factory.newInstances(AppPropertyReader.class);
    private static final List<AppConfigReader>   configReaders   = Factory.newInstances(AppConfigReader.class);
    private static final List<AppConfigListener> configListeners = Factory.newInstances(AppConfigListener.class);

    protected AppPropertyProcessor propertyProcessor = new PropertyProcessorWrapper();

    @Override
    public AppConfig loadConfig(Object externalContext, Map<String, String> externalProperties) {
        if(null == externalProperties) {
            externalProperties = new LinkedHashMap<>();
        }

        //resolve profile
        String profile = profileResolver.resolveProfile(externalContext, externalProperties);

        log.info("\n\n   *** app profile : {} ***\n", profile);

        //load init properties from system environment.
        Map<String, AppProperty> initProperties = initProperties(externalProperties);

        //Create loader for loading the configuration.
        Loader loader = createLoader(externalContext, initProperties, profile);

        //Load config
        DefaultAppConfig config = loader.load();

        //post loading.
        postLoad(config);

        return config;
    }

    @Override
    public void registerBeans(AppConfig config, BeanFactory factory) {
        factory.setPrimaryBean(AppConfig.class, config);

        for(Map.Entry<Class<?>, Object> extension : config.getExtensions().entrySet()) {

            Class<?> type = extension.getKey();
            Object   inst = extension.getValue();

            if(inst instanceof ConfigBean) {
                factory.setPrimaryBean(type, factory.inject(inst));
            }
        }
    }

    private Map<String, AppProperty> initProperties(Map<String, String> externalProperties) {
        Map<String, AppProperty> props = new HashMap<>();
        if(null != externalProperties) {
            if(!externalProperties.isEmpty()) {
                Maps.resolveValues(externalProperties, new DefaultPlaceholderResolver(SystemPropertyAccessor.INSTANCE));
            }
            externalProperties.forEach((k,v) -> props.put(k, new SimpleAppProperty("<external>", k, v)));
        }

        Map<String,String> env = System.getenv();
        if(null != env) {
            env.forEach((name, value) -> {
                //converts all '_' -> '.' if the first character is lower case.
                if(Character.isLowerCase(name.charAt(0))) {
                    name = Strings.replace(name, '_', '.');
                }
                props.put(name, new SimpleAppProperty("<env>", name, value, true));
            });
        }

        Properties sysProps = System.getProperties();
        for(Object key : sysProps.keySet()) {
            String name = key.toString();
            props.put(name, new SimpleAppProperty("<sys>", name, System.getProperty(name), true));
        }

        return props;
    }

    protected void postLoad(DefaultAppConfig config) {
        config.postLoad();
    }

    protected Loader createLoader(Object externalContext, Map<String, AppProperty> props, String profile) {
        return new Loader(externalContext, props, profile);
    }

    protected void loadProperties(ConfigContext context, AppResource... resources) {
        for(AppResource ar : resources){
            try{
                Resource resource  = ar.getResource();
                String resourceUrl = resource.getURL().toString();

                if(log.isDebugEnabled()){
                    if(AppResources.isFrameworkAndCoreResource(resourceUrl)) {
                        log.trace("Load properties : {}", LogUtils.getUrl(resource));
                    }else{
                        log.debug("Load properties : {}", LogUtils.getUrl(resource));
                    }
                }

                if(context.resources.contains(resourceUrl)){
                    throw new AppConfigException("Cycle importing detected of '" + resourceUrl + "', please check your config : " + resourceUrl);
                }

                context.resources.add(resourceUrl);

                context.setDefaultOverride(ar.isDefaultOverride());
                for(AppPropertyReader reader : propertyReaders) {
                    if(reader.readProperties(context, resource)) {
                        break;
                    }
                }
                context.resetDefaultOverride();
            }catch(IOException e) {
                throw new AppConfigException("I/O Exception",e);
            }
        }
    }

    private void loadConfig(ConfigContext context, AppResource... resources){
        for(AppResource ar : resources){
            try{
                Resource resource  = ar.getResource();
                String resourceUrl = resource.getURL().toString();

                if(log.isDebugEnabled()){
                    if(AppResources.isFrameworkAndCoreResource(resourceUrl)) {
                        log.trace("Load config : {}", LogUtils.getUrl(resource));
                    }else{
                        log.debug("Load config : {}", LogUtils.getUrl(resource));
                    }
                }

                if(context.resources.contains(resourceUrl)){
                    throw new AppConfigException("Cycle importing detected of '" + resourceUrl + "', please check your config : " + resourceUrl);
                }

                context.resources.add(resourceUrl);

                context.setDefaultOverride(ar.isDefaultOverride());
                for(AppConfigReader reader : configReaders) {
                    if(reader.readConfig(context, resource)) {
                        break;
                    }
                }
                context.resetDefaultOverride();

            }catch(IOException e) {
                throw new AppConfigException("I/O Exception",e);
            }
        }
    }

    protected class Loader {

        final DefaultAppConfigSource parent = DefaultAppConfigSource.this;

        final Object                     externalContext;
        final Map<String, AppProperty>   initProperties;
        final DefaultAppConfig           config;
        final AppResources               appResources;
        final AppResource[]              configResources;
        final Set<String>                extendedConfigNames = new HashSet<>();
        final Set<String>                resolvingProperties = new HashSet<>();
        final DefaultPlaceholderResolver resolver;

        protected final Map<String, AppProperty>                     properties        = new ConcurrentHashMap<>();
        protected final Map<String, List<String>>                    arrayProperties   = new ConcurrentHashMap<>();
        protected final Set<Resource>                                resources         = new HashSet<>();
        protected final List<SysPermissionDef>                       permissions       = new ArrayList<>();
        protected final Map<Class<?>, Map<String, SysPermissionDef>> typedPermissions  = new HashMap<>();
        protected final Map<String, DataSourceProps.Builder>         dataSourceConfigs = new HashMap<>();
        protected final Set<AppPropertyLoaderConfig> propertyLoaders;

        Loader(Object externalContext, Map<String,AppProperty> props, String profile) {
            this.externalContext = externalContext;
            this.initProperties  = props;
            this.config          = new DefaultAppConfig(profile);
            this.appResources    = AppResources.create(config, externalContext);
            this.configResources = appResources.search("config");

            this.resolver = new DefaultPlaceholderResolver(this::resolveProperty);
            this.resolver.setEmptyUnresolvablePlaceholders(false);
            this.resolver.setIgnoreUnresolvablePlaceholders(true);

            this.propertyLoaders = new TreeSet<>(Comparators.ORDERED_COMPARATOR);
        }

        protected void init() {

        }

        protected DefaultAppConfig load() {
            init();

            //===properties====
            //load local properties
            loadLocalProperties(new ConfigContext(this, false, true));

            //load external properties.
            loadExternalProperties(new ConfigContext(this, false, true));

            //===config====
            ConfigContext context = new ConfigContext(this, false, false);

            // pre load configuration
            configListeners.forEach(listener -> listener.preLoadConfig(context, context));

            //Load configuration.
            loadConfig(context);

            //post load configuration
            configListeners.forEach(listener -> listener.postLoadConfig(context));

            //complete loading configuration.
            complete();
            configListeners.forEach(listener -> listener.completeLoadConfig(config));

            return config;
        }

        protected void loadLocalProperties(ConfigContext context) {
            parent.loadProperties(context, configResources);
        }

        protected void loadExternalProperties(ConfigContext context) {

            Map<String,String> props = getPropertiesMap();

            for(AppPropertyLoaderConfig conf : propertyLoaders) {

                if(!conf.load(props)) {
                    log.info("Property loader '{}' disabled", conf.getClassName());
                    continue;
                }

                Class<?> cls = Classes.tryForName(conf.getClassName());
                if(null == cls) {
                    throw new AppConfigException("The property loader class '" + conf.getClassName() +
                                                 "' not found, check your config");
                }

                if(!AppPropertyLoader.class.isAssignableFrom(cls)) {
                    throw new AppConfigException("The loader class '" + conf.getClassName() +
                                                 "' must implements interface '" + AppPropertyLoader.class.getName() + "'");
                }

                AppPropertyLoader loader = (AppPropertyLoader) Reflection.newInstance(cls);

                BeanType bt = BeanType.of(cls);

                for(Map.Entry<String,String> prop : conf.getProperties().entrySet()) {
                    String name  = prop.getKey();
                    String value = resolver.resolveString(prop.getValue());

                    if(!Strings.isEmpty(value)) {
                        BeanProperty bp = bt.getProperty(name);
                        bp.setValue(loader, Converts.convert(value, bp.getType(), bp.getGenericType()));
                    }
                }

                log.info("Load properties by loader : {}", cls.getSimpleName());
                loader.loadProperties(context);
            }

            //external properties overrides the configured properties.
            properties.putAll(initProperties);

            if(properties.containsKey(INIT_PROPERTY_BASE_PACKAGE)) {
                config.basePackage = properties.get(INIT_PROPERTY_BASE_PACKAGE).getValue();
            }

            if(properties.containsKey(INIT_PROPERTY_DEBUG)) {
                String value = properties.get(INIT_PROPERTY_DEBUG).getValue();
                if(!Strings.isEmpty(value)) {
                    config.debug = Converts.toBoolean(value);
                }
            }

            if(properties.containsKey(INIT_PROPERTY_LAZY_TEMPLATE)) {
                String value = properties.get(INIT_PROPERTY_LAZY_TEMPLATE).getValue();
                if(!Strings.isEmpty(value)) {
                    config.lazyTemplate = Converts.toBoolean(value);
                }
            }

            if(properties.containsKey(INIT_PROPERTY_DEFAULT_CHARSET)) {
                String value = properties.get(INIT_PROPERTY_DEFAULT_CHARSET).getValue();
                if(!Strings.isEmpty(value)) {
                    config.defaultCharset = Converts.convert(value, Charset.class);
                }
            }

            if(properties.containsKey(INIT_PROPERTY_DEFAULT_LOCALE)) {
                String value = properties.get(INIT_PROPERTY_DEFAULT_LOCALE).getValue();
                if(!Strings.isEmpty(value)) {
                    config.defaultLocale = Converts.convert(value, Locale.class);
                }
            }

            //base package
            if(Strings.isEmpty(config.basePackage)){
                config.basePackage = DEFAULT_BASE_PACKAGE;
                config.properties.put(INIT_PROPERTY_BASE_PACKAGE,config.basePackage);
            }

            //debug
            if(null == config.debug){
                config.debug = AppConfig.PROFILE_DEVELOPMENT.equals(config.getProfile()) ? true : false;
                config.properties.put(INIT_PROPERTY_DEBUG,String.valueOf(config.debug));
            }

            //lazyTemplate
            if(null == config.lazyTemplate){
                config.lazyTemplate = false;
                config.properties.put(INIT_PROPERTY_LAZY_TEMPLATE,String.valueOf(config.lazyTemplate));
            }

            //default locale
            if(null == config.defaultLocale){
                config.defaultLocale = DEFAULT_LOCALE;
                config.properties.put(INIT_PROPERTY_DEFAULT_LOCALE,config.defaultLocale.toString());
            }

            //default charset
            if(null == config.defaultCharset){
                config.defaultCharset = DEFAULT_CHARSET;
                config.properties.put(INIT_PROPERTY_DEFAULT_CHARSET,config.defaultCharset.name());
            }

            resolveProperties();
            processProperties();
        }

        protected void loadConfig(ConfigContext context) {
            AppResource[] files = appResources.search("config");
            for(String path : extendedConfigNames){
                files = Arrays2.concat(files,appResources.search(path));
            }
            if(files.length > 0) {
                parent.loadConfig(context, files);
            }
        }

        protected Map<String, String> getPropertiesMap() {
            Map<String,String> props = new HashMap<>();
            properties.forEach((k,p) -> props.put(p.getName(), p.getValue()));
            return props;
        }

        protected void complete() {
            //Apply all the properties to config object.
            config.loadProperties(getPropertiesMap());
            config.loadArrayProperties(this.arrayProperties);

            log.info("\n\n   *** {} : {} ***\n",INIT_PROPERTY_BASE_PACKAGE,config.basePackage);
            log.info("Load {} properties (includes system)",config.properties.size());
            printProperties();

            //resources
            try {
                Map<String,Resource> urlResourceMap = new HashMap<>();

                loadBasePackageResources(urlResourceMap,config.basePackage);
                for(String basePackage : config.getAdditionalPackages()) {
                    loadBasePackageResources(urlResourceMap, basePackage);
                }
                loadResources(urlResourceMap);
                
                config.resources = new SimpleResourceSet(urlResourceMap.values().toArray(new Resource[]{}));
            } catch (IOException e) {
                throw new AppConfigException("Unexpected IOException : " + e.getMessage(), e);
            }

            //permissions
            config.permissions.addAll(permissions);

            properties.clear();
            arrayProperties.clear();
        }

        protected void printProperties(){
            if(log.isDebugEnabled()){
                StringWriter msg = new StringWriter();
                PrintWriter writer = new PrintWriter(msg);
                propertyPrinter.printProperties(this.properties.values(), writer);
                log.debug("Print properties : \n\n{}",msg.toString());
            }
        }

        protected void processProperties() {
            Out<String> out = new Out<>();
            for(Map.Entry<String, AppProperty> entry : this.properties.entrySet()) {
                String      name = entry.getKey();
                AppProperty prop = entry.getValue();

                if(propertyProcessor.process(name, prop.getValue(), out)) {
                    prop.updateProcessedValue(out.getValue());
                }
            }

            for(Map.Entry<String,List<String>> p : this.arrayProperties.entrySet()) {
                String       name   = p.getKey();
                List<String> values = p.getValue();

                boolean processed = false;

                for(int i=0;i<values.size();i++) {
                    String value = values.get(i);

                    if(propertyProcessor.process(name, value, out)) {
                        value = out.getValue();
                        processed = true;
                        values.set(i, value);
                    }
                }

                if(processed) {
                    arrayProperties.put(name, values);
                }
            }
        }

        protected void putProperty(Object source, String name, String value) {
            properties.put(name, new SimpleAppProperty(source, name, value));
        }

        protected String resolveProperty(String name) {
            if(resolvingProperties.contains(name)) {
                throw new AppConfigException("Found cyclic reference property '" + name + "'");
            }

            resolvingProperties.add(name);

            AppProperty p = properties.get(name);
            if(null == p) {
                p = initProperties.get(name);
            }

            String value = null == p ? null : p.getValue();

            if(resolver.hasPlaceholder(value)) {
                String newValue = resolver.resolveString(value);
                if(!newValue.equals(value)) {
                    value = newValue;
                    p.updateResolvedValue(newValue);
                }
            }

            resolvingProperties.remove(name);

            return value;
        }

        protected void resolveProperties() {
            resolveObjectProperties(properties);

            for(Map.Entry<String,List<String>> p : arrayProperties.entrySet()) {
                String name = p.getKey();
                List<String> values = p.getValue();

                for(int i=0;i<values.size();i++) {
                    String value = values.get(i);
                    if(resolver.hasPlaceholder(value)) {
                        String newValue = resolver.resolveString(value);
                        if(!newValue.equals(value)) {
                            values.set(i, newValue);
                        }
                    }
                }
            }
            /*
            for(Map.Entry<String,DataSourceConfig.Builder> entry : dataSourceConfigs.entrySet()) {
                String name = entry.getKey();
                DataSourceConfig.Builder c = entry.getValue();

                Map<String, String> resolvedProperties = new ConcurrentHashMap<>(c.getProperties());

                resolveStringProperties(resolvedProperties);

                c.setProperties(resolvedProperties);

                config.dataSourceConfigs.put(name, c.build());
            }
            */
        }

        protected void resolveObjectProperties(Map<String, AppProperty> properties) {
            for(Map.Entry<String,AppProperty> entry : properties.entrySet()) {
                AppProperty prop = entry.getValue();

                String value = prop.getValue();
                if(resolver.hasPlaceholder(value)) {
                    String newValue = resolver.resolveString(value);
                    if (!newValue.equals(value)) {
                        prop.updateResolvedValue(newValue);
                    }
                }
            }
        }

        protected void loadResources(Map<String,Resource> urlResourceMap) throws IOException{
            for(String basePackage : config.additionalPackages) {
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

    protected class ConfigContext extends MapAttributeAccessor implements AppConfigContext,AppPropertyContext,AppPropertySetter,AppConfigProcessors,AppConfigPaths {

        protected final AppConfigProcessor[] processors =
                Factory.newInstances(AppConfigProcessor.class).toArray(new AppConfigProcessor[0]);

        protected final boolean          forProperty;
        protected final Loader           loader;
        protected final DefaultAppConfig config;
        protected final boolean          originalDefaultOverride;
        protected final Stack<Boolean>   defaultOverrideStack = new Stack<>();

        protected boolean     defaultOverride;
        protected boolean     hasDefaultDataSource = false;
        protected Set<String> resources            = new HashSet<>();

        ConfigContext(Loader loader, boolean defaultOverride, boolean forProperty){
            this.loader = loader;
            this.config = loader.config;
            this.originalDefaultOverride = defaultOverride;
            this.defaultOverride = defaultOverride;
            this.forProperty = forProperty;
        }

        @Override
        public boolean handleXmlElement(AppConfigContext context, XmlReader reader, String defaultNsURI) {
            if(!reader.isStartElement()) {
                return false;
            }

            String nsURI = reader.getElementName().getNamespaceURI();
            if(Strings.isEmpty(nsURI) || nsURI.equals(defaultNsURI)) {
                return false;
            }

            for(AppConfigProcessor processor : processors){

                if(nsURI.equals(processor.getNamespaceURI())){
                    processor.processElement(context,reader);
                    return true;
                }

            }

            if(null != defaultNsURI && !nsURI.equals(defaultNsURI)) {
                throw new AppConfigException("Namespace uri '" + nsURI +
                                            "' not supported at '" + reader.getCurrentLocation() +
                                            "', check your config : " + reader.getSource());
            }

            return false;
        }

        @Override
        public AppConfigProcessors getProcessors() {
            return this;
        }

        @Override
        public String getProfile() {
            return config.profile;
        }

        @Override
        public boolean isDefaultOverride() {
            return defaultOverride;
        }

        @Override
        public void setDefaultOverride(boolean b) {
            this.defaultOverride = b;
            defaultOverrideStack.add(b);
        }

        @Override
        public void resetDefaultOverride() {
            if(defaultOverrideStack.isEmpty()) {
                this.defaultOverride = originalDefaultOverride;
            }else{
                this.defaultOverride = defaultOverrideStack.pop();
            }
        }

        @Override
        public boolean hasProperty(String name) {
            return loader.properties.containsKey(name) || loader.arrayProperties.containsKey(name);
        }

        @Override
        public Set<String> getAdditionalPackages() {
            return loader.config.additionalPackages;
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
            AppResource ar = new SimpleAppResource(resource, override);

            if(forProperty) {
                loadProperties(new ConfigContext(loader, override, true),  ar);
            }else{
                loadConfig(new ConfigContext(loader, override, false), ar);
            }
        }

        @Override
        public void putProperty(Object source, String name, String value) {
            if(name.endsWith("[]")) {
                name = name.substring(0, name.length()-2);
                List<String> list = loader.arrayProperties.get(name);
                if(null == list) {
                    list = new ArrayList<>();
                    loader.arrayProperties.put(name, list);
                }
                list.add(value);
            }else{
                loader.putProperty(source, name, value);
            }
        }

        @Override
        public void putProperties(Object source, Map<String, String> props) {
            if(null != props) {
                props.forEach((k,v) -> putProperty(source, k, v));
            }
        }

        @Override
        public void addLoader(AppPropertyLoaderConfig config) {
            Args.notNull(config);
            loader.propertyLoaders.add(config);
        }

        @Override
        public Map<String, String> getProperties() {
            return loader.getPropertiesMap();
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
        public void addConfigName(String name) {
            loader.extendedConfigNames.add(name);
        }

        @Override
        public PlaceholderResolver getPlaceholderResolver() {
            return loader.resolver;
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

    /*
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
                        conf.setDefault(true);
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
    */

}