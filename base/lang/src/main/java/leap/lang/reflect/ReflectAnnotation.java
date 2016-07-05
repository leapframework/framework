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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import leap.lang.Arrays2;
import leap.lang.Assert;
import leap.lang.Named;
import leap.lang.exception.ObjectNotFoundException;

public class ReflectAnnotation {

	public static ReflectAnnotation of(Class<?> annotationType){
		Assert.isTrue(annotationType.isAnnotation(),"Not an annotation type");
		return new ReflectAnnotation(annotationType);
	}
	
	/**
	 * Represents an annotation element.
	 */
	public static class AElement implements Named {
		
		private final Method m;
		
		private AElement(Method m){
			this.m = m;
		}
		
		public String getName(){
			return m.getName();
		}
		
		public Class<?> getType(){
			return m.getReturnType();
		}
		
		public Object getValue(Object annotation){
			try {
	            return m.invoke(annotation, Arrays2.EMPTY_OBJECT_ARRAY);
            } catch (Exception e) {
            	throw new ReflectException(e.getMessage(),e);
            }
		}
	}
	
	private final Class<?> type;
	private AElement[] 	   elements;
	
	private ReflectAnnotation(Class<?> annotationType){
		this.type = annotationType;
	}
	
	public AElement getElement(String name) throws ObjectNotFoundException {
		AElement e = tryGetElement(name);
		if(null == e){
			throw new ObjectNotFoundException("Annotation element '" + name + "' not found in type '" + type.getName() + "'");
		}
		return e;
	}
	
	public AElement tryGetElement(String name) {
		initElements();
		
		for(AElement e : elements){
			if(e.getName().equals(name)){
				return e;
			}
		}
		return null;
	}
	
	public AElement[] getElements(){
		initElements();
		return elements;
	}
	
	public AElement getValueElement() throws ObjectNotFoundException{
		return getElement("value");
	}
	
	public AElement tryGetValueElement() {
		return tryGetElement("value");
	}
	
	protected void initElements() {
		if(null == elements){
			List<AElement> list = new ArrayList<AElement>();

			for(Method m : type.getDeclaredMethods()){
				if(Modifier.isPublic(m.getModifiers()) && 
				   !Modifier.isStatic(m.getModifiers()) && m.getParameterTypes().length == 0){
					list.add(new AElement(m));
				}
			}
			
			elements = list.toArray(new AElement[list.size()]);
		}
	}
}
