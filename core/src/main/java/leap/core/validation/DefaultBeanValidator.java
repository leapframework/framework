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
import leap.lang.beans.BeanProperty;
import leap.lang.beans.BeanType;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

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

	protected @Inject @M ValidationManager validationManager;
	
	@Override
	public boolean validate(Object bean, Validation validation) {
		return validate(bean, validation, 0);
	}
	
	@Override
    public boolean validate(Object bean, Validation validation, int maxErrors) {
		if(null != bean){
			BeanType bt = BeanType.of(bean.getClass());

			ValidatedProperty[] validateProperties = 
					(ValidatedProperty[])bt.getAttribute(BEAN_VALIDATION_INFO_KEY);
			
			if(null == validateProperties){
				validateProperties = resolveValidateProperties(bt);
				bt.setAttribute(BEAN_VALIDATION_INFO_KEY, validateProperties);
			}
			
			return validate(bean, validation, maxErrors, bt, validateProperties);
		}	  
		
		return true;
    }

	protected boolean validate(Object bean,Validation validation, int maxErrors, BeanType bt,ValidatedProperty[] vps) {
		//Validate properties
		
		boolean pass = true;
		
		for(int i=0;i<vps.length;i++){
			ValidatedProperty vp = vps[i];
			
			if(!validateProperty(bean, validation, maxErrors, vp)){
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
	
	protected boolean validateProperty(Object bean,Validation validation,int maxErrors, ValidatedProperty vp){
		BeanProperty p = vp.property;
		Object v = p.getValue(bean);
		
		boolean pass = true;

        boolean validateBean = false;

		//Validated by validators
		for(int i=0;i<vp.validators.length;i++){
			Validator validator = vp.validators[i];

            if(validator == BEAN_VALIDATOR) {
                validateBean = true;
                continue;
            }else{
                if(!validation.stateValidate(p.getName(), v, validator)){
                    pass = false;

                    if(validation.maxErrorsReached(maxErrors)){
                        break;
                    }
                }
            }
		}

        if(validateBean && pass && null != v) {
            return validate(v, validation);
        }

		return pass;
	}
	
	protected ValidatedProperty[] resolveValidateProperties(BeanType bt) {
		List<ValidatedProperty> vps = new ArrayList<>();
		
		for(BeanProperty bp : bt.getProperties()){
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

            if(bp.isComplexType()) {
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