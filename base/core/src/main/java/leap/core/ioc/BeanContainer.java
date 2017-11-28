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
import leap.core.config.AppConfigInitializer;
import leap.core.config.dyna.PropertyProvider;
import leap.core.validation.annotations.NotEmpty;
import leap.core.validation.annotations.NotNull;
import leap.core.web.ServletContextAware;
import leap.lang.*;
import leap.lang.Comparators;
import leap.lang.annotation.Destroy;
import leap.lang.annotation.Init;
import leap.lang.annotation.Internal;
import leap.lang.annotation.Nullable;
import leap.lang.beans.*;
import leap.lang.convert.Converts;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.lang.reflect.*;
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
import java.util.function.Function;
import java.util.function.Supplier;

@Internal
@SuppressWarnings("unchecked")
public class BeanContainer implements BeanFactory {
	
	private static final Log log = LogFactory.get(BeanContainer.class);
	
	/** Definition of beans that are currently in creation */
	private final ThreadLocal<Map<BeanDefinitionBase,Object>> beansCurrentlyInCreation = new ThreadLocal<>();
	
	private static final BeanDefinitionBase NULL_BD = new BeanDefinitionBase(null);

    protected final PlaceholderResolver            placeholderResolver;
    protected final AnnotationBeanDefinitionLoader annotationBeanDefinitionLoader;
    protected final XmlBeanDefinitionLoader        xmlBeanDefinitionLoader;

    protected Set<InitDefinition>                            initDefinitions     = new CopyOnWriteArraySet<>();
    protected BeanDefinitionsImpl                            bds                 = new BeanDefinitionsImpl(false);
    protected BeanDefinitionsImpl                            bpds                = new BeanDefinitionsImpl(true); //proxy defs
    protected Map<String, BeanListDefinition>                beanListDefinitions = new HashMap<>();
    protected List<BeanDefinitionBase>                       processorBeans      = new ArrayList<>();
    protected List<BeanDefinitionBase>                       initializableBeans  = new ArrayList<>();
    protected List<BeanDefinitionBase>                       injectorBeans       = new ArrayList<>();
    protected List<BeanDefinitionBase>                       supportBeans       = new ArrayList<>();

    private Map<String, List<?>>                  typedBeansMap  = new ConcurrentHashMap<>();
    private Map<Class<?>, Map<String, ?>>         namedBeansMap  = new ConcurrentHashMap<>();
    private Map<Class<?>, Map<?, BeanDefinition>> typedInstances = new ConcurrentHashMap<>();
    private Map<Class<?>, Object>                 primaryBeans   = new ConcurrentHashMap<>();

    private   AppConfig                  config;
    private   AppContext                 appContext;
    private   BeanFactory                beanFactory;
    private   BeanConfigurator           beanConfigurator;
    protected BeanFactoryInitializable[] initializables;
    protected BeanFactorySupport[]       postSupports = new BeanFactorySupport[0];
    protected BeanProcessor[]            processors;
    protected BeanInjector[]             injectors;
    private   boolean                    initializing;
    private   boolean                    containerInited;
    private   boolean                    appInited;

	/** Flag that indicates whether this container has been closed already */
	private boolean closed = false;
	
	/** Reference to the JVM shutdown hook, if registered */
	private Thread shutdownHook;
	
	/** Synchronization monitor for the "active" flag */
	private final Object activeMonitor = new Object();
	
	/** Synchronization monitor for the "refresh" and "destroy" */
	private final Object startupShutdownMonitor = new Object();

	public BeanContainer(AppConfig config){
        this.config                         = config;
        this.beanConfigurator               = new BeanConfigurator(config);
		this.placeholderResolver            = config.getPlaceholderResolver();
		this.annotationBeanDefinitionLoader = new AnnotationBeanDefinitionLoader();
		this.xmlBeanDefinitionLoader        = new XmlBeanDefinitionLoader(this);
	}
	
	public AppContext getAppContext() {
		return appContext;
	}

    public AppConfig getAppConfig() {
        return config;
    }
	
	public void setAppContext(AppContext appContext){
		this.appContext  = appContext;
		this.beanFactory = appContext.getBeanFactory();
	}
	
	public BeanContainer loadFromClasses(Class<?>[] classes) throws IllegalStateException{
        log.debug("Load beans from {} classes",classes.length);
		ensureContainerNotInited();
		this.annotationBeanDefinitionLoader.load(this,classes);
		return this;
	}

