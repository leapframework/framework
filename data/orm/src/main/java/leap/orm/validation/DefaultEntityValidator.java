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
import leap.core.validation.*;
import leap.lang.Arrays2;
import leap.orm.mapping.EntityMapping;
import leap.orm.mapping.FieldMapping;
import leap.orm.mapping.JoinFieldMapping;
import leap.orm.mapping.RelationMapping;
import leap.orm.value.EntityWrapper;
import java.util.Objects;

public class DefaultEntityValidator implements EntityValidator {

    public static final String ERROR_RELATION = "error_relation";

    protected @Inject @M AppConfig         appConfig;
    protected @Inject @M ValidationManager validationManager;

    @Override
    public Errors validate(EntityWrapper entity) {
        return validate(entity, null);
    }

    @Override
    public Errors validate(EntityWrapper entity, Iterable<String> fields) {
        Validation validation = validationManager.createValidation();
        validate(entity, validation, 1, fields);
        return validation.errors();
    }

    @Override
	public boolean validate(EntityWrapper entity, Validation validation, int maxErrors) {
        return validate(entity, validation, maxErrors, null);
	}

    @Override
    public boolean validate(EntityWrapper entity, Validation validation, int maxErrors, Iterable<String> fields) {
        EntityMapping em = entity.getEntityMapping();

        if(null != fields) {
            for(String name : fields) {
                FieldMapping fm = em.tryGetFieldMapping(name);
                if(null == fm) {
                    continue;
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

        RelationMapping[] relations = em.getRelationMappings();
        if (!Arrays2.isEmpty(relations)) {
            for (RelationMapping relation : relations) {
                if (!validateRelation(entity, validation, em, relation)) {
                    return false;
                }
            }
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

    protected boolean validateRelation(EntityWrapper entity, Validation validation, EntityMapping em, RelationMapping relation) {
        if (relation.isManyToOne() && em.getEntityName().equalsIgnoreCase(relation.getTargetEntityName())) {
            if (relation.isAllowSelfReference()) {
                return true;
            }

            JoinFieldMapping[] joinFields =  relation.getJoinFields();
            boolean validate = true;
            for (JoinFieldMapping joinField : joinFields) {
                Object localValue     = entity.get(joinField.getLocalFieldName());
                Object referenceValue = entity.get(joinField.getReferencedFieldName());
                if (null == referenceValue && Arrays2.contains(em.getKeyFieldNames(), joinField.getReferencedFieldName())) {
                   referenceValue = entity.tryGetIdByName(joinField.getReferencedFieldName());
                }

                if (null == localValue && null == referenceValue) {
                    continue;
                } else validate = !Objects.equals(localValue, referenceValue);

                if (validate) {
                    break;
                }
            }

            if (!validate) {
                validation.addError(ERROR_RELATION, "many-to-one relation '" + relation.getName() + "' cannot point to itself!");
                return false;
            }
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