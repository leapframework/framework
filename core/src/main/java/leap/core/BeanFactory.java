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

import java.util.List;
import java.util.Map;

import leap.core.ioc.BeanDefinition;
import leap.core.validation.annotations.NotEmpty;
import leap.core.validation.annotations.NotNull;
import leap.lang.beans.BeanException;
import leap.lang.beans.BeanFactoryBase;
import leap.lang.beans.NoSuchBeanException;

public interface BeanFactory extends BeanFactoryBase, AppContextInitializable {
	
	/**
	 * Returns the app context.
	 */
	AppContext getAppContext();
	
	/**
	 * Returns the app config.
	 */
	AppConfig getAppConfig();
	
	/**
	 * Injects a not managed bean.
	 * 
	 * <p>
	 * Returns the given bean itself.
	 */
	<T> T inject(T bean) throws BeanException;
	
	/**
	 * Validates the given bean, throws a {@link BeanException} if error. 
	 * 
	 * <p>
	 * Supported validation constraints : {@link NotNull} and {@link NotEmpty}.
	 */
	<T> T validate(T bean) throws BeanException;
	
	/**
	 * Creates a new instance of the given class.
	 * 
	 * <p>
	 * The created instance will be injected by this factory.
	 */
	<T> T newInstance(Class<T> cls) throws BeanException;
	
	/**
	 * Adds a primary bean.
	 */
	<T> void addBean(T bean);
	
	/**
	 * Adds a created bean
	 */
	<T> void addBean(Class<T> type, T bean, boolean primary);
	
	/**
	 * Adds a created bean
	 */
	<T> void addBean(Class<T> type, T bean, String name, boolean primary);
	
	/**
	 * Do the normal creation such as autowire, aware, validate, etc for the given bean class.
	 * 
	 * <p>
	 * After the creation, the bean instance will be register as singleton bean to this container with the given id.
	 */
	<T> void addBean(String id,boolean lazyInit,Class<? extends T> beanClass,Object... constructorArgs) throws BeanException;
	
	/**
	 * Do the normal creation such as autowire, aware, validate, etc for the given bean class.
	 * 
	 * <p>
	 * After the creation, the bean instance will be register as singleton bean to this container with the given type.
	 */
	<T> void addBean(Class<? super T> typeClass,boolean primary,boolean lazyInit,Class<T> beanClass,Object... constructorArgs) throws BeanException;
	
	/**
	 * Do the normal creation such as autowire, aware, validate, etc for the given bean class.
	 * 
	 * <p>
	 * After the creation, the bean instance will be register as singleton bean to this container with the given type and name.
	 */
	<T> void addBean(Class<? super T> typeClass,boolean primary,String name,boolean lazyInit,Class<T> beanClass, Object... constructorArgs) throws BeanException;
	
	/**
	 * Returns the bean's instance identified by the given id (case sensitive).
	 * 
	 * @throws NoSuchBeanException if the given id not exists.
	 * @throws BeanException if the bean could not be obtained.
	 */
	<T> T getBean(String id) throws NoSuchBeanException,BeanException;
	
	/**
	 * Returns the bean's instance identified by the given id (case sensitive).
	 * 
	 * <p>
	 * Returns <code>null<code> if the given id not exists.
	 * 
	 * @throws BeanException if the bean could not be obtained.
	 */
	<T> T tryGetBean(String id) throws BeanException;
	
	/**
	 * Returns the primary bean's instance for the given type.
	 * 
	 * @throws NoSuchBeanException if no primary bean defined for the given type.
	 * @throws BeanException if the bean could not be obtained.
	 */
	<T> T getBean(Class<? super T> type) throws NoSuchBeanException,BeanException;
	
	/**
	 * Returns the primary bean's instance for the given type.
	 * 
	 * <p>
	 * Returns <code>null</code> if no primary bean defined for the given type.
	 */
	<T> T tryGetBean(Class<? super T> type) throws BeanException;
	
    /**
     * Returns the primary bean's instance for the given type.
     * 
     * <p>
     * Note : The bean must be declared as primary bean explicitly.
     * 
     * <p>
     * Returns <code>null</code> if no primary bean defined for the given type.
     */
    <T> T tryGetBeanExplicitly(Class<? super T> type) throws BeanException;	
	
	/**
	 * Returns the bean's instance named to the given name (case sensitive) for the given type.
	 * 
	 * @throws NoSuchBeanException if no bean defined with the given name
	 * @throws BeanException if the bean could not be obtained.
	 */
	<T> T getBean(Class<? super T> type,String name) throws NoSuchBeanException,BeanException;
	
	/**
	 * Returns the bean's instance named to the given name (case sensitive) for the given type.
	 * 
	 * <p>
	 * Returns <code>null</code> if the given name not exists for the given type.
	 * 
	 * @throws BeanException if the bean could not be obtained.
	 */
	<T> T tryGetBean(Class<? super T> type,String name) throws BeanException;	

