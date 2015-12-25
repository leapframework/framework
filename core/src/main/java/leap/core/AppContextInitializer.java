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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import leap.core.sys.DefaultSysSecurity;
import leap.core.sys.SysContext;
import leap.lang.Exceptions;
import leap.lang.Factory;
import leap.lang.Strings;
import leap.lang.accessor.AttributeAccessor;
import leap.lang.accessor.MapAttributeAccessor;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;

public class AppContextInitializer {
	
	private static final Log log = LogFactory.get(AppContextInitializer.class);
	
	private static ThreadLocal<ResourceSet> initialContext;
	private static ThreadLocal<AppConfig>   initialAppConfig;
	private static boolean					initializing;
	
	public static final String CP_FRAMEWORK_CORE_PREFIX    = "/META-INF/leap/core";
	public static final String CP_FRAMEWORK_MODULES_PREFIX = "/META-INF/leap/framework";
	public static final String CP_EXTENSION_MODULES_PREFIX = "/META-INF/leap/extension";
	public static final String CP_APPLICATION_SYS_PREFIX   = "/META-INF/conf";
	public static final String CP_APPLICATION_USR_PREFIX   = "/conf";
	
	private static final String XML_EXT = ".xml";

	private static final String[] FRAMEWORK_CORE_LOCATIONS = new String[]{CP_FRAMEWORK_CORE_PREFIX + "/{0}{1}",
																		  CP_FRAMEWORK_CORE_PREFIX + "/{0}/**/*{1}"};
	
	private static final String[] FRAMEWORK_TEMPLATE_LOCATIONS = new String[]{CP_FRAMEWORK_MODULES_PREFIX + "/{0}{1}",
																			  CP_FRAMEWORK_MODULES_PREFIX + "/{0}/**/*{1}"};
	
	private static final String[] EXTENSION_TEMPLATE_LOCATIONS = new String[]{CP_EXTENSION_MODULES_PREFIX + "/{0}{1}",
																			  CP_EXTENSION_MODULES_PREFIX + "/{0}/**/*{1}"};

	private static final String[] APPSYS_TEMPLATE_LOCATIONS = new String[]{CP_APPLICATION_SYS_PREFIX + "/{0}{1}",
																		   CP_APPLICATION_SYS_PREFIX + "/{0}/**/*{1}"};

	private static final String[] APPUSR_TEMPLATE_LOCATIONS = new String[]{CP_APPLICATION_USR_PREFIX + "/{0}{1}",
		   																   CP_APPLICATION_USR_PREFIX + "/{0}/**/*{1}"};		

	private static final String[] FRAMEWORK_CORE_LOCATIONS_BY_PATTERN = new String[]{CP_FRAMEWORK_CORE_PREFIX + "/{0}{1}"};
	
	private static final String[] FRAMEWORK_TEMPLATE_LOCATIONS_BY_PATTERN = new String[]{CP_FRAMEWORK_MODULES_PREFIX + "/{0}{1}"};	
	
	private static final String[] EXTENSION_TEMPLATE_LOCATIONS_BY_PATTERN = new String[]{CP_EXTENSION_MODULES_PREFIX + "/{0}{1}"};	
	
	private static final String[] APPSYS_TEMPLATE_LOCATIONS_BY_PATTERN = new String[]{CP_APPLICATION_SYS_PREFIX + "/{0}{1}"};
	
	private static final String[] APPUSR_TEMPLATE_LOCATIONS_BY_PATTERN = new String[]{CP_APPLICATION_USR_PREFIX + "/{0}{1}"};	
	
	private static final String RES_FRAMEWORK_CORE_LOCATION    = Strings.format("classpath*:{0}/**/*",CP_FRAMEWORK_CORE_PREFIX);
	private static final String RES_FRAMEWORK_MODULES_LOCATION = Strings.format("classpath*:{0}/**/*",CP_FRAMEWORK_MODULES_PREFIX);
	private static final String RES_EXTENSION_MODULES_LOCATION = Strings.format("classpath*:{0}/**/*",CP_EXTENSION_MODULES_PREFIX);
	private static final String RES_APPLICATION_SYS_LOCATION   = Strings.format("classpath*:{0}/**/*",CP_APPLICATION_SYS_PREFIX);
	private static final String RES_APPLICATION_USR_LOCATION   = Strings.format("classpath:{0}/**/*", CP_APPLICATION_USR_PREFIX);
	
	public static boolean isFrameworkResource(String url) {
	    return url.contains(CP_FRAMEWORK_CORE_PREFIX) || url.contains(CP_FRAMEWORK_MODULES_PREFIX);
	}
	
