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

import leap.core.annotation.Configurable;
import leap.lang.Strings;
import leap.lang.beans.BeanType;
import leap.lang.tostring.ToStringBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("rawtypes")
class BeanDefinitionBase implements BeanDefinition,TypeDefinition,BeanDefinitionConfigurator {
	
	protected final Object source;
    protected final Object singletonLock = new Object();
	
    protected String                   id;
    protected String                   name;
    protected Class<?>                 type;
    protected Class<?>                 beanClass;
    protected BeanType                 beanClassType;
    protected Method                   initMethod;
    protected Method                   destroyMethod;
    protected boolean override        = true;
    protected boolean defaultOverride = false;
    protected boolean                  singleton;
    protected boolean                  lazyInit;
    protected boolean                  primary;
    protected String                   factoryBeanName;
    protected Object                   factoryBean;
    protected String                   factoryMethodName;
    protected Method                   factoryMethod;
    protected Constructor              constructor;
    protected Boolean                  configurable;
    protected String                   configurationPrefix;

    protected List<TypeDefinition>     additionalTypeDefs   = new ArrayList<>();
    protected List<FactoryDefinition>  factoryDefs          = new ArrayList<>();
    protected List<ArgumentDefinition> constructorArguments = new ArrayList<>();
    protected List<PropertyDefinition> properties           = new ArrayList<>();
    protected List<InvokeDefinition>   invokes              = new ArrayList<>();
    protected Set<String>              qualifiers           = new LinkedHashSet<>();
    protected ValueDefinition          valueDefinition;
	
	protected Object singletonInstance;
    protected Object proxyInstance;
	protected int    sortOrder = DEFAULT_SORT_ORDER;

    protected boolean inited;
	
	public BeanDefinitionBase(Object source){
		this.source = source;
	}

    public Object getSingletonLock() {
        return singletonLock;
    }

    @Override
	public BeanDefinition definition() {
		return this;
	}

	@Override
    public Object getSource() {
	    return source;
    }
	
	@Override
    public int getSortOrder() {
	    return sortOrder;
    }

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	@Override
    public String getId() {
		return id;
	}

	@Override
    public String getName() {
		return name;
	}
	
	public String getIdOrName(){
		return Strings.firstNotEmpty(id,name);
	}

	protected void setId(String id) {
		this.id = id;
	}
	
	protected void setName(String name) {
		this.name = name;
	}
	
	public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public ValueDefinition getValueDefinition() {
		return valueDefinition;
	}

	protected void setValueDefinition(ValueDefinition value) {
		this.valueDefinition = value;
	}

	public Method getInitMethod() {
		return initMethod;
	}

	protected void setInitMethod(Method initMethod) {
		this.initMethod = initMethod;
	}
	
	public Method getDestroyMethod() {
		return destroyMethod;
	}

