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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import leap.lang.Arrays2;
import leap.lang.Classes;
import leap.lang.Primitives;
import leap.lang.Strings;

public class ReflectField extends ReflectMember {
	
	private final Field    reflectedField;
	private final Class<?> fieldType;
	
	private final ReflectMethod setter;
	private final ReflectMethod getter;
	
	private final int fieldAccessorIndex;
	private final int setterAccessorIndex;
	private final int getterAccessorIndex;
	
	private final ReflectAccessor accessor;
	
	protected ReflectField(ReflectClass reflectiveClass, Field field){
		super(reflectiveClass,field);
		
		this.reflectedField = field;
		this.fieldType      = field.getType();
		this.setter         = findSetter();
		this.getter         = findGetter();
		this.accessor       = reflectiveClass.getAccessor();
		
		if(null != setter){
			setter.setSetterOf(this);
		}
		
		if(null != getter){
			getter.setGetterOf(this);
		}
		
		this.fieldAccessorIndex  = null == accessor ? -1 : accessor.getFieldIndex(field);
		this.setterAccessorIndex = null == setter || null == accessor ? -1 : accessor.getMethodIndex(setter.getReflectedMethod());
		this.getterAccessorIndex = null == getter || null == accessor ? -1 : accessor.getMethodIndex(getter.getReflectedMethod());
		
		this.initialize();
	}
	
	public String getName() {
	    return reflectedField.getName();
    }

    public Class<?> getType(){
        return fieldType;
    }
    
    public Type getGenericType(){
        return reflectedField.getGenericType();
    }
    
    public Annotation[] getAnnotations() {
    	return reflectedField.getAnnotations();
    }
    
	public Field getReflectedField(){
		return reflectedField;
	}
	
