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
package leap.lang.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import leap.lang.Args;
import leap.lang.Arrays2;
import leap.lang.Classes;
import leap.lang.Factory;
import leap.lang.Strings;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.exception.TooManyElementsException;

public class Reflection {
	
	private static ReflectMetadata metadata = Factory.newInstance(ReflectMetadata.class,ASMReflectMetadata.class);
	private static ReflectFactory  factory  = Factory.newInstance(ReflectFactory.class,ASMReflectFactory.class);
	
	protected static ReflectMetadata metadata(){
		return metadata;
	}
	
	protected static ReflectFactory factory(){
		return factory;
	}
	
	/**
	 * create a new instance of the given classs.
	 */
	public static <T> T newInstance(Class<T> clazz){
		return ReflectClass.of(clazz).newInstance();
	}
	
	/**
	 * create a new instance by the given constructor.
	 */
	public static <T> T newInstance(Constructor<T> constructor){
		return newInstance(constructor, Arrays2.EMPTY_BOOLEAN_ARRAY);
	}
	
	/**
	 * create a new instance by the given constructor.
	 */
	public static <T> T newInstance(Constructor<T> constructor,Object... args){
		try {
	        return constructor.newInstance(args);
        } catch (Exception e) {
        	handleException(e);
        	return null;
        }
	}
	
	public static String[] getParameterNames(Method method) {
		return metadata.getParameterNames(method);
	}
	
	public static String[] getParameterNames(Constructor<?> constructor) {
		return metadata.getParameterNames(constructor);
		//return Arrays.stream(constructor.getParameters()).map((p) -> p.getName()).toArray((size) -> new String[size]);
	}
	
	/**
	 * returns all the declared fields of the given class and all the superclasses up to <code>Object</code>.
	 */
    public static List<Field> getFields(Class<?> clazz){
        List<Field> fields = new ArrayList<Field>();
        
        for (Class<?> search = clazz; search != null; search = search.getSuperclass()) {
            for(Field field : search.getDeclaredFields()){
            	fields.add(field) ;
            }
        }
        
        return fields;
    }
    
	public static String fullQualifyName(Method m){
		StringBuilder sb = new StringBuilder(50);
		sb.append(m.getDeclaringClass().getName()).append(".").append(m.getName());
		sb.append("(");
		
		Class<?>[] paramTypes = m.getParameterTypes();
		for(int i=0;i<paramTypes.length;i++){
			if(i > 0){
				sb.append(",");
			}
			sb.append(paramTypes[i].getSimpleName());
		}
		sb.append(")");
		return sb.toString();
	}
	
	/**
	 * 
	 */
	public static Method getMethod(Class<?> cls,String name) throws ObjectNotFoundException, TooManyElementsException {
		List<Method> methods = getMethods(cls,name);
		if(methods.isEmpty()){
			throw new ObjectNotFoundException("No such method '" + name + "' in class '" + cls.getName() + "'");
		}
		if(methods.size() > 1){
			throw new TooManyElementsException("There are " + methods.size() + " methods named '" + name + "' in class '" + cls.getName() + "'");
		}
		return methods.get(0);
	}
	
