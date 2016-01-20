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

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import leap.core.AppConfig;
import leap.core.AppContext;
import leap.core.AppException;
import leap.core.BeanFactory;
import leap.core.ioc.*;
import leap.core.web.ServletContextInitializerBase;
import leap.lang.Classes;
import leap.lang.beans.BeanCreationException;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.reflect.Reflection;

public class AppBootstrap extends ServletContextInitializerBase {
    private static final Log log = LogFactory.get(AppBootstrap.class);
	
	public static final String GLOBAL_CLASS_NAME  = "Global";
	public static final String APP_ATTRIBUTE_NAME = App.class.getName(); 
	
	protected String	 basePath;
	protected App        app;
	protected AppHandler handler;
	
	private Object _token;
	
	public static AppBootstrap tryGet(ServletContext sc) {
		return (AppBootstrap)sc.getAttribute(AppBootstrap.class.getName());
	}

	public static App getApplication(ServletContext sc) throws ObjectNotFoundException{
		App c = (App)sc.getAttribute(APP_ATTRIBUTE_NAME);
		if(null == c){
			throw new ObjectNotFoundException("Application not exists in the given ServletContext");
		}
		return c;
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
		return appContext.getConfig();
	}
	
	public BeanFactory getBeanFactory() {
		return beanFactory;
	}
	
	protected final void bootApplication(final ServletContext sc,final Map<String,String> initParams) throws ServletException {
		try {
		    log.info("Booting app '{}'...", getAppDisplayName(sc));
		    
			super.initAppContext(sc,initParams);
			
			for(AppServletContainerInitializer initializer : beanFactory.getBeans(AppServletContainerInitializer.class)) {
				initializer.onStartup(sc, app);
			}
			
			sc.setAttribute(AppBootstrap.class.getName(), this);
        } catch (Throwable e) {
        	if(e instanceof RuntimeException){
        		throw (RuntimeException)e;
        	}
        	throw new ServletException("Error booting application, message : " + e.getMessage(),e);
        }
	}
	
	@Override
    protected void onAppConfigReady(AppConfig config,Map<String, String> initParams) {
		this.basePath = initParams.getOrDefault(App.INIT_PARAM_BASE_PATH, App.DEFAULT_BASE_PATH);
		
		this.app = scanGlobalObject(servletContext, config);
		if(null == app){
			app = new App();
		}
		
		app.onConfigReady(config, servletContext, basePath);
		servletContext.setAttribute(APP_ATTRIBUTE_NAME, app);
	}
	
	@Override
    protected void onAppContexReady(AppContext context) {
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
				servletContext.removeAttribute(APP_ATTRIBUTE_NAME);
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
            if(AppInitializable.class.isAssignableFrom(bd.getBeanClass())) {
                ServletContext sc = context.tryGetServletContext();
                if(null == sc) {
                    throw new BeanDefinitionException("Current app context must be servlet environment, cannot init bean " + bd);
                }

                if(!bd.isSingleton()) {
                    throw new BeanDefinitionException("AppInitializable bean must be singleton, check the bean " + bd);
                }

                app(sc,bd).initializableBeans().add(bd);
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
            App app = (App)sc.getAttribute(APP_ATTRIBUTE_NAME);
            if(null == app){
                throw new BeanCreationException("App not ready yet, cannot create bean " + bd);
            }
            return app;
		}
	}
}