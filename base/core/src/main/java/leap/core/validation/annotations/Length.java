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
package leap.core.validation.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import leap.core.validation.ConstraintAnnotation;
import leap.core.validation.ValidatedBy;
import leap.core.validation.validators.LengthValidator;

/**
 * Validate the length of a {@link CharSequence} value.
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@ConstraintAnnotation
@ValidatedBy(validator=LengthValidator.class)
public @interface Length {

	/**
	 * The minimum length of a String. Defaults to 0.
	 * <p>
	 * Checks that the string length is greater than or equal to min.
	 * </p>
	 */
	int min() default 0;

	/**
	 * The maximum length of a String. Defaults to Integer.MAX_VALUE.
	 * <p>
	 * Checks that the string length is less than or equal to max.
	 * </p>
	 */
	int max() default Integer.MAX_VALUE;

}
