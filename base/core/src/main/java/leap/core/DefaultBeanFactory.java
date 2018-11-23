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

import leap.core.ioc.BeanContainer;
import leap.core.ioc.BeanDefinition;
import leap.core.ioc.BeanDefinitionException;
import leap.lang.Args;
import leap.lang.Disposable;
import leap.lang.beans.BeanException;
import leap.lang.beans.NoSuchBeanException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.reflect.ReflectValued;

import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;

/**
 * A default implementation of {@link BeanFactory}
 */
@SuppressWarnings("unchecked")
public class DefaultBeanFactory extends BeanFactoryInternal implements BeanFactory {

    private static final Log log = LogFactory.get(DefaultBeanFactory.class);

    protected AppConfig     config;
    protected BeanFactory   externalFactory;
    protected boolean       initialized;
    protected BeanContainer beanContainer;

    public DefaultBeanFactory() {

    }
	
    @Override
    protected BeanFactoryInternal init(AppConfig config, BeanFactory externalFactory) {
        this.config	         = config;
        this.externalFactory = externalFactory;
        this.beanContainer   = new BeanContainer(config);
        return this;
    }

    protected DefaultBeanFactory load(AppContext appContext){
        AppResources resources = AppResources.get(appContext.getConfig());

		this.beanContainer.setAppContext(appContext);
		this.beanContainer.loadFromClasses(config.getResources().searchClasses())
						  .loadFromResources(resources.search("beans"))
						  .init()
						  .registerShutdownHook();
		return this;
	}
	
	@Override
    public AppContext getAppContext() {
	    return beanContainer.getAppContext();
    }

	@Override
    public AppConfig getAppConfig() {
	    return config;
    }

    @Override
    public void configure(Object bean, String prefix) {
        beanContainer.configure(bean, prefix);
    }

    @Override
    public <T> T inject(T bean) throws BeanException {
		Args.notNull(bean,"bean");
		if(null != externalFactory){
			externalFactory.inject(bean);
		}
		this.beanContainer.inject(bean);
		return bean;
    }

    @Override
    public void injectStatic(Class<?> cls) throws BeanException {
        Args.notNull(cls, "class");
        if(null != externalFactory) {
            externalFactory.injectStatic(cls);
        }
        beanContainer.injectStatic(cls);
    }

    @Override
    public <T> T validate(T bean) throws BeanException {
		Args.notNull(bean,"bean");
		if(null != externalFactory){
			externalFactory.validate(bean);
		}
		this.beanContainer.validate(bean);
		return bean;
    }
	
	@Override
    public <T> void addBean(T bean) {
		addBean((Class<T>)bean.getClass(),bean,true);
    }

	@Override
    public <T> void addBean(Class<T> type, T bean, boolean primary) {
		Args.notNull(type,"type");
		Args.notNull(bean,"bean");
		
		/*
		if(primary && tryGetBean(type) != null){
			throw new BeanDefinitionException("Primary bean of type '" + type.getName() + "' aleady exists");
		}
		*/
		
		this.beanContainer.addBean(type, bean, primary);
    }
	
	@Override
    public <T> void addBean(Class<T> type, T bean, String name, boolean primary) {
		Args.notNull(type,"type");
		Args.notNull(bean,"bean");

		/*
		if(primary && tryGetBean(type) != null){
			throw new BeanDefinitionException("Primary bean of type '" + type.getName() + "' aleady exists");
		}
		*/
		
		this.beanContainer.addBean(type, bean, name, primary);
    }

	@Override
    public <T> void addBean(String id, boolean lazyInit, Class<? extends T> beanClass, Object... constructorArgs) throws BeanException {
		Args.notNull(beanClass);
		Args.notEmpty(id);
	    if(tryGetBean(id) != null){
	    	throw new BeanDefinitionException("the to be added bean '" + id + "' aleady exists");
	    }
	    this.beanContainer.addBean(id, lazyInit, beanClass, constructorArgs);
    }

	@Override
    public <T> void addBean(Class<? super T> beanType, boolean primary, boolean lazyInit, Class<T> beanClass, Object... constructorArgs) throws BeanException {
		Args.notNull(beanClass);
		Args.notNull(beanType);

		/*
		if(primary && tryGetBean(beanType) != null){
			throw new BeanDefinitionException("primary bean for type '" + beanType.getName() + "' aleady exists");
		}
		*/
		
	    this.beanContainer.addBean(beanType, primary, lazyInit, beanClass, constructorArgs);
    }

	@Override
    public <T> void addBean(Class<? super T> beanType, boolean primary, String name, boolean lazyInit, Class<T> beanClass, Object... constructorArgs) throws BeanException {
		Args.notNull(beanClass);
		Args.notNull(beanType);
		Args.notEmpty(name);

		/*
		if(primary && tryGetBean(beanType) != null){
			throw new BeanDefinitionException("primary bean for type '" + beanType.getName() + "' aleady exists");
		}
		*/
		
		if(tryGetBean(beanType, name) != null){
			throw new BeanDefinitionException("the bean '" + name + "' for type '" + beanType.getName() + "' aleady exists");
		}
		
	    this.beanContainer.addBean(beanType, primary, name, lazyInit, beanClass, constructorArgs);
    }

