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
package leap.web.action;

import leap.core.validation.Validation;
import leap.web.Params;
import leap.web.Request;
import leap.web.Response;
import leap.web.Result;
import leap.web.Results;


public abstract class ControllerBase extends Results {
	
	/**
	 * Returns current http request.
	 * 
	 * @throws IllegalStateException if http request not exists in current execution context.
	 */
	protected static Request request() throws IllegalStateException{
		return Request.current();
	}
	
	/**
	 * Returns current http response.
	 * 
	 * @throws IllegalStateException if http response not exists in current execution context.
	 */
	protected static Response response(){
		return Request.current().response();
	}

	/**
	 * Returns current {@link Result} object.
	 * 
	 * @see Result#content()
	 */
	protected static Result result(){
		return Result.current();
	}
	
	/**
	 * Returns a {@link Validation} for validating.
	 */
	protected static Validation validation(){
		return Request.current().getValidation();
	}
	
	/**
	 * Returns the {@link Params} object in current request.
	 */
	protected static Params params(){
		return request().params();
	}
}