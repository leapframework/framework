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
import leap.core.validation.annotations.NotNull;

public class NotNullValidator extends AbstractConstraintValidator<NotNull, Object> {
	
	public static final String			 ERROR_CODE = "notNull";
	public static final NotNullValidator INSTANCE   = new NotNullValidator();
	
	public NotNullValidator() {
	    super();
    }
	
	public NotNullValidator(NotNull constraint, Class<?> valueType) {
	    super(constraint, valueType);
    }

	@Override
    public boolean doValidate(Object value) {
	    return null != value;
    }

	@Override
    public String getErrorCode() {
	    return ERROR_CODE;
    }
}