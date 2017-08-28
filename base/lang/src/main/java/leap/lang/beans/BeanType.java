/*
* Copyright 2012 the original author or authors.
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
package leap.lang.beans;

import static leap.lang.Beans.GETTER_PREFIX;
import static leap.lang.Beans.IS_PREFIX;
import static leap.lang.Beans.SETTER_PREFIX;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.Types;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.reflect.ReflectClass;
import leap.lang.reflect.ReflectException;
import leap.lang.reflect.ReflectField;
import leap.lang.reflect.ReflectMethod;

public class BeanType {
	
	//private static final Map<Class<?>, BeanType> cache = Collections.synchronizedMap(new WeakHashMap<Class<?>, BeanType>());
    private static final Map<Class<?>, BeanType> cache = new ConcurrentHashMap<Class<?>, BeanType>();
    
	public static BeanType of(Class<?> beanClass) {
		Args.notNull(beanClass,"bean class");
		
		BeanType accessor = cache.get(beanClass);
		
		if(null == accessor){
			try {
	            accessor = new BeanType(beanClass);
            } catch (Throwable e) {
            	if(e instanceof ReflectException){
            		throw (ReflectException)e;
            	}else{
            		throw new ReflectException("Error create bean type for class '" + beanClass.getName() + "' : " + e.getMessage(),e);
            	}
            }
			
			cache.put(beanClass, accessor);
		}
		
		return accessor;
	}

	private final Map<Object, Object>	   attributes;
	private final Class<?>	               beanClass;
	private final ReflectClass             reflectClass;
	private final BeanProperty[]           properties;
	private final Map<String,BeanProperty> originalNamePropertyMap;
	private final Map<String,BeanProperty> lowerCaseNamePropertyMap;
	
	protected BeanType(Class<?> beanClass){
		this.attributes				  = Collections.synchronizedMap(new WeakHashMap<Object, Object>(1));
		this.beanClass                = beanClass;
		this.reflectClass             = ReflectClass.of(beanClass);
		this.properties               = initProperties();
		this.originalNamePropertyMap  = createPropertyMap(false);
		this.lowerCaseNamePropertyMap = createPropertyMap(true);
	}
	
	public Object getAttribute(Object key){
		return attributes.get(key);
	}
	
	public void setAttribute(Object key,Object value){
		attributes.put(key, value);
	}
	
	public Object removeAttribute(Object key){
		return attributes.remove(key);
	}
	
	public Class<?> getBeanClass(){
		return beanClass;
	}
	
	public ReflectClass getReflectClass(){
		return reflectClass;
	}
	
	public BeanProperty[] getProperties(){
		return properties;
	}
	
	public boolean hasProperty(String name){
		return tryGetProperty(name) != null;
	}
	
	public BeanProperty getProperty(String name) throws ObjectNotFoundException {
		return getProperty(name,false);
	}
	
	public BeanProperty getProperty(String name,boolean ignoreCase) throws ObjectNotFoundException{
		BeanProperty p = ignoreCase ? lowerCaseNamePropertyMap.get(name.toLowerCase()) : originalNamePropertyMap.get(name);
		if(null == p){
			throw new ObjectNotFoundException("property '" + name + "' not found in class '" + beanClass.getName() + "'");
		}
		return p;
	}
	
	public BeanProperty tryGetProperty(String name){
		return originalNamePropertyMap.get(name);
	}
	
	public BeanProperty tryGetProperty(String name,boolean ignorecase){
		return ignorecase ? lowerCaseNamePropertyMap.get(name.toLowerCase()) : originalNamePropertyMap.get(name);
	}

    public Object tryGetProperty(Object bean, String property) {
        BeanProperty bp = tryGetProperty(property);
        if(null == bp) {
            return null;
        }
        return bp.getValue(bean);
    }

    public Object tryGetProperty(Object bean, String property, boolean ignoreCase) {
        BeanProperty bp = tryGetProperty(property, ignoreCase);
        if(null == bp) {
            return null;
        }
        return bp.getValue(bean);
    }
	
	public void setProperty(Object bean,String property, Object value) throws ObjectNotFoundException {
		getProperty(property).setValue(bean, value);
	}
	
	public void setProperty(Object bean,String property, Object value, boolean ignoreCase) throws ObjectNotFoundException{
		getProperty(property, ignoreCase).setValue(bean, value);
	}
	
	public boolean trySetProperty(Object bean, String property, Object value){
        return trySetProperty(bean, property, value, false);
	}
	
	public boolean trySetProperty(Object bean, String property, Object value, boolean ignoreCase){
		BeanProperty prop = tryGetProperty(property,ignoreCase);
		
		if(null != prop){
			prop.setValue(bean, value);
			return true;
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
    public <T> T newInstance(){
		return (T)reflectClass.newInstance();
	}
	
	public Map<String, Object> toMap(Object bean,Predicate<BeanProperty> predicate){
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		
		for(BeanProperty p : properties){
			if(p.isReadable() && predicate.test(p)){
				map.put(p.getName(),p.getValue(bean));
			}
		}
		
		return map;
	}
	
	public Map<String, Object> toMap(Object bean){
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		
		for(BeanProperty p : properties){
			if(p.isReadable()){
				map.put(p.getName(),p.getValue(bean));
			}
		}
		
		return map;
	}
	
	private BeanProperty[] initProperties(){
		Map<String, BeanProperty> props = new LinkedHashMap<String, BeanProperty>();
		
		Set<Method> methods = new HashSet<Method>();
		
		//from fields
		for(ReflectField field : reflectClass.getFields()){
			
			if(field.isStatic()){
				continue;
			}
			
			if(field.isPublic() || field.hasGetter() || field.hasSetter()){
			
				String name = getTranditionalPropertyName(field);
				
				if(props.containsKey(name)){
					continue;
				}
				
				BeanProperty prop = new BeanProperty(this,name);
				
				prop.setType(field.getType());
				prop.setGenericType(field.getGenericType());
				prop.setTypeInfo(Types.getTypeInfo(field.getType(), field.getGenericType()));
				
				prop.setField(field);

				prop.setGetter(field.getGetter());
				prop.setReadable(field.isPublicGet());

				prop.setSetter(field.getSetter());
				prop.setWritable(field.isPublicSet() || (field.isPublic() && !field.isFinal()));
				prop.setTransient(field.isTransient());
				prop.setAnnotations(field.getReflectedField().getAnnotations());
				
				if(prop.hasGetter()){
					methods.add(prop.getGetter());
					prop.setAnnotations(Arrays2.concat(prop.getAnnotations(), prop.getGetter().getAnnotations()));
				}
				
				if(prop.hasSetter()){
					methods.add(prop.getSetter());
					prop.setAnnotations(Arrays2.concat(prop.getAnnotations(), prop.getSetter().getAnnotations()));
				}
				
				props.put(name, prop);
			}
		}
		
		//from methods
		for(ReflectMethod rm : reflectClass.getMethods()){
			Method m = rm.getReflectedMethod();
			
			if(rm.isStatic()){
				continue;
			}
			
			if(!methods.contains(m)){
				String methodName = rm.getName();
				
				if(methodName.startsWith(SETTER_PREFIX) && methodName.length() > SETTER_PREFIX.length()){
					if(m.getReturnType().equals(void.class) && m.getParameterTypes().length == 1){
						BeanProperty prop = getOrCreatePropertyFor(props,methodName,SETTER_PREFIX,m.getParameterTypes()[0]);
						
						if(null != prop){
							prop.setSetter(rm);
							prop.setWritable(rm.isPublic());
							
							if(null == prop.getGenericType()){
								prop.setGenericType(m.getGenericParameterTypes()[0]);	
								prop.setTypeInfo(Types.getTypeInfo(prop.getType(), prop.getGenericType()));
								//TODO : duplicate annotations ?
								prop.setAnnotations(Arrays2.concat(prop.getAnnotations(), m.getAnnotations()));
							}
						}
						
					}
				}else if(methodName.startsWith(GETTER_PREFIX) && methodName.length() > GETTER_PREFIX.length()){
					
					if(!m.getReturnType().equals(void.class) && m.getParameterTypes().length == 0) {
						
						BeanProperty prop = getOrCreatePropertyFor(props, methodName, GETTER_PREFIX, m.getReturnType());

						if(null != prop){
							prop.setGetter(rm);
							prop.setReadable(rm.isPublic());
							if(null == prop.getGenericType()){
								prop.setGenericType(m.getGenericReturnType());	
								prop.setTypeInfo(Types.getTypeInfo(prop.getType(), prop.getGenericType()));
								prop.setAnnotations(Arrays2.concat(prop.getAnnotations(), m.getAnnotations()));
							}							
						}
					}
					
				}else if(methodName.startsWith(IS_PREFIX) && methodName.length() > IS_PREFIX.length()){

					if((m.getReturnType().equals(Boolean.class) || m.getReturnType().equals(Boolean.TYPE)) && m.getParameterTypes().length == 0){
						
						BeanProperty prop = getOrCreatePropertyFor(props, methodName, IS_PREFIX, m.getReturnType());
						
						if(null != prop && !prop.hasGetter()){
							prop.setGetter(rm);
							prop.setReadable(rm.isPublic());
							if(null == prop.getGenericType()){
								prop.setGenericType(m.getGenericReturnType());	
								prop.setTypeInfo(Types.getTypeInfo(prop.getType(), prop.getGenericType()));
								prop.setAnnotations(Arrays2.concat(prop.getAnnotations(), m.getAnnotations()));
							}
						}
					}
				}
			}
		}
		
		return props.values().toArray(new BeanProperty[props.size()]);
	}
	
	private BeanProperty getOrCreatePropertyFor(Map<String, BeanProperty> props, String methodName,String prefix,Class<?> type){
		String propName = methodName.substring(prefix.length());
		
		char c = propName.charAt(0);
		if(Character.isUpperCase(c)){
			propName = Character.toLowerCase(c) + propName.substring(1);
			
			BeanProperty prop = props .get(propName);
			
			if(null == prop){
				prop = new BeanProperty(this, propName);
				
				prop.setType(type);
				props.put(propName, prop);
			}else if(!type.equals(prop.getType())){
				return null;
			}
			
			return prop; 
		}
		
		return null;
	}
	
	private String getTranditionalPropertyName(ReflectField field) {
		String name = null;
		if(field.hasSetter()){
			name = field.getSetter().getName().substring(SETTER_PREFIX.length());
		}else if(field.hasGetter()){
			name = field.getGetter().getName();
			
			if(name.startsWith(IS_PREFIX)){
				name = name.substring(IS_PREFIX.length());
			}else{
				name = name.substring(GETTER_PREFIX.length());
			}
		}else{
			name = field.getName().startsWith("_") ? field.getName().substring(1) : field.getName();
		}
		
		return name.length() == 1 ? name.toLowerCase() : Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}
	
	private Map<String,BeanProperty> createPropertyMap(boolean lowercase){
		Map<String,BeanProperty> map = new LinkedHashMap<String, BeanProperty>(properties.length);
		for(BeanProperty p : properties){
			map.put(lowercase ? p.getName().toLowerCase() : p.getName(), p);
		}
		return map;
	}

	@Override
    public String toString() {
	    return "BeanType:" + beanClass.getName();
    }
}
