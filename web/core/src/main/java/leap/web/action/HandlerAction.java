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

import leap.web.Handler;
import leap.web.Request;
import leap.web.Response;

public class HandlerAction extends AbstractAction {

	protected final Handler handler;
	
	public HandlerAction(Handler handler) {
		this.handler = handler;
	}
	
	@Override
    protected Object doExecute(ActionContext context, Object[] args, Request req, Response resp) throws Throwable {
		handler.handle(req, resp);
		return null;
    }

	@Override
    public String toString() {
	    return handler.toString();
    }
	
}
