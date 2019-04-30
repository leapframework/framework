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

package leap.web.api.mvc;

import leap.core.annotation.Inject;
import leap.core.exception.EmptyRecordsException;
import leap.core.exception.RecordNotFoundException;
import leap.core.validation.Errors;
import leap.core.validation.ValidationException;
import leap.lang.NamedError;
import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.http.HTTP;
import leap.web.Response;
import leap.web.Result;
import leap.web.action.ActionContext;
import leap.web.action.ActionExecution;
import leap.web.exception.ResponseException;

import java.util.NoSuchElementException;

public class DefaultApiFailureHandler implements ApiFailureHandler {

    protected @Inject ApiErrorHandler errorHandler;

    protected @Inject ApiExceptionHandler[] exceptionHandlers;

    public DefaultApiFailureHandler() {

    }

    public DefaultApiFailureHandler(ApiErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public boolean handleFailure(ActionContext context, ActionExecution execution, Result result) {
        Response response = context.getResponse();
        ApiErrorHandler errorHandler = getErrorHandler(context);

        if(execution.isValidationError()) {
            handleValidationError(errorHandler, response, execution.getValidation().errors());
            return true;
        }

        if(execution.hasException()) {
            Throwable e = execution.getException();

            for(ApiExceptionHandler handler : exceptionHandlers) {
                if(handler.handleApiException(errorHandler, context, execution, response, e)) {
                    return true;
                }
            }

            if(handleException(errorHandler, response, e)) {
                return true;
            }

            //other exceptions.
            errorHandler.internalServerError(response, e.getMessage());
            return true;
        }

        errorHandler.internalServerError(response, "Execution failed.");
        return true;
    }

    protected boolean handleException(ApiErrorHandler errorHandler, Response response, Throwable e) {

        if(e instanceof ValidationException) {
            Errors errors = ((ValidationException) e).getErrors();
            handleValidationError(errorHandler, response, errors);
            return true;
        }

        if(e instanceof ApiResponseException) {
            ApiResponseException ex = (ApiResponseException)e;
            errorHandler.responseError(response, ex.getStatus(), ex.getCode(), ex.getMessage());
            return true;
        }

        if(e instanceof ResponseException) {
            int code = ((ResponseException) e).getStatus();
            HTTP.Status status = HTTP.Status.valueOf(code);

            errorHandler.responseError(response, code, status.name() , e.getMessage());
            return true;
        }

        if(e instanceof ObjectExistsException) {
            errorHandler.badRequest(response, e.getMessage());
            return true;
        }

        if(e instanceof ObjectNotFoundException) {
            errorHandler.notFound(response, e.getMessage());
            return true;
        }

        if(e instanceof RecordNotFoundException) {
            errorHandler.notFound(response, e.getMessage());
            return true;
        }

        if(e instanceof EmptyRecordsException) {
            errorHandler.notFound(response, e.getMessage());
            return true;
        }

        //jdk exceptions
        if(e instanceof NoSuchElementException) {
            errorHandler.notFound(response, e.getMessage());
            return true;
        }

        if(e instanceof IllegalArgumentException) {
            errorHandler.badRequest(response, e.getMessage());
            return true;
        }

        if(e instanceof  UnsupportedOperationException) {
            errorHandler.responseError(response, HTTP.SC_NOT_IMPLEMENTED, e.getMessage());
            return true;
        }

        return false;
    }

    protected void handleValidationError(ApiErrorHandler errorHandler, Response response, Errors errors) {
        NamedError error = errors.first();
        errorHandler.badRequest(response, "Validation failed -> " + error.getName() + " : " + error.getMessage());
    }

    protected ApiErrorHandler getErrorHandler(ActionContext context) {
        ApiErrorHandler errorHandler = (ApiErrorHandler)context.getRequest().getAttribute(ApiErrorHandler.class.getName());
        if(null == errorHandler) {
            errorHandler = this.errorHandler;
        }
        return errorHandler;
    }
}