	protected void setDestroyMethod(Method destroyMethod) {
		this.destroyMethod = destroyMethod;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	protected void setBeanClass(Class<?> beanClass) {
		this.beanClass = beanClass;
	}
	
	public BeanType getBeanClassType() {
		return beanClassType;
	}

	public Set<String> getQualifiers() {
		return qualifiers;
	}

	public void addQualifier(String qualifier) {
		this.qualifiers.add(qualifier);
	}

	protected void setBeanClassType(BeanType beanType) {
		this.beanClassType = beanType;
	}
	
	@Override
	public boolean isSingleton() {
		return singleton;
	}

	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public boolean isOverride() {
		return override;
	}

	public void setOverride(boolean overried) {
		this.override = overried;
	}

    public boolean isDefaultOverride() {
        return defaultOverride;
    }

    public void setDefaultOverride(boolean defaultOverride) {
        this.defaultOverride = defaultOverride;
    }

    public boolean isLazyInit() {
		return lazyInit;
	}

	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	@Override
    public boolean isPrimary() {
		return primary;
	}

	protected void setPrimary(boolean primary) {
		this.primary = primary;
	}
	
	public String getFactoryBeanName() {
		return factoryBeanName;
	}

	public String getFactoryMethodName() {
		return factoryMethodName;
	}

	protected void setFactoryMethodName(String factoryMethod) {
		this.factoryMethodName = factoryMethod;
	}
	
	public Method getFactoryMethod() {
		return factoryMethod;
	}

	protected void setFactoryMethod(Method factoryMethod) {
		this.factoryMethod = factoryMethod;
	}

	protected void setFactoryBeanName(String factoryBean) {
		this.factoryBeanName = factoryBean;
	}
	
	public Object getFactoryBean() {
		return factoryBean;
	}

	protected void setFactoryBean(Object factoryBean) {
		this.factoryBean = factoryBean;
	}
	
	public Constructor getConstructor() {
		return constructor;
	}

	protected void setConstructor(Constructor constructor) {
		this.constructor = constructor;
	}

	public List<ArgumentDefinition> getConstructorArguments() {
		return constructorArguments;
	}
	
	public void addConstructorArgument(ArgumentDefinition argumentDefinition){
		constructorArguments.add(argumentDefinition);
	}
	
	public List<TypeDefinition> getAdditionalTypeDefs() {
	    return additionalTypeDefs;
	}
	
	public void addAdditionalTypeDef(TypeDefinition def) {
	    additionalTypeDefs.add(def);
	}
	
	public List<FactoryDefinition> getFactoryDefs() {
	    return factoryDefs;
	}
	
	public void addFactoryDef(FactoryDefinition def) {
	    factoryDefs.add(def);
	}
	
	public List<PropertyDefinition> getProperties() {
		return properties;
	}

	public void addProperty(PropertyDefinition propertyDefinition){
		properties.add(propertyDefinition);
	}
	
	public List<InvokeDefinition> getInvokes() {
		return invokes;
	}
	
	public void addInvoke(InvokeDefinition invokeDefinition){
		invokes.add(invokeDefinition);
	}

	public boolean isValued(){
		return null != valueDefinition;
	}

    /**
     * Returns the proxy instance or the singleton instance.
     */
    public Object getInstance() {
        return null != proxyInstance ? proxyInstance : singletonInstance;
    }
	
	public Object getSingletonInstance() {
		return singletonInstance;
	}

	protected void setSingletonInstance(Object resolvedValue) {
		this.singletonInstance = resolvedValue;
	}

    public boolean hasProxy() {
        return null != proxyInstance;
    }

    public Object getProxyInstance() {
        return proxyInstance;
    }

    public void setProxyInstance(Object proxyInstance) {
        this.proxyInstance = proxyInstance;
    }

    public boolean isListBean(){
		return isTypeOf(List.class);
	}
	
	public boolean isSetBean(){
		return isTypeOf(Set.class);
	}
	
	public boolean isMapBean(){
		return isTypeOf(Map.class);
	}

    public boolean isInited() {
        return inited;
    }

    public void setInited(boolean inited) {
        this.inited = inited;
    }

    public boolean isConfigurable() {
		if(null == configurable) {
			configurable = beanClass.isAnnotationPresent(Configurable.class); 
		}
		return configurable;
	}
	
	public String getConfigurationPrefix() {
		if(null == configurationPrefix) {
			Configurable a = beanClass.getAnnotation(Configurable.class);
			configurationPrefix = null == a ? "" : a.prefix();
		}
		return configurationPrefix;
	}
	
	protected boolean isTypeOf(Class<?> beanType){
	    if(null != type && type.equals(beanType)) {
	        return true;
	    }
	    
	    for(TypeDefinition td : additionalTypeDefs) {
	        if(td.getType().equals(beanType)) {
	            return true;
	        }
	    }
	    
	    return false;
	}
	
	@Override
    public String toString() {
		ToStringBuilder sb = new ToStringBuilder();
		
		sb.append("class", beanClass);
		  
		if(!Strings.isEmpty(id)) {
		    sb.append("id", id);
		}
		
		if(!Strings.isEmpty(name)) {
		    sb.append("name", name);
		}
		
		if(null != source) {
		    sb.append("source", source);
		}
		
		return sb.toString();
    }
}