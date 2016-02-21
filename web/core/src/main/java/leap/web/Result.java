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
package leap.web;

import leap.lang.annotation.Nullable;
import leap.lang.http.HTTP;
import leap.web.view.LinkedViewData;
import leap.web.view.ViewData;

import java.util.Map;


/**
 * Indicates an action execution result use to render a http response.
 */
public class Result {
	
	public static final int STATUS_UNDEFINED = -1;

	/**
	 * Returns current action result.
	 * 
	 * @throws IllegalStateException if result not exists in current execution context. 
	 */
	public static Result current(){
		return Request.current().getResult();
	}
	
	private int        status = STATUS_UNDEFINED;
	private Renderable renderable;
	private ViewData   viewData = new LinkedViewData();
	private int        executionCount; 
	
	public Result(){

	}
	
	public Result(int status){
		this(status,null);
	}
	
	public Result(int status,@Nullable Renderable renderable){
		this.status     = status;
		this.renderable = renderable;
	}
	
	/**
     * Returns a boolean indicating if the result has been committed.  
     * 
     * A committed result has already had its status code or renderable content set.
     */
    public boolean isCommitted(){
    	return null != renderable || STATUS_UNDEFINED != status;
    }
    
    public int getStatus() {
        return status;
    }
    
	public Result setStatus(int status){
    	this.status = status;
    	return this;
    }

    public Renderable getRenderable() {
        return renderable;
    }
    
    public Result setRenderable(Renderable renderable){
    	this.renderable = renderable;
    	return this;
    }
    
    public ViewData getViewData() {
    	return viewData;
    }
    
    public Object getReturnValue(){
    	return viewData.getReturnValue();
    }
    
    public Result setReturnValue(Object returnValue){
    	this.viewData.setReturnValue(returnValue);
    	return this;
    }
    
    public Result setViewData(String name,Object value){
    	viewData.put(name, value);
    	return this;
    }
    
    public Result putViewDatas(Map<String, ?> attributes){
    	viewData.putAll(attributes);
    	return this;
    }
    
    public Result render(Renderable renderable){
    	return render(status > 0 ? -1 : HTTP.SC_OK,renderable);
    }
    
    public Result render(Renderable renderable, Object data){
    	return render(status > 0 ? -1 : HTTP.SC_OK,renderable,data);
    }
    
    public Result render(Renderable renderable,Object data,Map<String, ?> attributes){
    	return render(status > 0 ? -1 : HTTP.SC_OK,renderable,data,attributes);
    }
    
    public Result render(int status,Renderable renderable){
        if(status > 0) {
            this.status = status;
        }
    	this.renderable = renderable;
    	return this;
    }
    
    public Result render(int status, Renderable renderable, Object data){
        if(status > 0) {
            this.status = status;
        }

    	this.renderable  = renderable;
    	
    	this.setReturnValue(data);
    	
    	return this;
    }
    
    public Result render(int status, Renderable renderable, Object data,Map<String, ?> attributes){
        if(status > 0) {
            this.status = status;
        }

    	this.renderable  = renderable;
    	
    	this.setReturnValue(data);
    	
    	if(null != attributes){
    		this.putViewDatas(attributes);
    	}
    	return this;
    }
    
    public Result text(String text) {
    	return render(status > 0 ? -1 : HTTP.SC_OK, Contents.text(text));
    }
    
    public Result text(int status, String text) {
    	return render(status, Contents.text(text));
    }
    
    public Result html(String html) {
    	return render(status > 0 ? -1 : HTTP.SC_OK, Contents.html(html));
    }
    
    public Result html(int status, String html) {
    	return render(status, Contents.html(html));
    }
    
    public Result json(String json) {
    	return render(status > 0 ? -1 : HTTP.SC_OK, Contents.json(json));
    }
    
    public Result json(int status, String json) {
    	return render(status, Contents.json(json));
    }
    
    public Result renderView(String viewName) {
    	return render(Contents.view(viewName));
    }
    
    /**
     * Sets this result from other result object.
     */
    public Result setResult(Result other){
    	this.status     = other.status;
    	this.renderable = other.renderable;
    	this.viewData   = other.viewData;
    	return this;
    }
    
    int getExecutionCount(){
    	return executionCount;
    }
    
    int increaseAndGetExecutionCount(){
    	return (++executionCount);
    }
    
    @Override
    public String toString() {
		return "{status:" + status + ",content:" + renderable + "}";
    }
}