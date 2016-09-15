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
package leap.web;

import java.util.List;

import leap.lang.intercepting.State;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.action.ActionContext;
import leap.web.route.Route;


class RequestInterceptors implements RequestInterceptor {
	
	private static final Log log = LogFactory.get(RequestInterceptors.class);

	private final List<RequestInterceptor> interceptors;
	
	public RequestInterceptors(List<RequestInterceptor> interceptors) {
		this.interceptors = interceptors;
	}

	@Override
	public State preHandleRequest(Request request, Response response) throws Throwable {
		State state = null;
		for(int i=0;i<interceptors.size();i++){
			if(!State.isContinue(state = interceptors.get(i).preHandleRequest(request, response))){
				response.markHandled();
				break;
			}
		}
		return state;
	}

    @Override
    public State handleRoute(Request request, Response response, Route route, ActionContext ac) throws Throwable {
        State state = null;
        for(int i=0;i<interceptors.size();i++){
            if(!State.isContinue(state = interceptors.get(i).handleRoute(request, response, route, ac))){
                response.markHandled();
                break;
            }
        }
        return state;
    }

    @Override
    public State handleNoRoute(Request request, Response response) throws Throwable {
        State state = null;
        for(int i=0;i<interceptors.size();i++){
            if(!State.isContinue(state = interceptors.get(i).handleNoRoute(request, response))){
                response.markHandled();
                break;
            }
        }
        return state;
    }

    @Override
	public State onRequestFailure(Request request, Response response, RequestExecution execution) throws Throwable {
		State state = null;
		for(int i=0;i<interceptors.size();i++){
			if(!State.isContinue(state = interceptors.get(i).onRequestFailure(request, response, execution))){
				break;
			}
		}
		return state;
	}

	@Override
	public State postHandleRequest(Request request, Response response, RequestExecution execution) throws Throwable {
		State state = null;
		for(int i=0;i<interceptors.size();i++){
			if(!State.isContinue(state = interceptors.get(i).postHandleRequest(request, response, execution))){
				break;
			}
		}
		return state;
	}

	@Override
	public void completeHandleRequest(Request request, Response response, RequestExecution execution) throws Throwable {
		for(int i=0;i<interceptors.size();i++){
			try {
	            interceptors.get(i).completeHandleRequest(request, response, execution);
            } catch (Throwable e) {
            	log.info("Error executing 'completeHandleRequest' on interceptor '{}' : {}",interceptors.get(i), e.getMessage(), e);
            }
		}
	}
}
