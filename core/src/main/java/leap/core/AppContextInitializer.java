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

import leap.core.sys.DefaultSysSecurity;
import leap.core.sys.SysContext;
import leap.lang.Classes;
import leap.lang.Exceptions;
import leap.lang.Factory;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class AppContextInitializer {
	
	private static final Log log = LogFactory.get(AppContextInitializer.class);
	
	private static ThreadLocal<AppConfig> initialAppConfig;
	private static boolean				  initializing;

    public static AppConfig getInitialConfig() {
        return null == initialAppConfig ? null : initialAppConfig.get();
    }

	public static void initStandalone(){
		initStandalone(null);
	}

    public static AppContext newStandalone(){
        return initStandalone(null,true);
    }

	public static void initStandalone(BeanFactory externalAppFactory){
		initStandalone(externalAppFactory, true);
	}

	protected static synchronized AppContext initStandalone(BeanFactory externalAppFactory,boolean createNew){
		if(initializing){
			return null;
		}
		if(!createNew && AppContext.tryGetCurrent() != null){
			throw new IllegalStateException("App context already initialized");
		}
        AppConfig config = null;
		try{
			initializing = true;
			initialAppConfig = new InheritableThreadLocal<>();

			log.debug("Starting standalone app...");
            AppConfigSource cs = Factory.newInstance(AppConfigSource.class);
			config = cs.loadConfig(null, null);

			initialAppConfig.set(config);
            ClassLoader parent = Classes.getClassLoader(AppContextInitializer.class);
            ClassLoader ctxCl  = Thread.currentThread().getContextClassLoader();
            AppClassLoader appCl  = AppClassLoader.init(parent);
            Thread.currentThread().setContextClassLoader(appCl);
            appCl.load(config);
            try{
                BeanFactoryInternal factory = createStandaloneAppFactory(config,externalAppFactory);

                //register beans
                cs.registerBeans(config, factory);

                AppContext context = new AppContext(config, factory, null);

                initSysContext(context,config);

                AppContext.setStandalone(context);
                RequestContext.setStandalone(new StandaloneRequestContext());

                factory.load(context);

                onInited(context);

                log.debug("Standalone app started!!!\n\n");

                return context;
            }finally {
                appCl.done();
                Thread.currentThread().setContextClassLoader(ctxCl);
            }
		}finally{
			initialAppConfig.remove();
			initialAppConfig = null;
			initializing = false;

            if(null != config) {
                AppResources.destroy(config);
            }
		}
	}

    public static synchronized void initExternal(Object externalContext,
                                                 Function<AppConfig, BeanFactory> beanFactoryCreator,
                                                 Consumer<AppContext> callback,
                                                 Map<String, String> initProperties) {

        if(initializing){
			return;
		}
		
		if(AppContext.tryGetCurrent() != null){
			throw new IllegalStateException("App context already initialized");
		}

        AppConfig config = null;
		try{
			initializing     = true;
			initialAppConfig = new InheritableThreadLocal<>();

			//log.info("Initializing app context");
            AppConfigSource cs = Factory.newInstance(AppConfigSource.class);
            config = cs.loadConfig(externalContext, initProperties);

            initialAppConfig.set(config);
            ClassLoader parent = Classes.getClassLoader(AppContextInitializer.class);
            ClassLoader ctxCl  = Thread.currentThread().getContextClassLoader();
            AppClassLoader appCl  = AppClassLoader.init(parent);
            Thread.currentThread().setContextClassLoader(appCl);
            appCl.load(config);
            try {
                BeanFactory factory = beanFactoryCreator.apply(config);

                //register bean
                cs.registerBeans(config, factory);

                AppContext context = new AppContext(config, factory, externalContext);

                initSysContext(context, config);

                AppContext.setCurrent(context);

                callback.accept(context);

                onInited(context);
            }finally {
                appCl.done();
                Thread.currentThread().setContextClassLoader(ctxCl);
            }
		}finally{
			initialAppConfig.remove();
			initialAppConfig = null;
			initializing   = false;

            if(null != config) {
                AppResources.destroy(config);
            }
		}
	}
	
	protected static BeanFactoryInternal createStandaloneAppFactory(AppConfig config,BeanFactory externalAppFactory){
		BeanFactoryInternal factory = Factory.newInstance(BeanFactoryInternal.class);
        factory.init(config,externalAppFactory);
        return factory;
	}
	
	protected static void initSysContext(AppContext appContext,AppConfig config){
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