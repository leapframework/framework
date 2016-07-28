/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package leap.web.api.controller;

import leap.lang.NamedError;
import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;
import leap.web.Result;
import leap.web.action.ActionContext;
import leap.web.action.ActionExecution;
import leap.web.exception.ResponseException;

public class DefaultApiFailureHandler extends AbstractApiFailureHandler {

    @Override
    public boolean handleFailure(ActionContext context, ActionExecution execution, Result result) {
        if(execution.isValidationError()) {
            NamedError error = execution.getValidation().errors().first();
            badRequest(result, "Validation failed -> " + error.getName() + " : " + error.getMessage());
            return true;
        }

        if(execution.hasException()) {
            Throwable e = execution.getException();

            if(e instanceof ResponseException) {
                errResponse(result, ((ResponseException) e).getStatus(), e.getMessage());
                return true;
            }

            if(e instanceof ObjectExistsException) {
                badRequest(result, e.getMessage());
                return true;
            }

            if(e instanceof ObjectNotFoundException) {
                notFound(result, e.getMessage());
                return true;
            }

            internalServerError(result, e.getMessage());
            return true;
        }

        internalServerError(result, "Execution failed.");
        return true;
    }

}
