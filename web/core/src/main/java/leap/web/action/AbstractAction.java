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

import leap.lang.http.HTTP;
import leap.web.Request;
import leap.web.Response;

public abstract class AbstractAction implements Action {

	@Override
	public final Object execute(ActionContext context, Object[] args) throws Throwable {
		Request  req  = context.getRequest();
		Response resp = context.getResponse();
		
		Object v = doExecute(context, args, req, resp);
		
		if(!context.getResult().isCommitted() && !resp.isHandled()) {
			resp.setStatus(HTTP.SC_OK);
		}
		
	    return v;
	}

	protected abstract Object doExecute(ActionContext context, Object[] args, Request req, Response resp) throws Throwable;

}