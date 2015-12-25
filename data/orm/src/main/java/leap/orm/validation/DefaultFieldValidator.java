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

import leap.core.validation.Validation;
import leap.core.validation.ValidationManager;
import leap.core.validation.Validator;
import leap.orm.mapping.FieldMapping;
import leap.orm.value.EntityWrapper;

/**
 * An implementation of {@link FieldValidator}, wrapps a {@link Validator} for validating.
 */
public class DefaultFieldValidator implements FieldValidator {
	
    private final Validator validator;
    
	public DefaultFieldValidator(ValidationManager validation, Validator validator){
		this.validator = validator;
	}
	
    @Override
    public boolean validate(EntityWrapper entity, FieldMapping fm, Object value, Validation validation, int maxErrors) {
    	return validation.stateValidate(fm.getFieldName(), value, validator);
    }
    
}