	public static ResourceSet resources() throws IllegalStateException {
		if(initialContext != null){
			ResourceSet resources = initialContext.get();
			
			if(null == resources){
				resources = Resources.scan(RES_FRAMEWORK_CORE_LOCATION,
										   RES_FRAMEWORK_MODULES_LOCATION,
										   RES_EXTENSION_MODULES_LOCATION,
										   RES_APPLICATION_SYS_LOCATION,
										   RES_APPLICATION_USR_LOCATION);
				initialContext.set(resources);
			}
			return resources;
			
		}else{
			return Resources.scan(RES_FRAMEWORK_CORE_LOCATION,
					   			  RES_FRAMEWORK_MODULES_LOCATION,
					   			  RES_EXTENSION_MODULES_LOCATION,
					   			  RES_APPLICATION_SYS_LOCATION,
					   			  RES_APPLICATION_USR_LOCATION);
		}
	}
	
	public static Resource getClasspathDirectoryForAppUsr(String name) {
		return Resources.getResource("classpath:" + CP_APPLICATION_USR_PREFIX + "/" + name);
	}
	
	public static Resource[] searchClasspathXmlResourcesForFramework(String xmlResourceName){
		return searchClasspathResourcesForFramework(xmlResourceName, XML_EXT);
	}
	
	public static Resource[] searchClasspathResourcesForFramework(String xmlResourceName,String ext){
		List<Resource> list = new ArrayList<Resource>();
		
		ResourceSet rs = resources();
		
		searchClassPathResources(list, FRAMEWORK_CORE_LOCATIONS, 	 rs, xmlResourceName, ext);
		searchClassPathResources(list, FRAMEWORK_TEMPLATE_LOCATIONS, rs, xmlResourceName, ext);
		searchClassPathResources(list, EXTENSION_TEMPLATE_LOCATIONS, rs, xmlResourceName, ext);
		searchClassPathResources(list, APPSYS_TEMPLATE_LOCATIONS,    rs, xmlResourceName, ext);
		searchClassPathResources(list, APPUSR_TEMPLATE_LOCATIONS,    rs, xmlResourceName, ext);
		
		return list.toArray(new Resource[list.size()]);
	}
	
	public static Resource[] searchClasspathXmlResourcesForFrameworkAndExtOnly(String xmlResourceName){
		return searchClasspathResourcesForFrameworkAndExtOnly(xmlResourceName, XML_EXT);
	}
	
	public static Resource[] searchClasspathResourcesForFrameworkAndExtOnly(String xmlResourceName,String ext){
		List<Resource> list = new ArrayList<Resource>();
		
		ResourceSet rs = resources();
		
		searchClassPathResources(list, FRAMEWORK_CORE_LOCATIONS, 	 rs, xmlResourceName, ext);
		searchClassPathResources(list, FRAMEWORK_TEMPLATE_LOCATIONS, rs, xmlResourceName, ext);
		searchClassPathResources(list, EXTENSION_TEMPLATE_LOCATIONS, rs, xmlResourceName, ext);
		
		return list.toArray(new Resource[list.size()]);
	}
	
	public static Resource[] searchClasspathXmlResourcesForAppOnly(String xmlResourceName){
		return searchClasspathResourcesForFrameworkAndExtOnly(xmlResourceName, XML_EXT);
	}
	
	public static Resource[] searchClasspathResourcesForAppOnly(String xmlResourceName,String ext){
		List<Resource> list = new ArrayList<Resource>();
		
		ResourceSet rs = resources();
		
		searchClassPathResources(list, APPSYS_TEMPLATE_LOCATIONS,    rs, xmlResourceName, ext);
		searchClassPathResources(list, APPUSR_TEMPLATE_LOCATIONS,    rs, xmlResourceName, ext);
		
		return list.toArray(new Resource[list.size()]);
	}
	
	public static Resource[] searchClasspathXmlResourcesForAppSysOnly(String xmlResourceName){
		return searchClasspathResourcesForAppSysOnly(xmlResourceName, XML_EXT);
	}
	
	public static Resource[] searchClasspathResourcesForAppSysOnly(String xmlResourceName,String ext){
		List<Resource> list = new ArrayList<Resource>();
		
		ResourceSet rs = resources();
		
		searchClassPathResources(list, APPSYS_TEMPLATE_LOCATIONS,    rs, xmlResourceName, ext);
		
		return list.toArray(new Resource[list.size()]);
	}
	
	public static Resource[] searchClasspathXmlResourcesForAppUsrOnly(String xmlResourceName){
		return searchClasspathResourcesForAppUsrOnly(xmlResourceName, XML_EXT);
	}
	
