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

import leap.core.ds.DataSourceManager;
import leap.core.i18n.MessageSource;
import leap.lang.Classes;
import leap.lang.accessor.AttributeAccessor;
import leap.lang.accessor.AttributeGetter;
import leap.lang.beans.NoSuchBeanException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global context object of an application.
 */
public class AppContext implements AttributeAccessor {
	public static final String APP_CONTEXT_ATTRIBUTE = AppContext.class.getName();
	
	private static ThreadLocal<AppContext> threadlocal = new InheritableThreadLocal<AppContext>();
	private static AppContext              standalone  = null;
	
	private static boolean foundServletContext;
	
	static {
		foundServletContext = Classes.isPresent("javax.servlet.ServletContext");
	}
	
	/**
	 * Initializes standalone app context if current context did not initialized.
	 * 
	 * @see AppContextInitializer#initStandalone();
	 */
	public static AppContext initStandalone(){
		if(tryGetCurrent() == null){
			AppContextInitializer.initStandalone();	
		}
		return current();
	}
	
	/**
	 * Returns the {@link AppContext} instance binded to the external context.
	 * 
	 * <p/>
	 * 
	 * Returns {@code null} if not instance binded to the external context.
	 */
	public static AppContext get(Object externalContext) {
		if(foundServletContext){
			if(externalContext instanceof javax.servlet.ServletContext){
				return (AppContext)((javax.servlet.ServletContext)externalContext).getAttribute(APP_CONTEXT_ATTRIBUTE);
			}
		}
		
		if(externalContext instanceof AttributeGetter){
			return (AppContext)((AttributeGetter) externalContext).getAttribute(APP_CONTEXT_ATTRIBUTE);
		}

		throw new IllegalArgumentException("The given external context must be ServletContext or implement the AttributeGetter interface");
	}
	
	/**
	 * Returns the current {@link AppContext} instance.
	 * 
	 * <p>
	 * Returns <code>null</code> if no {@link AppContext} inited.
	 */
	public static AppContext tryGetCurrent() {
		if(null != standalone){
			return standalone;
		}
		return threadlocal.get();
	}
	
	/**
	 * Returns the current {@link AppContext} instance.
	 * 
	 * <p/>
	 * 
	 * a {@link AppContext} instance must be initialized by {@link AppContextInitializer}.
	 * 
	 * <p/>
	 * 
	 * @throws IllegalStateException if current app context not inited.
	 */
	public static AppContext current() throws IllegalStateException {
		if(null != standalone){
			return standalone;
		}
		
		AppContext current = threadlocal.get();
		
		if(null == current){
			throw new IllegalStateException("AppContext must be initialized");
		}
		
		return current;
	}
	
	/**
	 * Returns the primary bean of the given type in current app context.
	 * 
	 * @see BeanFactory#getBean(Class)
	 */
	public static <T> T getBean(Class<T> type) throws NoSuchBeanException {
		return factory().getBean(type);
	}
	
	/**
	 * Returns current application's config.
	 */
	public static AppConfig config(){
		return current().getConfig();
	}
	
	/**
	 * Returns current application's {@link BeanFactory}.
	 */
	public static BeanFactory factory(){
		return current().getBeanFactory();
	}
	
	/**
	 * @see AppContext#getServletContext()
	 */
	public static javax.servlet.ServletContext servletContext() throws IllegalStateException {
		return current().getServletContext();
	}
	
	/**
	 * Binds {@link AppContext} instance to current {@link Thread}.
	 */
	public static void setCurrent(AppContext current){
		threadlocal.set(current);
	}
	
	/**
	 * Removes {@link AppContext} from current {@link Thread}.
	 */
	public static void removeCurrent(){
		threadlocal.remove();
		threadlocal.set(null);
	}
	
	protected static void setStandalone(AppContext standalone){
		AppContext.standalone = standalone;
	}
	
	protected static AppContext getStandalone(){
		return standalone;
	}
	
	protected final AppConfig		   config;
	protected final BeanFactory		   beanFactory;
	protected final Object 			   externalContext;
	protected final Map<String,Object> attributes = new ConcurrentHashMap<>(10);
	
	protected AppHome		home;
	protected MessageSource messageSource;
	
	protected AppContext(Map<String, Object> attrs, AppConfig config,BeanFactory beanFactory,Object externalContext){
		this.config			 = config;
		this.beanFactory     = beanFactory;
		this.externalContext = externalContext;
		this.setAttributes(attrs);
	}
	
	public AppHome getHome() {
		return home;
	}
	
	public AppConfig getConfig() {
		return config;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}
	
	public MessageSource getMessageSource(){
		return messageSource;
	}
	
	public DataSourceManager getDataSourceProvider() {
		return null;
	}
	
	public Object getExternalContext() {
		return externalContext;
	}
	
	public boolean isServletEnvironment(){
		return foundServletContext && externalContext instanceof javax.servlet.ServletContext;
	}
	
	public javax.servlet.ServletContext getServletContext() throws IllegalStateException {
		if(!foundServletContext || !(externalContext instanceof javax.servlet.ServletContext)){
			throw new IllegalStateException("This app is not running in servlet environment");
		}
		return (javax.servlet.ServletContext)externalContext;
	}
	
	public javax.servlet.ServletContext tryGetServletContext() {
		return foundServletContext ? (javax.servlet.ServletContext)externalContext : null;
	}

	public boolean hasAttribute(String name){
		return attributes.containsKey(name);
	}
	
	public Object getAttribute(String name){
		return attributes.get(name);
	}
	
	public void setAttribute(String name,Object value){
		attributes.put(name, value);
	}
	
	public void setAttributes(Map<String, Object> attrs){
		if(null != attrs){
			this.attributes.putAll(attrs);
		}
	}
	
	public void removeAttribute(String name){
		attributes.remove(name);
	}
	
	/**
	 * Called by {@link AppContextInitializer}
	 */
	final synchronized void postInit(){
		this.home		   = beanFactory.getBean(AppHome.class);
		this.messageSource = beanFactory.getBean(MessageSource.class);
	}
}