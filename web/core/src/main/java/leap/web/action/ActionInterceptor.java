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
import leap.lang.intercepting.Interceptor;
import leap.lang.intercepting.State;

public interface ActionInterceptor extends Interceptor {
	/**
	 * Called before resolve parameters
	 */
	default State preResolveActionParameters(ActionContext context, Validation validation) throws Throwable {
		return State.CONTINUE;
	}
	
	/**
	 * Called after resolve and before executing the action.
     */
	default State preExecuteAction(ActionContext context, Validation validation) throws Throwable {
		return State.CONTINUE;
	}

	/**
	 * Called after executing the action (before rendering view) successfully.
     */
	default State postExecuteAction(ActionContext context, Validation validation, ActionExecution execution) throws Throwable {
		return State.CONTINUE;
	}

	/**
	 * Called if failed to execute the action.
     */
	default State onActionFailure(ActionContext context, Validation validation, ActionExecution execution) throws Throwable {
		return State.CONTINUE;
	}

	/**
	 * Like Java's <code>finally</code>. Called after executing action (before rendering view) whenever success or failure.
     */
	default void completeExecuteAction(ActionContext context, Validation validation, ActionExecution execution) throws Throwable {
		
	}

}