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

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import leap.lang.Strings;

public class ReflectConstructor extends ReflectMember {

	private final Constructor<?> reflectedConstructor;
	private ReflectParameter[]   parameters;
	
	protected ReflectConstructor(ReflectClass reflectiveClass, Constructor<?> javaConstructor){
		super(reflectiveClass,javaConstructor);
		this.reflectedConstructor = javaConstructor;
		this.initialize();
	}
	
	public String getName() {
	    return reflectedConstructor.getName();
    }
	
	public ReflectParameter[] getParameters(){
		return parameters;
	}
	
	public Constructor<?> getReflectedConstructor(){
		return this.reflectedConstructor;
	}
	
	@SuppressWarnings("unchecked")
    public <T> T newInstance(Object... args){
		try {
	        return (T)reflectedConstructor.newInstance(args);
        } catch (Exception e) {
        	StringBuilder s = new StringBuilder();
        	for(int i=0;i<parameters.length;i++){
        		if(i>0){
        			s.append(',');
        		}
        		s.append(parameters[i].getName());
        	}
        	
        	throw new ReflectException(Strings.format(
        			"Error newInstance in constructor '{0}'({1})", getName(), s.toString()),e);
        }
	}
	
	private void initialize(){
		this.setAccessiable();

		this.parameters = new ReflectParameter[reflectedConstructor.getParameterTypes().length];
		
		if(this.parameters.length > 0){
			String[] names = Reflection.getParameterNames(reflectedConstructor);

			if(null == names){
				names = createUnknowParameterNames(parameters.length);
			}

			if(reflectedConstructor.getDeclaringClass().isEnum() && reflectedConstructor.getGenericParameterTypes().length != this.parameters.length){
				//enum constructor's parameter size not equals to generic parameter types.
				
				for(int i=0;i<parameters.length;i++){
					Parameter p 		  = reflectedConstructor.getParameters()[i];
					Type 	  genericType = null;
					
					if(i < 2){
						genericType = p.getType();
					}else{
						genericType = reflectedConstructor.getGenericParameterTypes()[i-2];
					}

					parameters[i] = new ReflectParameter(i+1,
														 names[i],
														 reflectedConstructor.getParameters()[i],
														 genericType);
				}
				
			}else{
				for(int i=0;i<parameters.length;i++){
					parameters[i] = new ReflectParameter(i+1,
														 names[i],
														 reflectedConstructor.getParameters()[i],
														 reflectedConstructor.getGenericParameterTypes()[i]);
				}
			}
		}
	}
	
	private void setAccessiable(){
		try {
	        this.reflectedConstructor.setAccessible(true);
        } catch (SecurityException e) {
        	;
        }
	}
	
	@Override
    public String toString() {
		return reflectedConstructor.toString();
    }
	
	private static String[] createUnknowParameterNames(int length){
		String[] names = new String[length];
		
		for(int i=0;i<length;i++){
			names[i] = "arg" + (i+1);
		}
		
		return names;
	}
}