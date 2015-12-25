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

import leap.core.AppConfig;
import leap.core.AppContext;
import leap.core.AppHome;
import leap.core.BeanFactory;
import leap.core.i18n.MessageSource;
import leap.core.ioc.BeanList;
import leap.core.ioc.CopyOnWriteArrayBeanList;
import leap.lang.accessor.AttributeAccessor;
import leap.lang.annotation.Internal;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.resource.FileResource;
import leap.lang.servlet.ServletResource;
import leap.lang.servlet.Servlets;
import leap.web.config.WebConfig;
import leap.web.config.WebConfigurator;
import leap.web.config.WebInterceptors;
import leap.web.error.ErrorCodes;
import leap.web.error.ErrorViews;
import leap.web.route.Routes;

import javax.servlet.ServletContext;
import java.nio.charset.Charset;
import java.util.Locale;

public class App implements AttributeAccessor {
	
	public static final String INIT_PARAM_BASE_PATH    = "base-path";
	public static final String DEFAULT_BASE_PATH       = "";
	
	/**
	 * Returns the instance of {@link App} for the given servlet context.
	 */
	public static App get(ServletContext sc) {
		return AppBootstrap.getApplication(sc);
	}
	
	protected Log log = LogFactory.get(this.getClass());

	protected AppContext 	    context;
	protected AppHome			home;
	protected AppConfig	        config;
	protected BeanFactory	    factory;
	protected ServletContext    servletContext;	
	protected String            basePath;
	protected FileResource		baseDir;
	protected ServletResource   rootResource;
	
	private WebConfig			webConfig;
	private WebConfigurator	    webConfigurator;
	private Endpoint[]          endpoints;
	
	private final BeanList<AppInitializable> initializableBeans = new CopyOnWriteArrayBeanList<AppInitializable>();
	private final BeanList<AppListener>      listeners          = new CopyOnWriteArrayBeanList<>();
	
	public App(){

	}
	
	/**
	 * Returns the {@link AppContext} of current application.
	 */
	public final AppContext context() {
		return context;
	}
	
	/**
	 * Returns the {@link AppHome} of current application.
	 */
	public final AppHome home(){
		return home;
	}
	
	/**
	 * Returns the {@link AppConfig} of current application.
	 * 
	 * @see AppContext#getConfig()
	 */
	public final AppConfig config() {
		return config;
	}
	
	/**
	 * Returns the {@link BeanFactory} of current application.
	 * 
	 * @see AppContext#getBeanFactory()
	 */
	public final BeanFactory factory() {
		return factory;
	}
	
	/**
	 * Returns the {@link WebConfig} of current application.
	 */
	public final WebConfig getWebConfig() {
		if(null == webConfig) {
			webConfig = factory.getBean(WebConfig.class);
		}
		return webConfig;
	}
	
	/**
	 * Returns the configurator bean of {@link WebConfig} of current application.
	 */
	private final WebConfigurator getWebConfigurator() {
		if(null == webConfigurator){
			webConfigurator = factory.getBean(WebConfigurator.class);
		}
		return webConfigurator;
	}
	
	/**
	 * Returns the {@link ServletContext} of current application.
	 */
	public final ServletContext getServletContext() {
		return servletContext;
	}
	
	/**
     * Returns context path by calling {@link ServletContext#getContextPath()}.
     */
    public final String getContextPath(){
    	return servletContext.getContextPath();
    }
    
    /**
     * Reutrns a {@link ServletResource} represents the root resource path '/'.
     * 
     * @see Servlets#getRealPath(ServletContext, String).
     */
    public final ServletResource getRootResource() {
    	return rootResource;
    }
    
    /**
     * Returns the base directory of current application.
     * 
     * <p>
     * Returns <code>null</code> if current application not deploy as an unpacked directory.
     */
    public final FileResource getBaseDir() {
    	return baseDir;
    }
	
	/**
	 * Returns the base path of current application.
	 */
	public final String getBasePath(){
		return basePath;
	}
	
	/**
	 * Returns base package of current application.
	 * 
	 * @see AppConfig#getBasePackage()
	 */
	public final String getBasePackage() {
		return config.getBasePackage();
	}

	/**
	 * Returns default locale of current application.
	 * 
	 * @see AppConfig#getDefaultLocale()
	 */
	public final Locale getDefaultLocale() {
		return config.getDefaultLocale();
	}
	
	@Internal
	final BeanList<AppInitializable> initializableBeans() {
	    return initializableBeans;
	}
	
	/**
	 * Returns default charset of current application.
	 * 
	 * @see AppConfig#getDefaultCharset()
	 */
	public final Charset getDefaultCharset() {
		return config.getDefaultCharset();
	}
	
	/**
	 * Returns the {@link MessageSource} of current application.
	 * 
	 * @see AppContext#getMessageSource()
	 */
	public final MessageSource getMessageSource(){
		return context.getMessageSource();
	}
	
	public final Routes routes() {
		return getWebConfigurator().routes();
	}
	
	public final BeanList<AppListener> listeners() {
		return listeners;
	}
	
	public final FilterMappings filters(){
		return getWebConfigurator().filters();
	}
	
	public final WebInterceptors interceptors(){
		return getWebConfigurator().interceptors();
	}
	
	public final ErrorViews errorViews() {
		return getWebConfigurator().errorViews();
	}
	
	public final ErrorCodes errorCodes() {
		return getWebConfigurator().errorCodes();
	}
	
