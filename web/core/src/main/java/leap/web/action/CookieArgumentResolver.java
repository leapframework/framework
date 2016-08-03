/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.web.action;

import leap.web.App;
import leap.web.Request;
import leap.web.route.RouteBase;

public class CookieArgumentResolver extends AbstractArgumentResolver {

	public CookieArgumentResolver(App app, RouteBase route, Argument arg) {
		super(app, route, arg);
	}

	@Override
	public Object resolveValue(ActionContext context, Argument argument) throws Throwable {
		Request request = context.getRequest();
        return convertFromCookie(request.getCookie(argument.getName()), argument);
	}

}
