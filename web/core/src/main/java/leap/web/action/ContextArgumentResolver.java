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
import leap.core.web.RequestBase;
import leap.core.web.ResponseBase;
import leap.web.Params;
import leap.web.Request;
import leap.web.Response;
import leap.web.Result;
import leap.web.view.ViewData;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ContextArgumentResolver implements ArgumentResolver {
	
	private static final Map<Class<?>, ContextArgumentResolver> resolvers = new HashMap<>();

	static {
		addContextResolver(RequestBase.class);
		addContextResolver(Request.class);
		addContextResolver(Response.class);
		addContextResolver(ResponseBase.class);
		addContextResolver(HttpServletRequest.class);
		addContextResolver(HttpServletResponse.class);
		addContextResolver(ServletContext.class);
		addContextResolver(ActionContext.class);
		addContextResolver(Result.class);
		addContextResolver(ViewData.class);
		addContextResolver(Params.class);
		addContextResolver(Validation.class);
	}
	
	public static boolean isContext(Class<?> type){
		return resolvers.containsKey(type);
	}

    public static ArgumentResolver of(Class<?> type) {
        ContextArgumentResolver r = resolvers.get(type);
        if(null == r) {
            throw new IllegalStateException("The type '" + type + "' is not a context type");
        }
        return r;
    }

    public static ArgumentResolver ofAttribute(String name) {
        return new ContextArgumentResolver((c) -> c.getRequest().getAttribute(name));
    }

    protected static void addContextResolver(Class<?> type) {
        Function<ActionContext, Object> func;

        if(type.equals(Request.class) || type.equals(RequestBase.class)){
            func = c -> c.getRequest();
        }else if(type.equals(ViewData.class)){
            func = c -> c.getResult().getViewData();
        }else if(type.equals(Params.class)){
            func = c -> c.getRequest().params();
        }else if(type.equals(Response.class) || type.equals(ResponseBase.class)){
            func = c -> c.getResponse();
        }else if(type.equals(HttpServletRequest.class)){
            func = c -> c.getRequest().getServletRequest();
        }else if(type.equals(HttpServletResponse.class)){
            func = c -> c.getResponse().getServletResponse();
        }else if(type.equals(ActionContext.class)){
            func = c -> c;
        }else if(type.equals(Result.class)){
            func = c -> c.getResult();
        }else if(type.equals(Validation.class)) {
            func = c -> c.getRequest().getValidation();
        }else if(type.equals(ServletContext.class)) {
            func = c -> c.getRequest().getServletContext();
        }else{
            throw new IllegalStateException("The argument type '" + type.getName() + "' is not a supported context type");
        }

        resolvers.put(type, new ContextArgumentResolver(func));
    }

    protected final Function<ActionContext, Object> func;

    protected ContextArgumentResolver(Function<ActionContext, Object> func) {
        this.func = func;
    }

	@Override
    public Object resolveValue(ActionContext context, Argument argument) throws Throwable {
        return func.apply(context);
    }
}