	@Override
    public final Object getAttribute(String name) {
	    return context.getAttribute(name);
    }

	@Override
    public final void setAttribute(String name, Object value) {
		context.setAttribute(name, value);
    }
	
	@Override
    public final void removeAttribute(String name) {
		context.removeAttribute(name);
    }
	
	@Internal
	final void onConfigReady(AppConfig config, ServletContext sc,String basePath){
		this.servletContext = sc;
		this.basePath       = basePath;
		this.config         = config;
		initConfig();
	}
	
	@Internal
	final void onContextReady(AppContext context){
		this.context = context;
		this.home    = context.getHome();
		this.factory = context.getBeanFactory();
		initBeans();
	}
	
	@Internal
	final void _configure() throws Throwable {
		this.notifyAppConfigure();
	}
	
	@Internal
	final void _init() throws Throwable {
		this.notifyAppInit();
	}
	
	@Internal
	final void _start() throws Throwable{
		notifyAppStart();
	}
	
	@Internal
	final void _end() throws Throwable{
		notifyAppStop();
	}
	
	private void initConfig() {
		this.rootResource = Servlets.getResource(servletContext, "/"); 
        this.baseDir      = rootResource.isFile() ? rootResource.toFileResource() : null;
	}
	
	private void initBeans() {
		this.listeners.addAll(factory.getBeans(AppListener.class));
		this.endpoints = factory.getBeans(Endpoint.class).toArray(new Endpoint[]{});
	}
	
	private void notifyAppConfigure() throws Throwable {
		for(AppListener listener : listeners){
			try {
	            listener.preAppConfigure(this, webConfigurator);
            } catch (Throwable e) {
            	log.error("Error notifying app pre configure on listener '{}', {}", listener.getClass().getName(),e .getMessage());
            	throw e;
            }
		}
		
		configure(webConfigurator);
		
		for(AppListener listener : listeners){
			try {
	            listener.postAppConfigure(this,webConfigurator.config());
            } catch (Throwable e) {
            	log.error("Error notifying app post configure on listener '{}', {}", listener.getClass().getName(),e .getMessage());
            	throw e;
            }
		}
	}
	
	private void notifyAppInit() throws Throwable {
		for(AppListener listener : listeners){
			try {
	            listener.preAppInit(this);
            } catch (Throwable e) {
            	log.error("Error notifying app pre init on listener '{}', {}", listener.getClass().getName(),e .getMessage());
            	throw e;
            }
		}
		
		filtering(filters());

		intercepting(interceptors());
		
		routing(routes());

		init();

		for(AppInitializable bean : initializableBeans) {
            try {
                bean.postAppInit(this);
            } catch (Throwable e) {
                log.error("Error notifying app post init on bean '{}', {}", bean.getClass().getName(), e.getMessage());
                throw e;
            }
		}
		
		for(AppListener listener : listeners){
			try {
	            listener.postAppInit(this);
            } catch (Throwable e) {
            	log.error("Error notifying app post init on listener '{}', {}", listener.getClass().getName(),e .getMessage());
            	throw e;
            }
		}
	}
	
	private void notifyAppStart() throws Throwable {
		for(AppListener listener : listeners){
			try {
	            listener.preAppStart(this);
            } catch (Throwable e) {
            	log.error("Error notifying app starting on listener '{}', {}", listener.getClass().getName(),e .getMessage());
            	throw e;
            }
		}
		
		//Start endpoints.
		for(Endpoint endpoint : endpoints) {
		    endpoint.startEndpoint(this, routes());
		}
		
		//Start app.
		start();
		
		for(AppListener listener : listeners){
			try {
	            listener.postAppStart(this);
            } catch (Throwable e) {
            	log.error("Error notifying app started on listener '{}', {}", listener.getClass().getName(),e .getMessage());
            	throw e;
            }
		}
	}
	
	private void notifyAppStop() throws Throwable {
		for(AppListener listener : listeners){
			try {
	            listener.preAppStop(this);
            } catch (Throwable e) {
            	log.warn("Error notifying app ending on listener '{}', {}", listener.getClass().getName(),e .getMessage());
            }
		}
		
	    //Stop endpoints.
        for(Endpoint endpoint : endpoints) {
            endpoint.stopEndpoint(this);
        }
		
		stop();
		
		for(AppListener listener : listeners){
			try {
	            listener.postAppStop(this);
            } catch (Throwable e) {
            	log.warn("Error notifying app ended on listener '{}', {}", listener.getClass().getName(),e .getMessage());
            }
		}
	}
	
	/**
	 * Configure the app in this method.
	 */
	protected void configure(WebConfigurator c) {
		
	}
	
	/**
	 * Add filters in this method.
	 */
	protected void filtering(FilterMappings filters) {
		
	}
	
	/**
	 * Add interceptors in this method.
	 */
	protected void intercepting(WebInterceptors interceptors) {
		
	}
	
	/**
	 * Add routes in this method.
	 */
	protected void routing(Routes routes) {
		
	}

	
	/**
	 * Initialize the app.
	 */
	protected void init() throws Throwable {
		
	}
	
	/**
	 * Starts the app.
	 */
	protected void start() throws Throwable {
		
	}
	
	/**
	 * Stop the app.
	 */
	protected void stop() throws Throwable {
		
	}

    @Override
    public String toString() {
        return super.toString() + "(contextPath=" + getContextPath() + ")";
    }
}