	/**
	 * @throws ObjectNotFoundException if class or method not found
	 */
	public static Method getMethodByFqName(String fqName) throws ObjectNotFoundException {
		int leftParenIndex = fqName.lastIndexOf('(');
		int separateIndex  = leftParenIndex != -1 ? fqName.lastIndexOf('.', leftParenIndex) : fqName.lastIndexOf('.');
		
		if(separateIndex > 0){
			String   className  = fqName.substring(0,separateIndex);
			String   methodName = fqName.substring(separateIndex+1,separateIndex != -1 ? separateIndex : fqName.length());
			String[] paramTypes = null;
			
			if(separateIndex != -1){
				paramTypes = Strings.split(fqName.substring(separateIndex+1,fqName.length()+1),",");
			}
			
			Class<?> clazz = Classes.forName(className);
			
			List<Method> methods = getMethods(clazz,methodName);
			
			for(Method method : methods){
				if(null == paramTypes || paramTypes.length == 0){
					if(method.getParameterTypes().length == 0){
						return method;	
					}
				}else{
					if(method.getParameterTypes().length == paramTypes.length){
						
						boolean matched = true;
						
						for(int i=0;i<paramTypes.length;i++){
							Class<?> typeClass = method.getParameterTypes()[i];
							String   typeName  = paramTypes[i];
							
							if(!(typeClass.getSimpleName().equals(typeName) || typeClass.getName().equals(typeName))){
								matched = false;
								break;
							}
						}
						
						if(matched){
							return method;
						}
					}
				}
			}
		}
		
		throw new ObjectNotFoundException("method '" + fqName + "' not found");
	}
	
	/**
	 * @throws ObjectNotFoundException if method not found
	 */
	public static Method getMethodByNameOrDesc(Class<?> cls, String methodNameOrDesc) throws ObjectNotFoundException {
		int leftParenIndex = methodNameOrDesc.lastIndexOf('(');
		if(leftParenIndex < 0){
			return getMethod(cls, methodNameOrDesc);
		}
		
		int rightParenIndex = methodNameOrDesc.lastIndexOf(')');
		if(rightParenIndex != methodNameOrDesc.length() - 1){
			throw new IllegalArgumentException("Invalid method desc '" + methodNameOrDesc + "'");
		}
		
		String methodName = methodNameOrDesc.substring(0,leftParenIndex);
		String paramsDesc = methodNameOrDesc.substring(leftParenIndex+1,methodNameOrDesc.length() - 1);
		
		String[] paramTypes = Strings.split(paramsDesc,",");
		
		List<Method> methods = getMethods(cls,methodName);
		
		for(Method method : methods){
			if(null == paramTypes || paramTypes.length == 0){
				if(method.getParameterTypes().length == 0){
					return method;	
				}
			}else{
				if(method.getParameterTypes().length == paramTypes.length){
					
					boolean matched = true;
					
					for(int i=0;i<paramTypes.length;i++){
						Class<?> typeClass = method.getParameterTypes()[i];
						String   typeName  = paramTypes[i];
						
						if(!(typeClass.getSimpleName().equals(typeName) || typeClass.getName().equals(typeName))){
							matched = false;
							break;
						}
					}
					
					if(matched){
						return method;
					}
				}
			}
		}
		
		throw new ObjectNotFoundException("Method '" + methodNameOrDesc + "' not found in class '" + cls.getName() + "'");
	}
    
	/**
	 * returns all the declared methods of the given class and all the superclasses up to <code>Object</code>.
	 * 
	 * <p/>
	 * 
	 * synthetic method will be ignored. 
	 */
    public static List<Method> getMethods(Class<?> clazz){
        List<Method> methods = new ArrayList<Method>();
        
        for (Class<?> search = clazz; search != null; search = search.getSuperclass()) {
            for(Method method : search.getDeclaredMethods()){
                //ignore synthetic method
                if(!method.isSynthetic()){
                    methods.add(method) ;
                }
            }
        }
        
        return methods;
    }
    
	/**
	 * returns all the declared methods match the given name of the given class and all the superclasses up to <code>Object</code>.
	 */
    public static List<Method> getMethods(Class<?> clazz,String name){
        List<Method> methods = new ArrayList<Method>();
        
        for (Class<?> search = clazz; search != null; search = search.getSuperclass()) {
            for(Method method : search.getDeclaredMethods()){
                if(method.getName().equals(name)){
                    methods.add(method) ;
                }
            }
        }
        
        return methods;
    }    
	
