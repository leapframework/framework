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
import leap.core.ds.DataSourceConfig;
import leap.core.ds.DataSourceManager;
import leap.core.instrument.AppInstrumentProcessor;
import leap.core.instrument.DefaultAppInstrumentContext;
import leap.core.sys.SysPermissionDefinition;
import leap.lang.*;
import leap.lang.accessor.SystemPropertyAccessor;
import leap.lang.convert.Converts;
import leap.lang.io.IO;
import leap.lang.json.JSON;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.*;
import leap.lang.security.RSA;
import leap.lang.security.RSA.RsaKeyPair;
import leap.lang.text.DefaultPlaceholderResolver;
import leap.lang.text.PlaceholderResolver;
import leap.lang.tools.DEV;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static leap.core.AppResources.*;

/**
 * A default implementation of {@link AppConfig}
 */
public class DefaultAppConfig extends AppConfigBase implements AppConfig {
	
	private static final Log log = LogFactory.get(DefaultAppConfig.class);
	
	private static final String APP_PROFILE_CONFIG_RESOURCE = CP_APP_PREFIX + "/profile";
	
	private static final String[] BASE_CONFIG_LOCATIONS = new String[]{
            CP_APP_PREFIX + "/config.xml"
    };

	private static final String[] APP_CONFIG_LOCATIONS  = new String[]{
            CP_APP_PREFIX + "/config.xml",
            CP_APP_PREFIX + "/config/**/*.xml",
            CP_APP_PREFIX + "/profiles/{profile}/config.xml",
            CP_APP_PREFIX + "/profiles/{profile}/config/**/*.xml"
    };
	
	//all init properties
	protected static Set<String> INIT_PROPERTIES = new HashSet<>();
	static {
		INIT_PROPERTIES.add(INIT_PROPERTY_PROFILE);
		INIT_PROPERTIES.add(INIT_PROPERTY_BASE_PACKAGE);
		INIT_PROPERTIES.add(INIT_PROPERTY_DEBUG);
		INIT_PROPERTIES.add(INIT_PROPERTY_DEFAULT_CHARSET);
		INIT_PROPERTIES.add(INIT_PROPERTY_DEFAULT_LOCALE);
	}
	
	protected Object					    externalContext     = null;
    protected PropertyProvider              propertyProvider    = null;
	protected String						profile				= null;
	protected Boolean						debug				= null;
	protected String						basePackage			= null;
	protected Locale						defaultLocale		= null;
	protected Charset						defaultCharset      = null;
	protected boolean						reloadEnabled		= false;
	protected String						secret				= null;
	protected PrivateKey                    privateKey          = null;
	protected Map<Class<?>, Object>         extensions          = new HashMap<>();
	protected Map<String,String>            properties          = new ConcurrentHashMap<>();
	protected Map<String,String>            propertiesReadonly  = Collections.unmodifiableMap(properties);
    protected Map<String,List<String>>      arrayProperties     = new ConcurrentHashMap<>();
	protected List<SysPermissionDefinition> permissions         = new ArrayList<>();
	protected List<SysPermissionDefinition> permissionsReadonly = Collections.unmodifiableList(permissions);
	protected ResourceSet		            resources           = null;
	protected DefaultPlaceholderResolver    placeholderResolver = new DefaultPlaceholderResolver(this);
	protected AppPropertyProcessor		    propertyProcessor	= new DefaultPropertyProcessor();
	protected Map<String, DataSourceConfig> dataSourceConfigs   = new ConcurrentHashMap<>();
	protected Map<String, DataSourceConfig> dataSourceConfigsReadonly = Collections.unmodifiableMap(dataSourceConfigs);
	
	protected DefaultAppConfig(Object externalContext, Map<String, String> initProperties){
		this.externalContext = externalContext;
		this.placeholderResolver.setEmptyUnresolvablePlaceholders(false);
		this.placeholderResolver.setIgnoreUnresolvablePlaceholders(true);
		this.init(initProperties);
	}

