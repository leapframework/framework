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
package leap.core.validation.validators;

import java.math.BigDecimal;
import java.math.BigInteger;

import leap.core.validation.AbstractConstraintValidator;
import leap.core.validation.annotations.Max;

public class MaxValidator extends AbstractConstraintValidator<Max,Number>{
	
	public static final String ERROR_CODE  = "graterThanMaxValue";
	public static final String MESSAGE_KEY = MESSAGE_KEY_PREFIX + ERROR_CODE;
	
	protected long max;

	public MaxValidator(long max) {
	    this.max = max;
	    this.messageKey1 = MESSAGE_KEY;
    }
	
	public MaxValidator(Max constraint, Class<?> valueType) {
	    super(constraint, valueType);
	    this.max = constraint.value();
	    this.messageKey1 = MESSAGE_KEY;
    }

	@Override
    public String getErrorCode() {
	    return ERROR_CODE;
    }

	@Override
    protected Object[] createMessageArguments1() {
		return new Object[]{max};
	}

	@Override
    protected boolean doValidate(Number value) {
		return validate(value,max);
    }
	
	public static boolean validate(Number value,long max) {
		if ( value == null ) {
			return true;
		}
		if ( value instanceof BigDecimal ) {
			return ( ( BigDecimal ) value ).compareTo( BigDecimal.valueOf( max ) ) != 1;
		}
		else if ( value instanceof BigInteger ) {
			return ( ( BigInteger ) value ).compareTo( BigInteger.valueOf( max ) ) != 1;
		}
		else {
			long longValue = value.longValue();
			return longValue <= max;
		}
	}
	
	public static Object[] getMessagesArguments(Number value,long max){
		return new Object[]{value,max};
	}
}