    @Override
    public void addAlias(Class<?> type, String name, String alias) {
        beanContainer.addAlias(type, name, alias);
    }

    @Override
    public <T> T getBean(String id) throws NoSuchBeanException {
		T bean = (T)(null != externalFactory ? externalFactory.tryGetBean(id) : null);
		
	    if(null == bean){
	    	return beanContainer.getBean(id);
	    }
		
	    return bean;
    }
	
    @Override
    public <T> T createBean(Class<T> cls) throws BeanException {
		T bean = (T)(null != externalFactory ? externalFactory.createBean(cls) : null);
		
	    if(null == bean){
	    	return beanContainer.createBean(cls);
	    }
		
	    return bean;
    }

    @Override
    public <T> T tryGetBean(String id) {
	    T bean = (T)(null != externalFactory ? externalFactory.tryGetBean(id) : null);
	    
	    if(null == bean){
	    	bean = beanContainer.tryGetBean(id);
	    }
	    
	    return bean;
    }

    @Override
    public <T> T getBean(String namespace, String name) throws BeanException {
        T bean = (T)(null != externalFactory ? externalFactory.tryGetBean(namespace, name) : null);

        if(null == bean){
            return beanContainer.getBean(namespace, name);
        }

        return bean;
    }

    @Override
    public <T> T tryGetBean(String namespace, String name) throws BeanException {
        T bean = (T)(null != externalFactory ? externalFactory.tryGetBean(namespace, name) : null);

        if(null == bean){
            bean = beanContainer.tryGetBean(namespace, name);
        }

        return bean;
    }

    @Override
    public <T> T getBean(Class<? super T> type) throws NoSuchBeanException, BeanException {
		T bean = (T)(null != externalFactory ? externalFactory.tryGetBean(type) : null);
		
		if(null == bean){
			bean = beanContainer.<T>getBean(type);
		}
		
	    return bean;
    }

    @Override
    public <T> T getOrAddBean(Class<T> type) throws BeanException {
        return beanContainer.getOrAddBean(type);
    }

    @Override
    public <T> T getOrAddBean(Class<T> type, String name) throws BeanException {
        return beanContainer.getOrAddBean(type, name);
    }

    @Override
    public <T> T getOrCreateBean(Class<T> type) throws NoSuchBeanException, BeanException {
		T bean = null;// (T)(null != externalFactory ? externalFactory.tryCreateBean(type) : null);
		
		if(null == bean){
			bean = beanContainer.getOrCreateBean(type);
		}
		
	    return bean;
    }

	@Override
    public <T> T tryGetBean(Class<? super T> type) throws BeanException {
		T bean = (T)(null != externalFactory ? externalFactory.tryGetBean(type) : null);
		
		if(null == bean){
			bean = beanContainer.<T>tryGetBean(type);
		}
		
	    return bean;
    }
	
	@Override
    public <T> T tryGetBeanExplicitly(Class<? super T> type) throws BeanException {
        T bean = (T)(null != externalFactory ? externalFactory.tryGetBeanExplicitly(type) : null);
        
        if(null == bean){
            bean = beanContainer.<T>tryGetBeanExplicitly(type);
        }
        
        return bean;
    }

	@Override
    public <T> T getBean(Class<? super T> type, String name) throws NoSuchBeanException, BeanException {
		T bean = (T)(null != externalFactory ? externalFactory.tryGetBean(type,name) : null);
		
		if(null == bean){
			bean = beanContainer.<T>getBean(type,name);
		}
		
	    return bean;
    }
	
	@Override
    public <T> T getOrCreateBean(Class<T> type, String name) throws NoSuchBeanException, BeanException {
		T bean = null; // (T)(null != externalFactory ? externalFactory.tryCreateBean(type,name) : null);
		
		if(null == bean){
			bean = beanContainer.getOrCreateBean(type,name);
		}
		
	    return bean;
    }

	@Override
    public <T> T tryGetBean(Class<? super T> type, String name) throws BeanException {
		T bean = (T)(null != externalFactory ? externalFactory.tryGetBean(type,name) : null);
		
		if(null == bean){
			bean = beanContainer.<T>tryGetBean(type,name);
		}
		
	    return bean;
    }
	
	@Override
    public void setPrimaryBean(Class<?> type, Object bean) {
        if(!type.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException("The bean '" + bean + "' must be instance of the type '" + type + "'");
        }

        beanContainer.setPrimaryBean(type, bean);
    }

	@Override
    public <T> List<T> getBeans(Class<? super T> type) throws BeanException {
		//TODO : duplicated name between external factory and internal factory ?
		List<T> beans = null != externalFactory ? externalFactory.getBeans(type) : null;
		
		if(null == beans){
			return beanContainer.getBeans(type);
		}else{
			List<T> list = new ArrayList<T>(beans);
			
			list.addAll(beanContainer.<T>getBeans(type));
			
			return list;
		}
    }
	