	public static Resource[] searchClasspathResourcesForAppUsrOnly(String xmlResourceName,String ext){
		List<Resource> list = new ArrayList<Resource>();
		
		ResourceSet rs = resources();
		
		searchClassPathResources(list, APPUSR_TEMPLATE_LOCATIONS,    rs, xmlResourceName, ext);
		
		return list.toArray(new Resource[list.size()]);
	}
	
	public static Resource[] searchClasspathXmlResourcesForFrameworkByPattern(String xmlResourcePattern){
		return searchClasspathResourcesForFrameworkByPattern(xmlResourcePattern, XML_EXT);
	}
	
	public static Resource[] searchClasspathResourcesForFrameworkByPattern(String xmlResourcePattern, String ext){
		List<Resource> list = new ArrayList<Resource>();
		
		ResourceSet rs = resources();
		
		searchClassPathResources(list, FRAMEWORK_CORE_LOCATIONS_BY_PATTERN,     rs, xmlResourcePattern, ext);
		searchClassPathResources(list, FRAMEWORK_TEMPLATE_LOCATIONS_BY_PATTERN, rs, xmlResourcePattern, ext);
		searchClassPathResources(list, EXTENSION_TEMPLATE_LOCATIONS_BY_PATTERN, rs, xmlResourcePattern, ext);
		searchClassPathResources(list, APPSYS_TEMPLATE_LOCATIONS_BY_PATTERN,    rs, xmlResourcePattern, ext);
		searchClassPathResources(list, APPUSR_TEMPLATE_LOCATIONS_BY_PATTERN,    rs, xmlResourcePattern, ext);
		
		return list.toArray(new Resource[list.size()]);
	}
	
	public static Resource[] searchClasspathXmlResourcesForExtension(String xmlResourceName){
		return searchClasspathResourcesForExtension(xmlResourceName,XML_EXT);
	}
	
	public static Resource[] searchClasspathResourcesForExtension(String xmlResourceName,String ext){
		List<Resource> list = new ArrayList<Resource>();
		
		ResourceSet rs = resources();
		
		searchClassPathResources(list, EXTENSION_TEMPLATE_LOCATIONS, rs, xmlResourceName, ext);
		searchClassPathResources(list, APPSYS_TEMPLATE_LOCATIONS,    rs, xmlResourceName, ext);
		searchClassPathResources(list, APPUSR_TEMPLATE_LOCATIONS,    rs, xmlResourceName, ext);
		
		return list.toArray(new Resource[list.size()]);
	}

	public static Resource[] searchClasspathXmlResourcesForExtensionByPattern(String xmlResourcePattern){
		return searchClasspathResourcesForExtensionByPattern(xmlResourcePattern,XML_EXT);
	}
	
	public static Resource[] searchClasspathResourcesForExtensionByPattern(String xmlResourcePattern, String ext){
		List<Resource> list = new ArrayList<Resource>();
		
		ResourceSet rs = resources();
		
		searchClassPathResources(list, EXTENSION_TEMPLATE_LOCATIONS_BY_PATTERN, rs, xmlResourcePattern, ext);
		searchClassPathResources(list, APPSYS_TEMPLATE_LOCATIONS_BY_PATTERN,    rs, xmlResourcePattern, ext);
		searchClassPathResources(list, APPUSR_TEMPLATE_LOCATIONS_BY_PATTERN,    rs, xmlResourcePattern, ext);
		
		return list.toArray(new Resource[list.size()]);
	}
	
	public static void loadClasspathXmlResourcesForFramework(String xmlResourceName,Consumer<Resource> processor) {
		for(Resource resource : searchClasspathXmlResourcesForFramework(xmlResourceName)){
			processor.accept(resource);
		}
	}
	
	public static void loadClasspathXmlResourcesForExtension(String xmlResourceName,Consumer<Resource> processor) {
		for(Resource resource : searchClasspathXmlResourcesForExtension(xmlResourceName)){
			processor.accept(resource);
		}
	}
	
	private static void searchClassPathResources(List<Resource> list,
												 String[] templateLocations, 
												 ResourceSet rs, 
												 String resourceName,
												 String ext){
		
		String[] locations = new String[templateLocations.length];
		
		for(int i=0; i<locations.length; i++){
			locations[i] = Strings.format(templateLocations[i],resourceName,ext);
		}
		
		Resource[] resources = rs.searchClasspaths(locations);
		for(Resource resource : resources){
			list.add(resource);
		}
	}
	
