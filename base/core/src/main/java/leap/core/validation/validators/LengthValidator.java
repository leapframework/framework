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

import leap.core.validation.AbstractConstraintValidator;
import leap.core.validation.annotations.Length;

public class LengthValidator extends AbstractConstraintValidator<Length, CharSequence> {
	
	public static final String ERROR_CODE  = "invalidLength";
	public static final String MESSAGE_KEY = MESSAGE_KEY_PREFIX + ERROR_CODE;
	
	
	private int min;
	private int max;
	
	public LengthValidator(int min,int max) {
	    this.min = min;
	    this.max = max;
	    this.messageKey1 = MESSAGE_KEY;
    }
	
	public LengthValidator(Length constraint, Class<?> valueType) {
	    super(constraint, valueType);
	    this.min	    = constraint.min();
	    this.max		= constraint.max();
	    this.messageKey1 = MESSAGE_KEY;
    }

	@Override
    public String getErrorCode() {
	    return ERROR_CODE;
    }
	
	@Override
    protected Object[] createMessageArguments1() {
		return new Object[]{min,max};
	}

	@Override
    protected boolean doValidate(CharSequence value) {
		return validate(value,min,max);
    }
	
	public static boolean validate(CharSequence value,int min,int max){
		if(null == value){
			return true;
		}
		int l = ((CharSequence) value).length();
		return l >= min && l <= max;
	}
	
	public static Object[] getMessageArguments(CharSequence value,int min,int max) {
		return new Object[]{value,min,max};
	}
}