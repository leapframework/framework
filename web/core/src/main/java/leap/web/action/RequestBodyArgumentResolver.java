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
package leap.web.action;

import leap.lang.Out;
import leap.lang.convert.Converts;
import leap.web.App;
import leap.web.annotation.RequestBody;
import leap.web.body.RequestBodyReader;
import leap.web.format.RequestFormat;

import java.util.Optional;

public class RequestBodyArgumentResolver implements ArgumentResolver {
	
	protected final RequestBody 	  annotation;
	protected final boolean			  requestBodyDeclared;
	protected final RequestBodyReader requestBodyReader;
	protected final ArgumentResolver  nonRequestBodyResolver;
	
	public RequestBodyArgumentResolver(App app,
									   Action action,
									   Argument argument,
									   RequestBody annotation,
									   boolean requestBodyDeclared,
									   ArgumentResolver nonRequestBodyResolver) {
		
		this.annotation             = annotation;
		this.requestBodyDeclared    = requestBodyDeclared;
		this.nonRequestBodyResolver = nonRequestBodyResolver;
		this.requestBodyReader      = getRequestBodyReader(app, argument);
	}

	@Override
	public Object resolveValue(ActionContext context, Argument argument) throws Throwable {

        if(requestBodyDeclared && null != requestBodyReader) {
            return requestBodyReader.readRequestBody(context.getRequest(), argument.getType(), argument.getGenericType());
        }

        RequestFormat format = context.getRequestFormat();
        if(null != format && format.supportsRequestBody()){
            Object body = format.readRequestBody(context.getRequest());
            if(null == body) {
                return null;
            }

            if(null != argument.getBinder()) {
                Optional value = argument.getBinder().bind(context, argument, body);
                if(null != value) {
                    return value.get();
                }
            }

            return Converts.convert(body, argument.getType(), argument.getGenericType());
        }

        if(requestBodyDeclared){
            throw new IllegalArgumentException("Reading request body for type '" + argument.getType() + "' not supported");
        }

        return nonRequestBodyResolver.resolveValue(context, argument);
	}

	protected RequestBodyReader getRequestBodyReader(App app,Argument argument) {
		for(RequestBodyReader reader : app.factory().getBeans(RequestBodyReader.class)){
			if(reader.canReadRequestBody(argument.getType(), argument.getGenericType())){
				return reader;
			}
		}
		return null;
	}
}