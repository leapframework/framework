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
package leap.web.action;

import leap.lang.*;
import leap.lang.accessor.AnnotationsGetter;
import leap.lang.accessor.TypeInfoGetter;
import leap.lang.beans.BeanType;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class Argument implements Named,AnnotationsGetter,TypeInfoGetter {
	
	public enum Location {
		UNDEFINED,
		
		PATH_PARAM,
		
		REQUEST_PARAM,
		
		QUERY_PARAM,

        HEADER_PARAM,

        COOKIE_PARAM,
		
		PART_PARAM,
		
		REQUEST_BODY
	}

	protected final String              name;
	protected final Class<?>            type;
	protected final Type                genericType;
	protected final TypeInfo            typeInfo;
    protected final BeanType            beanType;
	protected final Boolean             required;
	protected final Location            location;
	protected final Annotation[]        annotations;
	protected final ArgumentValidator[] validators;

	public Argument(String name, 
					Class<?> type, 
					Type genericType,
					TypeInfo typeInfo,
					Boolean	required,
					Location location,
					Annotation[] annotations,
					ArgumentValidator[] validators) {
		
		Args.notEmpty(name,   "name");
		Args.notNull(type,    "type");
		Args.notNull(typeInfo,"type info");
		
		this.name              = name;
		this.type              = type;
		this.genericType       = genericType;
		this.typeInfo	       = typeInfo;
        this.beanType          = typeInfo.isComplexType() ? BeanType.of(type) : null;
		this.required		   = required;
		this.location 		   = null == location ? Location.UNDEFINED : location;
		this.annotations       = null == annotations ? Classes.EMPTY_ANNOTATION_ARRAY : annotations;
		this.validators        = null == validators ? (ArgumentValidator[])Arrays2.EMPTY_OBJECT_ARRAY : validators;
	}

	@Override
    public String getName() {
	    return name;
    }

	/**
	 * Returns the type of this {@link Argument}.
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * Returns the generic type of this argument.
	 *
	 * <p>
	 * Returns <code>null</code> if no generic type.
	 */
	public Type getGenericType() {
		return genericType;
	}

	/**
	 * Returns the type info of this argument.
	 */
	public TypeInfo getTypeInfo() {
		return typeInfo;
	}

    /**
     * Returns null if not a complex type.
     */
    public BeanType getBeanType() {
        return beanType;
    }

	/**
	 * Returns <code>true</code> or <code>false</code> if this argument is required or not explicitly declared.
	 * 
	 * <p>
	 * Returns <code>null</code> means that the 'required' validation not declared in this argument.
	 */
	public Boolean getRequired() {
		return required;
	}

	/**
	 * Returns the {@link Location} of this argument.
	 */
	public Location getLocation() {
		return location;
	}

    /**
     * Returns true if the {@link Location} of argument is not null and not {@link Location#UNDEFINED}.
     */
    public boolean isLocationDeclared(){
       return null != location && location != Location.UNDEFINED;
    }

    /**
     * Returns true if the location of argument is {@link Location#REQUEST_BODY}.
     */
	public boolean isRequestBodyLocation() {
        return null != location && location == Location.REQUEST_BODY;
    }

	/**
	 * Returns the {@link ElementType#PARAMETER} annotations in the {@link Method} of this argument.
	 * 
	 * <p>
	 * Returns <code>{@link Annotation}[]</code> if no these annotations.
	 */
	public Annotation[] getAnnotations() {
		return annotations;
	}
	
	/**
	 * Returns the {@link ArgumentValidator} arrays.
	 */
	public ArgumentValidator[] getValidators() {
		return validators;
	}
	
    @Override
    public String toString() {
        return "Argument[name=" + name + ",type=" + type + "]";
    }
}