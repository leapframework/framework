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
package leap.web.format;

import java.util.Map;

import leap.core.BeanFactory;
import leap.core.annotation.Inject;
import leap.core.ioc.PostCreateBean;
import leap.lang.Strings;
import leap.lang.http.MimeType;
import leap.web.App;
import leap.web.Request;
import leap.web.config.WebConfig;

public class DefaultFormatManager implements FormatManager,PostCreateBean {
	
    protected @Inject App                      app;
    protected @Inject WebConfig                webConfig;
    protected @Inject RequestFormatResolver[]  requestFormatResolvers;
    protected @Inject ResponseFormatResolver[] responseFormatResolvers;
	
    private Map<String, ResponseFormat>        responseFormats;
    private ResponseFormat                     defaultResponseFormat;

	@Override
    public RequestFormat getRequestFormat(String name) throws FormatNotFoundException {
		RequestFormat fmt = tryGetRequestFormat(name);
		
		if(null == fmt){
			throw new FormatNotFoundException("The request format '" + name + "' not found");
		}
		
	    return fmt;
    }

	@Override
    public RequestFormat tryGetRequestFormat(String name) {
	    return app.factory().tryGetBean(RequestFormat.class,name);
    }

	@Override
    public RequestFormat resolveRequestFormat(Request request) throws Throwable {
		//Resolve response format from request's parameter
		if(webConfig.isFormatParameterEnabled()){
			String formatName = request.getParameter(webConfig.getFormatParameterName());
			if(!Strings.isEmpty(formatName)){
				//request format is not mandatory, 
				//so we just try to get the format object and return it or return null if not found.
				return tryGetRequestFormat(formatName);
			}
		}
		
		//Resovle by resolvers.
		RequestFormat f = null;
		for(int i=0;i<requestFormatResolvers.length;i++){
			if((f = requestFormatResolvers[i].resolveRequestFormat(request)) != null){
				return f;
			}
		}
		
	    return null;
    }
	
	@Override
    public RequestFormat selectRequestFormat(Request request, RequestFormat[] formats) throws Throwable {
		MimeType contentType = request.getContentType();
		if(null == contentType){
			return null;
		}
		
		for(int i=0;i<formats.length;i++){
			RequestFormat fmt = formats[i];
			if(fmt.supports(contentType)){
				return fmt;
			}
		}
		
	    return null;
    }

	@Override
    public ResponseFormat getResponseFormat(String name) throws FormatNotFoundException {
		ResponseFormat f = tryGetResponseFormat(name);
		
		if(null == f){
			throw new FormatNotFoundException("The response format '" + name + "' not found");
		}
		
	    return f;
    }

	@Override
	public ResponseFormat tryGetResponseFormat(String name) {
		return responseFormats.get(name);
	}

	@Override
    public ResponseFormat resolveResponseFormat(Request request) throws Throwable{
		//Resolve response format from request's parameter
		if(webConfig.isFormatParameterEnabled()){
			String formatName = request.getParameter(webConfig.getFormatParameterName());
			if(!Strings.isEmpty(formatName)){
				return getResponseFormat(formatName);
			}
		}
		
		//Resovle by resolvers.
		ResponseFormat f = null;
		for(int i=0;i<responseFormatResolvers.length;i++){
			if((f = responseFormatResolvers[i].resolveResponseFormat(request)) != null){
				return f;
			}
		}
		return f;
    }
	
	@Override
    public ResponseFormat selectResponseFormat(Request request, ResponseFormat[] formats) throws Throwable {
		MimeType[] acceptableMediaTypes = request.getAcceptableMediaTypes();

		for(int i=0;i<formats.length;i++){
			ResponseFormat fmt = formats[i];
			for(int j=0;j<acceptableMediaTypes.length;j++){
				if(fmt.supports(acceptableMediaTypes[j])){
					return fmt;
				}
			}
		}
		
		return null;
    }

	@Override
    public ResponseFormat getDefaultResponseFormat() {
	    return defaultResponseFormat;
    }

	@Override
    public void postCreate(BeanFactory beanFactory) throws Exception {
		this.responseFormats	   = beanFactory.getNamedBeans(ResponseFormat.class);
		this.defaultResponseFormat = beanFactory.getBean(ResponseFormat.class,webConfig.getDefaultFormatName());
    }
}