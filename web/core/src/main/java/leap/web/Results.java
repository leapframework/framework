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

import java.util.Map;

import leap.lang.http.HTTP;

public abstract class Results {
	
	public static final String HTML_FROMAT = "html";
	public static final String JSON_FOMRAT = "json";
	
	/**
	 * Nothing renderable.
	 */
	public static final Renderable NR = new Renderable() {
		@Override
		public void render(Request request, Response response) throws Throwable {}
	};
	
	public static Result noContent(){
		return Result.current().render(HTTP.SC_NO_CONTENT, NR); 
	}
	
	public static Result badRequest(){
		return Result.current().render(HTTP.SC_BAD_REQUEST, NR);
	}
	
	public static Result notImplemented(){
		return Result.current().render(HTTP.SC_NOT_IMPLEMENTED,NR); 
	}
	
	public static Result notFound(){
		return Result.current().render(HTTP.SC_NOT_FOUND,NR); 
	}
	
	public static Result ok(){
		return Result.current().render(HTTP.SC_OK,NR);
	}
	
	public static Result text(String text){
		return Result.current().render(Contents.text(text));
	}
	
	public static Result html(String html){
		return Result.current().render(Contents.html(html));
	}
	
	public static Result javascript(String js){
		return Result.current().render(Contents.js(js));
	}
	
	public static Result json(Object content){
		return render(content,JSON_FOMRAT);
	}
	
	public static Result render(Content content){
		return Result.current().render(content);
	}
	
	public static Result render(Object content,String format) {
        Request request = Request.current();
		return request.getResult().render(new Contents.FormattingContent(request.getActionContext(), content, format));
	}
	
	public static Result renderView(String viewName){
		return Result.current().render(Contents.view(viewName));
	}
	
	public static Result renderView(String viewName,String attribute,Object value){
		return renderView(viewName).setViewData(attribute, value);
	}
	
	public static Result renderView(String viewName,String attribute1,Object value1,String attribute2,Object value2){
		return renderView(viewName).setViewData(attribute1, value1).setViewData(attribute2, value2);
	}

	public static Result redirect(String location){
		return Result.current().render(RenderableRedirect.STATUS_DEFAULT,new RenderableRedirect(location));
	}
	
	public static Result redirect(String location,Map<String,?> queryParameters){
		return Result.current().render(RenderableRedirect.STATUS_DEFAULT,new RenderableRedirect(location,queryParameters));
	}
	
	/**
	 * Forwards current request to another resource.
	 * 
	 * @see RequestDispatcher#forward(javax.servlet.ServletRequest, javax.servlet.ServletResponse).
	 */
	public static Result forward(String path){
		return Result.current().render(new RenderableForward(path));
	}
	
	/**
	 * Set an view data in current result.
	 */
	public static Result setViewData(String name,Object value) {
		return Result.current().setViewData(name, value);
	}
}