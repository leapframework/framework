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
package leap.web.action;

import leap.core.validation.Validation;
import leap.web.Request;
import leap.web.Result;
import leap.web.view.View;

public final class ViewResultProcessor implements ResultProcessor  {

	protected final String viewName;
	protected final View   view;
	
	public ViewResultProcessor(String viewName,View view) {
		this.viewName = viewName;
		this.view     = view;
	}

	@Override
    public void processReturnValue(ActionContext context, Object returnValue, Result result) throws Throwable {
		result.setReturnValue(returnValue);
		
		Request request = context.getRequest();
		
		View view = request.getViewSource().getView(viewName, request.getLocale());
		if(null == view){
			view = this.view;
		}
		result.render(view);
    }

	@Override
    public void processValidationErrors(ActionContext context, Validation validation, Result result) throws Throwable {
	    result.render(view);
    }
}