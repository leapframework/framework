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
package leap.web.multipart;

import javax.servlet.http.HttpServletRequest;

import leap.web.action.ActionContext;

public class MultipartContext {

	public static final String MULTIPART_ROUTE_KEY = MultipartContext.class.getName() + "_actionContext";
	
	public static void setMultipartAction(HttpServletRequest request,ActionContext actionContext) {
		request.setAttribute(MULTIPART_ROUTE_KEY, actionContext);
	}

	public static ActionContext getMultipartAction(HttpServletRequest request) {
		return (ActionContext)request.getAttribute(MULTIPART_ROUTE_KEY);
	}

}
