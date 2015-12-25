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
import leap.web.Result;
import leap.web.route.RouteBuilder;

public interface ActionManager {
	
	/**
	 * Configs the action in the given {@link RouteBuilder}.
	 */
	void prepareAction(RouteBuilder route);
	
	/**
	 * Executes the {@link Action} in ghe given {@link ActionContext} and returns the result value.
	 */
	Object executeAction(ActionContext context,Validation validation) throws Throwable;
	
	/**
	 * Processes the return value returned by {@link #executeAction(ActionContext, Validation)}.
	 */
	void processResult(ActionContext context, Validation validation, Object returnValue,Result result) throws Throwable;
}