/*
 * Copyright 2015 the original author or authors.
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
package leap.core.validation;

import leap.core.AppException;

public class ValidationException extends AppException {

	private static final long serialVersionUID = -8463407181817460250L;
	
	protected final Errors errors;

	public ValidationException(Errors errors) {
		super(errors.getMessage());
		this.errors = errors;
	}

	public ValidationException(Errors errors, String message) {
		super(message);
		this.errors = errors;
	}

	public ValidationException(Errors errors, Throwable cause) {
		super(cause);
		this.errors = errors;
	}

	public ValidationException(Errors errors, String message, Throwable cause) {
		super(message, cause);
		this.errors = errors;
	}
	
	public Errors getErrors() {
		return errors;
	}

}