	/**
	 * Handle the given reflection exception. Should only be called if no checked exception is expected to be thrown by
	 * the target method.
	 * 
	 * <p>
	 * Throws the underlying RuntimeException or Error in case of an InvocationTargetException with such a root cause.
	 * Throws an IllegalStateException with an appropriate message else.
	 * 
	 * @param ex the reflection exception to handle
	 */
	public static void handleException(Exception ex) {
		if (ex instanceof InvocationTargetException) {
			Throwable cause = ((InvocationTargetException) ex).getTargetException();
			
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			
			if (cause instanceof Error) {
				throw (Error) cause;
			}
			throw new ReflectException(ex);
		}
		
		if (ex instanceof NoSuchMethodException) {
			throw new IllegalStateException("Method not found: " + ex.getMessage());
		}
		
		if (ex instanceof NoSuchFieldException) {
			throw new IllegalStateException("Field not found: " + ex.getMessage());
		}
		
		if (ex instanceof IllegalAccessException) {
			throw new IllegalStateException("Illegal access method or field: " + ex.getMessage());
		}
		
		if (ex instanceof RuntimeException) {
			throw (RuntimeException) ex;
		}
		
		throw new ReflectException(ex);
	} 	
	
	/**
	 * Attempt to find a {@link Method} on the supplied class with the supplied name and no parameters. 
	 * 
	 * Searches all superclasses up to <code>Object</code>.
	 * 
	 * <p>
	 * Returns <code>null</code> if no {@link Method} can be found.
	 * 
	 * @param clazz the class to introspect
	 * @param name the name of the method
	 * @return the Method object, or <code>null</code> if none found
	 */
	public static Method findMethod(Class<?> clazz, String name) {
		return findMethod(clazz, name, Arrays2.EMPTY_CLASS_ARRAY);
	}
	
	/**
	 * 
	 */
	public static Method findFirstDeclaredMethod(Class<?> cls,String name){
		for(Method m : cls.getDeclaredMethods()){
			if(m.getName().equals(name)){
				return m;
			}
		}
		return null;
	}
	
	/**
	 * Attempt to find a {@link Method} on the supplied class with the supplied name and parameter types. 
	 * 
	 * Searches all superclasses up to <code>Object</code>.
	 * 
	 * <p>
	 * Returns <code>null</code> if no {@link Method} can be found.
	 * 
	 * @param clazz the class to introspect
	 * @param name the name of the method
	 * @param paramTypes the parameter types of the method (may be <code>null</code> to indicate any signature)
	 * 
	 * @return the Method object, or <code>null</code> if none found
	 */
	public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
		Args.notNull(clazz, "clazz");
		Args.notNull(name, "method name");
		
