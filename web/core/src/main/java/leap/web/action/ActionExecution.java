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
package leap.web.action;

import leap.core.validation.Validation;
import leap.lang.http.HTTP;
import leap.lang.intercepting.Execution;

/**
 * Represents the execution info of action.
 */
public interface ActionExecution extends Execution {

	/**
	 * Returns the resolved argument values of action.
	 */
	Object[] getArgs();
	
	/**
	 * Returns <code>true</code> if validation error.
	 */
	boolean isValidationError();

	/**
	 * Returns the {@link Validation} object.
	 */
	Validation getValidation();

    /**
     * Optional. Returns the http status.
     */
    HTTP.Status getStatus();
	
	/**
	 * Optional, Returns the return value of action.
	 */
	Object getReturnValue();

	/**
	 * Sets the return value of action.
	 */
	void setReturnValue(Object returnValue);
}