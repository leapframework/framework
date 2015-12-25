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
package leap.lang.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import leap.lang.Arrays2;
import leap.lang.Classes;
import leap.lang.Named;
import leap.lang.Predicates;
import leap.lang.Strings;

public class ReflectClass implements Named {
	
	//protected static final Map<Class<?>, ReflectClass> cache = java.util.Collections.synchronizedMap(new WeakHashMap<Class<?>, ReflectClass>());
    protected static final Map<Class<?>, ReflectClass> cache = new ConcurrentHashMap<Class<?>, ReflectClass>();
    
	public static ReflectClass of(Class<?> clazz) {
		ReflectClass rclass = cache.get(clazz);
		
		if(null == rclass){
			rclass = new ReflectClass(clazz);
			cache.put(clazz, rclass);
		}
		
		return (ReflectClass)rclass;
	}
	
	private final Class<?>        clazz;
	private final ReflectAccessor accessor;
	private final boolean      	  innerClass;

	private ReflectConstructor[]  constructors;
	private ReflectField[]		  fields;
	private ReflectMethod[]		  methods;
	private ReflectField[]		  declaredFields;
	private ReflectMethod[]		  declaredMethods;	
	private ReflectConstructor    defaultConstructor;	
	private boolean			      defaultConstructorInner = false;
	
	protected ReflectClass(Class<?> clazz){
		this.clazz      = clazz;
		this.accessor   = Reflection.factory().createAccessor(this.clazz);
		this.innerClass = Classes.isInnerClass(clazz);
		
		this.initialize();
	}
	
	public String getName() {
	    return clazz.getName();
    }
	
	public boolean hasDefaultConstructor(){
		return null != defaultConstructor;
	}