		Class<?> searchType = clazz;
		while (searchType != null) {
			Method[] methods = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());
			for (Method method : methods) {
				if (name.equals(method.getName())
						&& (paramTypes == null || Arrays2.equals(paramTypes, method.getParameterTypes()))) {
					return method;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}
	
	/**
	 * Attempt to find a {@link Method} on the supplied class with the supplied name and parameter types. 
	 * 
	 * Searches all superclasses up to <code>Object</code>.
	 * 
	 * <p>
	 * Returns <code>null</code> if no {@link Method} can be found.
	 * 
	 * @param clazz the class to introspect
	 * @param name the name of the method
	 * @param paramTypeNames the parameter type names of the method (may be <code>null</code> to indicate any signature)
	 * 
	 * @return the Method object, or <code>null</code> if none found
	 */
	public static Method findMethod(Class<?> clazz, String name, String... paramTypeNames) {
		Args.notNull(clazz, "clazz");
		Args.notNull(name, "method name");
		
		Class<?> searchType = clazz;
		while (searchType != null) {
			Method[] methods = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());
			for (Method method : methods) {
				if (name.equals(method.getName())){
					if(null == paramTypeNames || paramTypeNames.length == 0){
						if(method.getParameterTypes().length == 0){
							return method;	
						}
					}else{
						if(method.getParameterTypes().length == paramTypeNames.length){
							boolean matched = true;
							
							for(int i=0;i<paramTypeNames.length;i++){
								Class<?> typeClass = method.getParameterTypes()[i];
								String   typeName  = paramTypeNames[i];
								
								if(!(typeClass.getSimpleName().equals(typeName) || typeClass.getName().equals(typeName))){
									matched = false;
									break;
								}
							}
							
							if(matched){
								return method;
							}
						}
					}	
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}	
	
	/**
	 * Attempt to find a {@link Field field} on the supplied {@link Class} with the supplied <code>name</code>. 
	 * 
	 * Searches all superclasses up to {@link Object}.
	 * 
	 * @param clazz the class to introspect
	 * @param name the name of the field
	 * @return the corresponding Field object, or <code>null</code> if not found
	 */
	public static Field findField(Class<?> clazz, String name) {
		return findField(clazz, name, null);
	}

	/**
	 * Attempt to find a {@link Field field} on the supplied {@link Class} with the supplied <code>name</code> and/or
	 * {@link Class type}. 
	 * 
	 * Searches all superclasses up to {@link Object}.
	 * 
	 * @param clazz the class to introspect
	 * @param name the name of the field (may be <code>null</code> if type is specified)
	 * @param type the type of the field (may be <code>null</code> if name is specified)
	 * @return the corresponding Field object, or <code>null</code> if not found
	 */
	public static Field findField(Class<?> clazz, String name, Class<?> type) {
		Args.notNull(clazz, "clazz");
		Args.assertTrue(name != null || type != null, "Either name or type of the field must be specified");
		Class<?> searchType = clazz;
		while (!Object.class.equals(searchType) && searchType != null) {
			Field[] fields = searchType.getDeclaredFields();
			for (Field field : fields) {
				if ((name == null || name.equals(field.getName())) && (type == null || type.equals(field.getType()))) {
					return field;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}
	
	/**
	 * Invoke the specified {@link Method} against the supplied target object with no Args. The target object can
	 * be <code>null</code> when invoking a static {@link Method}.
	 * 
	 * <p>
	 * Thrown exceptions are handled via a call to {@link #handleReflectionException}.
	 * 
	 * @param method the method to invoke
	 * @param target the target object to invoke the method on
	 * @return the invocation result, if any
	 * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
	 */
	public static Object invokeMethod(Method method, Object target) {
		return invokeMethod(method, target, Arrays2.EMPTY_OBJECT_ARRAY);
	}

	/**
	 * Invoke the specified {@link Method} against the supplied target object with the supplied Args. The target
	 * object can be <code>null</code> when invoking a static {@link Method}.
	 * 
	 * <p>
	 * Thrown exceptions are handled via a call to {@link #handleReflectionException}.
	 * 
	 * @param method the method to invoke
	 * @param target the target object to invoke the method on
	 * @param args the invocation Arguments (may be <code>null</code>)
	 * @return the invocation result, if any
	 */
	public static Object invokeMethod(Method method, Object target, Object... args) {
		try {
			if(!method.isAccessible()){
				method.setAccessible(true);
			}
			return method.invoke(target, args);
		} catch (Exception ex) {
			handleException(ex);
			throw new ReflectException(ex.getMessage(), ex);
		}
	}
	
	/**
	 * Get the field represented by the supplied {@link Field field object} on the
	 * specified {@link Object target object}. In accordance with {@link Field#get(Object)}
	 * semantics, the returned value is automatically wrapped if the underlying field
	 * has a primitive type.
	 * <p>Thrown exceptions are handled via a call to {@link #handleReflectionException(Exception)}.
	 * @param field the field to get
	 * @param target the target object from which to get the field
	 * @return the field's current value
	 */
	public static Object getFieldValue(Object target,Field field) {
		try {
			return field.get(target);
		} catch (IllegalAccessException ex) {
			handleException(ex);
			throw new ReflectException("Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
		}
	}	
	
	protected Reflection(){
		
	}
}