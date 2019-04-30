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

import leap.lang.intercepting.State;
import leap.web.action.ActionContext;
import leap.web.route.Route;

public interface RequestInterceptor {
	/**
	 * Called after {@link AppHandler#prepareRequest(Request, Response)}
	 */
	default void onPrepareRequest(Request request, Response response){}
	
	/**
	 * Called before handling request.
	 */
	default State preHandleRequest(Request request, Response response,ActionContext ac) throws Throwable {
	    return State.CONTINUE;
	}

    /**
     * Called after {@link #preHandleRequest(Request, Response,ActionContext)} and before handling the route.
     */
    default State handleRoute(Request request, Response response, Route route, ActionContext ac) throws Throwable {
        return State.CONTINUE;
    }

    /**
     * Called after {@link #preHandleRequest(Request, Response,ActionContext)} and before handling the request no route matched.
     */
    default State handleNoRoute(Request request, Response response) throws Throwable {
        return State.CONTINUE;
    }
	
	/**
	 * Called after handling request (the view was rendered) successfully.
	 */
	default State postHandleRequest(Request request, Response response, RequestExecution execution) throws Throwable {
	    return State.CONTINUE;
	}
	
	/**
	 * Called if failed to handle request (before handling the failure).
	 */
	default State onRequestFailure(Request request, Response response, RequestExecution execution) throws Throwable {
	    return State.CONTINUE;
	}

	/**
	 * Like Java's <code>finally</code>. Called after handling request whenever success or failure.
	 */
	default void completeHandleRequest(Request request, Response response, RequestExecution execution) throws Throwable {
	    
	}

}