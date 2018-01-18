/*
 * Copyright 2016 the original author or authors.
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
package leap.web.action;

import leap.core.validation.Validation;
import leap.core.validation.Validator;
import leap.lang.Out;

public class SimpleArgumentValidator implements ArgumentValidator {

    protected final Validator validator;

    public SimpleArgumentValidator(Validator validator) {
        this.validator = validator;
    }

    public Validator getValidator() {
        return validator;
    }

    @Override
    public boolean validate(Validation validation, Argument arg, Object value, Out<Object> out) {
        if(validation.validate(arg.getName(), value, validator).hasErrors()){
            return false;
        }
        return true;
    }
}
