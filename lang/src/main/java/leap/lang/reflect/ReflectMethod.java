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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import leap.lang.Classes;
import leap.lang.Strings;

public class ReflectMethod extends ReflectMember{
	
	private final ReflectAccessor accessor;
	private final int             accessorIndex;
	private final Method          reflectedMethod;
	private ReflectParameter[]    parameters;
	private boolean               setter;
	private boolean               getter;
	
	protected ReflectMethod(ReflectClass reflectClass,Method method) {
		super(reflectClass,method);
		
		this.accessor		 = reflectClass.getAccessor();
		this.reflectedMethod = method;
		this.accessorIndex   = accessor == null ? -1 : accessor.getMethodIndex(method);
		
		this.initialize();
	}
	
	public String getName() {
	    return reflectedMethod.getName();
    }

	public Method getReflectedMethod(){
		return this.reflectedMethod;
	}
	
	public ReflectParameter[] getParameters(){
		return parameters;
	}
	
	public Class<?> getReturnType(){
		return reflectedMethod.getReturnType();
	}
	
	public boolean isStatic(){
		return Modifier.isStatic(reflectedMethod.getModifiers());
	}
	
	public boolean isSynthetic(){
		return reflectedMethod.isSynthetic();
	}
	
	public boolean isGetterMethod(){
		return getter;
	}
	
	public boolean isSetterMethod(){
		return setter;
	}
	
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClsss){
		return reflectedMethod.isAnnotationPresent(annotationClsss);
	}
	
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass){
		return reflectedMethod.getAnnotation(annotationClass);
	}
	
	public Annotation[] getAnnotations(){
		return reflectedMethod.getAnnotations();
	}
	
	public boolean hasReturnValue(){
		return !Classes.isVoid(reflectedMethod.getReturnType());
	}
	
	public Object invoke(Object instance,Object... args) {
		try {
			if(parameters.length != args.length){
				throw new IllegalArgumentException("argument's length must be " + parameters.length);
			}
			
			for(int i=0;i<args.length;i++){
				if(null == args[i]) {
					args[i] = Classes.getDefaultValue(parameters[i].getType());
			    }
			}
			
	        if(accessorIndex == -1){
	        	return reflectedMethod.invoke(instance, args);
	        }else{
	        	return reflectiveClass.getAccessor().invokeMethod(instance, accessorIndex, args);
	        }
        } catch (Throwable e) {
        	if(e instanceof InvocationTargetException){
        		e = e.getCause();
        	}
        	if(e instanceof RuntimeException){
        		throw (RuntimeException)e;
        	}
        	throw new ReflectException(Strings.format("Error invoking method '{0}'",getName()),e);
        }
	}
	
	public Object invokeStatic(Object... args) {
		try {
			if(parameters.length != args.length){
				throw new IllegalArgumentException("argument's length must be " + parameters.length);
			}
			
			for(int i=0;i<args.length;i++){
				if(null == args[i]) {
					args[i] = Classes.getDefaultValue(parameters[i].getType());
			    }
			}
			
			return reflectedMethod.invoke(null, args);
			
//	        if(index == -1){
//	        	return javaMethod.invoke(null, args);
//	        }else{
//	        	return accessor.invokeMethod(null, index, args);
//	        }
        } catch (Throwable e) {
        	if(e instanceof InvocationTargetException){
        		e = e.getCause();
        	}
        	if(e instanceof RuntimeException){
        		throw (RuntimeException)e;
        	}
        	throw new ReflectException(Strings.format("Error invoking method '{0}'",getName()),e);
        }
	}
	
	private void initialize() {
		this.parameters = new ReflectParameter[reflectedMethod.getParameterTypes().length];
		
		if(this.parameters.length > 0){
			String[] names = Reflection.getParameterNames(reflectedMethod);

			if(null == names){
				names = createUnknowParameterNames(parameters.length);
			}

			for(int i=0;i<parameters.length;i++){
				Parameter p = reflectedMethod.getParameters()[i];
				
				parameters[i] = new ReflectParameter(i+ 1,
													 names[i],
													 p,
													 reflectedMethod.getGenericParameterTypes()[i]);
			}
		}
	}
	
	void setSetterOf(ReflectField field){
		this.setter			 = true;
	}
	
	void setGetterOf(ReflectField field){
		this.getter          = true;
	}
	
	private static String[] createUnknowParameterNames(int length){
		String[] names = new String[length];
		
		for(int i=0;i<length;i++){
			names[i] = "arg" + (i+1);
		}
		
		return names;
	}
	
	@Override
    public String toString() {
		return reflectedMethod.toString();
    }
}
