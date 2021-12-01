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
package leap.lang;

import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.exception.NestedIOException;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.io.IO;
import leap.lang.net.Urls;
import leap.lang.resource.Resource;
import leap.lang.resource.ResourceSet;
import leap.lang.resource.Resources;
import leap.lang.resource.SimpleResourceSet;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Factory {
	
	private static final String META_PREFIX      = "META-INF/services/";
    private static final String APP_PREFIX       = "/services/";
	private static final String CLASS_PROPERTY   = "class";
	private static final String IF_CLASS_PRESENT = "if-class-present";

    private static Map<Class<?>, Object> singleInstances = new ConcurrentHashMap<>(10);

    public static <T> T getInstance(Class<T> type) {
        T object = (T)singleInstances.get(type);
        if(null == object) {
            object = newInstance(type);
            singleInstances.put(type, object);
        }
        return object;
    }

	public static <T> T tryNewInstance(Class<T> type) throws FactoryException {
		Args.notNull(type);

		Resource resource = getSingleClassNameResource(type);
		if(null == resource || !resource.exists()){
			return null;
		}

		return newInstance(type, resource);
	}

	public static <T> T newInstance(Class<T> type) throws ObjectNotFoundException,FactoryException {
		Args.notNull(type);
		
		Resource resource = getSingleClassNameResource(type);
		if(null == resource || !resource.exists()){
			throw new ObjectNotFoundException("no instance of service class '" + type.getName() + "'");
		}
		
		return newInstance(type, resource);
	}

	public static <T> T newInstance(Class<T> type,Class<? extends T> defaultConcreteClass) {
		Args.notNull(type);
		Args.notNull(defaultConcreteClass);
		
		Resource resource = getSingleClassNameResource(type);
		if(null == resource || !resource.exists()){
			try {
	            return defaultConcreteClass.newInstance();
            } catch (Exception e) {
            	throw new FactoryException("Error new instance of class '" + defaultConcreteClass.getName() + "'", e);
            }
		}else{
			return newInstance(type, resource);
		}
	}
	
	public static <T> List<T> newInstances(Class<T> type){
		Args.notNull(type);
		
		ResourceSet resources = getAllClassNameResources(type);
		
		List<T> instances = new ArrayList<T>();
		
		for(Resource resource : resources){
			T instance = newInstance(type, resource);
			if (null != instance) {
				instances.add(newInstance(type, resource));
			}
		}
		
		return instances;
	}
	
	public static <T> Map<String, T> newNamedInstances(Class<T> type){
		Args.notNull(type);
		
		ResourceSet resources = getAllNamedClassNameResources(type);
		
		Map<String,T> instances = new LinkedHashMap<>();
		
		for(Resource resource : resources){
			instances.put(resource.getFilename(),newInstance(type, resource));
		}
		
		return instances;
	}
	
	private static Resource getSingleClassNameResource(Class<?> type){
		Resource r = Resources.getResource(Urls.CLASSPATH_ONE_URL_PREFIX + APP_PREFIX + type.getName());
		if(r.exists()) {
			return r;
		}
		return Resources.getResource(Urls.CLASSPATH_ONE_URL_PREFIX + META_PREFIX + type.getName());
	}
	
	private static ResourceSet getAllClassNameResources(Class<?> type){
		ResourceSet rs1 = Resources.scan(Urls.CLASSPATH_ALL_URL_PREFIX + META_PREFIX + type.getName().replace('.','/') + "/?*");
		ResourceSet rs2 = Resources.scan(Urls.CLASSPATH_ALL_URL_PREFIX + META_PREFIX + type.getName());

		Resource[] array = Arrays2.concat(rs1.toResourceArray(), rs2.toResourceArray());
		Arrays.sort(array, Comparator.comparing(Resource::getURLString));

		return new SimpleResourceSet(array);
	}
	
	private static ResourceSet getAllNamedClassNameResources(Class<?> type){
		return Resources.scan(Urls.CLASSPATH_ALL_URL_PREFIX + META_PREFIX + type.getName().replace('.','/') + "/?*");
	}
	
	@SuppressWarnings("unchecked")
    private static <T> T newInstance(Class<T> type, Resource resource) {
		Properties properties = readProperties(resource);
		
		String className = properties.getProperty(CLASS_PROPERTY);
		if(Strings.isEmpty(className)){
			throw new FactoryException("the 'class' property must not be empty in classpath resource : " + resource.getClasspath());
		}

		String ifPresent = properties.getProperty(IF_CLASS_PRESENT);
		if (!Strings.isEmpty(ifPresent) && null == Classes.tryForName(ifPresent)) {
			return null;
		}
		
		Class<T> clazz = (Class<T>)Classes.tryForName(className);
		if(null == clazz){
			throw new FactoryException("class '" + className + "' not found, check the 'class' property in classpath resource : " + resource.getClasspath());
		}
		
		T object = null;
		
		try {
	        object = clazz.newInstance();
	        
			BeanType beanType = BeanType.of(clazz);
			
			for(Object key : properties.keySet()){
				String name  = (String)key;
				
				if(!Strings.equals(CLASS_PROPERTY, name)){
					BeanProperty bp = beanType.getProperty(name);
					
					String value = properties.getProperty(name);
					
					bp.setValue(object, value);
				}
			}
	        
        } catch (Exception e) {
        	throw new FactoryException("Error creating instance of class '" + className + "', classpath resource : " + resource.getClasspath(), e);
        }
		
		return object;
	}
	
	private static Properties readProperties(Resource resource) {
		InputStream	is = null;
		try{
			is = resource.getInputStream();
			
			Properties properties = new Properties();
			
			if(Strings.endsWithIgnoreCase(resource.getFilename(),".xml")){
				properties.loadFromXML(is);
			}else{
				properties.load(is);
			}
			
			return properties;
		}catch(IOException e){
			throw new NestedIOException(e);
		}finally{
			IO.close(is);
		}
	}
	
	protected Factory(){
		
	}
}
