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

import leap.lang.Named;
import leap.lang.TypeInfo;
import leap.lang.Types;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public class ReflectParameter implements Named,ReflectValued {

	private final int 		index;
	private final String	name;
	private final Parameter p;
	private final Type	    genericType;
	private final TypeInfo  typeInfo;
	
	public ReflectParameter(int index, String name, Parameter p, Type genericType) {
		this.index	     = index;
		this.name		 = name;
		this.p    		 = p;
		this.genericType = genericType;
        this.typeInfo    = Types.getTypeInfo(p.getType(), genericType);
    }
	
	public Parameter getReflectedParameter() {
		return p;
	}

	public final int getIndex() {
		return index;
	}

	public final String getName() {
		return name;
	}

	public final Class<?> getType() {
		return p.getType();
	}

    public final TypeInfo getTypeInfo() {
        return typeInfo;
    }
	
	public final Type getGenericType() {
    	return genericType;
    }

	public final Annotation[] getAnnotations() {
		return p.getAnnotations();
	}
	
	public final <T extends Annotation> T getAnnotation(Class<T> annotationType){
		return p.getAnnotation(annotationType);
	}
	
	public final boolean isAnnotationPresent(Class<? extends Annotation> annotationType){
		return getAnnotation(annotationType) != null;
	}

    @Override
    public Object getValue(Object bean) {
        return ((Object[])bean)[index-1];
    }

    @Override
    public void setValue(Object bean, Object value) {
        ((Object[])bean)[index-1] = value;
    }
}