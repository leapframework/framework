/*
 * Copyright 2014 the original author or authors.
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

import leap.core.validation.Valid;
import leap.core.validation.ValidationManager;
import leap.core.validation.Validator;
import leap.core.validation.annotations.Required;
import leap.lang.Buildable;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.TypeInfo;
import leap.lang.annotation.Optional;
import leap.lang.beans.BeanProperty;
import leap.lang.reflect.ReflectParameter;
import leap.web.action.Argument.Location;
import leap.web.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ArgumentBuilder implements Buildable<Argument> {

	protected String       name;
	protected Class<?>     type;
	protected Type         genericType;
	protected TypeInfo     typeInfo;
	protected Boolean      required;
	protected Location     location;
	protected Annotation[] annotations;
	protected List<ArgumentValidator> validators = new ArrayList<>();
	
	public ArgumentBuilder() {
	    super();
    }

	public ArgumentBuilder(ValidationManager validationManager, BeanProperty p) {
		this.name 		 = p.getName();
		this.type 		 = p.getType();
		this.typeInfo    = p.getTypeInfo();
		this.genericType = p.getGenericType();
		this.annotations = p.getAnnotations();
		this.configAnnotations();
        this.resolverValidators(validationManager);
	}
	
	public ArgumentBuilder(ValidationManager validationManager, ReflectParameter p) {
		this.name        = p.getName();
		this.type        = p.getType();
		this.typeInfo    = p.getTypeInfo();
		this.genericType = p.getGenericType();
		this.annotations = p.getAnnotations();
		this.configAnnotations();
        this.resolverValidators(validationManager);
	}

    protected void resolverValidators(ValidationManager validationManager) {
        Validator v = null;
        for(Annotation pa : annotations){
            if((v = validationManager.tryCreateValidator(pa, type)) != null){
                addValidator(new SimpleArgumentValidator(v));
            }
        }

        if(Classes.isAnnotatioinPresent(annotations,Valid.class) ){
            addValidator(new NestedArgumentValidator(Classes.getAnnotation(annotations, Valid.class)));
        }else if(type.isAnnotationPresent(Valid.class)) {
            addValidator(new NestedArgumentValidator(type.getAnnotation(Valid.class)));
        }
    }

	public String getName() {
		return name;
	}

	public ArgumentBuilder setName(String name) {
		this.name = name;
		return this;
	}

	public Class<?> getType() {
		return type;
	}

	public ArgumentBuilder setType(Class<?> type) {
		this.type = type;
		return this;
	}

	public Type getGenericType() {
		return genericType;
	}

	public ArgumentBuilder setGenericType(Type genericType) {
		this.genericType = genericType;
		return this;
	}

	public TypeInfo getTypeInfo() {
		return typeInfo;
	}

	public ArgumentBuilder setTypeInfo(TypeInfo typeInfo) {
		this.typeInfo = typeInfo;
		return this;
	}
	
	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Location getLocation() {
		return location;
	}

	public ArgumentBuilder setLocation(Location from) {
		this.location = from;
		return this;
	}
	
	public Annotation[] getAnnotations() {
		return annotations;
	}

	public ArgumentBuilder setAnnotations(Annotation[] annotations) {
		this.annotations = annotations;
		return this;
	}
	
	public ArgumentBuilder addValidator(ArgumentValidator validator){
		validators.add(validator);
		return this;
	}
		
	public List<ArgumentValidator> getValidators() {
		return validators;
	}

	public ArgumentBuilder configAnnotations() {
		RequestParam rp = Classes.getAnnotation(annotations, RequestParam.class, true);
		if(null != rp){
			this.location = Location.REQUEST_PARAM;
			if(!Strings.isEmpty(rp.value())){
				this.name = rp.value();
			}
			return this;
		}
		
		PathVariable pv = Classes.getAnnotation(annotations, PathVariable.class, true);
		if(null != pv){
			this.location = Location.PATH_PARAM;
			if(!Strings.isEmpty(pv.value())){
				this.name = pv.value();
			}
			return this;
		}
		
        PathParam pp = Classes.getAnnotation(annotations, PathParam.class, true);
        if (null != pp) {
            this.location = Location.PATH_PARAM;
            if (!Strings.isEmpty(pp.value())) {
                this.name = pp.value();
            }
            return this;
        }
        
        QueryParam qp = Classes.getAnnotation(annotations, QueryParam.class, true);
        if (null != qp) {
            this.location = Location.QUERY_PARAM;
            if (!Strings.isEmpty(qp.value())) {
                this.name = qp.value();
            }
            return this;
        }
		
		RequestBody rb = Classes.getAnnotation(annotations, RequestBody.class, true);
		if(null != rb){
			this.location = Location.REQUEST_BODY;
			return this;
		}
		
		Optional o = Classes.getAnnotation(annotations, Optional.class, false);
		if(null != o) {
			required = false;
		}
		
		Required r = Classes.getAnnotation(annotations, Required.class, false);
		if(null != r) {
			required = true;
		}
		
		return this;
	}
	
	@Override
    public Argument build() {
		ArgumentValidator[] validators = null == this.validators ? null : this.validators.toArray(new ArgumentValidator[]{});
		
	    return new Argument(name, type, genericType, typeInfo, required, location, annotations,validators);
    }
}