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


public class ActionInterceptors implements ActionInterceptor {
	
	private static final Log log = LogFactory.get(ActionInterceptors.class);
	
	private final ActionInterceptor[] interceptors;

	public ActionInterceptors(ActionInterceptor[] interceptors) {
		this.interceptors = interceptors;
	}

	public State preExecuteAction(ActionContext context, Validation validation) throws Throwable {
		State state = null;
		for(int i=0;i<interceptors.length;i++){
			if(!State.isContinue(state = interceptors[i].preExecuteAction(context, validation))){
				context.getResponse().markHandled();
				break;
			}
		}
		return state;
	}
	
	@Override
    public State postExecuteAction(ActionContext context, Validation validation, ActionExecution execution) throws Throwable {
		State state = null;
		
		for(int i=0;i<interceptors.length;i++){
			if(!State.isContinue(state = interceptors[i].postExecuteAction(context, validation, execution))){
				break;
			}
		}
		
		return state;
    }
	
	@Override
    public State onActionFailure(ActionContext context, Validation validation, ActionExecution execution) throws Throwable {
		State state = null;
		for(int i=0;i<interceptors.length;i++){
			if(!State.isContinue(state = interceptors[i].onActionFailure(context, validation, execution))){
				break;
			}
		}
		return state;
    }

	@Override
    public void completeExecuteAction(ActionContext context, Validation validation, ActionExecution execution) throws Throwable {
		for(int i=0;i<interceptors.length;i++){
			try {
	            interceptors[i].completeExecuteAction(context, validation, execution);
            } catch (Throwable e) {
            	log.info("Error executing 'completeExecuteAction' on interceptor '{}' : {}", interceptors[i], e.getMessage(), e);
            }
		}
    }
}
