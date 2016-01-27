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

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leap.core.validation.Validation;
import leap.core.web.RequestBase;
import leap.core.web.ResponseBase;
import leap.web.Params;
import leap.web.Request;
import leap.web.Response;
import leap.web.Result;
import leap.web.view.ViewData;

public class ContextArgumentResolver implements ArgumentResolver {
	
	private static final Set<Class<?>>   TYPES    = new HashSet<Class<?>>();
	static final ContextArgumentResolver INSTANCE = new ContextArgumentResolver();
	
	static {
		TYPES.add(RequestBase.class);
		TYPES.add(Request.class);
		TYPES.add(Response.class);
		TYPES.add(ResponseBase.class);
		TYPES.add(HttpServletRequest.class);
		TYPES.add(HttpServletResponse.class);
		TYPES.add(ServletContext.class);
		TYPES.add(ActionContext.class);
		TYPES.add(Result.class);
		TYPES.add(ViewData.class);
		TYPES.add(Params.class);
		TYPES.add(Validation.class);
	}
	
	public static boolean isContext(Class<?> type){
		return TYPES.contains(type);
	}

	@Override
    public Object resolveValue(ActionContext context, Argument argument) throws Throwable {
		Class<?> type = argument.getType();
		
		if(type.equals(Request.class) || type.equals(RequestBase.class)){
			return context.getRequest();
		}else if(type.equals(ViewData.class)){
			return context.getResult().getViewData();
		}else if(type.equals(Params.class)){
			return context.getRequest().params();
		}else if(type.equals(Response.class) || type.equals(ResponseBase.class)){
			return context.getResponse();
		}else if(type.equals(HttpServletRequest.class)){
			return context.getRequest().getServletRequest();
		}else if(type.equals(HttpServletResponse.class)){
			return context.getResponse().getServletResponse();
		}else if(type.equals(ActionContext.class)){
			return context;
		}else if(type.equals(Result.class)){
			return context.getResult();
		}else if(type.equals(Validation.class)) {
            return context.getRequest().getValidation();
        }
		
		throw new IllegalStateException("The argument type '" + type.getName() + "' is not a supported context type");
    }
}