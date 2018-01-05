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
package leap.core.validation;

import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.validation.validators.RequiredValidator;
import leap.lang.Strings;
import leap.lang.TypeInfo;
import leap.lang.Types;
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DefaultBeanValidator implements BeanValidator {
	
	private static final String BEAN_VALIDATION_INFO_KEY = DefaultBeanValidator.class.getName() + "_VALIDATE";

    protected static final Validator BEAN_VALIDATOR = new AbstractValidator<Object>() {
        @Override
        protected boolean doValidate(Object value) {
            return false;
        }

        @Override
        public String getErrorCode() {
            return null;
        }
    };

    protected static final Validator MAP_VALIDATOR = new AbstractValidator<Object>() {
        @Override
        protected boolean doValidate(Object value) {
            return false;
        }

        @Override
        public String getErrorCode() {
            return null;
        }
    };

	protected @Inject @M ValidationManager validationManager;

    @Override
    public void validate(String name, Object bean) throws ValidationException {
        Validation validation = validationManager.createValidation();

        if(!validate(name, bean, validation, 1)) {
            throw new ValidationException(validation.errors());
        }
    }

    @Override
	public boolean validate(String name, Object bean, Validation validation) {
		return validate(name, bean, validation, 0);
	}
	
    @Override
    public boolean validate(String name, Object bean, Validation validation, int maxErrors) {
        if(null != bean){
            if(bean instanceof Map) {
                return validateMap(name, (Map)bean, validation, maxErrors);
            }

            if(bean instanceof Iterable) {
                return validateCollection(name, (Iterable)bean, validation, maxErrors);
            }

            BeanType bt = BeanType.of(bean.getClass());

            ValidatedProperty[] validateProperties =
                    (ValidatedProperty[])bt.getAttribute(BEAN_VALIDATION_INFO_KEY);

            if(null == validateProperties){
                validateProperties = resolveValidateProperties(bt);
                bt.setAttribute(BEAN_VALIDATION_INFO_KEY, validateProperties);
            }

            return validate(name, bean, validation, maxErrors, bt, validateProperties);
        }

        return true;
    }

    protected boolean validateMap(String name, Map map, Validation validation, int maxErrors) {
        TypeInfo typeInfo = null;

        for(Object item : map.entrySet()) {
            Map.Entry entry = (Map.Entry)item;

            Object value = entry.getValue();
            if(null == value) {
                continue;
            }

            if(null == typeInfo) {
                typeInfo = Types.getTypeInfo(value.getClass());
            }

            if(typeInfo.isComplexType() || typeInfo.isCollectionType()) {
                String fullName = Strings.nullToEmpty(name) + "['" + entry.getKey().toString() + "']";
                if(!validate(fullName, value, validation, maxErrors) ){
                    return false;
                }
            } else {
                return true;
            }
        }

        return true;
    }

    protected boolean validateCollection(String name, Iterable iterable, Validation validation, int maxErrors) {
        int i=0;

        TypeInfo typeInfo = null;
        for(Object value : iterable) {

            if(value == null) {
                continue;
            }

            if(typeInfo == null) {
                typeInfo = Types.getTypeInfo(value.getClass());
            }

            if(typeInfo.isComplexType() || typeInfo.isCollectionType()) {
                String fullName = Strings.nullToEmpty(name) + "[" + String.valueOf(i) + "]";
                if(!validate(fullName, value, validation, maxErrors) ){
                    return false;
                }
            } else {
                return true;
            }

            i++;
        }

        return true;
    }

	protected boolean validate(String name, Object bean, Validation validation, int maxErrors, BeanType bt, ValidatedProperty[] vps) {
		//Validate properties
		
		boolean pass = true;
		
		for(int i=0;i<vps.length;i++){
			ValidatedProperty vp = vps[i];
			
			if(!validateProperty(name, bean, validation, maxErrors, vp)){
				pass = false;
				
				if(validation.maxErrorsReached(maxErrors)){
					return pass;
				}
			}
		}
		
		if(bean instanceof Validatable){
			return ((Validatable) bean).validate(validation,maxErrors);
		}
		
		return pass;
	}
	
	protected boolean validateProperty(String name, Object bean, Validation validation, int maxErrors, ValidatedProperty vp){
		BeanProperty p = vp.property;
		Object v = p.getValue(bean);
		
		boolean pass = true;
        boolean validateBean = false;

        String fullPropertyName = (Strings.isEmpty(name) ? "" : name + ".") + p.getName();

		//Validated by validators
		for(int i=0;i<vp.validators.length;i++){
			Validator validator = vp.validators[i];

            if(validator == BEAN_VALIDATOR) {
                validateBean = true;
                continue;
            }else{

                if(!validation.stateValidate(fullPropertyName, v, validator)){
                    pass = false;

                    if(validation.maxErrorsReached(maxErrors)){
                        break;
                    }
                }
            }
		}

        if(validateBean && pass && null != v) {
            return validate(fullPropertyName, v, validation, maxErrors);
        }

		return pass;
	}
	
	protected ValidatedProperty[] resolveValidateProperties(BeanType bt) {
		List<ValidatedProperty> vps = new ArrayList<>();
		
		for(BeanProperty bp : bt.getProperties()){
            if(!bp.isReadable()) {
                continue;
            }

			Valid valid = bp.getAnnotation(Valid.class);
            if(null != valid && !valid.value()) {
                continue;
            }

			Annotation[] annotations = bp.getAnnotations();
			
			List<Validator> validators = new ArrayList<>();
			
			for(Annotation a : annotations){
				Validator validator = validationManager.tryCreateValidator(a, bp.getType());
				if(null != validator){
					validators.add(validator);
				}
			}

            if(bp.isComplexType() || bp.isCollectionType()) {
                if(null != valid) {
                    if(valid.required()) {
                        validators.add(RequiredValidator.INSTANCE);
                    }
                    validators.add(BEAN_VALIDATOR);
                }else if(!validators.isEmpty()) {
                    validators.add(BEAN_VALIDATOR);
                }
            }

			if(!validators.isEmpty()){
				vps.add(new ValidatedProperty(bp,validators.toArray(new Validator[validators.size()])));
			}
		}
		
		return vps.toArray(new ValidatedProperty[vps.size()]);
	}
	
	protected static final class ValidatedProperty {
		public final BeanProperty property;
		public final Validator[]  validators;
		
		public ValidatedProperty(BeanProperty bp, Validator[] validators){
			this.property   = bp;
			this.validators = validators;
		}

		@Override
        public String toString() {
			return property.toString();
		}
	}
}