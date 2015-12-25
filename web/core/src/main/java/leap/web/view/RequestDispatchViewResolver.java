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
package leap.web.view;

import java.util.Locale;

import leap.lang.Classes;
import leap.lang.servlet.ServletResource;

public class RequestDispatchViewResolver extends AbstractServletResourceViewResolver {
	
	private static final boolean jstlPresent = Classes.isPresent(RequestDispatchViewResolver.class.getClassLoader(), 
							  									 "javax.servlet.jsp.jstl.core.Config");
	
	protected Boolean alwaysInclude;
	protected Boolean preventDispatchLoop;
	
	public void setAlwaysInclude(Boolean alwaysInclude) {
		this.alwaysInclude = alwaysInclude;
	}

	public void setPreventDispatchLoop(Boolean preventDispatchLoop) {
		this.preventDispatchLoop = preventDispatchLoop;
	}

	@Override
	protected View loadView(String prefix, String suffix, String viewPath, Locale locale, String resourcePath, ServletResource resource) {
		RequestDispatchView view = createServletDispatchView(viewPath, resource);
		
		view.setReturnValueAttribute(AbstractView.DEFAULT_RETURN_VALUE_ATTRIBUTE);
		
		if(null != alwaysInclude){
			view.setAlwaysInclude(alwaysInclude);
		}
		
		if(null != preventDispatchLoop){
			view.setPreventDispatchLoop(preventDispatchLoop);
		}
		
		return view;
	}
	
	protected RequestDispatchView createServletDispatchView(String path,ServletResource resource){
		return jstlPresent ? new JstlView(app, path, resource) : new RequestDispatchView(app, path, resource);
	}
}