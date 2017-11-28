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
package leap.web;

import leap.core.*;
import leap.core.ioc.*;
import leap.core.web.ServletContextInitializerBase;
import leap.lang.Classes;
import leap.lang.beans.BeanCreationException;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.reflect.Reflection;
import leap.lang.servlet.Servlets;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AppBootstrap extends ServletContextInitializerBase implements ServletContextListener {
    private static final Log log = LogFactory.get(AppBootstrap.class);
	
	public static final String GLOBAL_CLASS_NAME   = "Global";
	public static final String APP_ATTR_NAME       = App.class.getName();
    public static final String BOOTSTRAP_ATTR_NAME = AppBootstrap.class.getName();

    protected String               basePath;
    protected App                  app;
    protected ServletContext       servletContext;
    protected AppHandler           handler;
    protected boolean              selfStarted;
    protected List<BeanDefinition> bootables = new ArrayList<>();

	private Object _token;

    public static AppBootstrap get(ServletContext sc) throws ObjectNotFoundException {
        AppBootstrap b = (AppBootstrap) sc.getAttribute(BOOTSTRAP_ATTR_NAME);
        if(null == b){
            throw new ObjectNotFoundException("AppBootstrap not exists in the given ServletContext");
        }
        return b;
    }

    public static AppBootstrap tryGet(ServletContext sc) {
		return (AppBootstrap)sc.getAttribute(BOOTSTRAP_ATTR_NAME);
	}

	public static App getApp(ServletContext sc) throws ObjectNotFoundException{
		App c = (App)sc.getAttribute(APP_ATTR_NAME);
		if(null == c){
			throw new ObjectNotFoundException("App not exists in the given ServletContext");
		}
		return c;
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();

        try {
            bootApplication(sc, Servlets.getInitParamsMap(sc));

            startApplication();

            selfStarted = true;
        } catch (ServletException e) {
            throw new AppInitException("Error booting app, " + e.getMessage(), e);
        }
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
        stopApplication();
	}

    public boolean isSelfStarted() {
        return selfStarted;
    }

	public String getBasePath() {
		return basePath;
	}
	
	public App getApp() {
		return app;
	}
	
	public AppHandler getAppHandler() {
		return handler;
	}
	
	public AppContext getAppContext() {
		return appContext;
	}
	
	public AppConfig getAppConfig() {
		return null != appContext ? appContext.getConfig() : null;
	}
	
	public BeanFactory getBeanFactory() {
		return beanFactory;
	}
	
	protected final void bootApplication(final ServletContext sc,final Map<String,String> initParams) throws ServletException {
		try {
            this.servletContext = sc;

            sc.setAttribute(AppBootstrap.class.getName(), this);

		    log.info("Booting app '{}'...", getAppDisplayName(sc));

            preBooting();
		    
			super.initAppContext(sc,initParams);

            for(BeanDefinition bootable : bootables) {
                beanFactory.tryInitBean(bootable);
                ((AppBootable)bootable.getSingletonInstance()).onAppBooting(app,sc);
            }

            postBooting();
        } catch (Throwable e) {
        	if(e instanceof RuntimeException){
        		throw (RuntimeException)e;
        	}
        	throw new ServletException("Error booting application, message : " + e.getMessage(),e);
        }
	}

    protected void preBooting() {

    }

    protected void postBooting() {

    }
	
	@Override
    protected void onAppConfigReady(AppConfig config,Map<String, String> initParams) {
		this.basePath = initParams.getOrDefault(App.INIT_PARAM_BASE_PATH, App.DEFAULT_BASE_PATH);
		
		this.app = scanGlobalObject(servletContext, config);
		if(null == app){
			app = new App();
		}
		
		app.onConfigReady(config, servletContext, basePath);
		servletContext.setAttribute(APP_ATTR_NAME, app);
	}
	
	@Override
	protected void onBeanFactoryReady(BeanFactory factory) {
		app.onBeanFactoryReady(factory);
	}
	
	@Override
    protected void onAppContextReady(AppContext context) {
		beanFactory.addBean(App.class, app, true);
		beanFactory.addBean(ServletContext.class, servletContext, true);
		
		beanFactory.inject(app);
		
		if(app.getClass() != App.class){
			beanFactory.addBean(app);	
		}
		
		app.onContextReady(context);
		this.handler = beanFactory.getBean(AppHandler.class);
		try {
	        this.handler.initApp();
        } catch (Throwable e) {
        	if(e instanceof RuntimeException){
        		throw (RuntimeException)e;
        	}
        	throw new AppException("Error init app, " + e.getMessage(), e);
        }
    }

	protected final void startApplication() throws ServletException {
        this._token = handler.startApp();
	}
	
	protected final void stopApplication(){
		try{
			AppContext.setCurrent(appContext);
			if(null != handler && null != _token){
				handler.stopApp(_token);	
				servletContext.removeAttribute(APP_ATTR_NAME);
			}

            for(BeanDefinition bootable : bootables) {
                try{
                    AppBootable bean = ((AppBootable)bootable.getSingletonInstance());
                    if(null != bean) {
                        bean.onAppStopped(app, servletContext);
                    }
                }catch(Throwable e) {
                    log.warn("Error invoke onAppStopped on bootable bean, {}", e.getMessage(), e);
                }
            }
		}finally{
			super.destroyAppContext();
		}
	}
	
    protected App scanGlobalObject(ServletContext sc, AppConfig config) {
    	String   globalClassName = config.getBasePackage() + "." + GLOBAL_CLASS_NAME;
    	Class<?> globalClass     = Classes.tryForName(Thread.currentThread().getContextClassLoader(), globalClassName);
    	if(null != globalClass && App.class.isAssignableFrom(globalClass)){
    		return (App)Reflection.newInstance(globalClass);
    	}else {
    		return null;
    	}
    }
	
    protected static String getAppDisplayName(ServletContext sc){
        String path = sc.getContextPath();
        
        if("".equals(path)){
            return "ROOT";
        }else{
            return path;
        }
    }
    
	public static final class AppBeanProcessor implements BeanProcessor,ServletOnlyBean {

        @Override
        public void postInitBean(AppContext context, BeanFactory factory, BeanDefinitionConfigurator c) throws Throwable {
            BeanDefinition bd = c.definition();

            boolean initializable = AppInitializable.class.isAssignableFrom(bd.getBeanClass());
            boolean bootable      = AppBootable.class.isAssignableFrom(bd.getBeanClass());

            if(initializable || bootable) {
                ServletContext sc = context.tryGetServletContext();
                if(null == sc) {
                    throw new BeanDefinitionException("Current app context must be servlet environment, cannot init bean " + bd);
                }

                if(!bd.isSingleton()) {
                    throw new BeanDefinitionException("Bean must be singleton, check the bean " + bd);
                }

                if(initializable) {
                    app(sc,bd).initializableBeans().add(bd);
                }

                if(bootable) {
                    get(sc).bootables.add(bd);
                }
            }
        }

        @Override
        public void postCreateBean(AppContext appContext, BeanFactory beanFactory, BeanDefinition definition, Object bean) throws Exception {
			ServletContext sc = appContext.tryGetServletContext();
			if(bean instanceof AppAware){
				if(null == sc){
					throw new BeanCreationException("Current app context must be servlet environment, cannot create bean " + definition);
				}
				((AppAware) bean).setApp(app(sc, definition));
			}
        }
		
		protected App app(ServletContext sc, BeanDefinition bd) {
            App app = (App)sc.getAttribute(APP_ATTR_NAME);
            if(null == app){
                throw new BeanCreationException("App not ready yet, cannot create bean " + bd);
            }
            return app;
		}
	}
}