	public Class<?> getDeclaringClass(){
		return reflectedField.getDeclaringClass();
	}
	
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationType){
		return reflectedField.isAnnotationPresent(annotationType);
	}
	
	public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
		return reflectedField.getAnnotation(annotationType);
	}
	
	public boolean isSynthetic(){
		return reflectedField.isSynthetic();
	}
	
	public boolean isStatic(){
		return Modifier.isStatic(reflectedField.getModifiers());
	}	
	
	public boolean isFinal(){
		return Modifier.isFinal(reflectedField.getModifiers());
	}
	
	public boolean isTransient(){
		return Modifier.isTransient(reflectedField.getModifiers());
	}
	
	public boolean hasGetter(){
		return null != getter;
	}
	
	public boolean hasSetter(){
		return null != setter;
	}
	
	public ReflectMethod getGetter(){
		return getter;
	}
	
	public ReflectMethod getSetter(){
		return setter;
	}
	
	public boolean isPublicGet(){
		return Modifier.isPublic(reflectedField.getModifiers()) || (null != getter && getter.isPublic()); 
	}
	
	public boolean isPublicSet(){
		return Modifier.isPublic(reflectedField.getModifiers()) || (null != setter && setter.isPublic()); 
	}
	
	public boolean isPublicGetSet(){
		return Modifier.isPublic(reflectedField.getModifiers()) || (
				(null != setter && setter.isPublic()) && (null != getter && getter.isPublic())); 
	}
	
	public void setValue(Object instance, Object value) {
		setValue(instance,value,false);
	}
	
	public void setValue(Object instance, Object value, boolean useSetter) {
        try {
        	if(reflectedField.isSynthetic() || isFinal()){
        		reflectedField.set(instance, safeValue(value));
        	}else{
            	if(useSetter && null != setter){
            		if(setterAccessorIndex != -1){
            			accessor.invokeMethod(instance, setterAccessorIndex, safeValue(value));
            		}else{
            			setter.invoke(instance, safeValue(value));
            		}
            	}else{
            		if(fieldAccessorIndex != -1){
                    	accessor.setFieldValue(instance, fieldAccessorIndex, safeValue(value));
                    }else{
                        reflectedField.set(instance, safeValue(value));
                    }
            	}
        	}
        } catch (Exception e) {
        	throw new ReflectException(Strings.format("Error setting value '{0}' to field '{1}'",value,getName()),e);
        }
	}
	
	public Object getValue(Object instance) {
		return getValue(instance,false);
	}
	
	public Object getValue(Object instance,boolean useGetter) {
        try {
        	if(useGetter && null != getter){
        		if(getterAccessorIndex != -1){
        			return accessor.invokeMethod(instance, getterAccessorIndex, Arrays2.EMPTY_OBJECT_ARRAY);
        		}else{
        			return getter.invoke(instance, Arrays2.EMPTY_OBJECT_ARRAY);
        		}
        	}else{
        		if(fieldAccessorIndex != -1){
                    return accessor.getFieldValue(instance, fieldAccessorIndex);
                }else {
                    return reflectedField.get(instance);
                }
        	}
        } catch (Throwable e) {
        	if(e instanceof InvocationTargetException){
        		e = e.getCause();
        	}
        	if(e instanceof RuntimeException){
        		throw (RuntimeException)e;
        	}
        	throw new ReflectException(Strings.format("Error getting value of field '{0}'",getName()),e);
        }
	}
	
	private void initialize(){
		this.setAccessiable();
	}
	
	private ReflectMethod findSetter(){
		String   fieldName  = reflectedField.getName().startsWith("_") ? reflectedField.getName().substring(1) : reflectedField.getName();
		String   nameToFind = "set" + Character.toUpperCase(fieldName.charAt(0)) + (fieldName.length() > 1 ? fieldName.substring(1) : "");
		Class<?> fieldType  = Primitives.wrap(reflectedField.getType());
		
		ReflectMethod m = findSetter(fieldType, nameToFind);
		
		if(null == m && Classes.isBoolean(fieldType) && fieldName.startsWith("is") && fieldName.length() > 2){
			nameToFind = "set" + Character.toUpperCase(fieldName.charAt(2)) + (fieldName.length() > 3 ? fieldName.substring(3) : "");
			
			m = findSetter(fieldType,nameToFind);
		}
		
		if(null == m){
			m = findSetter(fieldType,fieldName);
		}
		
		return m;
	}
	
	private ReflectMethod findSetter(Class<?> fieldType,String nameToFind){
		//iterate all public methods.
		for(ReflectMethod rm : reflectiveClass.getMethods()){
			Method m = rm.getReflectedMethod();
			if(m.getParameterTypes().length == 1 && 
					fieldType.isAssignableFrom(Primitives.wrap(m.getParameterTypes()[0]))){
				
				if(m.getName().equals(nameToFind)){
					return rm;
				}
			}
		}
		return null;		
	}
	
	private ReflectMethod findGetter(){
		String   fieldName  = reflectedField.getName().startsWith("_") ? reflectedField.getName().substring(1) : reflectedField.getName();
		String   nameToFind = "get" + Character.toUpperCase(fieldName.charAt(0)) + (fieldName.length() > 1 ? fieldName.substring(1) : "");
		Class<?> fieldType  = Primitives.wrap(reflectedField.getType());
		
		ReflectMethod m = findGetter(fieldType,nameToFind);
		
		if(null == m && Classes.isBoolean(fieldType)){
			if(fieldName.startsWith("is") && fieldName.length() > 2){
				if(Boolean.class.equals(reflectedField.getType())){
					nameToFind = "get" + Strings.upperFirst(fieldName.substring(2));
					
					if(null != (m = findGetter(fieldType, nameToFind))){
						return m;
					}
				}
				
				nameToFind = fieldName;
			}else{
				nameToFind = "is" + Character.toUpperCase(fieldName.charAt(0)) + (fieldName.length() > 1 ? fieldName.substring(1) : "");
			}
			
			m = findGetter(fieldType,nameToFind);
		}

		return m;
	}
	
	private ReflectMethod findGetter(Class<?> fieldType,String nameToFind){
		for(ReflectMethod rm : reflectiveClass.getMethods()){
			Method m = rm.getReflectedMethod();
			if(m.getParameterTypes().length == 0 && 
					Primitives.wrap(m.getReturnType()).isAssignableFrom(fieldType)){
				
				if(m.getName().equals(nameToFind)){
					return rm;
				}
			}
		}
		return null;		
	}
	
	private void setAccessiable(){
		try {
			if(!reflectedField.isAccessible()){
				this.reflectedField.setAccessible(true);	
			}
        } catch (SecurityException e) {
        	;
        }
	}
	
    private Object safeValue(Object value){
        if(null == value){
        	return Classes.getDefaultValue(fieldType);
        }
        return value;
    }
    
	@Override
    public String toString() {
		return reflectedField.toString();
    }
}