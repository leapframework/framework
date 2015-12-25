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
package leap.web;

import leap.core.validation.annotations.NotNull;
import leap.core.web.DefaultRequestMatcher;

public class DefaultRequestHandlerMapping extends DefaultRequestMatcher implements RequestHandlerMapping {
	
	protected @NotNull RequestHandler handler;
	
	public DefaultRequestHandlerMapping() {
	    super();
    }

	public DefaultRequestHandlerMapping(boolean ignoreCase) {
	    super(ignoreCase);
    }

	public RequestHandler getRequestHandler() {
		return handler;
	}

	public void setHandler(RequestHandler handler) {
		this.handler = handler;
	}

}