	/**
	 * Returns an immutable {@link List} contains all the bean's instances defined for the given type.
	 * 
	 * <p>
	 * Returns an immutable {@link List} contains no elements if no beans defined for the given type. 
	 * 
	 * @throws BeanException if the beans could not be obtained.
	 */
	<T> List<T> getBeans(Class<? super T> type) throws BeanException;
	
	/**
	 * Returns an immutable {@link List} contains all the bean's instances defined for the given type.
	 * 
	 * <p>
	 * Returns an immutable {@link List} contains no elements if no beans defined for the given type. 
	 * 
	 * @throws BeanException if the beans could not be obtained.
	 */
	<T> List<T> getBeans(Class<? super T> type,String qualifier) throws BeanException;
	
	/**
	 * Returns an immutable {@link Map} contains all the named bean's instances for the given type.
	 * 
	 * <p>
	 * The key of returned {@link Map} is the name of bean.
	 * 
	 * <p>
	 * The value of returned {@link Map} is the instance of bean.
	 *  
	 * <p>
	 * Returns a {@link Map} contains no elements if no named beans defined for the given type.
	 */
	<T> Map<String,T> getNamedBeans(Class<? super T> type) throws BeanException;
	
	/**
	 * Returns an immutable {@link Map} all the beans's instances and definitions for the given type.
	 * 
	 * <p>
	 * The key of returned {@link Map} is the instance of bean.
	 * 
	 * <p>
	 * The value of returned {@link Map} is the definition of bean.
	 */
	<T> Map<T,BeanDefinition> getBeansWithDefinition(Class<? super T> type) throws BeanException;
	
	/**
	 * Returns a new created bean's instance identified by the given id (case sensitive).
	 * 
	 * @throws NoSuchBeanException if the given id not exists.
	 * @throws BeanException if the bean could not be obtained.
	 */
	<T> T createBean(String id) throws NoSuchBeanException,BeanException;
	
	/**
	 * Returns a new created bean's instance identified by the given id (case sensitive).
	 * 
	 * <p>
	 * Returns <code>null<code> if the given id not exists.
	 * 
	 * @throws BeanException if the bean could not be obtained.
	 */
	<T> T tryCreateBean(String id) throws BeanException;
	
	/**
	 * Returns a new created instance of primary bean for the given type.
	 * 
	 * <p>
	 * Creates a new instance of the given type and do injection if no primary bean exists. 
	 * 
	 * @throws BeanException if the bean could not be obtained or error occurs on creating bean.
	 */
	<T> T createBean(Class<T> type) throws BeanException;
	
	/**
	 * Returns a new created instance of primary bean for the given type.
	 * 
	 * <p>
	 * Returns <code>null</code> if no primary bean defined for the given type.
	 */
	<T> T tryCreateBean(Class<T> type) throws BeanException;
	
	/**
	 * Returns a new created bean's instance named to the given name (case sensitive) for the given type.
	 * 
	 * <p>
	 * Returns a new instance of the given type if the bean definition not exists. 
	 * 
	 * @throws BeanException if the bean could not be obtained or error occurs on creating bean.
	 */
	<T> T createBean(Class<T> type,String name) throws BeanException;
	
	/**
	 * Returns a new created bean's instance named to the given name (case sensitive) for the given type.
	 * 
	 * <p>
	 * Returns <code>null</code> if the given name not exists for the given type.
	 * 
	 * @throws BeanException if the bean could not be obtained.
	 */
	<T> T tryCreateBean(Class<T> type,String name) throws BeanException;	
	
	/**
	 * Updates the primary bean of the given type as the given instance. 
	 */
	<T> void setPrimaryBean(Class<T> type, T bean);
	
	/**
	 * Returns <code>true</code> if the bean identified by the given id is singleton.
	 * 
	 * <p>
	 * Returns <code>false</code> if the id not exists or the bean is not singleton. 
	 * 
	 * @throws NoSuchBeanException if such bean not exist
	 */
	boolean isSingleton(String beanId) throws NoSuchBeanException;
	
	/**
	 * Return <code>true</code> if the primary bean for the given type is singleton.
	 * 
	 * <p>
	 * Returns <code>false></code> if no primary bean defined for the given type or the bean is not singleton.
	 * 
	 * @throws NoSuchBeanException if such bean not exist
	 */
	boolean isSingleton(Class<?> type) throws NoSuchBeanException;
	
	/**
	 * Returns <code>true</code> if the bean named to the name for the given type is singleton.
	 * 
	 * <p>
	 * Returns <code>false</code> if no bean named to the name for the given type or the bean is not singleton.
	 * 
	 * @throws NoSuchBeanException if such bean not exist
	 */
	boolean isSingleton(Class<?> type,String name) throws NoSuchBeanException;
}