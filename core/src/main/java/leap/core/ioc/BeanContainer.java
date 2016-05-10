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
package leap.core.ioc;

import leap.core.*;
import leap.core.annotation.*;
import leap.core.config.*;
import leap.core.instrument.AppInstrumentation;
import leap.core.validation.annotations.NotEmpty;
import leap.core.validation.annotations.NotNull;
import leap.core.web.ServletContextAware;
import leap.lang.*;
import leap.lang.Comparators;
import leap.lang.accessor.PropertyGetter;
import leap.lang.annotation.Internal;
import leap.lang.beans.*;
import leap.lang.convert.Converts;
import leap.lang.json.JSON;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.reflect.*;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.text.DefaultPlaceholderResolver;
import leap.lang.text.PlaceholderResolver;

import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;

@Internal
public class BeanContainer implements BeanFactory {
	
	private static final Log log = LogFactory.get(BeanContainer.class);
	
	/** Definition of beans that are currently in creation */
	private final ThreadLocal<Map<BeanDefinitionBase,Object>> beansCurrentlyInCreation = new ThreadLocal<>();
	
	private static final BeanDefinitionBase NULL_BD = new BeanDefinitionBase(null);

    protected Set<InitDefinition>                    initDefinitions           = new CopyOnWriteArraySet<>();
    protected Set<BeanDefinitionBase>                allBeanDefinitions        = new CopyOnWriteArraySet<>();
    protected Map<String, BeanDefinitionBase>        identifiedBeanDefinitions = new HashMap<>();
    protected Map<Class<?>, Set<BeanDefinitionBase>> beanTypeDefinitions       = new HashMap<>();
    protected Map<Class<?>, Set<BeanDefinitionBase>> beanClassDefinitions      = new HashMap<>();
    protected Map<String, BeanListDefinition>        beanListDefinitions       = new HashMap<>();
    protected Map<Class<?>, FactoryBean>             typedFactoryBeans         = new HashMap<>();
    protected Map<Class<?>, BeanDefinitionBase>      typedFactoryDefinitions   = new HashMap<>();
    protected Map<String, BeanDefinitionBase>        namedBeanDefinitions      = new HashMap<>();
    protected Map<Class<?>, BeanDefinitionBase>      primaryBeanDefinitions    = new HashMap<>();
    protected Map<String, AliasDefinition>           aliasDefinitions          = new HashMap<>();

    private Map<String, List<?>>                  typedBeansMap  = new ConcurrentHashMap<>();
    private Map<Class<?>, Map<String, ?>>         namedBeansMap  = new ConcurrentHashMap<>();
    private Map<Class<?>, Map<?, BeanDefinition>> typedInstances = new ConcurrentHashMap<>();
    private Map<Class<?>, Object>                 primaryBeans   = new ConcurrentHashMap<>();

    protected List<BeanDefinitionBase>       postProcessorBeans = new ArrayList<>();
    protected BeanProcessor[]                processors;
    protected List<BeanFactoryInitializable> beanFactoryInitializables = new ArrayList<>();

    protected final PlaceholderResolver                        placeholderResolver;
    protected final AnnotationBeanDefinitionLoader             annotationBeanDefinitionLoader;
    protected final XmlBeanDefinitionLoader                    xmlBeanDefinitionLoader;
    protected final AppInstrumentation                         instrumentation = Factory.getInstance(AppInstrumentation.class);

    private AppContext  appContext;
    private BeanFactory beanFactory;
    private boolean     initializing;
    private boolean     containerInited;
    private boolean     appInited;

	/** Flag that indicates whether this container has been closed already */
	private boolean closed = false;
	
	/** Reference to the JVM shutdown hook, if registered */
	private Thread shutdownHook;
	
	/** Synchronization monitor for the "active" flag */
	private final Object activeMonitor = new Object();
	
	/** Synchronization monitor for the "refresh" and "destroy" */
	private final Object startupShutdownMonitor = new Object();

	public BeanContainer(PropertyGetter properties){
		this.placeholderResolver            = new DefaultPlaceholderResolver(properties);
		this.annotationBeanDefinitionLoader = new AnnotationBeanDefinitionLoader();
		this.xmlBeanDefinitionLoader        = new XmlBeanDefinitionLoader(this);
	}

	public BeanContainer(AppConfig config){
		this.placeholderResolver            = config.getPlaceholderResolver();
		this.annotationBeanDefinitionLoader = new AnnotationBeanDefinitionLoader();
		this.xmlBeanDefinitionLoader        = new XmlBeanDefinitionLoader(this, instrumentation);
	}
	
	public AppContext getAppContext() {
		return appContext;
	}
	
	public AppConfig getAppConfig(){
		return appContext == null ? null : appContext.getConfig();
	}

	public void setAppContext(AppContext appContext){
		this.appContext  = appContext;
		this.beanFactory = appContext.getBeanFactory();
	}
	
	public void addInitializableBean(BeanFactoryInitializable bean){
		this.beanFactoryInitializables.add(bean);
	}
	
	/**
	 * Loads all the beans definitions from the given {@link ResourceSet}.
	 * 
	 * <p/>
	 * 
	 * throws {@link IllegalStateException} if this container aleady finish loading.
	 */
	public BeanContainer loadFromResources(Resource[] resources) throws IllegalStateException {
		ensureContainerNotInited();
		this.xmlBeanDefinitionLoader.load(resources);
		return this;
	}
	
	public BeanContainer loadFromClasses(Class<?>[] classes) throws IllegalStateException{
		ensureContainerNotInited();
		this.annotationBeanDefinitionLoader.load(this,classes);
		return this;
	}
	
	@Override
    public <T> T inject(T bean) throws BeanException {
		try {
            BeanDefinitionBase bd = createBeanDefinition(bean.getClass());

			this.doBeanAware(bd, bean);
			this.doBeanConfigure(bd, bean);
	        this.doBeanInjection(bd, bean);
        } catch (Throwable e) {
        	if(e instanceof BeanException){
        		throw (BeanException)e;
        	}
        	throw new BeanException("Error injecting the bean '" + bean.getClass().getName() + "' : " + e.getMessage(),e);
        }
		
		//Validates
		validateFields(null, bean);
		
	    return bean;
    }
	
    protected void validateFields(Object bean) {
        validateFields(null, bean);
    }
	
	protected void validateFields(BeanDefinition d, Object bean) {
        ReflectClass cls = ReflectClass.of(bean.getClass());
        for(ReflectField field : cls.getFields()){
            validateField(d, bean, field);
        }   
	}
	
	protected void validateField(BeanDefinition d, Object bean, ReflectField field) {
	    
        if((field.isAnnotationPresent(NotNull.class) || field.isAnnotationPresent(M.class)) && 
            null == field.getValue(bean)){
            
            throw new BeanException("Field '" + field.getName() + "' must not null in bean " + (null == d ? bean : d));
        }
        
        if((field.isAnnotationPresent(NotEmpty.class) || field.isAnnotationPresent(R.class) ) && 
            Objects2.isEmpty(field.getValue(bean))){
            
            throw new BeanException("Field '" + field.getName() + "' must not empty in bean " + (null == d ? bean : d));
        }
	}
	
	@Override
    public <T> T validate(T bean) throws BeanException {
		this.doBeanValidation(bean);
		return bean;
    }

	/**
	 * called after loaded, check and resolve all the beans defintions.
	 * 
	 * <p/>
	 * 
	 * throws {@link IllegalStateException} if this container already initialized.
	 */
	public BeanContainer init() throws IllegalStateException{
		if(initializing){
			throw new IllegalStateException("Container is initializing");
		}
		
		ensureContainerNotInited();
		
		initializing = true;
		this.checkAfterLoading();
		this.initAfterLoading();
		initializing = false;
		
		this.resolveAfterLoading();

        if(null != processors){
            for(BeanDefinitionBase bd : allBeanDefinitions) {
                for(int i=0;i<processors.length;i++){
                    try{
                        processors[i].postInitBean(appContext, beanFactory, bd);
                    }catch(Throwable e) {
                        throw new AppInitException(e.getMessage(), e);
                    }
                }
            }
        }

        AppConfig config = getAppConfig();
        if(config instanceof AppConfigBase) {
            ((AppConfigBase) config).setPropertyProvider(tryCreateBean(PropertyProvider.class));
        }

		this.initBeanFactoryInitializableBeans();
		this.initNonLazyBeans();

        this.containerInited = true;

		return this;
	}