	@SuppressWarnings("unchecked")
	public <T> T newInstance() throws ReflectException{
		if(null == defaultConstructor){
			throw new ReflectException(Strings.format("there is no default constructor available in class '{0}'",getName()));	
		}
		
		if(defaultConstructorInner){
			return (T)defaultConstructor.newInstance(Reflection.newInstance(clazz.getEnclosingClass()));
		}else{
			if(null != accessor && accessor.canNewInstance()){
				return (T)accessor.newInstance();	
			}else{
				return (T)defaultConstructor.newInstance((Object[])null);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T[] newArray(int length){
		return null != accessor ? (T[])accessor.newArray(length) : (T[])Array.newInstance(this.clazz, length);
	}
	
	public int getArrayLength(Object array){
		return null != accessor ? accessor.getArrayLength(array) : Array.getLength(array);
	}
	
	public Object getArrayItem(Object array,int index){
		return null != accessor ? accessor.getArrayItem(array, index) : Array.get(array, index);
	}
	
	public void setArrayItem(Object array,int index,Object value){
		if(null != accessor) {
			accessor.setArrayItem(array, index, value);	
		}else{
			Array.set(array, index, value);
		}
	}
	
	public boolean isMap(){
		return Map.class.isAssignableFrom(clazz);
	}
	
	public boolean isArray() {
		return clazz.isArray();
	}
	
	public boolean isAbstract() {
		return Modifier.isAbstract(clazz.getModifiers());
	}
	
	public boolean isInterface() {
		return clazz.isInterface();
	}
	
	public boolean isEnum(){
		return clazz.isEnum();
	}
	
	public boolean isConcrete(){
		return !clazz.isInterface() && !isAbstract();
	}
	
	public boolean isInnerClass(){
		return innerClass;
	}
	
	public Class<?> getReflectedClass() {
    	return clazz;
    }
	
	public Annotation[] getAnnotations(){
		return clazz.getAnnotations();
	}
	
	public ReflectConstructor[] getConstructors(){
		return constructors;
	}
	
	public ReflectConstructor getConstructor(){
		return getConstructor((Class<?>[])null);
	}
	
	public ReflectConstructor getConstructor(Class<?>... parameterTypes) {
		if(null == parameterTypes || parameterTypes.length == 0){
			return defaultConstructor;
		}
		
		for(ReflectConstructor c : constructors) {
			Constructor<?> jc = c.getReflectedConstructor();

			if(jc.getParameterTypes().length == parameterTypes.length){
				
				boolean match = true;
				
				for(int i=0;i<parameterTypes.length;i++){
					
					if(!jc.getParameterTypes()[i].equals(parameterTypes[i])){
						match = false;
						break;
					}
				}
				
				if(match){
					return c;
				}
			}
		}
		
		return null;
	}
	
	public ReflectField[] getFields() {
		return fields;
	}
	
	public ReflectField[] getDeclaredFields(){
		return declaredFields;
	}
	
	public ReflectField getField(final String name){
		return Arrays2.firstOrNull(fields, new Predicate<ReflectField>() {
			public boolean test(ReflectField object) {
	            return object.getName().equals(name);
            }
		});
	}
	
	public ReflectField getField(final String name,final Class<?> fieldType){
		return Arrays2.firstOrNull(fields,Predicates.<ReflectField>nameEquals(name));
	}
	
	public ReflectField getFieldIgnorecase(final String name){
		return Arrays2.firstOrNull(fields,Predicates.<ReflectField>nameEqualsIgnoreCase(name));
	}
	
	public ReflectMethod[] getMethods(){
		return methods;
	}
	
	public ReflectMethod[] getDeclaredMethods(){
		return declaredMethods;
	}
	
	public ReflectMethod getMethod(final String name){
		return Arrays2.firstOrNull(methods, Predicates.<ReflectMethod>nameEquals(name));
	}
	
	public ReflectMethod getMethod(final Method m){
		for(ReflectMethod rm : methods){
			if(rm.getReflectedMethod().equals(m)){
				return rm;
			}
		}
		return null;
	}
	
	public ReflectMethod getMethod(final String name,final Class<?>... argumentTypes){
		for(ReflectMethod rm : methods){
			if(rm.getName().equals(name)){
				Method m = rm.getReflectedMethod();
				
				//Avoids NullPointerException
				if(null == argumentTypes || argumentTypes.length == 0){
					if(m.getParameterTypes().length == 0){
						return rm;
					}
					break;
				}
            	
            	if(m.getParameterTypes().length == argumentTypes.length){
                    boolean matched = true;
                    
                    for(int i=0;i<m.getParameterTypes().length;i++){
                    	if(argumentTypes[i] == null){
                    		continue;
                    	}
                    	
                        if(!m.getParameterTypes()[i].equals(argumentTypes[i])){
                            matched = false;
                            break;
                        }
                    }
                    
                    if(matched){
                        return rm;    
                    }
            	}
			}
		}
		return null;
	}
	
	public ReflectMethod getMethod(String name,Class<?> returnType,Class<?>... argumentTypes){
		ReflectMethod m = getMethod(name, argumentTypes);
		
		return null == m ? null : (m.getReflectedMethod().getReturnType().equals(returnType) ? m : null);
	}	
	
	public ReflectMethod[] getMethods(final String name) {
		return Arrays2.filter(methods, new Predicate<ReflectMethod>() {
			public boolean test(ReflectMethod object) {
	            return object.getName().equals(name);
            }
		}).toArray(new ReflectMethod[]{});
	}
	
	ReflectAccessor getAccessor(){
		return accessor;
	}
	
	private void initialize(){
		//constructors
		this.createConstructors();
		
		//methods
		this.createMethods();	 //must create methods firstly , will be used in createFields()
		
		//fields
		this.createFields();
	}

	private void createConstructors(){
		//new an empty ArrayList.
		List<ReflectConstructor> constructorList = new ArrayList<ReflectConstructor>();
		
		//iterate all declared constructors.
		Constructor<?>[] constructors = clazz.getDeclaredConstructors();
		for(int i=0;i<constructors.length;i++) {
			Constructor<?> c = constructors[i];
			if(!c.isSynthetic()){
				ReflectConstructor rc = new ReflectConstructor(this,c);
				
				constructorList.add(rc);
				
				if(innerClass && !Modifier.isStatic(clazz.getModifiers())){
					if(c.getParameterTypes().length == 1 
							&& c.getParameterTypes()[0].equals(clazz.getEnclosingClass())){
						defaultConstructor      = rc;
						defaultConstructorInner = true;
					}
					
				}else if(c.getParameterTypes().length == 0){
					defaultConstructor = rc;
				}
			}
		}
		
		this.constructors = constructorList.toArray(new ReflectConstructor[constructorList.size()]);
	}
	
	private void createFields(){
		List<ReflectField> fieldList = new ArrayList<ReflectField>();
		
		for(Field f : Reflection.getFields(clazz)){
			fieldList.add(new ReflectField(this,f));
		}
		
		this.fields = fieldList.toArray(new ReflectField[fieldList.size()]);
		this.declaredFields = getDeclaredMembers(fieldList).toArray(new ReflectField[]{});
	}
	
	private void createMethods(){
		List<ReflectMethod> methodList = new ArrayList<ReflectMethod>();
		
		for(Method m : Reflection.getMethods(clazz)){
			if(Object.class.equals(m.getDeclaringClass())){
				continue;
			}
			
			methodList.add(new ReflectMethod(this,m));
		}
		
		this.methods = methodList.toArray(new ReflectMethod[methodList.size()]);
		this.declaredMethods = getDeclaredMembers(methodList).toArray(new ReflectMethod[]{});
	}
	
	@Override
    public String toString() {
		return clazz.toString();
    }
	
	private static <T extends ReflectMember,E> List<T> getDeclaredMembers(List<T> members){
		return members.stream().filter((m) -> {return m.isDeclared();}).collect(Collectors.toList());
	}
}