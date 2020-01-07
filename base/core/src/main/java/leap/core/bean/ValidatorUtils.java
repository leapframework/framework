/*
 *  Copyright 2020 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package leap.core.bean;

import leap.lang.Strings;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.Set;

/**
 * Utils for standard {@link javax.validation.Validator}.
 */
public class ValidatorUtils {

    public static String validate(Validator validator, Object v) {
        return validate(validator, null, v);
    }

    public static String validate(Validator validator, String name, Object v) {
        if (null == v || !v.getClass().isAnnotationPresent(Valid.class)) {
            return null;
        }

        Set<ConstraintViolation<Object>> errs = validator.validate(v);
        if (!errs.isEmpty()) {
            final javax.validation.ConstraintViolation<Object> err = errs.iterator().next();
            final String message = (!Strings.isEmpty(name) ? name + ", property '" : "Property '") +
                    (err.getPropertyPath() + "' : " + err.getMessage());
            return message;
        } else {
            return null;
        }
    }

    public static void validateWithException(Validator validator, Object v) throws ValidationException {
        validateWithException(validator, null, v);
    }

    public static void validateWithException(Validator validator, String name, Object v) throws ValidationException {
        final String error = validate(validator, name, v);
        if(null != error) {
            throw new ValidationException(error);
        }
    }

}