    @Override
    public boolean tryInitBean(BeanDefinition bd) {
        if(!allBeanDefinitions.contains(bd)) {
            throw new IllegalStateException("Not a managed bean " + bd);
        }

        if(bd.isInited()) {
            return false;
        }

        doGetBean((BeanDefinitionBase)bd);
        return true;
    }

    @Override
    public void postInit(AppContext context) throws Exception {
    	if(appInited){
    		throw new IllegalStateException("postInitialize already called");
    	}
    	this.appInited = true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> void addBean(T bean) {
		addBean((Class<T>)bean.getClass(),bean,true);
    }

	@Override
    public <T> void addBean(Class<T> type, T bean, boolean primary) {
		addBeanDefinition(createBeanDefinition(type, bean, primary));
    }
	
	@Override
    public <T> void addBean(Class<T> type, T bean, String name, boolean primary) {
		addBeanDefinition(createBeanDefinition(type, bean, name, primary));	    
    }

	protected <T> BeanDefinitionBase createBeanDefinition(Class<T> type, T bean, boolean primary){
		return createBeanDefinition(type, bean, null, primary);
	}
	
	protected <T> BeanDefinitionBase createBeanDefinition(Class<T> type, T bean, String name, boolean primary){
		BeanDefinitionBase bd = new BeanDefinitionBase(XmlBeanDefinitionLoader.RUNTIME_SOURCE);
		
		bd.setName(name);
		bd.setType(type);
		bd.setBeanClass(bean.getClass());
		bd.setSingleton(true);
		bd.setSingletonInstance(bean);
		bd.setPrimary(primary);
		
		return bd;
	}

	protected <T> BeanDefinitionBase createBeanDefinition(Class<T> type){
		BeanDefinitionBase bd = new BeanDefinitionBase(XmlBeanDefinitionLoader.RUNTIME_SOURCE);

		bd.setType(type);
		bd.setBeanClass(type);
        bd.setBeanClassType(BeanType.of(type));
		bd.setSingleton(true);
		bd.setPrimary(true);

		return bd;
	}

	@Override
    public <T> void addBean(String id, boolean lazyInit, Class<? extends T> beanClass, Object... constructorArgs) throws BeanException {
		ensureAppNotInited();
    	addBeanDefinition(xmlBeanDefinitionLoader.create(id, lazyInit, beanClass, constructorArgs));
    }

	@Override
    public <T> void addBean(Class<? super T> typeClass, boolean primary, boolean lazyInit, Class<T> beanClass, Object... constructorArgs) throws BeanException {
		ensureAppNotInited();
		addBeanDefinition(xmlBeanDefinitionLoader.create(typeClass, primary, lazyInit, beanClass, constructorArgs));
    }

	@Override
    public <T> void addBean(Class<? super T> typeClass, boolean primary, String name, boolean lazyInit, Class<T> beanClass, Object... constructorArgs) throws BeanException {
		ensureAppNotInited();
		addBeanDefinition(xmlBeanDefinitionLoader.create(typeClass, primary, name, lazyInit, beanClass, constructorArgs));
    }

	public <T> T getBean(String id) throws NoSuchBeanException,BeanException {
    	T bean = tryGetBean(id);
		if(null == bean){
			throw new NoSuchBeanException("No such bean '" + id + "'");
		}
		return bean;
	}
	
	@Override
    public <T> T createBean(Class<T> cls) throws BeanException {
        return (T)doCreateBean(createBeanDefinition(cls));
        /*

		try {
	        T bean = Reflection.newInstance(cls);
	        
	        inject(bean);
	        
	        if(bean instanceof PostCreateBean){
	        	((PostCreateBean) bean).postCreate(appContext.getBeanFactory());
	        }
	        
	        doBeanValidation(bean);
	        
	        return bean;
		} catch (BeanException e){
			throw e;
        } catch (Throwable e) {
        	throw new BeanCreationException("Error creating instance of '" + cls.getName() + "', " + e.getMessage(), e);
        }
        */
    }

    public <T> T createBean(String id) throws NoSuchBeanException, BeanException {
		T bean = tryCreateBean(id);
		if(null == bean){
			throw new NoSuchBeanException("No such bean '"  + id + "'");
		}
	    return bean;
    }

	@Override
	@SuppressWarnings("unchecked")
    public <T> T tryGetBean(String id) throws BeanException {
		Args.notEmpty(id,"bean id");
		BeanDefinitionBase bd = findBeanOrAliasDefinition(id);
		if(null == bd){
			return null;
		}
		return (T)doGetBean(bd);
    }
	
    @SuppressWarnings("unchecked")
    public <T> T tryCreateBean(String id) throws BeanException {
    	Args.notEmpty(id,"bean id");
		BeanDefinitionBase bd = findBeanOrAliasDefinition(id);
		if(null == bd){
			return null;
		}
	    return (T)doCreateBean(bd);
    }

	@Override
    public <T> T getBean(Class<? super T> type) throws NoSuchBeanException,BeanException {
    	T bean = tryGetBean(type);
		
		if(null == bean){
			throw new NoSuchBeanException("No primary bean for type '" + type.getName() + "'");
		}
		
	    return bean;
    }
    
    @Override
    public <T> T getOrCreateBean(Class<T> type) throws BeanException {
    	T bean = tryCreateBean(type);
		
		if(null == bean){
			return (T) createBean(type);
		}
		
	    return bean;
    }

	@Override
	@SuppressWarnings("unchecked")
    public <T> T tryGetBean(Class<? super T> type) throws BeanException {
		Args.notNull(type,"bean type");
		
		T bean = (T)primaryBeans.get(type);
		if(null != bean){
			return bean;
		}
		
		BeanDefinitionBase bd = findPrimaryBeanDefinition(type);
		if(null != bd){
			return (T)doGetBean(bd);
		}
		
		FactoryBean factoryBean = typedFactoryBeans.get(type);
		if(null != factoryBean){
			return (T)factoryBean.getBean(beanFactory, type);
		}
		return null;
    }
	
    @SuppressWarnings("unchecked")
    public <T> T tryGetBeanExplicitly(Class<? super T> type) throws BeanException {
        Args.notNull(type, "bean type");

        T bean = (T) primaryBeans.get(type);
        if (null != bean) {
            return bean;
        }

        BeanDefinitionBase bd = findPrimaryBeanDefinition(type);
        if(null == bd) {
            return null;
        }
        
        if(!bd.isPrimary()) {
            return null;
        }
        
        return (T)doGetBean(bd);
    }
	
    @SuppressWarnings("unchecked")
    public <T> T tryCreateBean(Class<T> type) throws BeanException {
		Args.notNull(type,"bean type");
		BeanDefinitionBase bd = findPrimaryBeanDefinition(type);
		
		if(null != bd){
			return (T)doCreateBean(bd);
		}
		
		FactoryBean factoryBean = typedFactoryBeans.get(type);
		if(null != factoryBean){
			return (T)factoryBean.getBean(beanFactory, type);
		}
		return null;
    }

	@Override
    public <T> T getBean(Class<? super T> type, String name) throws NoSuchBeanException, BeanException {
		T bean = tryGetBean(type, name);
		
		if(null == bean){
			throw new NoSuchBeanException("No bean named '" + name + "' for type '" + type.getName() + "'");
		}
		
	    return bean;
    }
	
    public <T> T getOrCreateBean(Class<T> type, String name) throws BeanException {
		T bean = tryCreateBean(type, name);
		
		if(null == bean){
			return (T) createBean(type);
		}
		
	    return bean;
    }
	
	@Override
	@SuppressWarnings("unchecked")
    public <T> T tryGetBean(Class<? super T> type, String name) throws BeanException {
		Args.notNull(type,"bean type");
		Args.notNull(name,"bean name");
		
		BeanDefinitionBase bd = findBeanOrAliasDefinition(type, name);
		
		if(null != bd){
			return (T)doGetBean(bd);
		}
		
		return null;
    }
	
    @SuppressWarnings("unchecked")
    public <T> T tryCreateBean(Class<T> type, String name) throws BeanException {
		Args.notNull(type,"bean type");
		Args.notNull(name,"bean name");
		
		BeanDefinitionBase bd = findBeanOrAliasDefinition(type, name);
		
		if(null != bd){
			return (T)doCreateBean(bd);
		}
		
		return null;
    }
    
	@Override
    public <T> void setPrimaryBean(Class<T> type, T bean) {
		Args.notNull(type,"type");
		Args.notNull(bean,"bean");
		primaryBeans.put(type, bean);
    }

	@Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getBeans(Class<? super T> type) throws BeanException {
    	List<T> beans = (List<T>)typedBeansMap.get(type);
    	
    	if(null == beans){
    		beans = new ArrayList<T>(5);
    		
    		BeanListDefinition bld = beanListDefinitions.get(type.getName());
    		if(null != bld){
    			for(ValueDefinition vd : bld.getValues()){
    				Object bean = doCreateBean(vd);
    				if(!type.isAssignableFrom(bean.getClass())){
    					throw new BeanDefinitionException("The bean list's element must be instance of '" + type.getName() + "' in '" + bld.getSource() + "'");
    				}
    				beans.add((T)bean);
    			}
    		}else{
        		Set<BeanDefinitionBase> typeSet = beanTypeDefinitions.get(type);
        		if(null != typeSet){
        			for(BeanDefinitionBase bd : typeSet){
        				beans.add((T)doGetBean(bd));
        			}
        		}
    		}
    		
    		typedBeansMap.put(type.getName(),Collections.unmodifiableList(beans));
    	}
		
		return beans;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getBeans(Class<? super T> type, String qualifier) throws BeanException {
    	String key = type.getName() + "$" + qualifier;
    	
        List<T> beans = (List<T>)typedBeansMap.get(key);
    	
    	if(null == beans){
    		beans = new ArrayList<T>();
    		BeanListDefinition bld = beanListDefinitions.get(key);
    		if(null != bld){
    			for(ValueDefinition vd : bld.getValues()){
    				Object bean = doCreateBean(vd);
    				if(!type.isAssignableFrom(bean.getClass())){
    					throw new BeanDefinitionException("The bean list's element must be instance of '" + type.getName() + "' in '" + bld.getSource() + "'");
    				}
    				beans.add((T)bean);
    			}
    		}else{
    	    	Map<T, BeanDefinition> bds = getBeansWithDefinition(type);
    	    	
    	    	for(Entry<T, BeanDefinition> entry : bds.entrySet()){
    	    		BeanDefinition bd = entry.getValue();
    	    		if(bd.getQualifiers().contains(qualifier)){
    	    			beans.add(entry.getKey());
    	    		}
    	    	}
    		}
    		
    		typedBeansMap.put(key, beans);
    	}
    	return beans;
    }

	@Override
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getNamedBeans(Class<? super T> type) throws BeanException {
		Map<String,T> beans = (Map<String,T>)namedBeansMap.get(type);
		
		if(null == beans){
			beans = new LinkedHashMap<>(5);
			
			Set<BeanDefinitionBase> typeSet = beanTypeDefinitions.get(type);
			if(null != typeSet){
				for(BeanDefinitionBase bd : typeSet){
					if(!Strings.isEmpty(bd.getName())){
						if(!bd.isSingleton()){
							throw new BeanDefinitionException("the bean '" + bd.getName() + "' must be singleton, cannot cache the named beans for type '" + type.getName() + "'");
						}
						beans.put(bd.getName(), (T)doGetBean(bd));
					}
				}
			}
			
			namedBeansMap.put(type, Collections.unmodifiableMap(beans));
		}
		
	    return beans;
    }
	
    @Override
    @SuppressWarnings("unchecked")
    public <T> Map<T, BeanDefinition> getBeansWithDefinition(Class<? super T> type) throws BeanException {
		Map<T,BeanDefinition> beans = (Map<T,BeanDefinition>)typedInstances.get(type);
		
		if(null == beans){
			beans = new LinkedHashMap<>(5);
			
			Set<BeanDefinitionBase> typeSet = beanTypeDefinitions.get(type);
			if(null != typeSet){
				for(BeanDefinitionBase bd : typeSet){
					if(!bd.isSingleton()){
						throw new BeanDefinitionException("The bean '" + bd.getName() + "' must be singleton, cannot cache the named beans for type '" + type.getName() + "'");
					}
					beans.put((T)doGetBean(bd),bd);
				}
			}
			
			typedInstances.put(type, Collections.unmodifiableMap(beans));
		}
		
	    return beans;
    }

	@Override
    public boolean isSingleton(String beanId) throws NoSuchBeanException{
		BeanDefinition bd = findBeanDefinition(beanId);
		if(null == bd){
			throw new NoSuchBeanException("Bean '" + beanId + "' not found");
		}
	    return bd.isSingleton();
    }

	@Override
    public boolean isSingleton(Class<?> type) throws NoSuchBeanException {
		BeanDefinition bd = findPrimaryBeanDefinition(type);
		if(null == bd){
			throw new NoSuchBeanException("No primary bean defined for type '" + type.getName() + "'");
		}
	    return bd.isSingleton();
    }

	@Override
    public boolean isSingleton(Class<?> type, String name) throws NoSuchBeanException {
		BeanDefinition bd = findBeanDefinition(type,name);
		if(null == bd){
			throw new NoSuchBeanException("No bean named '" + name + "' for type '" + type.getName() + "'");
		}
	    return bd.isSingleton();
    }

	/**
	 * Register a shutdown hook with the JVM runtime, closing this context
	 * on JVM shutdown unless it has already been closed at that time.
	 * <p>This method can be called multiple times. Only one shutdown hook
	 * (at max) will be registered for each instance.
	 * @see java.lang.Runtime#addShutdownHook
	 * @see #close()
	 */
	public void registerShutdownHook() {
		if (this.shutdownHook == null) {
			// No shutdown hook registered yet.
			this.shutdownHook = new Thread() {
				@Override
				public void run() {
					doClose();
				}
			};
			Runtime.getRuntime().addShutdownHook(this.shutdownHook);
		}		
	}
	
	/**
	 * Close this container, releasing all resources and locks. 
	 * 
	 * <p>
	 * This includes destroying all cached singleton beans.
	 * 
	 * <p>
	 * This method can be called multiple times without side effects.
	 */
	public void close(){
		synchronized (this.startupShutdownMonitor) {
			doClose();
			// If we registered a JVM shutdown hook, we don't need it anymore now:
			// We've already explicitly closed the context.
			if (this.shutdownHook != null) {
				try {
					Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
				}
				catch (IllegalStateException ex) {
					// ignore - VM is already shutting down
				}
			}
		}		
	}
	
	protected void doClose(){
		boolean actuallyClose;
		synchronized (this.activeMonitor) {
			actuallyClose = !this.closed;
			this.closed = true;
		}

		if (actuallyClose) {
			// Destroy all cached singletons in the context's BeanFactory.
			destroyBeans();
		}		
	}
	
	protected void destroyBeans() {
		for(BeanDefinitionBase bd : allBeanDefinitions){
			Object instance = bd.getSingletonInstance();
			if(null != instance){
				Method destroyMethod = bd.getDestroyMethod();
				if(null != destroyMethod){
					try {
	                    Reflection.invokeMethod(destroyMethod,instance);
                    } catch (Exception e) {
                    	log.warn("Error destroying bean '" + bd + "' : " + e.getMessage(),e);
                    }
				}else if(instance instanceof Disposable){
					try {
	                    ((Disposable)instance).dispose();
                    } catch (Throwable e) {
                    	log.warn("Error disposing bean '" + bd + "' : " + e.getMessage(),e);
                    }
				}else if(instance instanceof Closeable){
					try {
	                    ((Closeable)instance).close();
                    } catch (Exception e) {
                    	log.warn("Error closing bean '" + bd + "' : " + e.getMessage(),e);
                    }
				}
			}
		}
	}

	protected Set<BeanDefinitionBase> getAllBeanDefinitions() {
		return allBeanDefinitions;
	}
	
	protected Map<String, AliasDefinition> getAliasDefinitions() {
		return aliasDefinitions;
	}

	protected BeanDefinitionBase findBeanDefinition(BeanReference br){
		if(Strings.isNotEmpty(br.getTargetId())){
			return findBeanDefinition(br.getTargetId());
		}
		if(Strings.isNotEmpty(br.getBeanName()) || null != br.getBeanType()){
			return findBeanOrAliasDefinition(br.getBeanType(),br.getBeanName());
		}
		return null;
	}

	protected BeanDefinitionBase findBeanDefinition(String id){
		return identifiedBeanDefinitions.get(id);
	}
	
	protected BeanDefinitionBase findBeanOrAliasDefinition(String id){
		BeanDefinitionBase bd = identifiedBeanDefinitions.get(id);
		if(null == bd){
			AliasDefinition ad = aliasDefinitions.get(id);
			if(null != ad){
				bd = identifiedBeanDefinitions.get(ad.getId());
			}
		}
		return bd;
	}
	
	protected BeanDefinitionBase findBeanDefinition(Class<?> beanType, String name){
		return namedBeanDefinitions.get(beanType.getName() + "$" + name);
	}
	
	protected BeanDefinitionBase findBeanOrAliasDefinition(Class<?> beanType, String name){
		String key = beanType.getName() + "$" + name;
		BeanDefinitionBase bd = namedBeanDefinitions.get(key);
		if(null == bd){
			AliasDefinition ad = aliasDefinitions.get(key);
			if(null != ad){
				bd = namedBeanDefinitions.get(ad.getType().getName() + "$" + ad.getName());
			}
		}
		return bd;
	}
	
	protected BeanDefinitionBase findPrimaryBeanDefinition(Class<?> beanType){
		BeanDefinitionBase bd = primaryBeanDefinitions.get(beanType);
		
		if(bd == NULL_BD){
			return null;
		}
		
		if(bd != null){
			return bd;
		}
		
		Set<BeanDefinitionBase> bds = beanTypeDefinitions.get(beanType);
		if(null != bds && bds.size() == 1){
			return bds.iterator().next();
		}

        bds = beanClassDefinitions.get(beanType);
        if(null != bds && bds.size() == 1){
            return bds.iterator().next();
        }
		
		return null;
	}
	
	protected AliasDefinition findAliasDefinition(String alias){
		return aliasDefinitions.get(alias);
	}
	
	protected void checkAfterLoading(){
		for(AliasDefinition aliasDefinition : aliasDefinitions.values()){
			if(!Strings.isEmpty(aliasDefinition.getId())){
				if(null == findBeanDefinition(aliasDefinition.getId())){
					throw new BeanDefinitionException("bean id '" + aliasDefinition.getId() + "' defined in alias '" + 
													  aliasDefinition.getAlias() + "' not found, source : " + 
													  aliasDefinition.getSource());
				}
				
				BeanDefinitionBase bean = findBeanDefinition(aliasDefinition.getAlias());
				if(null != bean){
					throw new BeanDefinitionException("the alias name '" + aliasDefinition.getAlias() + 
													  "' aleady used by another bean, please check the xmls : " + 
													  aliasDefinition.getSource() + "," + bean.getSource());
				}
			}else{
				if(null == findBeanDefinition(aliasDefinition.getType(),aliasDefinition.getName())){
					throw new BeanDefinitionException("Bean name '" + aliasDefinition.getName() + "' defined in alias '" + 
							  aliasDefinition.getAlias() + "' not found, source : " + 
							  aliasDefinition.getSource());

				}
				
				BeanDefinitionBase bean = findBeanDefinition(aliasDefinition.getType(),aliasDefinition.getAlias());
				if(null != bean){
					throw new BeanDefinitionException("The alias name '" + aliasDefinition.getAlias() + 
													  "' aleady used by another bean, please check the xmls : " + 
													  aliasDefinition.getSource() + "," + bean.getSource());
				}
			}
		}
	}
	
	protected void initAfterLoading(){
		for(InitDefinition init : initDefinitions){
			String initClassName  = init.getInitClassName();
			String initMethodName = init.getInitMethodName();
			
	        Class<?> clazz = Classes.tryForName(initClassName);
	        
	        if(null == clazz){
	        	throw new BeanDefinitionException("the init class name '" + initClassName + "' not found, source : " + init.getSource());
	        }
	        
	        Method initMethod = null;
	        
	        if(!Strings.isEmpty(initMethodName)){
	        	initMethod = Reflection.findMethod(clazz, initMethodName);
	        	
	        	if(null == initMethod){
	        		throw new BeanDefinitionException("the init method '" + initMethodName + "' with no parameters not found in class '" + clazz.getName() + "', source : " + init.getSource());
	        	}
	        	
	        	if(!Modifier.isStatic(initMethod.getModifiers())){
	        		throw new BeanDefinitionException("the init method '" + initMethodName + "' in class '" + clazz.getName() + "' must be static, source : " + init.getSource());
	        	}
	        	
	        	try {
	                Reflection.invokeMethod(initMethod,null);
                } catch (Exception e) {
                	throw new BeanException("Error invoking the init method '" + initMethodName + "' in class '" + initClassName + "', source : " + init.getSource());
                }
	        }
		}
	}
	
	protected void initBeanFactoryInitializableBeans(){
		//bean factory initializables
		for(BeanFactoryInitializable initializable : beanFactoryInitializables){
			try {
	            initializable.postInit(appContext, beanFactory);
            } catch (Throwable e) {
            	Exceptions.uncheckAndThrow(e);
            }
		}		
	}
	
	protected void resolveAfterLoading() {
		//bean post processors
		List<BeanProcessor> postProcessorList = new ArrayList<>();
		for(BeanDefinitionBase bd : postProcessorBeans){
			postProcessorList.add((BeanProcessor)doGetBean(bd));
		}
		this.processors = postProcessorList.toArray(new BeanProcessor[]{});

		//create factory beans
		for(BeanDefinitionBase bd : typedFactoryDefinitions.values()){
		    for(FactoryDefinition fd : bd.getFactoryDefs()) {
		        typedFactoryBeans.put(fd.getTargetType(),(FactoryBean)doGetBean(bd));    
		    }
		}

        for(BeanProcessor processor : processors) {
            try {
                processor.postCreateProcessors(getAppContext(), this);
            } catch (Throwable throwable) {
                throw Exceptions.uncheck(throwable);
            }
        }
	}
	
	protected void initNonLazyBeans() {
		for(BeanDefinitionBase bd : allBeanDefinitions){
			if(!bd.isLazyInit()){
				doGetBean(bd);
			}
		}
	}
	
	protected Object doGetBean(BeanDefinitionBase bd){
		if(bd.isSingleton()){
			Object instance = bd.getSingletonInstance();
			if(null != instance){
				return instance;
			}
		}
		return doCreateBean(bd);
	}
	
	protected Object doCreateBean(BeanDefinitionBase bd){
		if(initializing){
			throw new IllegalStateException("Cannot get bean when this container is initializing");
		}
		
		Object bean = null;
		
		if(isBeanCurrentlyInCreation(bd)){
			bean = getBeanCurrentlyInCreation(bd);
			
			if(null != bean){
				return bean;
			}
			
			throw new BeanCreationException("Requested bean '" + bd + "' is currently in creation: Is there an unresolvable circular reference?");
		}
		
		beforeBeanCreation(bd);
		
		bean = doBeanCreation(bd);
		
		afterBeanCreation(bd);

        bd.setInited(true);
		
		return bean;
	}
	
	protected Object doBeanCreation(BeanDefinitionBase bd){
		log.trace("Creating bean {}",bd);
		
		Object          bean = null;
		ValueDefinition vd   = bd.getValueDefinition();
		try {
	        if(null != vd){
	        	bean = doResolveValue(bd, vd, null);
	        }else{
	        	bean = doBeanCreationByConstructor(bd);
	        }
	        
	        setBeanCurrentlyInCreation(bd, bean);
	        
	        doBeanAware(bd, bean);
	        
	        doBeanConfigure(bd, bean);
	        if(bean instanceof PostConfigureBean){
	            ((PostConfigureBean) bean).postConfigure(appContext.getBeanFactory(), appContext.getConfig());
	        }
	        
	        doBeanSetProperties(bd,bean);
	        doBeanInjection(bd,bean);
            doBeanInvokeMethods(bd,bean);
	        
			if(null != bd.getInitMethod()){
				Reflection.invokeMethod(bd.getInitMethod(), bean);
			}
			
			//null if post processors not resolved, see #resolveAfterLoading 
			if(null != processors){
				for(int i=0;i<processors.length;i++){
					processors[i].postCreateBean(appContext, beanFactory, bd, bean);
				}
			}
			
	        if(bean instanceof PostCreateBean){
	        	((PostCreateBean) bean).postCreate(appContext.getBeanFactory());
	        }
	        
	        if(bean instanceof LoadableBean){
	        	if(!((LoadableBean) bean).load(appContext.getBeanFactory())){
	        		return null;
	        	}
	        }
        } catch (Throwable e) {
        	throw errorCreateBean(bd,e);
        }
		
		if(bd.isSingleton()){
			doBeanValidation(bd,bean);
			bd.setSingletonInstance(bean);
		}
		
		return bean;
	}
	
	protected void postBeanCreation(Object bean) {
		
	}

    protected Object resolveConfigProperty(BeanDefinitionBase bd, ConfigProperty p, String name, Class<?> type, Type genericType) {
        String keyPrefix = bd.getConfigurationPrefix();
        if(!Strings.isEmpty(keyPrefix)) {
            char lastChar = keyPrefix.charAt(keyPrefix.length() - 1);
            if(Character.isLetter(lastChar) || Character.isDigit(lastChar)) {
                keyPrefix = keyPrefix + ".";
            }
        }else{
            keyPrefix = "";
        }
        if(p.value().length > 0) {
            for(String key : p.value()) {
                Object value = resolveConfigProperty(keyPrefix + key, type, genericType);
                if(null != value){
                    return value;
                }
            }
        }else{
            Object value = resolveConfigProperty(keyPrefix + name, type, genericType);
            if(null != value) {
                return value;
            }

            value = resolveConfigProperty(keyPrefix + Strings.lowerHyphen(name), type, genericType);
            if(null != value) {
                return value;
            }
        }

        return null;
    }

    protected Object resolveConfigProperty(String key, Class<?> type, Type genericType) {
        String prop = appContext.getConfig().getProperty(key);

        if(null != prop){
            if(!Strings.isEmpty(prop = prop.trim())) {
                try {
                    return Converts.convert(prop, type, genericType);
                } catch (Exception e) {
                    throw new BeanCreationException("Error resolve property for type '" + type +
                            "' using config key '" + key + "', " + e.getMessage(), e);
                }
            }
        }

        return null;
    }
	
	protected void doBeanConfigure(BeanDefinitionBase bd, Object bean) throws Throwable {
		if(null != bd && bd.isConfigurable()) {
			BeanType bt = null == bd ? BeanType.of(bean.getClass()) : bd.getBeanClassType();
			
			String keyPrefix = bd.getConfigurationPrefix();
			if(!Strings.isEmpty(keyPrefix)) {
				char lastChar = keyPrefix.charAt(keyPrefix.length() - 1);
				if(Character.isLetter(lastChar) || Character.isDigit(lastChar)) {
					keyPrefix = keyPrefix + ".";
				}
			}

            Set<ReflectField> done = new HashSet<>();
			
			for(BeanProperty bp : bt.getProperties()){
				if(!bp.isWritable()) {
					continue;
				}

                ConfigProperty a = bp.getAnnotation(ConfigProperty.class);
                if(!Property.class.isAssignableFrom(bp.getType()) && null == a) {
                    continue;
                }

                doBeanConfigure(bean, bp, keyPrefix, a);

                if(null != bp.getReflectField()) {
                    done.add(bp.getReflectField());
                }
			}

            for(ReflectField field : bt.getReflectClass().getFields()) {
                if(done.contains(field)) {
                    continue;
                }

                if(Property.class.isAssignableFrom(field.getType())) {
                    doBeanConfigure(bean, field, keyPrefix, field.getAnnotation(ConfigProperty.class));
                    continue;
                }

                ConfigProperty a = field.getAnnotation(ConfigProperty.class);
                if(null == a) {
                    continue;
                }

                doBeanConfigure(bean, field, keyPrefix, a);
            }

            done.clear();
		}
	}


    protected void doBeanConfigure(Object bean, ReflectValued v, String keyPrefix, ConfigProperty a) {
        if(null != a && a.value().length > 0) {
            for(String key : a.value()) {
                if(doBeanConfigure(bean, v, keyPrefix + key)) {
                    break;
                }
            }
        }else{
            if(doBeanConfigure(bean, v, keyPrefix + v.getName())) {
                return;
            }

            if(doBeanConfigure(bean, v, keyPrefix + Strings.lowerHyphen(v.getName()))) {
                return;
            }
        }
    }
	
	protected boolean doBeanConfigure(Object bean, ReflectValued v, String key) {
        if(Property.class.isAssignableFrom(v.getType())) {
            doBeanConfigureDynaProperty(bean, v, key);
            return true;
        }

        if(v.getType().isArray()) {
            String[] array = getAppConfig().getArrayProperty(key);
            if(null != array) {
                v.setValue(bean, array);
                return true;
            }
        }

        if(List.class.equals(v.getType())) {
            String[] array = getAppConfig().getArrayProperty(key);
            if(null != array) {
                List<String> list = new ArrayList<>();
                Collections2.addAll(list, array);
                v.setValue(bean, list);
                return true;
            }
        }

        if(Set.class.equals(v.getType())) {
            String[] array = getAppConfig().getArrayProperty(key);
            if(null != array) {
                Set<String> set = new LinkedHashSet<>();
                Collections2.addAll(set, array);
                v.setValue(bean, set);
                return true;
            }
        }

        String prop = getAppConfig().getProperty(key);
        if(null != prop) {
            if(prop.length() > 0) {
                try {
                    Object value;
                    if(Classes.isSimpleValueType(v.getType())) {
                        value = Converts.convert(prop, v.getType(), v.getGenericType());
                    }else{
                        value = JSON.decode(prop, v.getType());
                    }
                    v.setValue(bean, value);
                } catch (Exception e) {
                    throw new BeanCreationException("Error configure property '" + bean.getClass().getName() + "#" + v.getName() +
                            "' using config key '" + key + "', " + e.getMessage(), e);
                }
            }
			return true;
		}

		return false;
	}

    protected void doBeanConfigureDynaProperty(Object bean, ReflectValued v, String key) {
        AppConfig config = getAppConfig();

        Class<?> type  = v.getType();
        Property value = (Property)v.getValue(bean);

        if(null != value) {
            config.bindDynaProperty(key, type, value);
            return;
        }

        if(type.equals(StringProperty.class)) {

            value = config.getDynaProperty(key);

        }else if(type.equals(IntegerProperty.class)) {

            value = config.getDynaIntegerProperty(key);

        }else if(type.equals(LongProperty.class)) {

            value = config.getDynaLongProperty(key);

        }else if(type.equals(BooleanProperty.class)) {

            value = config.getDynaBooleanProperty(key);

        }else if(type.equals(DoubleProperty.class)) {

            value = config.getDynaDoubleProperty(key);

        }else if(type.equals(Property.class)){

            Class<?> valueType = Types.getActualTypeArgument(v.getGenericType());

            value = config.getDynaProperty(key, valueType);

        }else{
            throw new IllegalStateException("Not supported property type '" + type + "'");
        }

        if(null != value) {
            v.setValue(bean, value);
        }
    }

    protected void doBeanInjection(BeanDefinitionBase bd,Object bean) throws Throwable {
        BeanFactory factory = null != beanFactory ? beanFactory : this;
        
        BeanType bt = null == bd ? null : bd.getBeanClassType();
        if(null == bt) {
            bt = BeanType.of(bean.getClass());
        }

        if (bean instanceof PreInjectBean) {
            ((PreInjectBean) bean).preInject(factory);
        }

        for (BeanProperty bp : bt.getProperties()) {
            if (bp.isWritable()) {
                Inject inject = bp.getAnnotation(Inject.class);
                if (null == inject || !inject.value()) {
                    continue;
                }

                //Skip simple type
                if (Types.isSimpleType(bp.getType(), bp.getGenericType())) {
                    continue;
                }

                log.trace("Injecting property '{}'", bp.getName());

                try {
                    //skip when bean value already set.
                    if (null != bp.getReflectField() && !bp.getType().isPrimitive()) {
                        if (null != bp.getReflectField().getValue(bean)) {
                            continue;
                        }
                    }

                    Object injectedBean = resolveInjectValue(factory, bd, bp.getName(), bp.getType(), bp.getGenericType(), bp.getAnnotations());

                    if (null != injectedBean) {
                        bp.setValue(bean, injectedBean);
                    } 
                } catch (Exception e) {
                    log.error("Error injecting property '{}' in bean '{}'", bp.getName(), bd, e);
                    throw e;
                }
            }
        }

        for (ReflectField rf : bt.getReflectClass().getFields()) {
            if (rf.isAnnotationPresent(Inject.class)) {
                try {
                    //skip when bean value already set.
                    if (null != rf.getValue(bean)) {
                        continue;
                    }

                    Object injectedBean = resolveInjectValue(factory, bd, rf.getName(), rf.getType(), rf.getGenericType(), rf.getAnnotations());

                    if (null != injectedBean) {
                        rf.setValue(bean, injectedBean);
                    }
                } catch (Exception e) {
                    log.error("Error injecting field '{}' in bean '{}'", rf.getName(), bd, e);
                    throw e;
                }
            }
        }

        if (bean instanceof PostInjectBean) {
            ((PostInjectBean) bean).postInject(factory);
        }
	}
	
    @SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object resolveInjectValue(BeanFactory factory, BeanDefinitionBase bd, String name, Class<?> type,Type genericType,Annotation[] annotations) {
		Inject inject = Classes.getAnnotation(annotations, Inject.class);
		if(null != inject && !inject.value()){
			return null;
		}
		
		if(type.equals(BeanFactory.class)) {
		    return factory;
		}
		
		if(type.equals(AppConfig.class)) {
		    return null == appContext ? null : appContext.getConfig();
		}
		
		if(type.equals(AppContext.class)) {
		    return appContext;
		}
		
		Object injectedBean = null;
		
		if(null == bd || !Iterables.any(bd.getProperties(), Predicates.<PropertyDefinition>nameEquals(name))){
			//inject by bean's id
			if(null != inject && !Strings.isEmpty(inject.id())){
				injectedBean = factory.getBean(inject.id());
			}else{
				Class  beanType = null == inject ? null : inject.type();
				String beanName = null == inject ? null : inject.name();
				
				if(Lazy.class.equals(type)){
					boolean nullable = Classes.isAnnotatioinPresent(annotations, NotNull.class) || 
					                   Classes.isAnnotatioinPresent(annotations, M.class);
					
					boolean required = Classes.isAnnotatioinPresent(annotations, NotEmpty.class) ||
					                   Classes.isAnnotatioinPresent(annotations, R.class);
					
					Type typeArgument = Types.getTypeArgument(genericType);
					Class<?> acturalTypeArgument = Types.getActualType(typeArgument);
					
					if(List.class.equals(acturalTypeArgument)){
						if(null == beanType || Object.class.equals(beanType)){
							beanType = Types.getActualTypeArgument(typeArgument);
						}
						injectedBean = new LazyBeanList(factory, beanType,
						                                null == inject ? null : inject.qualifier(),
						                                required);
					}else{
						if(null == beanType || Object.class.equals(beanType)){
							beanType = acturalTypeArgument;
						}
						injectedBean = new LazyBean(factory, beanType, beanName, 
						                            null == inject ? false : inject.namedOrPrimary(), 
						                            nullable, required);
					}
				}else if(List.class.equals(type) || BeanList.class.equals(type)){
					if(!Strings.isEmpty(beanName)){
						throw new BeanCreationException("The injected List property does not support the 'name' annotation field in bean " + bd);
					}
					
					if(null == beanType || Object.class.equals(beanType)){
						beanType = Types.getActualTypeArgument(genericType);
					}
					
					if(Beans.isSimpleProperty(beanType)){
						return null;
					}
					
					List<Object> beans;
					if(null != inject && !Strings.isEmpty(inject.qualifier())){
						beans = getBeans(beanType,inject.qualifier());
					}else{
						beans = getBeans(beanType);	
					}
					
					if(BeanList.class.equals(type)) {
					    injectedBean = new CopyOnWriteArrayBeanList(beans);
					}else{
					    injectedBean = new ArrayList(beans);
					}
				}else if(type.isArray()){
					if(!Strings.isEmpty(beanName)){
						throw new BeanCreationException("Auto Injected Array property does not support the 'name' annotation field in bean " + bd);
					}
					
					if(null == beanType || Object.class.equals(beanType)){
						beanType = type.getComponentType();
					}
					
					if(Beans.isSimpleProperty(beanType)){
						return null;
					}
					
					if(null != inject && !Strings.isEmpty(inject.qualifier())){
						injectedBean = getBeans(beanType,inject.qualifier()).toArray((Object[])Array.newInstance(type.getComponentType(), 0));
					}else{
						injectedBean = getBeans(beanType).toArray((Object[])Array.newInstance(type.getComponentType(), 0));
					}
				/*	
				}else if(Map.class.equals(bp.getType())){
					if(!Strings.isEmpty(beanName)){
						throw new BeanCreationException("Autowired Map property does not support the 'name' annotation field in bean " + bd);
					}
					
					if(null == beanType || Object.class.equals(beanType)){
						beanType = Types.getActualType(Types.getTypeArguments(bp.getGenericType())[0]);
					}
					
					injectedBean = factory.getNamedBeans(beanType);
				*/
				}else{
					if(null == beanType || Object.class.equals(beanType)){
						beanType = type;
					}
					
					if(Strings.isEmpty(beanName)){
						injectedBean = factory.tryGetBean(beanType);	
					}else{
						injectedBean = factory.tryGetBean(beanType, beanName);
						if(null == injectedBean && null != inject && inject.namedOrPrimary()){
							injectedBean = factory.tryGetBean(beanType);
						}
					}
				}
			}
		}
		
		return injectedBean;
	}
	
	protected void doBeanAware(BeanDefinitionBase bd,Object bean){
		if(bean instanceof ServletContextAware){
			if(appContext.isServletEnvironment()){
				((ServletContextAware) bean).setServletContext(appContext.getServletContext());
			}
		}
		
		if(bean instanceof BeanFactoryAware){
			((BeanFactoryAware) bean).setBeanFactory(beanFactory);
		}
		
		if(bean instanceof AppContextAware){
			((AppContextAware) bean).setAppContext(appContext);
		}
		
		if(bean instanceof AppConfigAware){
			((AppConfigAware) bean).setAppConfig(appContext.getConfig());
		}
		
		//TODO : check the bean name is empty?
		if(bean instanceof BeanNameAware){
			((BeanNameAware) bean).setBeanName(bd.getName());
		}
	}
	
	protected void doBeanValidation(BeanDefinitionBase bd,Object bean){
	    validateFields(bd, bean);
	}
	
	protected void doBeanValidation(Object bean){
	    validateFields(bean);
	}
	
    @SuppressWarnings("unchecked")
    protected Object doBeanCreationByConstructor(BeanDefinitionBase bd){
		Class<?> beanClass = bd.getBeanClass();
		Object   bean      = null;
		
		//TODO : factoryBean

		List<ArgumentDefinition> constructorArguments = bd.getConstructorArguments();
		if(constructorArguments.isEmpty()){
            ReflectClass rc = ReflectClass.of(beanClass);
            if(rc.hasDefaultConstructor()) {
                bean = rc.newInstance();
            }else{
                if(rc.getConstructors().length == 1) {
                    ReflectConstructor c = rc.getConstructors()[0];
                    Object[] args = new Object[c.getParameters().length];

                    for(int i=0;i<args.length;i++) {
                        ReflectParameter p = c.getParameters()[i];

                        if(p.isAnnotationPresent(Inject.class)) {
                            args[i] = resolveInjectValue(beanFactory, bd, p.getName(), p.getType(), p.getGenericType(), p.getAnnotations());
                            continue;
                        }

                        ConfigProperty a = p.getAnnotation(ConfigProperty.class);
                        if(null != a) {
                            args[i] = resolveConfigProperty(bd, a, p.getName(), p.getType(), p.getGenericType());
                            continue;
                        }
                    }
                    return c.newInstance(args);
                }else{
                    throw new BeanCreationException("Cannot create bean without default constructor, check the bean : " + bd);
                }
            }
		}else{
			bean = Reflection.newInstance(bd.getConstructor(),doResolveArgs(bd, constructorArguments));
		}
		
		return bean;
	}
	
    protected void doBeanSetProperties(BeanDefinitionBase bd,Object bean){
		BeanType beanType = bd.getBeanClassType();
		
		for(PropertyDefinition pd : bd.getProperties()){
			BeanProperty bp = beanType.getProperty(pd.getName());
			
			if(null == bp){
				throw new BeanCreationException("No such property '" + pd.getName() + " in bean " + bd);
			}
			
			bp.setValue(bean, doResolveValue(bd, pd.getValueDefinition(), pd.getDefaultValue()));
		}
    }
    
    protected void doBeanInvokeMethods(BeanDefinitionBase bd,Object bean){
		for(InvokeDefinition invoke : bd.getInvokes()){
			Method m = invoke.getMethod();
			try {
	            m.invoke(bean, doResolveArgs(bd, invoke.getArguments()));
            } catch (Exception e) {
            	throw new BeanCreationException("Error invoke method '" + m.getName() + "' of bean '" + bd + "' : " + e.getMessage(),e);
            }
		}
    }
    
    protected Object doCreateBean( ValueDefinition vd){
    	Object definedValue = vd.getDefinedValue();
    	
    	if(null == definedValue){
    		throw new IllegalStateException("The defined value must not be null");
    	}
    	
		if(definedValue instanceof BeanReference){
			return doGetBeanReferenceInstance((BeanReference)definedValue);
		}else if(definedValue instanceof BeanDefinitionBase){
			return doBeanCreation((BeanDefinitionBase)definedValue);
		}
		
		throw new IllegalStateException("The value definition must be bean reference or bean definition");
    }
    
    protected Object doGetBeanReferenceInstance(BeanReference br){
    	throw new IllegalStateException("Not supported yet");
    }
    
	@SuppressWarnings({ "rawtypes", "unused" })
    protected Object doResolveValue(BeanDefinitionBase bd,ValueDefinition vd, String defaultValue){
		if(vd.isResolved()){
			Object value = vd.getResolvedValue();
			
			if(value instanceof Supplier){
				value = ((Supplier) value).get();
			}
			
			return value;
		}
		
		Object definedValue = vd.getDefinedValue();
		
		if(null == definedValue){
			if(Strings.isEmpty(defaultValue)){
				vd.resolved(null);
			}else{
				definedValue = placeholderResolver.resolveString(defaultValue);
				vd.resolved(doConvertValue(definedValue, vd.getDefinedType()));
			}
			return null;
		}
		
		if(definedValue instanceof BeanReference){
			Supplier<Object> beanReferenceValue = createBeanReferenceValue(bd, (BeanReference)definedValue);
			vd.resolved(beanReferenceValue);
			return beanReferenceValue.get();
		}else if(definedValue instanceof BeanDefinitionBase){
			Supplier<Object> beanValue = createBeanValue(bd,(BeanDefinitionBase)definedValue);
			vd.resolved(beanValue);
			return beanValue.get();
		}else if(definedValue instanceof Collection){
			return doResolveCollection(bd, vd);
		}else if(definedValue.getClass().isArray()){
			return doResolveArray(bd, vd);
		}else if(definedValue instanceof Map){
			return doResolveMap(bd, vd);
		}else{
			Object resolvedValue = definedValue;
			
			if(resolvedValue instanceof String){
			    
			    if(!Strings.isEmpty(defaultValue)) {
			        resolvedValue = placeholderResolver.resolveString( (String)resolvedValue, defaultValue);
			    }else{
			        resolvedValue = placeholderResolver.resolveString( (String)resolvedValue);    
			    }
			    
			}else if(null == resolvedValue && !Strings.isEmpty(defaultValue)){
				resolvedValue = placeholderResolver.resolveString(defaultValue);
			}
			
			vd.resolved(doConvertValue(resolvedValue, vd.getDefinedType()));
			return resolvedValue;
		}
	}
	
	/**
	 * Return whether the specified bean is currently in creation (within the current thread).
	 */
	protected boolean isBeanCurrentlyInCreation(BeanDefinition bd) {
		Map<BeanDefinitionBase,Object> curVal = this.beansCurrentlyInCreation.get();
		return (curVal != null && curVal.containsKey(bd));
	}
	
	protected Object getBeanCurrentlyInCreation(BeanDefinition bd){
		Map<BeanDefinitionBase,Object> curVal = this.beansCurrentlyInCreation.get();
		return null == curVal ? null : curVal.get(bd);
	}
	
	protected void setBeanCurrentlyInCreation(BeanDefinitionBase bd,Object bean){
		Map<BeanDefinitionBase,Object> curVal = this.beansCurrentlyInCreation.get();
		if(null != curVal){
			curVal.put(bd, bean);
		}
	}

	protected void beforeBeanCreation(BeanDefinitionBase bd) {
		Map<BeanDefinitionBase,Object> curVal = this.beansCurrentlyInCreation.get();
		if(null == curVal){
			curVal = new HashMap<>(5);
			this.beansCurrentlyInCreation.set(curVal);
		}
		curVal.put(bd, null);
	}

	protected void afterBeanCreation(BeanDefinition bd) {
		Map<BeanDefinitionBase,Object> curVal = this.beansCurrentlyInCreation.get();
		if(null != curVal){
			curVal.remove(bd);
			if(curVal.isEmpty()){
				this.beansCurrentlyInCreation.remove();
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
    protected Object doResolveCollection(final BeanDefinitionBase bd,final ValueDefinition vd){
		Supplier<Collection> func = new Supplier<Collection>() {
			@Override
			public Collection get() {
				Collection definedCol = (Collection)vd.getDefinedValue();
				Collection createdCol = createCollection(bd, vd,definedCol);

				for(Object element : definedCol){
					Object value = element instanceof ValueDefinition ? doResolveValue(bd, (ValueDefinition)element, null) : element;

					createdCol.add(doConvertValue(value, vd.getDefinedElementType()));
				}

				return createdCol;
            }
		};
		
		vd.resolved(func);
		
		return func.get();
	}
	
	protected Object[] doResolveArgs(final BeanDefinitionBase bd,final List<ArgumentDefinition> argDefs){
		List<Object> args = new ArrayList<>();
		
		for(ArgumentDefinition ad : argDefs){
			if(null != ad.getValueDefinition().getDefinedType()){
				args.add(Converts.convert(doResolveValue(bd, ad.getValueDefinition(),ad.getDefaultValue()),ad.getValueDefinition().getDefinedType()));
			}else{
				args.add(doResolveValue(bd, ad.getValueDefinition(),ad.getDefaultValue()));
			}
		}
		
		return args.toArray(new Object[args.size()]);
	}
	
    protected Object doResolveArray(final BeanDefinitionBase bd,final ValueDefinition vd){
		Supplier<Object> func = new Supplier<Object>() {
			@Override
			public Object get() {
                Object definedArray = vd.getDefinedValue();
                Object createdArray = null;
                
                int arrayLength = Array.getLength(definedArray);
                createdArray = Array.newInstance(definedArray.getClass().getComponentType(), arrayLength);
                
                for(int i=0;i<arrayLength;i++){
                	Object element = Array.get(definedArray, i);
                	Object value  = element instanceof ValueDefinition ? doResolveValue(bd, (ValueDefinition)element, null) : element;
                	
                	Array.set(createdArray, i, doConvertValue(value, vd.getDefinedElementType()));
                }
                
                return createdArray;
            }
		};
		
		vd.resolved(func);
		
		return func.get();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
    protected Object doResolveMap(final BeanDefinitionBase bd,final ValueDefinition vd){
		Supplier<Object> func = new Supplier<Object>() {
			@Override
			public Object get() {
                Map definedMap = (Map)vd.getDefinedValue();
                Map createdMap = null;
                
                if(null == vd.getDefinedType()){
                	createdMap = new LinkedHashMap(definedMap.size());
                }else{
                	Class<?> clzz = vd.getDefinedType();
                	if(clzz.isInterface() || Modifier.isAbstract(clzz.getModifiers())){
                		boolean isAssignableFrom = clzz.isAssignableFrom(vd.getDefinedValue().getClass());
            			if(!isAssignableFrom){
            				throw new BeanDefinitionException("Error create instance of collection type '" + vd.getDefinedType() + "' bean " + 
            						bd + " cause "+clzz+" is interface or abstract class and the value type " + 
            						vd.getDefinedValue().getClass().getName() + " is not the subclass of "+clzz);
            			}else{
            				clzz = vd.getDefinedValue().getClass();
            			}
                	}
                	createdMap = (Map)Reflection.newInstance(clzz);
                }

                for(Object object : definedMap.entrySet()){
                	Entry entry  = (Entry)object;
                	Object key   = entry.getKey();
                	Object val   = entry.getValue();
                	
                	if(key instanceof ValueDefinition){
                		key = doResolveValue(bd, (ValueDefinition)key, null);
                	}
                	
                	if(val instanceof ValueDefinition){
                		val = doResolveValue(bd, (ValueDefinition)val, null);
                	}
                	
                	createdMap.put(doConvertValue(key, vd.getDefinedKeyType()), doConvertValue(val, vd.getDefinedElementType()));
                }
                
                return createdMap;
            }
		};
		
		vd.resolved(func);
		
		return func.get();
	}
	
	protected Object doConvertValue(Object value,Class<?> targetType){
		if(targetType == null){
			return value;
		}
		return Converts.convert(value, targetType);
	}
	
	@SuppressWarnings("unchecked")
    protected Collection<Object> createCollection(BeanDefinitionBase bd, ValueDefinition vd,Collection<Object> col){
		try {
			Class<?> definedType = vd.getDefinedType();
        	if(definedType != null){
        		if(definedType.isInterface() || Modifier.isAbstract(definedType.getModifiers())){
        			boolean isAssignableFrom = definedType.isAssignableFrom(vd.getDefinedValue().getClass());
        			if(!isAssignableFrom){
        				throw new BeanDefinitionException("Error create instance of collection type '" + vd.getDefinedType() + "' bean " + 
        						bd + " cause "+definedType+" is interface or abstract class and the value type " + 
        						vd.getDefinedValue().getClass().getName() + " is not the subclass of "+definedType);
        			}else{
        				definedType = vd.getDefinedValue().getClass();
        			}
        		}
        		return (Collection<Object>)Reflection.newInstance(definedType);
        	}
        } catch (ReflectException e) {
        	throw new BeanDefinitionException("Error create instance of collection type '" + vd.getDefinedType() + "' bean " + bd, e);
        }
        	
        if(col instanceof List){
        	return new ArrayList<Object>();
        }else if(col instanceof Set){
        	return new ArrayList<Object>();
        }else{
        	throw new BeanDefinitionException("Invalid collection type '" + col.getClass().getName() + "', only 'list' and 'set' are supported, please check the bean " + bd);
        }
	}
	
	protected Supplier<Object> createBeanReferenceValue(BeanDefinitionBase bd,final BeanReference br){
		BeanDefinition referenced = findBeanDefinition(br);
		if(referenced == null){
			throw new BeanDefinitionException("The referenced bean '" + br.getTargetId() + "' not exists, please check the bean : " + bd);
		}
		br.setTargetBeanDefinition(referenced);
		
		return new Supplier<Object>() {
			@Override
            public Object get() {
				if(Strings.isNotEmpty(br.getTargetId())){
					return BeanContainer.this.getBean(br.getTargetId());
				}if(Strings.isNotEmpty(br.getBeanName()) && null!=br.getBeanType()){
					return BeanContainer.this.getBean(br.getBeanType(),br.getBeanName());
				}
				return null;
            }
		};
	}
	
	protected Supplier<Object> createBeanValue(BeanDefinition bd,final BeanDefinitionBase value){
		return new Supplier<Object>() {
			@Override
            public Object get() {
	            return BeanContainer.this.doBeanCreation(value);
            }
		};
	}
	
	protected BeanCreationException errorCreateBean(BeanDefinitionBase bd,Throwable e){
		if(e instanceof BeanCreationException){
			return (BeanCreationException)e;
		}
		return new BeanCreationException(e.getMessage() + ", error create bean " + bd, e);
	}
	
	protected void addInitDefinition(InitDefinition initDefinition){
		initDefinitions.add(initDefinition);
	}
	
	protected void addAliasDefinition(AliasDefinition ad){
		if(!Strings.isEmpty(ad.getId())){
			AliasDefinition existsAliasDefinition = aliasDefinitions.get(ad.getAlias());
			if(null != existsAliasDefinition){
				throw new BeanDefinitionException("Found duplicated bean alias '" + ad.getAlias()  + "' in resource : " + 
						ad.getSource() + ", " + 
						ad.getSource());
			}
			aliasDefinitions.put(ad.getAlias(),ad);
		}else{
			String key =  ad.getType().getName() + "$" + ad.getAlias();
			
			AliasDefinition existsAliasDefinition = aliasDefinitions.get(key);
			if(null != existsAliasDefinition){
				throw new BeanDefinitionException("Found duplicated bean alias '" + key  + "' in resource : " + 
						ad.getSource() + ", " + 
						ad.getSource());
			}
			aliasDefinitions.put(key,ad);
		}
	}
	
	protected void addBeanDefinition(BeanDefinitionBase bd) throws BeanDefinitionException {
        if(null != appContext && !appContext.isServletEnvironment()){
            boolean  ignore;
            Class<?> beanClass = bd.getBeanClass();
            
            if(ServletOnlyBean.class.isAssignableFrom(beanClass)){
                ignore = true;
            }else{
                ignore = false;
            }

            if(ignore){
                log.debug("Ignore bean " + bd + " in non-servlet environment");
                return;
            }
        }
        
        String id = bd.getId();
        
        //add to identified bean definition collection
        if(!Strings.isEmpty(id)){
            BeanDefinitionBase existsBeanDefinition = identifiedBeanDefinitions.get(id);
            if(null != existsBeanDefinition){
                throw new BeanDefinitionException("Found duplicated bean id '" + id + "' in resource : " + 
                                                  bd.getSource() + ", " + 
                                                  existsBeanDefinition.getSource());
            }
            identifiedBeanDefinitions.put(id, bd);
        }	   
        
        if(null == bd.getType()) {
            bd.setType(bd.getBeanClass());
        }
	    
	    List<TypeDefinition> defs = new ArrayList<>();
	    defs.add(bd);
	    defs.addAll(bd.getAdditionalTypeDefs());
	    
	    for(TypeDefinition def : defs) {
	        Class<?> beanType = def.getType();
	        
            if(beanType.equals(BeanProcessor.class)){
                postProcessorBeans.add(bd);
                continue;
            }
            
            //add to named bean definition collection
            if(!Strings.isEmpty(def.getName())){
                String key = beanType.getName() + "$" + def.getName();
                if(!def.isOverride()) {
                    BeanDefinitionBase existsBeanDefinition = namedBeanDefinitions.get(key);
                    if(null != existsBeanDefinition && !existsBeanDefinition.isDefaultOverrided()){
                        throw new BeanDefinitionException("Found duplicated bean name '" + bd.getName() + 
                                                          "' for type '" + beanType.getName() + 
                                                          "' in resource : " + bd.getSource() + " with exists bean " + existsBeanDefinition);
                    }
                }
                namedBeanDefinitions.put(key, bd);
            }
            
            //Add to primary bean definition collection
            if(def.isPrimary()){
                if(!def.isOverride()) {
                    BeanDefinitionBase existsBeanDefinition = primaryBeanDefinitions.get(beanType);
                    if(null != existsBeanDefinition && existsBeanDefinition != NULL_BD && !existsBeanDefinition.isDefaultOverrided()){
                        throw new BeanDefinitionException("Found duplicated primary bean " + bd + 
                                                          " for type '" + beanType.getName() + 
                                                          "' with exists bean " + existsBeanDefinition.getSource());
                    }
                }
                primaryBeanDefinitions.put(beanType, bd);
            }
            
            //add to bean type definition collection
            Set<BeanDefinitionBase> typeSet = beanTypeDefinitions.get(beanType);
            if(null == typeSet){
                typeSet = new TreeSet<>(Comparators.ORDERED_COMPARATOR);
                beanTypeDefinitions.put(beanType, typeSet);
            }
            typeSet.add(bd);
	    }

        //add to bean class definition collection
        Set<BeanDefinitionBase> clsSet = beanClassDefinitions.get(bd.getBeanClass());
        if(null == clsSet){
            clsSet = new HashSet<>(1);
            beanClassDefinitions.put(bd.getBeanClass(), clsSet);
        }
        clsSet.add(bd);
	    
	    for(FactoryDefinition fd : bd.getFactoryDefs()) {
	        typedFactoryDefinitions.put(fd.getTargetType(), bd);
	    }
	    
        //add to all bean definition collection
        allBeanDefinitions.add(bd);
	}
	
	protected void addBeanList(BeanListDefinition bld){
		String key = bld.getType().getName();
		
		if(!Strings.isEmpty(bld.getQualifier())){
			key = key + "$" + bld.getQualifier();
		}
		
		if(!bld.isOverride()){
			BeanListDefinition exists = beanListDefinitions.get(key);
			
			if(null != exists){
				throw new BeanDefinitionException("Found duplicated bean list of type '" + bld.getType().getName() +
						  "', qualifier '" + bld.getQualifier() + "' in " + exists.getSource() + ", " + bld.getSource());
			}
		}
	
		beanListDefinitions.put(key, bld);
	}
	
	protected BeanDefinition findBeanDefinition(Set<BeanDefinitionBase> beanDefinitions,String name){
		for(BeanDefinitionBase bd : beanDefinitions){
			if(Strings.equals(bd.name, name)){
				return bd;
			}
		}
		return null;
	}
	
	private void ensureContainerNotInited(){
    	if(containerInited){
    		throw new IllegalStateException("BeanContainer already initialized");
    	}
	}
	
	private void ensureAppNotInited(){
    	if(appInited){
    		throw new IllegalStateException("Cannot perform this operation, application already initialized");
    	}
	}
}