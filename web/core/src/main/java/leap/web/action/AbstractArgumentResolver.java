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

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Map;

import javax.servlet.http.Part;

import leap.lang.Charsets;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.http.MimeType;
import leap.lang.http.MimeTypes;
import leap.lang.io.IO;
import leap.web.App;
import leap.web.Request;
import leap.web.action.Argument.BindingFrom;
import leap.web.annotation.PartParam;
import leap.web.annotation.PathParam;
import leap.web.annotation.PathVariable;
import leap.web.annotation.QueryParam;
import leap.web.annotation.RequestBody;
import leap.web.annotation.RequestParam;

public abstract class AbstractArgumentResolver implements ArgumentResolver {
	
	protected final BindingFrom bindingFrom;

	protected AbstractArgumentResolver(App app,Action action,Argument arg) {
		this.bindingFrom = resolveBindingFrom(arg);
	}
	
	protected Object getParameter(ActionContext ac,Argument arg) throws Throwable{
		Request            request = ac.getRequest();
		String             name    = arg.getName();
		Map<String,Object> params  = request.getParameters();
		
		if(bindingFrom == BindingFrom.PATH_PARAM){
			return ac.getPathParameters().get(name);
		}
		
        if (bindingFrom == BindingFrom.QUERY_PARAM) {
            return request.getQueryParameters().get(name);
        }
		
		if(bindingFrom == BindingFrom.REQUEST_PARAM){
			return params.get(name);
		}
		
		if(bindingFrom == BindingFrom.PART_PARAM) {
			return request.getPart(name);
		}
		
		if(ac.getPathParameters().containsKey(name)){
			return ac.getPathParameters().get(name);
		}
		
		if(params.containsKey(name)) {
			return params.get(name);
		}
		
		if(request.isMultipart()) {
			return request.getPart(name);
		}
		
		return null;
	}
	
	protected BindingFrom resolveBindingFrom(Argument arg) {
		for(Annotation a : arg.getAnnotations()) {
			if(a.annotationType().equals(RequestParam.class)){
				return BindingFrom.REQUEST_PARAM;
			}
			
			if(a.annotationType().equals(PathVariable.class)){
				return BindingFrom.PATH_PARAM;
			}
			
			if(a.annotationType().equals(PathParam.class)) {
			    return BindingFrom.PART_PARAM;
			}
			
			if(a.annotationType().equals(QueryParam.class)) {
			    return BindingFrom.QUERY_PARAM;
			}
			
			if(a.annotationType().equals(RequestBody.class)){
				return BindingFrom.REQUEST_BODY;
			}
			
			if(a.annotationType().equals(PartParam.class)) {
				return BindingFrom.PART_PARAM;
			}
		}
		return BindingFrom.UNDEFINED;
	}
	
	protected Object convertFromPart(Part part,Argument arg) throws Throwable {
		if(part.getSize() == 0) {
			return null;
		}
		
		try(InputStream in = part.getInputStream()) {
			if(arg.getType().equals(byte[].class)) {
				return IO.readByteArray(in);
			}
			
			if(!Strings.isEmpty(part.getContentType())) {
				MimeType mimeType = MimeTypes.parse(part.getContentType());
				
				if(!Strings.isEmpty(mimeType.getCharset())) {
					return Converts.convert(IO.readString(in, Charsets.forName(mimeType.getCharset())),arg.getType(),arg.getGenericType());
				}
			}
			
			return Converts.convert(IO.readString(in, Charsets.UTF_8), arg.getType(), arg.getGenericType());
		}
	}
}