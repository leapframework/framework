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
import leap.lang.annotation.Internal;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Internal
public class AppContextInitializer {

    private static final Log log = LogFactory.get(AppContextInitializer.class);

    private static ThreadLocal<AppConfig> initialAppConfig;
    private static boolean                initializing;
    private static boolean                testing;
    private static boolean                instrumentDisabled;
    private static String                 basePackage;
    private static AppClassLoaderGetter   classLoaderGetter = Factory.tryNewInstance(AppClassLoaderGetter.class);

    public static String getBasePackage() {
        return basePackage;
    }

    public static void setBasePackage(String basePackage) {
        AppContextInitializer.basePackage = basePackage;
    }

    public static void setInstrumentDisabled(boolean disabled) {
        instrumentDisabled = disabled;
    }

    public static boolean isTesting() {
        return testing;
    }

    public static void markTesting() {
        testing = true;
    }

    public static AppConfig getInitialConfig() {
        return null == initialAppConfig ? null : initialAppConfig.get();
    }

    public static AppContext newStandalone() {
        return initStandalone(null, null, true);
    }

    public static void initStandalone() {
        initStandalone(null, null, true);
    }

    public static void initStandalone(Map<String, String> props) {
        initStandalone(null, props, true);
    }

    public static void initStandalone(BeanFactory externalAppFactory) {
        initStandalone(externalAppFactory, null, true);
    }

    protected static ClassLoader getClassLoader() {
        ClassLoader current = Classes.getClassLoader(AppContextInitializer.class);
        return null == classLoaderGetter ? current : classLoaderGetter.getClassLoader(current);
    }

    protected static synchronized AppContext initStandalone(BeanFactory externalAppFactory, Map<String, String> props, boolean createNew) {
        if (initializing) {
            return null;
        }
        if (!createNew && AppContext.tryGetCurrent() != null) {
            throw new IllegalStateException("App context already initialized");
        }
        AppConfig config = null;
        try {
            initializing = true;
            initialAppConfig = new InheritableThreadLocal<>();

            log.debug("Starting standalone app...");
            AppConfigSource cs = Factory.newInstance(AppConfigSource.class);
            config = cs.loadConfig(null, props);

            initialAppConfig.set(config);

            ClassLoader    ctxCl = Thread.currentThread().getContextClassLoader();
            AppClassLoader appCl = null;
            if (!instrumentDisabled) {
                ClassLoader parent = getClassLoader();
                appCl = AppClassLoader.init(parent);
                Thread.currentThread().setContextClassLoader(appCl);
                appCl.load(config);
            }
            try {
                BeanFactoryInternal factory = createStandaloneAppFactory(config, externalAppFactory);

                //register beans
                cs.registerBeans(config, factory);

                AppContext context = new AppContext("app", config, factory, null);

                initSysContext(context, config);

                AppContext.setStandalone(context);
                RequestContext.setStandalone(new StandaloneRequestContext());

                factory.load(context);

                onInited(context);

                log.debug("Standalone app started!!!\n\n");

                return context;
            } finally {
                if (null != appCl) {
                    appCl.done();
                    Thread.currentThread().setContextClassLoader(ctxCl);
                }
            }
        } finally {
            initialAppConfig.remove();
            initialAppConfig = null;
            initializing = false;

            if (null != config) {
                AppResources.destroy(config);
            }
        }
    }

    public static synchronized void initExternal(Object externalContext,
                                                 String name,
                                                 Function<AppConfig, BeanFactory> newBeanFactory,
                                                 Consumer<AppContext> onAppContextCreated,
                                                 Consumer<AppContext> onAppContextinited,
                                                 Map<String, String> initProperties) {

        if (initializing) {
            return;
        }

        if (AppContext.tryGetCurrent() != null) {
            throw new IllegalStateException("App context already initialized");
        }

        AppConfig config = null;
        try {
            initializing = true;
            initialAppConfig = new InheritableThreadLocal<>();

            //log.info("Initializing app context");
            AppConfigSource cs = Factory.newInstance(AppConfigSource.class);
            config = cs.loadConfig(externalContext, initProperties);

            initialAppConfig.set(config);

            ClassLoader    ctxCl = Thread.currentThread().getContextClassLoader();
            AppClassLoader appCl = null;

            if (!instrumentDisabled) {
                ClassLoader parent = getClassLoader();
                appCl = AppClassLoader.init(parent);
                Thread.currentThread().setContextClassLoader(appCl);
                appCl.load(config);
            }
            try {
                BeanFactory factory = newBeanFactory.apply(config);

                //register bean
                cs.registerBeans(config, factory);

                AppContext context = new AppContext(name, config, factory, externalContext);

                initSysContext(context, config);

                AppContext.setCurrent(context);

                onAppContextCreated.accept(context);

                onInited(context);

                onAppContextinited.accept(context);
            } finally {
                if (null != appCl) {
                    appCl.done();
                    Thread.currentThread().setContextClassLoader(ctxCl);
                }
            }
        } finally {
            initialAppConfig.remove();
            initialAppConfig = null;
            initializing = false;

            if (null != config) {
                AppResources.destroy(config);
            }
        }
    }

    protected static BeanFactoryInternal createStandaloneAppFactory(AppConfig config, BeanFactory externalAppFactory) {
        BeanFactoryInternal factory = Factory.newInstance(BeanFactoryInternal.class);
        factory.init(config, externalAppFactory);
        return factory;
    }

    protected static void initSysContext(AppContext appContext, AppConfig config) {
        appContext.setAttribute(SysContext.SYS_CONTEXT_ATTRIBUTE_KEY, new SysContext(new DefaultSysSecurity(config)));
    }

    protected static void onInited(AppContext context) {
        try {
            context.postInit();

            for (AppContextInitializable bean : context.getBeanFactory().getBeans(AppContextInitializable.class)) {
                bean.postInit(context);
            }

            context.getBeanFactory().postInit(context);
        } catch (Throwable e) {
            Exceptions.uncheckAndThrow(e);
        }
    }

    private AppContextInitializer() {

    }
}