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
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;
import leap.lang.reflect.ReflectParameter;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.sql.Ref;
import java.util.Collections;
import java.util.Map;

public class Argument extends ExtensibleBase implements Named,AnnotationsGetter,TypeInfoGetter,Extensible {

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

    protected final String                name;
    protected final String                declaredName;
    protected final ReflectParameter      parameter;
    protected final Class<?>              type;
    protected final Type                  genericType;
    protected final TypeInfo              typeInfo;
    protected final BeanType              beanType;
    protected final BeanProperty          beanProperty;
    protected final Boolean               required;
    protected final Location              location;
    protected final Annotation[]          annotations;
    protected final ArgumentBinder        binder;
    protected final ArgumentProcessor     processor;
    protected final ArgumentValidator[]   validators;
    protected final Argument[]            wrappedArguments;

    protected boolean contextual;

	public Argument(String name,
                    String declaredName,
                    ReflectParameter parameter,
                    BeanProperty beanProperty,
                    Class<?> type,
                    Type genericType,
                    TypeInfo typeInfo,
                    Boolean	required,
                    Location location,
                    Annotation[] annotations,
                    ArgumentBinder binder,
                    ArgumentProcessor processor,
                    ArgumentValidator[] validators,
                    Argument[] wrappedArguments,
                    Map<Class<?>, Object> extensions) {
		
		Args.notEmpty(name,   "name");
		Args.notNull(type,    "type");
		Args.notNull(typeInfo,"type info");
		
		this.name              = name;
		this.declaredName	   = declaredName;
        this.parameter         = parameter;
        this.beanProperty      = beanProperty;
		this.type              = type;
		this.genericType       = genericType;
		this.typeInfo	       = typeInfo;
        this.beanType          = typeInfo.isComplexType() ? BeanType.of(type) : null;
		this.required		   = required;
		this.location 		   = null == location ? Location.UNDEFINED : location;
		this.annotations       = null == annotations ? Classes.EMPTY_ANNOTATION_ARRAY : annotations;
        this.binder            = binder;
        this.processor         = processor;
		this.validators        = null == validators ? (ArgumentValidator[])Arrays2.EMPTY_OBJECT_ARRAY : validators;
        this.wrappedArguments  = wrappedArguments;

        putExtensions(extensions);
    }

	@Override
    public String getName() {
	    return name;
    }

    public String getDeclaredName() {
        return declaredName;
    }

    /**
     * Optional.
     */
    public ReflectParameter getParameter() {
        return parameter;
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
     * Optional. Returns the {@link BeanProperty} of wrapper class.
     *
     * <p/>
     * Valid only if this argument is a wrapped argument.
     */
    public BeanProperty getBeanProperty() {
        return beanProperty;
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
     * Returns <code>true</code> if the argument is path parameter.
     */
    public boolean isPathParam() {
        return Location.PATH_PARAM == location;
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
	public boolean isRequestBody() {
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
     * Optional. Returns the {@link ArgumentBinder} of this argument.
     */
    public ArgumentBinder getBinder() {
        return binder;
    }

    /**
     * Optional. Returns the {@link ArgumentProcessor}.
     */
    public ArgumentProcessor getProcessor() {
        return processor;
    }

    /**
	 * Returns the {@link ArgumentValidator} arrays.
	 */
	public ArgumentValidator[] getValidators() {
		return validators;
	}

    /**
     * Returns true if this argument is a wrapper of wrapped arguments.
     */
    public boolean isWrapper() {
        return wrappedArguments.length > 0;
    }

    /**
     * Returns true if this argument is a contextual argument.
     */
    public boolean isContextual() {
        return contextual;
    }

    /**
     * Sets contextual.
     */
    protected void setContextual(boolean contextual) {
        this.contextual = contextual;
    }

    /**
     * Returns the wrapped arguments.
     */
    public Argument[] getWrappedArguments() {
        return wrappedArguments;
    }

    @Override
    public String toString() {
        return "Argument[name=" + name + ",type=" + type + "]";
    }
}