	public static void initStandalone(){
		initStandalone(null);
	}
	
	public static void initStandalone(BeanFactory externalAppFactory){
		initStandalone(externalAppFactory, null);
	}
	
	public static synchronized void initStandalone(BeanFactory externalAppFactory,Map<String, String> initProperties){
		if(initializing){
			return;
		}
		if(AppContext.tryGetCurrent() != null){
			throw new IllegalStateException("App context aleady initialized");
		}
		try{
			initializing     = true;
			initialContext   = new InheritableThreadLocal<ResourceSet>();
			initialAppConfig = new InheritableThreadLocal<AppConfig>();
			
			MapAttributeAccessor attrs = new MapAttributeAccessor();
			
			log.debug("Initializing standalone app context...");
			
			DefaultAppConfig config = loadDefaultAppConfig(attrs, null, initProperties);
			
			initialAppConfig.set(config);
			
			DefaultBeanFactory factory  = createStandaloneAppFactory(config,externalAppFactory);
			
			//register bean 
			factory.setPrimaryBean(AppConfig.class, config);
			
			AppContext context = new AppContext(attrs.map(), config, factory, null);
			
			initSysContext(context,config);
			
			AppContext.setStandalone(context);
			RequestContext.setStandalone(new StandaloneRequestContext());
			
			factory.load(context);
		
			onInited(context);
			
			log.debug("Standalone app context initialized");
		}finally{
			initialContext.remove();
			initialContext = null;
			initialAppConfig.remove();
			initialAppConfig = null;
			initializing = false;
		}
	}
	
	public static void initExternal(Object externalContext,
									Function<AppConfig,BeanFactory> beanFactoryCreator,
									Consumer<AppContext> callback){
		
		initExternal(externalContext, beanFactoryCreator, callback, null);
	}

	public static synchronized void initExternal(Object 					     externalContext,
										  		 Function<AppConfig,BeanFactory> beanFactoryCreator,
										  		 Consumer<AppContext> 		 	 callback,
										  		 Map<String, String>          	 initProperties){
		
		if(initializing){
			return;
		}
		
		if(AppContext.tryGetCurrent() != null){
			throw new IllegalStateException("App context aleady initialized");
		}
		
		try{
			initializing     = true;
			initialContext   = new InheritableThreadLocal<ResourceSet>();
			initialAppConfig = new InheritableThreadLocal<AppConfig>();
			
			MapAttributeAccessor attrs = new MapAttributeAccessor();
			
			//log.info("Initializing app context");
			
			DefaultAppConfig config = loadDefaultAppConfig(attrs, externalContext, initProperties);
			
			initialAppConfig.set(config);
			
			BeanFactory factory = beanFactoryCreator.apply(config);
			
			//register bean
			factory.setPrimaryBean(AppConfig.class, config);
			
			AppContext context = new AppContext(attrs.map(),config, factory, externalContext);
			
			initSysContext(context,config);
			
			AppContext.setCurrent(context);
			
			callback.accept(context);
			
			onInited(context);
		}finally{
			initialContext.remove();
			initialContext = null;
			initialAppConfig.remove();
			initialAppConfig = null;
			initializing   = false;
		}
	}
	
	protected static DefaultAppConfig loadDefaultAppConfig(AttributeAccessor attrs, Object externalContext, Map<String, String> initProperties){
		DefaultAppConfig config = new DefaultAppConfig(externalContext, initProperties).load();
		
		for(AppConfigInitializable o : Factory.newInstances(AppConfigInitializable.class)){
			try {
	            o.postInit(attrs, config);
            } catch (Throwable e) {	
            	throw new AppInitException("Error calling object '" + o + "', " + e.getMessage(), e);
            }
		}
		
		return config;
	}
	
	protected static DefaultBeanFactory createStandaloneAppFactory(AppConfig config,BeanFactory externalAppFactory){
		return new DefaultBeanFactory(config,externalAppFactory);
	}
	
	protected static void initSysContext(AppContext appContext,DefaultAppConfig config){
		appContext.setAttribute(SysContext.SYS_CONTEXT_ATTRIBUTE_KEY, new SysContext(new DefaultSysSecurity(config)));
	}
	
	protected static void onInited(AppContext context){
		try {
			context.postInit();
			
			for(AppContextInitializable bean : context.getBeanFactory().getBeans(AppContextInitializable.class)){
				bean.postInit(context);
			}
			
			context.getBeanFactory().postInit(context);
        } catch (Throwable e) {
        	Exceptions.uncheckAndThrow(e);
        }
	}
	
	private AppContextInitializer(){
		
	}
}