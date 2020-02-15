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
package leap.web.exception;

import leap.core.validation.Errors;
import leap.core.validation.Validation;

public class ValidateFailureException extends BadRequestException {

    protected final Validation validation;
    protected final Errors     errors;

    public ValidateFailureException(Validation validation) {
        super(validation.errors().getMessage());
        this.validation = validation;
        this.errors     = validation.errors();
    }

    public ValidateFailureException(Validation validation, Throwable cause) {
        super(validation.errors().getMessage(), cause);
        this.validation = validation;
        this.errors     = validation.errors();
    }

    public Validation getValidation() {
        return validation;
    }

    public Errors getErrors() {
        return errors;
    }
}
