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
package leap.web.action;

import java.util.function.Function;

import leap.web.Result;

public abstract class ConditionalFailureHandler implements FailureHandler {
	
	protected final Function<ActionExecution, Boolean> cond;
	
	public ConditionalFailureHandler(Function<ActionExecution, Boolean> cond) {
		this.cond = cond;
	}
	
	@Override
    public boolean handleFailure(ActionContext context, ActionExecution execution, Result result) {
		if(null != cond && !cond.apply(execution)) {
			return false;
		}
		
		doHandlerFailure(context, execution, result);
		
		return true;
	}
	
	protected abstract void doHandlerFailure(ActionContext context, ActionExecution execution, Result result);
}
