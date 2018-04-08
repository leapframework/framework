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
import leap.lang.intercepting.AbstractExecution;

public class DefaultActionExecution extends AbstractExecution implements ActionExecution {

	private Object[]    args;
    private HTTP.Status status;
	private Object	    returnValue;
	private Validation  validation;
	
	public DefaultActionExecution(Validation validation) {
		this.validation = validation;
	}

	@Override
    public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

    @Override
    public HTTP.Status getStatus() {
        return status;
    }

    public void setStatus(HTTP.Status status) {
        this.status = status;
    }

    @Override
    public Object getReturnValue() {
		return returnValue;
	}

	@Override
    public void setReturnValue(Object returnValue) {
		this.returnValue = returnValue;
	}

	@Override
    public Validation getValidation() {
	    return validation;
    }
	
	@Override
    public boolean isValidationError() {
	    return validation.hasErrors();
    }
}