	protected void init(Map<String, String> initProperties){
		if(null == initProperties){
			initProperties = new HashMap<>();
		}
		
		loadInitPropertiesFromSystem(initProperties);
		
		//init profile
		this.initProfile(initProperties);
		
		loadInitPropertiesFromConfig(initProperties);
		
		this.basePackage    = initProperties.get(INIT_PROPERTY_BASE_PACKAGE);
		this.debug          = Maps.get(initProperties, INIT_PROPERTY_DEBUG,Boolean.class);
		this.defaultCharset = Maps.get(initProperties, INIT_PROPERTY_DEFAULT_CHARSET, Charset.class);
		this.defaultLocale  = Maps.get(initProperties, INIT_PROPERTY_DEFAULT_LOCALE, Locale.class);
		
		loadProperties(initProperties);
	}
	
	protected void loadInitPropertiesFromSystem(Map<String, String> initProperties) {
		if(!initProperties.isEmpty()) {
			Maps.resolveValues(initProperties, new DefaultPlaceholderResolver(SystemPropertyAccessor.INSTANCE));
		}

        Properties props = System.getProperties();
        for(Object key : props.keySet()) {
            String name = key.toString();

            if(name.startsWith("sun.")){
                continue;
            }

            if(name.startsWith("org.apache.")){
                continue;
            }

            if(name.startsWith("com.oracle.")){
                continue;
            }

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

    public void setPropertyProvider(PropertyProvider p)  {
        this.propertyProvider = p;
    }

	protected void loadInitPropertiesFromConfig(Map<String, String> initProperties) {
		PlaceholderResolver pr = new DefaultPlaceholderResolver(initProperties);
		
		Resource confProperties = Resources.getResource("classpath:conf/config.properties");
		if(null != confProperties && confProperties.exists()) {
			initProperties.putAll(Props.load(confProperties).toMap(pr));
		}
		
		confProperties = Resources.getResource("classpath:conf/profiles/" + profile + "/config.properties");
		if(null != confProperties && confProperties.exists()) {
			initProperties.putAll(Props.load(confProperties).toMap(pr));
		}
	}

    protected void loadProperty(String name, String value) {

        //array property
        if(name.endsWith("[]")) {
            name = name.substring(0, name.length() - 2);

            List<String> values = arrayProperties.get(name);
            if(null == values){
                values = new ArrayList<>();
                arrayProperties.put(name, values);
            }

            values.add(value);
        }else{
            properties.put(name, value);
        }
    }

    protected void loadProperties(Map<String,String> map) {
        for(Entry<String,String> entry : map.entrySet()) {
            loadProperty(entry.getKey(), entry.getValue());
        }
    }

    protected void loadArrayProperties(Map<String, List<String>> map) {
        for(Entry<String, List<String>> entry : map.entrySet()) {
            String key = entry.getKey();

            List<String> list = arrayProperties.get(key);
            if(null == list) {
                list = new ArrayList<>();
                arrayProperties.put(key, list);
            }

            list.addAll(entry.getValue());
        }
    }
	
	@Override
    public String getProfile() {
	    return profile;
    }
	
	@Override
    public boolean isDebug() {
	    return debug;
    }
	
	@Override
	public String getBasePackage() {
		return basePackage;
	}
	
	@Override
    public Locale getDefaultLocale() {
	    return defaultLocale;
    }
	
	@Override
    public Charset getDefaultCharset() {
	    return defaultCharset;
    }
	
	@Override
    public boolean isReloadEnabled() {
	    return reloadEnabled;
    }
	
	@Override
    public String getSecret() {
	    return secret;
    }
	
	@Override
    public String ensureGetSecret() {
		if(Strings.isEmpty(secret)) {
			secret = loadOrGenerateSecret();
		}
	    return secret;
    }
	
	@Override
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    @Override
    public PrivateKey ensureGetPrivateKey() {
        if(null == privateKey) {
            privateKey = loadOrGeneratePrivateKey();
        }
        return privateKey;
    }

    @Override
    public Map<String, DataSourceConfig> getDataSourceConfigs() {
	    return dataSourceConfigsReadonly;
    }

	@Override
	public Map<String, String> getProperties() {
	    return propertiesReadonly;
    }

    public List<SysPermissionDefinition> getPermissions() {
	    return permissionsReadonly;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getExtension(Class<T> type) {
	    return (T)extensions.get(type);
    }
    
	@Override
	@SuppressWarnings("unchecked")
    public <T> T removeExtension(Class<T> type) {
	    return (T)extensions.remove(type);
    }

	@Override
    public String getProperty(String name) {
        String v = null == propertyProvider ? null : propertyProvider.getRawProperty(name);
        return null == v ? properties.get(name) : v;
    }
	
	@Override
    public String getProperty(String name, String defaultValue) {
		String v = getProperty(name);
	    return Strings.isEmpty(v) ? defaultValue : v;
    }
	
	@Override
    public <T> T getProperty(String name, Class<T> type) {
		String v = getProperty(name);
	    return Strings.isEmpty(v) ? null : Converts.convert(v, type);
    }

	@Override
    public <T> T getProperty(String name, Class<T> type, T defaultValue) {
		String v = getProperty(name);
	    return Strings.isEmpty(v) ? defaultValue : Converts.convert(v, type);
    }

	@Override
    public boolean getBooleanProperty(String name, boolean defaultValue) {
	    return Maps.getBoolean(properties, name, defaultValue);
    }

	@Override
    public int getIntProperty(String name, int defaultValue) {
	    return Maps.getInteger(properties, name, defaultValue);
    }

    @Override
    public String[] getArrayProperty(String name) {
        List<String> values = arrayProperties.get(name);

        return null == values ? null : values.toArray(Arrays2.EMPTY_STRING_ARRAY);
    }

    @Override
    public StringProperty getDynaProperty(String name) {
        if(null != propertyProvider) {
            return propertyProvider.getDynaProperty(name);
        }
        return new SimpleStringProperty(properties.get(name));
    }

    @Override
    public <T> Property<T> getDynaProperty(String name, Class<T> type) {
        if(null != propertyProvider) {
            return propertyProvider.getDynaProperty(name, type);
        }

        String v = properties.get(name);
        if(null == v || v.isEmpty()) {
            return new NullProperty<>();
        }

        TypeInfo ti = Types.getTypeInfo(type,null);
        if(ti.isComplexType()) {
            return new SimpleProperty<>(type, JSON.decode(v, type));
        }else{
            return new SimpleProperty<>(type, Converts.convert(v, type));
        }
    }

    @Override
    public IntegerProperty getDynaIntegerProperty(String name) {
        if(null != propertyProvider) {
            return propertyProvider.getDynaIntegerProperty(name);
        }
        return new SimpleIntegerProperty(Converts.convert(properties.get(name), Integer.class));
    }

    @Override
    public LongProperty getDynaLongProperty(String name) {
        if(null != propertyProvider) {
            return propertyProvider.getDynaLongProperty(name);
        }
        return new SimpleLongProperty(Converts.convert(properties.get(name), Long.class));
    }

    @Override
    public BooleanProperty getDynaBooleanProperty(String name) {
        if(null != propertyProvider) {
            return propertyProvider.getDynaBooleanProperty(name);
        }
        return new SimpleBooleanProperty(Converts.convert(properties.get(name), Boolean.class));
    }

    @Override
    public DoubleProperty getDynaDoubleProperty(String name) {
        if(null != propertyProvider) {
            return propertyProvider.getDynaDoubleProperty(name);
        }
        return new SimpleDoubleProperty(Converts.convert(properties.get(name), Double.class));
    }

    @Override
    public <T> void bindDynaProperty(String name, Class<T> type, Property<T> p) {
        if(null == p) {
            return;
        }

        if(null != propertyProvider) {
            propertyProvider.bindDynaProperty(name, type, p);
        }else if(properties.containsKey(name)){
            p.convert(getProperty(name));
        }

    }

    @Override
    public ResourceSet getResources() {
	    return resources;
    }
	
	@Override
    public PlaceholderResolver getPlaceholderResolver() {
	    return placeholderResolver;
    }

	protected DefaultAppConfig load(){
		//init base properties
		initBaseProperties();
		
		//load configs
        DefaultAppConfigLoader fmmLoader = load(AppResources.getFMMClasspathResourcesForXml("config"));
		DefaultAppConfigLoader appLoader = load(AppResources.getLocClasspathResources(profiled(APP_CONFIG_LOCATIONS,profile)));

		//properties
		loadProperties(fmmLoader.getProperties());
        loadArrayProperties(fmmLoader.getArrayProperties());

		loadProperties(appLoader.getProperties());
        loadArrayProperties(appLoader.getArrayProperties());

		//resources
		try {
	        Map<String,Resource> urlResourceMap = new HashMap<>();
	        
	        loadBasePackageResources(urlResourceMap,basePackage);
	        
	        loadResources(urlResourceMap, fmmLoader);
	        loadResources(urlResourceMap, appLoader);
	        
	        this.resources = new SimpleResourceSet(urlResourceMap.values().toArray(new Resource[]{}));
        } catch (IOException e) {
        	throw new AppConfigException("Unexpected IOException occured : " + e.getMessage(), e);
        }
		
		//instrument classes.
		instrumentClasses();
		
		//permissions
		fmmLoader.addPermissions(appLoader.getPermissions(), true);
		this.permissions.addAll(fmmLoader.getPermissions());
		
		this.postLoad();
		
		log.info("Load {} properties",properties.size());
		
		return this;
	}
	
	protected void initProfile(Map<String, String> initProperties){
		
		//read from config file
		Resource r = Resources.getResource(APP_PROFILE_CONFIG_RESOURCE);
		if(null != r && r.exists()){
			profile = Strings.trim(r.getContent());
		}

		//read from init properties
		if(Strings.isEmpty(profile)){
			profile = initProperties.get(INIT_PROPERTY_PROFILE);	
		}
		
		//auto detect profile name
		if(Strings.isEmpty(profile)){
			profile = autoDetectProfileName();
		}
	}
	
	protected String autoDetectProfileName(){
		//Auto detect development environment (maven environment)
		if(DEV.isDevProject(externalContext)){
			return AppProfile.DEVELOPMENT.getName();
		}else{
			return DEFAULT_PROFILE;
		}
	}
	
	protected void initBaseProperties(){
		DefaultAppConfigLoader loader = new DefaultAppConfigLoader(externalContext,properties,dataSourceConfigs,propertyProcessor);
		Resource[] appBaseConfigFiles = AppResources.get().searchClasspaths(BASE_CONFIG_LOCATIONS);
		if(appBaseConfigFiles.length > 0) {
			loader.loadBaseProperties(appBaseConfigFiles[0]);	
		}
		
		//base package
		if(!Strings.isEmpty(loader.basePackage)){
			this.basePackage = loader.basePackage;	
		}else if(Strings.isEmpty(this.basePackage)){
			this.basePackage = DEFAULT_BASE_PACKAGE;
		}
		
		//debug
		if(null != loader.debug){
			this.debug = loader.debug;
		}else if(null == this.debug){
			this.debug = AppProfile.DEVELOPMENT.matches(profile) ? true : false;
		}
		
		//default locale
		if(null != loader.defaultLocale){
			defaultLocale = loader.defaultLocale;
		}else if(null == defaultLocale){
			defaultLocale = DEFAULT_LOCALE;
		}
		
		//default charset
		if(null != loader.defaultCharset){
			defaultCharset = loader.defaultCharset;
		}else if(null == defaultCharset){
			defaultCharset = DEFAULT_CHARSET;
		}
		
		log.info("{}:{}, {}:{}, {}:{}, {}:{}",
				 INIT_PROPERTY_PROFILE,profile,
				 INIT_PROPERTY_BASE_PACKAGE,basePackage,
				 INIT_PROPERTY_DEFAULT_LOCALE,defaultLocale.toString(),
				 INIT_PROPERTY_DEFAULT_CHARSET,defaultCharset.name());
		
		properties.put(INIT_PROPERTY_PROFILE,profile);
		properties.put(INIT_PROPERTY_DEBUG,String.valueOf(debug));
		properties.put(INIT_PROPERTY_BASE_PACKAGE,basePackage);
		properties.put(INIT_PROPERTY_DEFAULT_LOCALE,defaultLocale.toString());
		properties.put(INIT_PROPERTY_DEFAULT_CHARSET,defaultCharset.name());
	}
	
	protected void instrumentClasses() {
        DefaultAppInstrumentContext context = new DefaultAppInstrumentContext();

		for(AppInstrumentProcessor p : Factory.newInstances(AppInstrumentProcessor.class)){
			try {
	            p.instrument(context, resources);
            } catch (Throwable e) {	
            	throw new AppInitException("Error calling instrument processor '" + p + "', " + e.getMessage(), e);
            }
		}

        context.postInstrumented();
	}
	
	protected void postLoad(){
		Boolean reloadEnabled = getProperty(PROPERTY_RELOAD_ENABLED, Boolean.class);
		if(null == reloadEnabled){
			this.reloadEnabled = isDebug() ? true : false;
		}else{
			this.reloadEnabled = reloadEnabled;
		}

		if(Strings.isEmpty(secret)) {
			this.secret = getProperty(PROPERTY_SECRET);
		}
		
		if(null == privateKey) {
		    String base64PrivateKey = getProperty(PROPERTY_PRIVATE_KEY);
		    if(!Strings.isEmpty(base64PrivateKey)){
		        this.privateKey = RSA.decodePrivateKey(base64PrivateKey);
		    }
		}
		
		//load datasource form properties
		new DataSourceConfigPropertiesLoader(properties, dataSourceConfigs).load();
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
	
	protected void loadResources(Map<String,Resource> urlResourceMap,DefaultAppConfigLoader loader) throws IOException{
		for(String basePackage : loader.additionalPackages) {
			loadBasePackageResources(urlResourceMap, basePackage);
		}
        for(Resource resource : loader.getResources()){
        	urlResourceMap.put(resource.getURL().toExternalForm(), resource);
        }
	}
	
	protected static String[] profiled(String[] locations,String profile) {
		String[] array = new String[locations.length];
		
		for(int i=0;i<array.length;i++) {
			array[i] = Strings.replace(locations[i], "{profile}", profile);
		}
		
		return array;
	}
	
	protected DefaultAppConfigLoader load(Resource[] resources){
		DefaultAppConfigLoader loader = new DefaultAppConfigLoader(externalContext,properties,dataSourceConfigs,propertyProcessor);
		
		loader.extensions.putAll(this.extensions);
		
		loader.load(profile, resources);
		
		this.extensions.putAll(loader.extensions);
		
		return loader;
	}
	
	protected String loadOrGenerateSecret() {
		FileResource userDir   = Resources.createFileResource(System.getProperty("user.dir"));
		FileResource targetDir = userDir.createRelative("./target");
		
		File secretFile = targetDir.exists() ? targetDir.createRelative("./.secret").getFile() : userDir.createRelative("./.secret").getFile();
		
		if(secretFile.exists()) {
			String secret = Strings.trim(IO.readString(secretFile, Charsets.UTF_8));
			if(!Strings.isEmpty(secret)) {
				return secret;
			}
		}
		
		String secret = Randoms.nextString(32);
		IO.writeString(secretFile, secret, Charsets.UTF_8);
		return secret;
	}
	
	protected PrivateKey loadOrGeneratePrivateKey() {
        FileResource userDir   = Resources.createFileResource(System.getProperty("user.dir"));
        FileResource targetDir = userDir.createRelative("./target");
        
        File keyFile = targetDir.exists() ? targetDir.createRelative("./.rsa_key").getFile() : userDir.createRelative("./.rsa_key").getFile();
	    
        /*
        #base64 rsa private key#
        
        ...
        
        #base64 rsa public key#
        
        ...
        
         */
        
        if(keyFile.exists()) {
            String keyContent = Strings.trim(IO.readString(keyFile, Charsets.UTF_8));
            if(!Strings.isEmpty(keyContent)) {
                int index0 = keyContent.indexOf('#',1) + 1;
                int index1 = keyContent.indexOf('#',index0);
                
                String base64PrivateKey = keyContent.substring(index0, index1).trim();
                return RSA.decodePrivateKey(base64PrivateKey);
            }
        }
        
        RsaKeyPair kp = RSA.generateKeyPair();
        StringBuilder content = new StringBuilder();
        content.append("#base64 rsa private key#\n")
               .append(kp.getBase64PrivateKey())
               .append("\n\n")
               .append("#base64 rsa public key#\n")
               .append(kp.getBase64PublicKey());

        IO.writeString(keyFile, content.toString(), Charsets.UTF_8);
        
	    return kp.getPrivateKey();
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
			
			for(Entry<String, String> entry : properties.entrySet()) {
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
				for(Entry<String, DataSourceConfig.Builder> entry : dsMap.entrySet()) {
					dataSourceConfigs.put(entry.getKey(), entry.getValue().build());
				}
			}
		}
	}
	
	protected final class DefaultPropertyProcessor implements AppPropertyProcessor {
		
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
}