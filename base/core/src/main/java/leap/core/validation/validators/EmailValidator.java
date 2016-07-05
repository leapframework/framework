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

import java.net.IDN;

import leap.core.validation.AbstractConstraintValidator;
import leap.core.validation.annotations.Email;
import leap.lang.Patterns;

public class EmailValidator extends AbstractConstraintValidator<Email, CharSequence> {

	public EmailValidator() {
	    super();
    }
	
	public EmailValidator(Email constraint, Class<?> valueType) {
	    super(constraint, valueType);
    }

	@Override
    public String getErrorCode() {
	    return "invalidEmail";
    }
	
	@Override
    protected boolean doValidate(CharSequence value) {
		if ( value == null || value.length() == 0 ) {
			return true;
		}
		
		//from hibernate validator : EmailValidator
		String asciiString = IDN.toASCII( value.toString() );
		
		return Patterns.EMAIL_PATTERN.matcher( asciiString ).matches();
    }
}