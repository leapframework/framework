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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import leap.core.validation.Validator;
import leap.core.validation.annotations.Required;
import leap.lang.Buildable;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.TypeInfo;
import leap.lang.annotation.Optional;
import leap.lang.reflect.ReflectParameter;
import leap.web.action.Argument.BindingFrom;
import leap.web.annotation.PathParam;
import leap.web.annotation.PathVariable;
import leap.web.annotation.QueryParam;
import leap.web.annotation.RequestBody;
import leap.web.annotation.RequestParam;

public class ArgumentBuilder implements Buildable<Argument> {
	
	protected String          name;
	protected Class<?>        type;
	protected Type            genericType;
	protected TypeInfo		  typeInfo;
	protected Boolean		  required;
	protected BindingFrom     bindingFrom;
	protected Annotation[]    annotations;
	protected List<Validator> validators = new ArrayList<>();
	
	public ArgumentBuilder() {
	    super();
    }
	
	public ArgumentBuilder(ReflectParameter p) {
		this.name        = p.getName();
		this.type        = p.getType();
		this.genericType = p.getGenericType();
		this.annotations = p.getAnnotations();
		this.configAnnotations();
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

	public BindingFrom getBindingFrom() {
		return bindingFrom;
	}

	public ArgumentBuilder setBindingFrom(BindingFrom from) {
		this.bindingFrom = from;
		return this;
	}
	
	public Annotation[] getAnnotations() {
		return annotations;
	}

	public ArgumentBuilder setAnnotations(Annotation[] annotations) {
		this.annotations = annotations;
		return this;
	}
	
	public ArgumentBuilder addValidator(Validator validator){
		validators.add(validator);
		return this;
	}
		
	public List<Validator> getValidators() {
		return validators;
	}

	public ArgumentBuilder setValidators(List<Validator> validators) {
		this.validators = validators;
		return this;
	}

	public ArgumentBuilder configAnnotations() {
		RequestParam rp = Classes.getAnnotation(annotations, RequestParam.class, true);
		if(null != rp){
			this.bindingFrom = BindingFrom.REQUEST_PARAM;
			if(!Strings.isEmpty(rp.value())){
				this.name = rp.value();
			}
			return this;
		}
		
		PathVariable pv = Classes.getAnnotation(annotations, PathVariable.class, true);
		if(null != pv){
			this.bindingFrom = BindingFrom.PATH_PARAM;
			if(!Strings.isEmpty(pv.value())){
				this.name = pv.value();
			}
			return this;
		}
		
        PathParam pp = Classes.getAnnotation(annotations, PathParam.class, true);
        if (null != pp) {
            this.bindingFrom = BindingFrom.PATH_PARAM;
            if (!Strings.isEmpty(pp.value())) {
                this.name = pp.value();
            }
            return this;
        }
        
        QueryParam qp = Classes.getAnnotation(annotations, QueryParam.class, true);
        if (null != qp) {
            this.bindingFrom = BindingFrom.QUERY_PARAM;
            if (!Strings.isEmpty(qp.value())) {
                this.name = qp.value();
            }
            return this;
        }
		
		RequestBody rb = Classes.getAnnotation(annotations, RequestBody.class, true);
		if(null != rb){
			this.bindingFrom = BindingFrom.REQUEST_BODY;
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
		Validator[] validators = null == this.validators ? null : this.validators.toArray(new Validator[]{});
		
	    return new Argument(name, type, genericType, typeInfo, required, bindingFrom, annotations,validators);
    }
}