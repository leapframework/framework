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
package leap.orm.validation;

import leap.core.AppConfig;
import leap.core.annotation.Inject;
import leap.core.annotation.M;
import leap.core.validation.Validatable;
import leap.core.validation.Validation;
import leap.core.validation.ValidationManager;
import leap.core.validation.Validator;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.value.EntityWrapper;

public class DefaultEntityValidator implements EntityValidator {

    protected @Inject @M AppConfig         appConfig;
    protected @Inject @M ValidationManager validationManager;
	
	@Override
	public boolean validate(EntityWrapper entity, Validation validation, int maxErrors) {
        return validate(entity, validation, maxErrors, null);
	}

    @Override
    public boolean validate(EntityWrapper entity, Validation validation, int maxErrors, Iterable<String> fields) {
        EntityMapping em = entity.getMapping();

        if(null != fields) {
            for(String name : fields) {
                FieldMapping fm = em.getFieldMapping(name);
                if(null == fm) {
                    throw new IllegalStateException("Field '" + name + "' not found in entity '" + em.getEntityName() + "'");
                }
                if(!validateField(entity, validation, maxErrors, fm)) {
                    return false;
                }
            }
        }else{
            //validates fields
            for(FieldMapping fm : em.getFieldMappings()){
                if(!validateField(entity, validation, maxErrors, fm)) {
                    return false;
                }
            }
        }

        if(validation.maxErrorsReached(maxErrors)){
            return false;
        }

        //validates entity
        EntityValidator[] validators = em.getValidators();
        if(validators.length > 0){
            if(!validateEntity(entity, validation, maxErrors, validators)){
                return false;
            }

            if(validation.maxErrorsReached(maxErrors)){
                return false;
            }
        }

        if(entity instanceof Validatable){
            return ((Validatable) entity).validate(validation,maxErrors);
        }

        return true;
    }

    protected boolean validateField(EntityWrapper entity, Validation validation, int maxErrors, FieldMapping fm) {
        FieldValidator[] validators = fm.getValidators();
        if(validators.length > 0){
            if(validateField(entity, validation, maxErrors, fm, validators)){
                return false;
            }

            if(validation.maxErrorsReached(maxErrors)){
                return false;
            }
        }
        return true;
    }

    protected boolean validateField(EntityWrapper entity, Validation validation, int maxErrors, FieldMapping fm, FieldValidator[] validators){
		Object value = entity.get(fm.getFieldName());
		
		//Validates : not null
		if(!fm.isNullable()){
			if(!validation.stateRequired(fm.getFieldName(), value)){
				return validation.maxErrorsReached(maxErrors);
			}
		}
		
		//Validated by validators
		for(int i=0;i<validators.length;i++){
			validators[i].validate(entity, fm, value, validation, maxErrors);
			if(validation.maxErrorsReached(maxErrors)){
				return true;
			}
		}
		
		return false;
	}
	
	protected boolean validateField(EntityWrapper entity,Validation validation,int maxErrors,FieldMapping fm,Validator validator,Object value){
		return validation.stateValidate(fm.getFieldName(),value,validator);
	}
	
	protected boolean validateEntity(EntityWrapper entity,Validation validation,int maxErrors,EntityValidator[] validators){
		boolean pass = true;
		
		for(int i=0;i<validators.length;i++){
			if(!validators[i].validate(entity, validation, maxErrors)){
				pass = false;
				if(validation.maxErrorsReached(maxErrors)){
					break;
				}
			}
		}
		return pass;
	}
}