    /**
     * Loads all the beans definitions from the given resources.
     *
     * <p/>
     *
     * throws {@link IllegalStateException} if this container already finish loading.
     */
    public BeanContainer loadFromResources(AppResource[] resources) throws IllegalStateException {
        log.debug("Load beans from {} resources", resources.length);
        ensureContainerNotInited();
        this.xmlBeanDefinitionLoader.load(resources);
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

    @Override
    public void injectStatic(Class<?> cls) throws BeanException {
        BeanDefinitionBase bd = createBeanDefinition(cls);

        ReflectClass rc = ReflectClass.of(cls);

        BeanFactory factory = null != beanFactory ? beanFactory : this;

        for (ReflectField rf : rc.getFields()) {
            if (rf.isStatic() && rf.isAnnotationPresent(Inject.class)) {
                try {
                    //skip when bean value already set.
                    if (null != rf.getValue(null)) {
                        continue;
                    }

                    Object injectedBean = resolveInjectValue(factory, bd, rf.getName(), rf.getType(), rf.getGenericType(), rf.getAnnotations());

                    if (null != injectedBean) {
                        rf.setValue(null, injectedBean);
                    }
                } catch (Exception e) {
                    log.error("Error injecting static field '{}' in class '{}' : {}", rf.getName(), cls.getName(), e.getMessage());
                    throw e;
                }
            }
        }

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
            
            throw new BeanException("Field '" + field.getName() + "'(" + field.getType() + ") must not null in bean " + (null == d ? bean : d));
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

        AppConfigurator configurator = new DefaultAppConfigurator((DefaultAppConfig)config);
        addBean(AppConfigurator.class, configurator, true);
        for(AppConfigInitializer ci : getBeans(AppConfigInitializer.class)) {
            ci.init(appContext, configurator);
        }

		this.resolveAfterLoading();

        if(null != processors){
            for(BeanDefinitionBase bd : bds.allBeanDefinitions) {
                for(int i=0;i<processors.length;i++){
                    try{
                        processors[i].postInitBean(appContext, beanFactory, bd);
                    }catch(Throwable e) {
                        throw new AppInitException(e.getMessage(), e);
                    }
                }
            }
        }

        if(null != initializables) {
            for(BeanFactoryInitializable initializable : initializables) {
                try{
                    initializable.postInit(getAppConfig(), this, bds);
                }catch (Throwable e) {
                    throw new AppInitException(e.getMessage(), e);
                }
            }
        }

        if(config instanceof AppConfigBase) {
            AppConfigBase c = (AppConfigBase)config;

            c.setPropertyProvider(tryCreateBean(PropertyProvider.class));

            List<AppConfigSupport> preSupports  = getBeans(AppConfigSupport.class, "pre");
            List<AppConfigSupport> postSupports = getBeans(AppConfigSupport.class, "post", true);

            c.setPreSupports(preSupports.toArray(new AppConfigSupport[0]));
            c.setPostSupports(postSupports.toArray(new AppConfigSupport[0]));
        }

        this.containerInited = true;

		return this;
	}

    @Override
    public boolean tryInitBean(BeanDefinition bd) {
        if(!bds.allBeanDefinitions.contains(bd)) {
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
		this.initNonLazyBeans();
    	this.appInited = true;
    }
    
    @Override
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
        return createBeanDefinition(type, null);
	}

    protected <T> BeanDefinitionBase createBeanDefinition(Class<T> type, String name){
        BeanDefinitionBase bd = new BeanDefinitionBase(XmlBeanDefinitionLoader.RUNTIME_SOURCE);

        bd.setType(type);
        bd.setName(name);
        bd.setBeanClass(type);
        bd.setBeanClassType(BeanType.of(type));
        bd.setSingleton(true);
        bd.setPrimary(Strings.isEmpty(name) ? true : false);

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
    }

	@Override
    public <T> T tryGetBean(String id) throws BeanException {
		Args.notEmpty(id,"bean id");

		BeanDefinitionBase bd = findBeanOrAliasDefinition(id);
		if(null == bd){
			return null;
		}

        for(BeanFactorySupport support : postSupports) {
            T bean = (T)support.tryGetBean(id);
            if(null != bean) {
                return bean;
            }
        }

		return (T)doGetBean(bd);
    }

    @Override
    public <T> T getBean(String namespace, String name) throws BeanException {
        T bean = tryGetBean(namespace, name);
        if(null == bean){
            throw new NoSuchBeanException("No such bean with namespace '" + namespace + "' and name '" + name + "'");
        }
        return bean;
    }

    @Override
    public <T> T tryGetBean(String namespace, String name) throws BeanException {
        return tryGetBean(bds.id(namespace, name));
    }

	@Override
    public <T> T getBean(Class<? super T> type) throws NoSuchBeanException,BeanException {
    	T bean = (T) tryGetBean(type);
		
		if(null == bean){
			throw new NoSuchBeanException("No primary bean for type '" + type.getName() + "'");
		}
		
	    return bean;
    }

    @Override
    public <T> T getOrAddBean(Class<T> type) throws BeanException {
        T bean = tryGetBean(type);

        if(null == bean) {
            //add bean definition and try again.
            addBeanDefinition(createBeanDefinition(type));
            bean = tryGetBean(type);
        }

        return bean;
    }

    @Override
    public <T> T getOrAddBean(Class<T> type, String name) throws BeanException {
        T bean = tryGetBean(type, name);

        if(null == bean){
            //add bean definition and try again.
            addBeanDefinition(createBeanDefinition(type, name));
            bean = tryGetBean(type, name);
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

    public <T> T getOrCreateBean(Class<T> type, String name) throws BeanException {
        T bean = tryCreateBean(type, name);

        if(null == bean){
            return (T) createBean(type);
        }

        return bean;
    }

	@Override
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
		
		FactoryBean factoryBean = bds.typedFactoryBeans.get(type);
		if(null != factoryBean){
			bean = (T)factoryBean.getBean(beanFactory, type);
            if(null != bean) {
                return bean;
            }
		}

        for(BeanFactorySupport support : postSupports) {
            bean = (T)support.tryGetBean(type);
            if(null != bean) {
                return bean;
            }
        }

        return null;
    }
	
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
	
	@Override
    public <T> T getBean(Class<? super T> type, String name) throws NoSuchBeanException, BeanException {
		T bean = (T) tryGetBean(type, name);
		
		if(null == bean){
			throw new NoSuchBeanException("No bean named '" + name + "' for type '" + type.getName() + "'");
		}
		
	    return bean;
    }
	
	@Override
    public <T> T tryGetBean(Class<? super T> type, String name) throws BeanException {
		Args.notNull(type,"bean type");
		Args.notNull(name,"bean name");

		BeanDefinitionBase bd = findBeanOrAliasDefinition(type, name);
		if(null != bd){
			return (T)doGetBean(bd);
		}

        for(BeanFactorySupport support : postSupports) {
            T bean = (T)support.tryGetBean(type, name);
            if(null != bean) {
                return bean;
            }
        }
		
		return null;
    }
	
    public <T> T tryCreateBean(Class<T> type, String name) throws BeanException {
		Args.notNull(type,"bean type");
		Args.notNull(name,"bean name");
		
		BeanDefinitionBase bd = findBeanOrAliasDefinition(type, name);
		
		if(null != bd){
			return (T)doCreateBean(bd);
		}
		
		return null;
    }

    public <T> T tryCreateBean(Class<T> type) throws BeanException {
        Args.notNull(type,"bean type");
        BeanDefinitionBase bd = findPrimaryBeanDefinition(type);

        if(null != bd){
            return (T)doCreateBean(bd);
        }

        FactoryBean factoryBean = bds.typedFactoryBeans.get(type);
        if(null != factoryBean){
            return (T)factoryBean.getBean(beanFactory, type);
        }
        return null;
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
    public <T> T tryCreateBean(String namespace, String name) {
        return tryCreateBean(bds.id(namespace, name));
    }

    public void setPrimaryBean(Class<?> type, Object bean) {
		Args.notNull(type,"type");
		Args.notNull(bean,"bean");
		primaryBeans.put(type, bean);
    }

	@Override
    public <T> List<T> getBeans(Class<? super T> type) throws BeanException {
        String key = type.getName();

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
                bds.keySet().forEach(beans::add);
            }
            typedBeansMap.put(key, beans);
        }

        return beans;
    }

    public <T> List<T> getBeans(Class<? super T> type, String qualifier, boolean orEmpty) throws BeanException {
        List<T> beans = getBeans(type, qualifier);
        if(orEmpty) {
            List<T> defaultBeans = getBeans(type, "");
            beans.addAll(defaultBeans);
        }
        return beans;
    }

    @Override
    public <T> List<T> getBeans(Class<? super T> type, String qualifier) throws BeanException {
        qualifier = Strings.nullToEmpty(qualifier);

        String key = null == qualifier ? type.getName() : type.getName() + "$" + qualifier;

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

                    if(Strings.isEmpty(qualifier) && bd.getQualifiers().isEmpty()) {
                        beans.add(entry.getKey());
                    }else if(bd.getQualifiers().contains(qualifier)){
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
			
			Set<BeanDefinitionBase> typeSet = bds.beanTypeDefinitions.get(type);
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
    public <T> Map<T, BeanDefinition> getBeansWithDefinition(Class<? super T> type) throws BeanException {
		Map<T,BeanDefinition> beans = (Map<T,BeanDefinition>)typedInstances.get(type);
		
		if(null == beans){
			beans = new LinkedHashMap<>(5);
			
			Set<BeanDefinitionBase> typeSet = bds.beanTypeDefinitions.get(type);
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

    @Override
    public boolean initBean(Object bean) {
        return Try.throwUncheckedWithResult(() -> doInitBean(null, bean));
    }

    @Override
    public boolean destroyBean(Object bean) {
        return doDestroyBean(null, bean);
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
            this.shutdownHook.setName("ShutdownHook-BeanFactory");
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

    protected boolean doInitBean(@Nullable BeanDefinitionBase bd, Object bean) throws Throwable {
        boolean init = false;

        if(null != bd && null != bd.getInitMethod()){
            Reflection.invokeMethod(bd.getInitMethod(), bean);
            init = true;
        }

        if(!init) {
            ReflectClass rc = ReflectClass.of(bean.getClass());
            for (ReflectMethod m : rc.getMethods()) {
                if (m.getParameters().length == 0 && m.isAnnotationPresent(Init.class)) {
                    m.invoke(bean, Arrays2.EMPTY_OBJECT_ARRAY);
                    init = true;
                    break;
                }
            }
        }

        if(!init) {
            if(bean instanceof Initializable) {
                ((Initializable) bean).init();
                init = true;
            }
        }

        //null if post processors not resolved, see #resolveAfterLoading
        if(null != bd && null != processors){
            for(int i=0;i<processors.length;i++){
                processors[i].postCreateBean(appContext, beanFactory, bd, bean);
            }
        }

        if(bean instanceof PostCreateBean){
            ((PostCreateBean) bean).postCreate(appContext.getBeanFactory());
            init = true;
        }

        return init;
    }

    protected boolean doDestroyBean(@Nullable  BeanDefinitionBase bd, Object bean) {
        if(null != bd) {
            Method destroyMethod = bd.getDestroyMethod();
            if(null != destroyMethod) {
                try {
                    Reflection.invokeMethod(destroyMethod, bean);
                } catch (Throwable e) {
                    log.warn("Error destroy bean '{}' : {}", null == bd ? bean : bd, e.getMessage(), e);
                }
                return true;
            }
        }

        if(bean instanceof Disposable){
            try {
                ((Disposable)bean).dispose();
            } catch (Throwable e) {
                log.warn("Error dispose bean '{}' : {}", null == bd ? bean : bd, e.getMessage(), e);
            }
            return true;
        }

        if(bean instanceof Closeable){
            try {
                ((Closeable)bean).close();
            } catch (Throwable e) {
                log.warn("Error close bean '{}' : {}", null == bd ? bean : bd, e.getMessage(), e);
            }
            return true;
        }

        ReflectClass rc = ReflectClass.of(bean.getClass());
        for(ReflectMethod m : rc.getMethods()) {
            if(m.getParameters().length == 0 && m.isAnnotationPresent(Destroy.class)) {
                try {
                    m.invoke(bean, Arrays2.EMPTY_OBJECT_ARRAY);
                } catch (Throwable e) {
                    log.warn("Error destroy bean '{}' : {}", null == bd ? bean : bd, e.getMessage(), e);
                }
                return true;
            }
        }

        return false;
    }

	protected void destroyBeans() {
		for(BeanDefinitionBase bd : bds.allBeanDefinitions){
			Object instance = bd.getSingletonInstance();
			if(null != instance){
                doDestroyBean(bd, instance);
			}
		}
	}

	protected Set<BeanDefinitionBase> getAllBeanDefinitions() {
		return bds.allBeanDefinitions;
	}
	
	protected Map<String, AliasDefinition> getAliasDefinitions() {
		return bds.aliasDefinitions;
	}

	protected BeanDefinitionBase findBeanDefinition(BeanReference br){
		if(Strings.isNotEmpty(br.getTargetId())){
			return findBeanDefinition(br.getTargetId());
		}

        if(Strings.isEmpty(br.getBeanName())) {
            return findPrimaryBeanDefinition(br.getBeanType());
        }else{
            return findBeanOrAliasDefinition(br.getBeanType(),br.getBeanName());
        }
	}

	protected BeanDefinitionBase findBeanDefinition(String id){
		return bds.identifiedBeanDefinitions.get(id);
	}
	
	protected BeanDefinitionBase findBeanOrAliasDefinition(String id){
		BeanDefinitionBase bd = bds.identifiedBeanDefinitions.get(id);
		if(null == bd){
			AliasDefinition ad = bds.aliasDefinitions.get(id);
			if(null != ad){
				bd = bds.identifiedBeanDefinitions.get(ad.getId());
			}
		}
		return bd;
	}
	
	protected BeanDefinitionBase findBeanDefinition(Class<?> beanType, String name){
		return bds.namedBeanDefinitions.get(beanType.getName() + "$" + name);
	}
	
	protected BeanDefinitionBase findBeanOrAliasDefinition(Class<?> beanType, String name){
        return bds.find(beanType, name);
	}
	
	protected BeanDefinitionBase findPrimaryBeanDefinition(Class<?> beanType){
        return bds.find(beanType);
	}
	
	protected AliasDefinition findAliasDefinition(String alias){
		return bds.aliasDefinitions.get(alias);
	}
	
	protected void checkAfterLoading(){
		for(AliasDefinition aliasDefinition : bds.aliasDefinitions.values()){
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

    @Override
    public String toString() {
        return super.toString();
    }

    protected void resolveAfterLoading() {
        //bean support
        List<BeanFactorySupport> supportsList = new ArrayList<>();
        for(BeanDefinitionBase bd : supportBeans) {
            supportsList.add((BeanFactorySupport)doGetBean(bd));
        }
        this.postSupports = supportsList.toArray(new BeanFactorySupport[0]);

        //bean injector.
        List<BeanInjector> injectorList = new ArrayList<>();
        for(BeanDefinitionBase bd : injectorBeans) {
            injectorList.add((BeanInjector)doGetBean(bd));
        }
        this.injectors = injectorList.toArray(new BeanInjector[0]);

        //bean processors
		List<BeanProcessor> processorList = new ArrayList<>();
		for(BeanDefinitionBase bd : processorBeans){
			processorList.add((BeanProcessor)doGetBean(bd));
		}
		this.processors = processorList.toArray(new BeanProcessor[]{});

        //bean factory initializable beans
        List<BeanFactoryInitializable> initializableList = new ArrayList<>();
        initializableBeans.forEach(bd -> initializableList.add((BeanFactoryInitializable)doGetBean(bd)));
        this.initializables = initializableList.toArray(new BeanFactoryInitializable[0]);

		//create factory beans
		for(BeanDefinitionBase bd : bds.typedFactoryDefinitions.values()){
		    for(FactoryBeanDefinition fd : bd.getFactoryBeanDefs()) {
                bds.typedFactoryBeans.put(fd.getTargetType(),(FactoryBean)doGetBean(bd));
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
		for(BeanDefinitionBase bd : bds.allBeanDefinitions){
			if(!bd.isLazyInit()){
				doGetBean(bd);
			}
		}
	}
	
	protected Object doGetBean(BeanDefinitionBase bd){
		if(bd.isSingleton()){
			Object instance = bd.getInstance();
            if(null == instance) {
                synchronized (bd.getSingletonLock()) {
                    if(null == (instance = bd.getInstance())) {
                        instance = doCreateBean(bd);
                    }
                }
            }
            return instance;
		}else{
            return doCreateBean(bd);
        }
	}
	
    protected Object doCreateProxy(BeanDefinitionBase pd, Class<?> type, Object bean){

        Object proxy;

        if(ProxyBean.class.isAssignableFrom(pd.getBeanClass())) {
            proxy = doCreateBeanOnly(pd);
            ((ProxyBean)proxy).setTargetBean(bean);
        }else{
            ReflectConstructor c = ReflectClass.of(pd.getBeanClass()).getConstructor(type);
            if(null == c) {
                throw new BeanCreationException("Can't create proxy '" + pd.getBeanClass() + "', No valid constructor");
            }

            proxy = c.newInstance(bean);

            beforeBeanCreation(pd);

            processBeanCreation(pd, proxy);

            afterBeanCreation(pd);

            pd.setInited(true);
        }

        return proxy;
    }

    protected Object doCreateBean(BeanDefinitionBase bd){

        Object bean = doCreateBeanOnly(bd);

        BeanDefinitionBase pd = findProxy(bd);
        if(null != pd) {
            Object proxyBean = doCreateProxy(pd, bd.getType(), bean);

            if(!isTypedProxy(pd)) {
                pd = findTypedProxy(bd);
                if(null != pd) {
                    Object typedProxyBean = doCreateProxy(pd, bd.getType(), proxyBean);
                    proxyBean = typedProxyBean;
                }
            }

            if(bd.isSingleton()) {
                bd.setProxyInstance(proxyBean);
            }

            return proxyBean;
        }

        return bean;
    }

    protected Object doCreateBeanOnly(BeanDefinitionBase bd) {
        if(initializing){
            throw new IllegalStateException("Cannot get bean when this container is initializing");
        }

        Object bean;

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

    protected BeanDefinitionBase findProxy(BeanDefinitionBase bd) {
        BeanDefinitionBase pd;

        //find by id.
        if(!Strings.isEmpty(bd.getId())) {
            pd = bpds.identifiedBeanDefinitions.get(bd.getId());
            if(null != pd) {
                return pd;
            }
        }

        //find by name.
        if(!Strings.isEmpty(bd.getName())) {
            pd = bpds.find(bd.getType(), bd.getName());
            if(null != pd) {
                return pd;
            }
        }

        //find by primary.
        if(bd.isPrimary()) {
            pd = bpds.primaryBeanDefinitions.get(bd.getType());
            if(null != pd) {
                return pd;
            }
        }

        //find typed proxy.
        return findTypedProxy(bd);
    }

    protected BeanDefinitionBase findTypedProxy(BeanDefinitionBase bd) {
        Set<BeanDefinitionBase> pds = bpds.beanTypeDefinitions.get(bd.getType());
        if(null != pds) {
            for(BeanDefinitionBase pd : pds) {
                if(isTypedProxy(pd)) {
                    return pd;
                }
            }
        }
        return null;
    }

    protected boolean isTypedProxy(BeanDefinitionBase pd) {
        if(!pd.isPrimary() && Strings.isEmpty(pd.getName()) && Strings.isEmpty(pd.getId())) {
            return true;
        }
        return false;
    }

	protected Object doBeanCreation(BeanDefinitionBase bd){
		log.trace("Creating bean {}",bd);
		
		Object            bean;
        FactoryDefinition fd = bd.getFactoryDefinition();
		ValueDefinition   vd = bd.getValueDefinition();

        if(null != fd) {
            bean = doResolveValueFromFactory(bd, fd);
        }else if(null != vd) {
            bean = doResolveValue(bd, vd, null);
        } else {
            bean = doBeanCreationByConstructor(bd);
        }

        processBeanCreation(bd, bean);

		return bean;
	}

    protected void processBeanCreation(BeanDefinitionBase bd, Object bean) {
        try {
            setBeanCurrentlyInCreation(bd, bean);

            doBeanAware(bd, bean);

            doBeanConfigure(bd, bean);
            if(bean instanceof PostConfigureBean){
                ((PostConfigureBean) bean).postConfigure(appContext.getBeanFactory(), appContext.getConfig());
            }

            doBeanSetProperties(bd,bean);
            doBeanInjection(bd,bean);
            doBeanInvokeMethods(bd,bean);

            doInitBean(bd, bean);

            if(bean instanceof LoadableBean){
                if(!((LoadableBean) bean).load(appContext.getBeanFactory())){
                    return;
                }
            }
        } catch (Throwable e) {
            throw errorCreateBean(bd,e);
        }

        if(bd.isSingleton()){
            doBeanValidation(bd,bean);
            bd.setSingletonInstance(bean);
        }
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

            beanConfigurator.configure(bean, bt, bd.getConfigurationPrefix());
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

            Object injectedValue = resolveInjectValue(bd, bean, bt, bp);

            if(null != injectedValue) {
                log.trace("Injecting property '{}'", bp.getName());

                try {
                    if(bp.isWritable()) {
                        bp.setValue(bean, injectedValue);
                    }else{
                        if(!bp.isField()) {
                            throw new BeanCreationException("Cannot inject not writable property '" + bp.getName() + "' in bean '" + bd + "'");
                        }
                        bp.getReflectField().setValue(bean, injectedValue);
                    }
                } catch (Exception e) {
                    log.error("Error injecting property '{}' in bean '{}' : {}", bp.getName(), bd, e.getMessage());
                    throw e;
                }
            }
        }

        for (ReflectField rf : bt.getReflectClass().getFields()) {

            Object injectedValue = resolveInjectValue(bd, bean, bt, rf);

            if(null != injectedValue) {
                try {
                        rf.setValue(bean, injectedValue);
                } catch (Exception e) {
                    log.error("Error injecting field '{}' in bean '{}' : {}", rf.getName(), bd, e.getMessage());
                    throw e;
                }
            }

        }

        if (bean instanceof PostInjectBean) {
            ((PostInjectBean) bean).postInject(factory);
        }
	}

    private final class MockBean {}
    private final MockBean mockBean = new MockBean();
    private final BeanDefinitionBase mockBeanDefinition = this.createBeanDefinition(MockBean.class);

    @Override
    public Object resolveInjectValue(Class<?> type, Type genericType) {
        SimpleReflectValued v = new SimpleReflectValued(type, genericType);
        return resolveInjectValue(mockBeanDefinition, mockBean, BeanType.of(MockBean.class), v, true);
    }

    protected Object resolveInjectValue(BeanDefinitionBase bd, Object bean, BeanType bt, ReflectValued v) {
        return resolveInjectValue(bd, bean, bt, v, false);
    }

    protected Object resolveInjectValue(BeanDefinitionBase bd, Object bean, BeanType bt, ReflectValued v, boolean force) {

        if(null != injectors && injectors.length > 0) {
            Annotation found = null;
            for(Annotation a : v.getAnnotations()) {
                if(a.annotationType().isAnnotationPresent(AInject.class)){
                    found = a;
                    break;
                }
            }

            if(null != found || force) {
                Out out = new Out();
                for (BeanInjector injector : injectors) {
                    if (injector.resolveInjectValue(bd, bean, v, found, out)) {
                        return out.get();
                    }
                }
            }
        }

        Inject inject = v.getAnnotation(Inject.class);
        if(!force && null == inject) {
            return null;
        }

        //skip when bean value already set.
        if (!v.getType().isPrimitive()) {
            Object rawValue = v.getRawValue(bean);
            if (null != rawValue && !(rawValue instanceof DummyBean)) {
                return null;
            }
        }

        return resolveInjectValue(beanFactory, bd, v.getName(), v.getType(), v.getGenericType(), v.getAnnotations());
    }
	
    @SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object resolveInjectValue(BeanFactory factory, BeanDefinitionBase bd, String name, Class<?> type,Type genericType,Annotation[] annotations) {

		if(type.equals(BeanFactory.class)) {
		    return factory;
		}
		
		if(type.equals(AppConfig.class)) {
		    return null == appContext ? null : appContext.getConfig();
		}
		
		if(type.equals(AppContext.class)) {
		    return appContext;
		}

        Inject inject = Classes.getAnnotation(annotations, Inject.class);
//        if(null == inject){
//            return null;
//        }
		
		Object injectedBean = null;
		
		if(null == bd || !Iterables.any(bd.getProperties(), Predicates.<PropertyDefinition>nameEquals(name))){
			//inject by bean's id
			if(null != inject && !Strings.isEmpty(inject.id())){
				injectedBean = factory.getBean(inject.id());
			}else{
				Class  beanType = null == inject ? null : inject.type();
				String beanName = null == inject ? null : inject.name();
				
				if(null != genericType && Lazy.class.equals(type)){
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
						                            null == inject ? false : inject.primary(),
						                            nullable, required);
					}
				}else if(null != genericType && (List.class.equals(type) || BeanList.class.equals(type))){
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
				}else if(null != genericType && Map.class.equals(type)){
					if(!Strings.isEmpty(beanName)){
						throw new BeanCreationException("Auto Injected Map property does not support the 'name' annotation field in bean " + bd);
					}
					
					if(null == beanType || Object.class.equals(beanType)){
                        Type[] types = Types.getTypeArguments(genericType);
                        if(types.length != 2) {
                            return null;
                        }
                        beanType = Types.getActualType(types[1]);
					}
					
					injectedBean = factory.getNamedBeans(beanType);
				}else{
					if(null == beanType || Object.class.equals(beanType)){
						beanType = type;
					}
					
					if(Strings.isEmpty(beanName)){
						injectedBean = factory.tryGetBean(beanType);	
					}else{
						injectedBean = factory.tryGetBean(beanType, beanName);
						if(null == injectedBean && null != inject && inject.primary()){
							injectedBean = factory.tryGetBean(beanType);
						}
					}
				}
			}
		}

        if(injectedBean == null && null != inject && inject.create() && Classes.isConcreteClass(type)) {
            injectedBean = createBean(type);
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

        if(bean instanceof BeanPrimaryAware) {
            ((BeanPrimaryAware) bean).setBeanPrimary(bd.isPrimary());
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

            ReflectConstructor dc = null;
            for(ReflectConstructor c : rc.getConstructors()) {
                if(c.getReflectedConstructor().isAnnotationPresent(DefaultConstructor.class)) {
                    dc = c;
                    break;
                }
            }
            if(null == dc) {
                if(rc.hasDefaultConstructor()) {
                    dc = rc.getDefaultConstructor();
                }else if(rc.getConstructors().length == 1) {
                    dc = rc.getConstructors()[0];
                }
            }

            if(null != dc) {
                Object[] args = new Object[dc.getParameters().length];

                for(int i=0;i<args.length;i++) {
                    ReflectParameter p = dc.getParameters()[i];

                    if(beanConfigurator.configure(args, p, bd.getConfigurationPrefix())){
                        continue;
                    }

                    if(p.isAnnotationPresent(Inject.class)) {
                        args[i] = resolveInjectValue(beanFactory, bd, p.getName(), p.getType(), p.getGenericType(), p.getAnnotations());
                        continue;
                    }
                }
                return dc.newInstance(args);
            }else{
                throw new BeanCreationException("Cannot create bean without default constructor, check the bean : " + bd);
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
        BeanDefinitionBase referenced = findBeanDefinition(br);

        if(referenced == null){
            throw new BeanDefinitionException("The referenced bean not exists : " + br);
        }

        br.setTargetBeanDefinition(referenced);

        Object bean = doGetBean(referenced);
        if(null == bean) {
            throw new BeanCreationException("The referenced bean '" + referenced + "' not exists!");
        }
        return bean;
    }

    protected Object doResolveValueFromFactory(BeanDefinitionBase bd, FactoryDefinition fd) {
        Method m = fd.getMethod();
        try {
            return m.invoke(null, doResolveArgs(bd, fd.getArguments()));
        } catch (Exception e) {
            throw new BeanCreationException("Error invoke factory method '" + m.getName() + "' in bean '" + bd + "' : " + e.getMessage(),e);
        }
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
			return vd.getResolvedValue();
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

		if(!Strings.isEmpty(br.getTargetId())){
			return () -> this.getBean(br.getTargetId());
		}
		if(!Strings.isEmpty(br.getBeanName())){
			return () -> this.getBean(br.getBeanType(),br.getBeanName());
		}
		return () -> this.getBean(br.getBeanType());
	}
	
	protected Supplier<Object> createBeanValue(BeanDefinition bd,final BeanDefinitionBase value){
		return () -> doBeanCreation(value);
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
			AliasDefinition existsAliasDefinition = bds.aliasDefinitions.get(ad.getAlias());
			if(null != existsAliasDefinition){
				throw new BeanDefinitionException("Found duplicated bean alias '" + ad.getAlias()  + "' in resource : " + 
						ad.getSource() + ", " + 
						ad.getSource());
			}
            bds.aliasDefinitions.put(ad.getAlias(),ad);
		}else{
			String key =  ad.getType().getName() + "$" + ad.getAlias();
			
			AliasDefinition existsAliasDefinition = bds.aliasDefinitions.get(key);
			if(null != existsAliasDefinition){
				throw new BeanDefinitionException("Found duplicated bean alias '" + key  + "' in resource : " + 
						ad.getSource() + ", " + 
						ad.getSource());
			}
            bds.aliasDefinitions.put(key,ad);
		}
	}

    protected void addBeanDefinition(BeanDefinitionBase bd) throws BeanDefinitionException {
        addBeanDefinition(bd, false);
    }
	
	protected void addBeanDefinition(BeanDefinitionBase bd, boolean proxy) throws BeanDefinitionException {
        BeanDefinitionsImpl bds = this.bds;
        if(proxy) {
            bds = this.bpds;
        }
		
        bds.add(bd);
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

    protected class BeanDefinitionsImpl implements BeanDefinitions {
        private final boolean proxy;

        protected Set<BeanDefinitionBase>                allBeanDefinitions        = new CopyOnWriteArraySet<>();
        protected Map<String, BeanDefinitionBase>        identifiedBeanDefinitions = new HashMap<>();
        protected Map<Class<?>, Set<BeanDefinitionBase>> beanTypeDefinitions       = new HashMap<>();
        protected Map<Class<?>, Set<BeanDefinitionBase>> beanClassDefinitions      = new HashMap<>();
        protected Map<Class<?>, FactoryBean>             typedFactoryBeans         = new HashMap<>();
        protected Map<Class<?>, BeanDefinitionBase>      typedFactoryDefinitions   = new HashMap<>();
        protected Map<String, BeanDefinitionBase>        namedBeanDefinitions      = new HashMap<>();
        protected Map<Class<?>, BeanDefinitionBase>      primaryBeanDefinitions    = new HashMap<>();
        protected Map<String, AliasDefinition>           aliasDefinitions          = new HashMap<>();

        public BeanDefinitionsImpl(boolean proxy) {
            this.proxy = proxy;
        }

        public String key(Class<?> type, String name) {
            return type.getName() + "$" + name;
        }

        public BeanDefinitionBase remove(String id) {
            return removeAll(identifiedBeanDefinitions.remove(id));
        }

        public BeanDefinitionBase remove(Class<?> type, String name) {
            String key = key(type, name);
            return removeAll(namedBeanDefinitions.remove(key));
        }

        public BeanDefinitionBase remove(Class<?> type, Class<?> cls) {
            Set<BeanDefinitionBase> set = beanTypeDefinitions.get(type);
            if(null != set) {
                Set<BeanDefinitionBase> found = new HashSet<>();
                Set<BeanDefinitionBase> notFound = new LinkedHashSet<>();

                for(BeanDefinitionBase bd : set) {
                    if(bd.getBeanClass().equals(cls)) {
                        found.add(bd);
                    }else{
                        notFound.add(bd);
                    }
                }

                set.clear();
                set.addAll(notFound);

                for(BeanDefinitionBase bd : found) {
                    removeAll(bd);
                }
            }
            return null;
        }

        public BeanDefinitionBase removePrimary(Class<?> type) {
            return removeAll(primaryBeanDefinitions.remove(type));
        }

        protected BeanDefinitionBase removeAll(BeanDefinitionBase bd) {
            if(null != bd) {
                allBeanDefinitions.remove(bd);

                if(!Strings.isEmpty(bd.getId())) {
                    identifiedBeanDefinitions.remove(bd.getId());
                }

                if(bd.isPrimary()) {
                    primaryBeanDefinitions.remove(bd.getType());
                }

                if(!Strings.isEmpty(bd.getName())) {
                    namedBeanDefinitions.remove(bd.getType(), bd.getName());
                }

                Set<BeanDefinitionBase> set = beanTypeDefinitions.get(bd.getType());
                if(null != set) {
                    Set<BeanDefinitionBase> found = new HashSet<>();
                    Set<BeanDefinitionBase> notFound = new LinkedHashSet<>();

                    for (BeanDefinitionBase item : set) {
                        if (item == bd) {
                            found.add(item);
                        } else {
                            notFound.add(item);
                        }
                    }

                    set.clear();
                    set.addAll(notFound);
                }
            }
            return bd;
        }

        @Override
        public BeanDefinitionConfigurator getOrAdd(Class<?> type, Class<?> beanClass) {
            BeanDefinitionBase bd =  find(type);
            if(null != bd) {
                return bd;
            }

            return add(create(type, beanClass));
        }

        @Override
        public BeanDefinitionConfigurator getOrAdd(Class<?> type, Class<?> beanClass, String name) {
            BeanDefinitionBase bd = find(type, name);
            if(null != bd) {
                return bd;
            }

            return add(create(type, beanClass, name));
        }

        protected BeanDefinitionBase find(Class<?> beanType){
            BeanDefinitionBase bd = primaryBeanDefinitions.get(beanType);

            if(bd == NULL_BD){
                return null;
            }

            if(bd != null){
                return bd;
            }

            Set<BeanDefinitionBase> btds = beanTypeDefinitions.get(beanType);
            if(null != btds && btds.size() == 1){
                return btds.iterator().next();
            }

            btds = beanClassDefinitions.get(beanType);
            if(null != btds && btds.size() == 1){
                return btds.iterator().next();
            }

            return null;
        }

        protected BeanDefinitionBase find(Class<?> beanType, String name){
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

        protected BeanDefinitionBase create(Class<?> type, Class<?> beanClass) {
            BeanDefinitionBase bd = new BeanDefinitionBase(XmlBeanDefinitionLoader.RUNTIME_SOURCE);

            bd.setType(type);
            bd.setBeanClass(beanClass);
            bd.setBeanClassType(BeanType.of(beanClass));
            bd.setSingleton(true);
            bd.setPrimary(true);

            return bd;
        }

        protected BeanDefinitionBase create(Class<?> type, Class<?> beanClass, String name) {
            BeanDefinitionBase bd = new BeanDefinitionBase(XmlBeanDefinitionLoader.RUNTIME_SOURCE);

            bd.setName(name);
            bd.setType(type);
            bd.setBeanClass(beanClass);
            bd.setBeanClassType(BeanType.of(beanClass));
            bd.setSingleton(true);

            return bd;
        }

        protected String id(String ns, String name) {
            return "ns::" + ns + "::" + name;
        }

        protected BeanDefinitionBase add(BeanDefinitionBase bd) throws BeanDefinitionException {
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
                    return bd;
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

            if(!Strings.isEmpty(bd.getNamespace()) && !Strings.isEmpty(bd.getName())) {
                id = id(bd.getNamespace(), bd.getName());
                BeanDefinitionBase existsBeanDefinition = identifiedBeanDefinitions.get(id);
                if(null != existsBeanDefinition){
                    throw new BeanDefinitionException("Found duplicated bean namespace '" + bd.getNamespace() +
                                                     "' and name '" + bd.getName() + "' in resource : " +
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
				addAdditionalTypeDef(bd,def);
			}

            //Add to bean class definition collection
            Set<BeanDefinitionBase> clsSet = beanClassDefinitions.get(bd.getBeanClass());
            if(null == clsSet){
                clsSet = new HashSet<>(1);
                beanClassDefinitions.put(bd.getBeanClass(), clsSet);
            }
            clsSet.add(bd);

            for(FactoryBeanDefinition fd : bd.getFactoryBeanDefs()) {
                typedFactoryDefinitions.put(fd.getTargetType(), bd);
            }

            //add to all bean definition collection
            allBeanDefinitions.add(bd);

            return bd;
        }

		@Override
		public void addAdditionalTypeDef(BeanDefinition definition, TypeDefinition type) {

			if(!BeanDefinitionBase.class.isAssignableFrom(definition.getClass())){
				throw new BeanDefinitionException("definition " + definition.getClass() + " must assignable from " + BeanDefinitionBase.class.getName());
			}

			BeanDefinitionBase bd = (BeanDefinitionBase) definition;
			Class<?> beanType = type.getType();

			if(beanType.equals(BeanProcessor.class)){
				processorBeans.add(bd);
			}

			if(beanType.equals(BeanInjector.class)) {
				injectorBeans.add(bd);
			}

            if(beanType.equals(BeanFactorySupport.class)) {
                supportBeans.add(bd);
            }

			if(beanType.equals(BeanFactoryInitializable.class)) {
				initializableBeans.add(bd);
			}

			//add to named bean definition collection
			if(!Strings.isEmpty(type.getName())){
				String key = key(beanType, type.getName());
				BeanDefinitionBase existsBeanDefinition = namedBeanDefinitions.get(key);
				if(existsBeanDefinition != null){
					Optional<TypeDefinition> op = existsBeanDefinition.getAdditionalTypeDefs().stream()
							.filter(td -> td.getType().equals(type.getType())).findAny();
					TypeDefinition existTd = op.orElse(existsBeanDefinition);

					if(!type.isOverride() && !existTd.isOverride()){
						throw new BeanDefinitionException("Found duplicated " + (proxy ? "proxy" : "bean") + " name '" + definition.getName() +
								"' for type '" + beanType.getName() +
								"' in resource : " + definition.getSource() + " with exists " + (proxy ? "proxy " : "bean ") + existsBeanDefinition);
					}

					if(type.isOverride() && !existTd.isOverride()){
						namedBeanDefinitions.put(key, bd);
					}else if(type.isOverride() && existTd.isOverride()){
						if(bd.getSortOrder() == existsBeanDefinition.getSortOrder()){
							log.warn("Found duplicated name bean " + (proxy ? "proxy " : "bean ") + definition +
									" for type '" + beanType.getName() +
									"' with exists " + (proxy ? "proxy" : "bean") + " in " +
									existsBeanDefinition.getSource()+", please use sort-order to defined which bean definition to be use.");
							log.warn("Now use is "+bd);
						}
						// todo: when profile is test, use '>=', but in dev or prod, use '>'? 
						if(bd.getSortOrder() >= existsBeanDefinition.getSortOrder()){
							log.info("In profile '"+config.getProfile()+"' override exist bean definition " + existsBeanDefinition + " with " + definition);
							namedBeanDefinitions.put(key, bd);
						}
					}
					
				}else {
					namedBeanDefinitions.put(key, bd);
				}
			}

			//Add to primary bean definition collection
			if(type.isPrimary()){
				// this type is primary
				BeanDefinitionBase existsBeanDefinition = primaryBeanDefinitions.get(beanType);
				if(null != existsBeanDefinition && existsBeanDefinition != NULL_BD && !existsBeanDefinition.isDefaultOverride()){
					Optional<TypeDefinition> op = existsBeanDefinition.getAdditionalTypeDefs().stream()
							.filter(td -> td.getType().equals(type.getType())).findAny();
					TypeDefinition existTd = op.orElse(existsBeanDefinition);
					
					if(!type.isOverride() && !existTd.isOverride()){
						throw new BeanDefinitionException("Found duplicated primary " + (proxy ? "proxy " : "bean ") + definition +
								" for type '" + beanType.getName() +
								"' with exists " + (proxy ? "proxy" : "bean") + " in " + existsBeanDefinition.getSource());
					}
					
					if(type.isOverride() && !existTd.isOverride()){
						primaryBeanDefinitions.put(beanType, bd);
					}else if(type.isOverride() && existTd.isOverride()){
						if(bd.getSortOrder() == existsBeanDefinition.getSortOrder()){
							log.warn("Found duplicated primary " + (proxy ? "proxy " : "bean ") + definition +
									" for type '" + beanType.getName() +
									"' with exists " + (proxy ? "proxy" : "bean") + " in " +
									existsBeanDefinition.getSource()+", please use sort-order to defined which bean definition to be use.");
							log.warn("Now use is "+bd);
						}
						// todo: when profile is test, use '>=', but in dev or prod, use '>'? 
						if(bd.getSortOrder() >= existsBeanDefinition.getSortOrder()){
							log.info("In profile '"+config.getProfile()+"' override exist bean definition " + existsBeanDefinition + " with " + definition);
							primaryBeanDefinitions.put(beanType, bd);
						}
					}
				}else{
					primaryBeanDefinitions.put(beanType, bd);
				}
			}

			//add to bean type definition collection
			Set<BeanDefinitionBase> typeSet = beanTypeDefinitions.get(beanType);
			if(null == typeSet){
				typeSet = new TreeSet<>(Comparators.ORDERED_COMPARATOR);
				beanTypeDefinitions.put(beanType, typeSet);
			}

            if(proxy && isTypedProxy(bd)) {

                BeanDefinitionBase typedProxy = null;

                for(BeanDefinitionBase d : typeSet) {
                    if(isTypedProxy(d)) {
                        if(bd.isOverride()) {
                            typedProxy = d;
                            break;
                        }else{
                            throw new BeanDefinitionException("Found duplicated type proxy "  + definition + " for type '" +
                                beanType.getName() + "' with exists proxy in " + d.getSource());
                        }
                    }
                }

                if(null != typedProxy) {
                    typeSet.remove(typedProxy);
                }
            }

			Iterator<BeanDefinitionBase> iterator = typeSet.iterator();
            
			while (iterator.hasNext()){
				BeanDefinitionBase bdf = iterator.next();
				if(bdf == bd){
					continue;
				}
				if(bdf.isAnnotation() &&
						bdf.getType().equals(bd.getType()) &&
						bdf.getBeanClass().equals(bd.getBeanClass())){
					if(bd.isOverrideAnnotation()){
						iterator.remove();
					}else{
						throw new BeanDefinitionException("duplicate bean definition in xml:"
								+bd.getSource() + " and annotation:"+bdf.getSource()
								+", if you want to override annotation bean definition, please "
								+"use override-annotation=\"true\" in xml definition.");
					}

				}
			}
            
			typeSet.add(bd);

		}
	}
}