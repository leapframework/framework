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
import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.config.WebInterceptors;


class ActionInterceptors implements ActionInterceptor {
	
	private static final Log log = LogFactory.get(ActionInterceptors.class);

    private final WebInterceptors interceptors;
	private final Action          action;

	public ActionInterceptors(WebInterceptors interceptors, Action action) {
        this.interceptors = interceptors;
		this.action = action;
	}

	public State preExecuteAction(ActionContext context, Validation validation) throws Throwable {
		State state = null;

        for(ActionInterceptor interceptor : interceptors.getActionInterceptors()) {
            if(!State.isContinue(state = interceptor.preExecuteAction(context, validation))){
                context.getResponse().markHandled();
                return state;
            }
        }

        ActionInterceptor[] actionInterceptors = action.getInterceptors();
		for(int i = 0; i< actionInterceptors.length; i++){
			if(!State.isContinue(state = actionInterceptors[i].preExecuteAction(context, validation))){
				context.getResponse().markHandled();
				return state;
			}
		}
		return state;
	}
	
	@Override
    public State postExecuteAction(ActionContext context, Validation validation, ActionExecution execution) throws Throwable {
		State state = null;

        for(ActionInterceptor interceptor : interceptors.getActionInterceptors()) {
            if(!State.isContinue(state = interceptor.postExecuteAction(context, validation, execution))){
                context.getResponse().markHandled();
                return state;
            }
        }

        ActionInterceptor[] actionInterceptors = action.getInterceptors();
		for(int i = 0; i< actionInterceptors.length; i++){
			if(!State.isContinue(state = actionInterceptors[i].postExecuteAction(context, validation, execution))){
				return state;
			}
		}
		
		return state;
    }
	
	@Override
    public State onActionFailure(ActionContext context, Validation validation, ActionExecution execution) throws Throwable {
		State state = null;

        for(ActionInterceptor interceptor : interceptors.getActionInterceptors()) {
            if(!State.isContinue(state = interceptor.onActionFailure(context, validation, execution))){
                context.getResponse().markHandled();
                return state;
            }
        }

        ActionInterceptor[] actionInterceptors = action.getInterceptors();
		for(int i = 0; i< actionInterceptors.length; i++){
			if(!State.isContinue(state = actionInterceptors[i].onActionFailure(context, validation, execution))){
				return state;
			}
		}
		return state;
    }

	@Override
    public void completeExecuteAction(ActionContext context, Validation validation, ActionExecution execution) throws Throwable {

        for(ActionInterceptor interceptor : interceptors.getActionInterceptors()) {
            try {
                interceptor.completeExecuteAction(context, validation, execution);
            } catch (Throwable e) {
                log.info("Error executing 'completeExecuteAction' on interceptor '{}' : {}", interceptor, e.getMessage(), e);
            }
        }

        ActionInterceptor[] actionInterceptors = action.getInterceptors();
		for(int i = 0; i< actionInterceptors.length; i++){
			try {
	            actionInterceptors[i].completeExecuteAction(context, validation, execution);
            } catch (Throwable e) {
            	log.info("Error executing 'completeExecuteAction' on interceptor '{}' : {}", actionInterceptors[i], e.getMessage(), e);
            }
		}
    }
}