	@Override
    public <T> List<T> getBeans(Class<? super T> type, String qualifier) throws BeanException {
		List<T> beans = null != externalFactory ? externalFactory.getBeans(type) : null;
		
		if(null == beans){
			return beanContainer.getBeans(type,qualifier);
		}else{
			List<T> list = new ArrayList<T>(beans);
			list.addAll(beanContainer.<T>getBeans(type,qualifier));
			return list;
		}
    }

	@Override
    public <T> Map<String, T> getNamedBeans(Class<? super T> type) throws BeanException {
		Map<String,T> beans = null != externalFactory ? externalFactory.getNamedBeans(type) : null;
		
		if(null == beans){
			return beanContainer.getNamedBeans(type);
		}else{
			Map<String,T> map = new LinkedHashMap<String, T>(beans);
			
			Map<String,T> internalBeans = beanContainer.getNamedBeans(type);
			
			for(Entry<String, T> namedBean : internalBeans.entrySet()){
				if(!map.containsKey(namedBean.getKey())){
					map.put(namedBean.getKey(), namedBean.getValue());
				}
			}
			return map;
		}
    }
	
	@Override
    public <T> Map<T, BeanDefinition> getBeansWithDefinition(Class<? super T> type) throws BeanException {
		Map<T, BeanDefinition> beans = null != externalFactory ? externalFactory.getBeansWithDefinition(type) : null;
		
		if(null == beans){
			return beanContainer.getBeansWithDefinition(type);
		}else{
			Map<T, BeanDefinition> map = new LinkedHashMap<T, BeanDefinition>(beans);
			
			
			Map<T, BeanDefinition> internalBeans = beanContainer.getBeansWithDefinition(type);
			
			for(Entry<T,BeanDefinition> namedBean : internalBeans.entrySet()){
				if(!map.containsKey(namedBean.getKey())){
					map.put(namedBean.getKey(), namedBean.getValue());
				}
			}
			return map;
		}
    }

	@Override
	public Set<String> getBeanAliases(Class<?> type, String name) {
		return beanContainer.getBeanAliases(type, name);
	}

	@Override
    public <T> Map<T, BeanDefinition> createBeansWithDefinition(Class<? super T> type) {
        return beanContainer.createBeansWithDefinition(type);
    }

    @Override
    public boolean tryInitBean(BeanDefinition bd) {
        if(null != externalFactory && externalFactory.tryInitBean(bd)) {
            return true;
        }
        return beanContainer.tryInitBean(bd);
    }

    @Override
    public <T> T tryCreateBean(String id) {
        return beanContainer.tryCreateBean(id);
    }

    @Override
    public <T> T tryCreateBean(String namespace, String name) {
        return beanContainer.tryCreateBean(namespace, name);
    }

    @Override
    public <T> T tryCreateBean(Class<T> type, String name) {
        return beanContainer.tryCreateBean(type, name);
    }

    @Override
    public boolean isSingleton(String beanId) throws NoSuchBeanException {
		Boolean singleton = null;
		
		if(null != externalFactory){
			try {
		        singleton = externalFactory.isSingleton(beanId);
	        } catch (NoSuchBeanException e) {
	        	return beanContainer.isSingleton(beanId);
	        }
		}
		
        return null != singleton ? singleton : beanContainer.isSingleton(beanId);

    }

	@Override
    public boolean isSingleton(Class<?> type) throws NoSuchBeanException {
		Boolean singleton = null;
		
		if(null != externalFactory){
			try {
		        singleton = externalFactory.isSingleton(type);
	        } catch (NoSuchBeanException e) {
	        	return beanContainer.isSingleton(type);
	        }
		}
		
        return null != singleton ? singleton : beanContainer.isSingleton(type);
    }

	@Override
    public boolean isSingleton(Class<?> type, String name) throws NoSuchBeanException {
		Boolean singleton = null;
		
		if(null != externalFactory){
			try {
		        singleton = externalFactory.isSingleton(type,name);
	        } catch (NoSuchBeanException e) {
	        	return beanContainer.isSingleton(type,name);
	        }
		}
		
        return null != singleton ? singleton : beanContainer.isSingleton(type,name);
    }

	@Override
    public void postInit(AppContext context) throws Exception {
    	beanContainer.postInit(context);
	    if(initialized){
	    	throw new IllegalStateException("BeanFactory already initialized");
	    }
	    
	    this.initialized = true;
    }

    @Override
    public boolean initBean(Object bean) {
        return beanContainer.initBean(bean);
    }

    @Override
    public boolean destroyBean(Object bean) {
        return beanContainer.destroyBean(bean);
    }

    @Override
    public Object resolveInjectValue(Class<?> type, Type genericType) {
        return beanContainer.resolveInjectValue(type, genericType);
    }

    @Override
    public Object resolveInjectValue(Class<?> type, Type genericType, String name) {
        return beanContainer.resolveInjectValue(type, genericType, name);
    }

    public void close(){
		try{
			if(null != beanContainer){
				beanContainer.close();
			}
		}finally{
			if(null != externalFactory && externalFactory instanceof Disposable){
				try {
					((Disposable)externalFactory).dispose();
				} catch (Throwable e) {
					log.warn("Error disposing external factory",e);
				}
			}
		}
	}
}