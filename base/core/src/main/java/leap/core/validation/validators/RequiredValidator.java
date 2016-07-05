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
package leap.core.validation.validators;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import leap.core.validation.AbstractConstraintValidator;
import leap.core.validation.annotations.NotEmpty;
import leap.core.validation.annotations.Required;

public class RequiredValidator extends AbstractConstraintValidator<Required, Object> {
	
	public static final String			  ERROR_CODE = "required";
	public static final RequiredValidator INSTANCE   = new RequiredValidator();
	
	public RequiredValidator() {
	    super();
    }
	
	public RequiredValidator(NotEmpty constraint, Class<?> valueType) {
	    super(constraint);
    }

	public RequiredValidator(Required constraint, Class<?> valueType) {
	    super(constraint, valueType);
    }

	@Override
    public String getErrorCode() {
	    return ERROR_CODE;
    }
	
    @Override
    @SuppressWarnings("rawtypes")
    protected boolean doValidate(Object value) {
		if(null == value){
			return false;
		}
		
        if(value instanceof CharSequence) {
        	return ((CharSequence)value).length() > 0;
        }

		if(value instanceof Collection){
			return !((Collection) value).isEmpty();
		}
		
		if(value instanceof Object[]) {
			return ((Object[])value).length > 0;
		}
		
		if(value.getClass().isArray()){
			return Array.getLength(value) > 0;
		}
		
		if(value instanceof Map){
			return !((Map)value).isEmpty();
		}
		
	    return true;
    }
}