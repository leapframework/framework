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
package app.controllers;

import leap.core.validation.annotations.Required;
import leap.lang.http.HTTP;
import leap.web.Contents;
import leap.web.RequestIntercepted;
import leap.web.Response;
import leap.web.Result;
import leap.web.action.ActionContext;
import leap.web.action.ActionExecution;
import leap.web.action.FailureHandler;
import leap.web.annotation.Failure;
import leap.web.exception.ResponseException;

public class FailureController {

	public void validationError(@Required String value) {
		
	}
	
	@Failure(handler=FailureHandler1.class)
	public void validationError1(@Required String value) {
		
	}

    @Failure(handler=FailureHandler2.class)
	public void intercepted(Response response) {
        response.setStatus(HTTP.SC_OK);
        response.getWriter().write("OK");
        RequestIntercepted.throwIt();
    }

    @Failure(handler=FailureHandler2.class)
    public void responseException() {
        throw new ResponseException(HTTP.SC_OK, Contents.text("OK"));
    }
	
	public static final class FailureHandler1 implements FailureHandler {

		@Override
        public boolean handleFailure(ActionContext context, ActionExecution execution, Result result) {
			if(execution.isValidationError()) {
				result.text(500, "Validation Error");
				return true;
			}
			return false;
        }
	}

    public static final class FailureHandler2 implements FailureHandler {
        @Override
        public boolean handleFailure(ActionContext context, ActionExecution execution, Result result) {
            result.setStatus(HTTP.SC_BAD_REQUEST);
            result.setRenderable(Contents.text("IllegalState"));
            return true;
        }
    }
}
