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
package leap.web.error;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import leap.core.annotation.Inject;
import leap.lang.Exceptions;
import leap.web.Request;
import leap.web.view.View;
import leap.web.view.ViewSource;

public class DefaultErrorViews implements ErrorViews {
	
	private @Inject ViewSource viewSource;
	
	private Map<Integer, ErrorView> codeViewMappings      = new ConcurrentHashMap<>();
	private Map<Class<?>,ErrorView> exceptionViewMappings = new ConcurrentHashMap<>();
	
	public DefaultErrorViews() {

	}
	
	public ViewSource getViewSource() {
		return viewSource;
	}

	public void setViewSource(ViewSource viewSource) {
		this.viewSource = viewSource;
	}

	@Override
    public ErrorViews addErrorView(Class<?> exceptionClass,View view) {
		exceptionViewMappings.put(exceptionClass, new ErrorView(null, view));
		return this;
	}
	
	@Override
    public ErrorViews addErrorView(Class<?> exceptionClass,ErrorView view) {
		exceptionViewMappings.put(exceptionClass, view);
		return this;
	}
	
	@Override
    public ErrorViews addErrorView(Class<?> exceptionClass,String viewName) {
		return addErrorView(exceptionClass, resolveView(viewName));
	}
	
	@Override
    public ErrorViews addErrorView(int code,ErrorView view){
		codeViewMappings.put(code, view);
		return this;
	}
	
	@Override
    public ErrorViews addErrorView(int code,View view){
		codeViewMappings.put(code, new ErrorView(null, view));
		return this;
	}
	
	@Override
    public ErrorViews addErrorView(int code,String viewName){
		return addErrorView(code,resolveView(viewName));
	}
	
	@Override
    public ErrorViews addErrorViews(ErrorsConfig config) {
		if(null != config){
			for(Entry<Integer, String> entry : config.getCodeViewMappings().entrySet()){
				addErrorView(entry.getKey(), entry.getValue());
			}
			for(Entry<Class<?>, String> entry : config.getExceptionViewMappings().entrySet()){
				addErrorView(entry.getKey(), entry.getValue());
			}
		}
		return this;
	}
	
	@Override
    public View resolveView(Request request,int code) throws Throwable {
		return resolveView(request,getErrorView(code));
	}
	
	@Override
    public View resolveView(Request request,Class<?> exceptionClass) throws Throwable {
		return resolveView(request,getErrorView(exceptionClass));
	}
	
	protected View resolveView(Request request,ErrorView ev) throws Throwable {
		if(null == ev){
			return null;
		}
		
		String name = ev.getName();
		if(null != name){
			return request.getViewSource().getView(name, request.getLocale());
		}
		
		return ev.getView();
	}
	
	@Override
    public ErrorView getErrorView(int code) {
		return codeViewMappings.get(code);
	}
	
	@Override
    public ErrorView getErrorView(Class<?> exceptionClass) {
		return exceptionViewMappings.get(exceptionClass);
	}
	
	@Override
    public Map<Integer, ErrorView> getStatusViewMappings() {
		return codeViewMappings;
	}

	@Override
    public Map<Class<?>, ErrorView> getExceptionViewMappings() {
		return exceptionViewMappings;
	}

	protected ErrorView resolveView(String viewName) {
		View view = null;
		
		try {
	        view = viewSource.getView(viewName, null);
        } catch (Throwable e) {
        	throw Exceptions.uncheck(e);
        }
		
		return new ErrorView(viewName, view);
	}
}