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

import leap.lang.Objects2;
import leap.lang.Strings;
import leap.lang.http.MimeTypes;
import leap.lang.path.Paths;
import leap.web.action.ActionContext;
import leap.web.format.FormatNotFoundException;
import leap.web.format.ResponseFormat;
import leap.web.view.View;
import leap.web.view.ViewNotFoundException;
import leap.web.view.ViewResolvingException;

public class Contents extends MimeTypes{
	
	public static String createContentType(Request request,String mediaType){
		return new StringBuilder(mediaType)
						.append("; charset=")
						.append(request.app().getDefaultCharset().name())
						.toString();
	}
	
	public static Content text(String text,String mediaType){
		return new TextContent(text,mediaType);
	}
	
	public static Content text(String text) {
		return new TextContent(text, TEXT_PLAIN);
	}
	
	public static Content html(String html){
		return new TextContent(html, TEXT_HTML);
	}
	
	public static Content js(String js) {
		return new TextContent(js, TEXT_JAVASCRIPT);
	}
	
	public static Content css(String css) {
		return new TextContent(css, TEXT_CSS);
	}
	
	public static Content json(String json){
		return new TextContent(json, APPLICATION_JSON);
	}
	
	public static Content xml(String xml){
		return new TextContent(xml, APPLICATION_XML);
	}
	
	public static Content view(String viewName){
		return new ViewContent(viewName);
	}

	public static abstract class AbstractTextContent implements Content {
		
		public abstract String getContentType(Request request) throws Throwable; 
		
		@Override
        public final void render(Request request, Response response) throws Throwable {
			response.setContentType(getContentType(request));
			doRender(request, response);
        }
		
		protected abstract void doRender(Request request,Response response) throws Throwable;
	}
	
	public static final class TextContent extends AbstractTextContent {
		
		protected final String text;
		protected final String mediaType;
		
		public TextContent(String text) {
			this(text, MimeTypes.TEXT_PLAIN);
        }
		
		public TextContent(String text,String mediaType) {
	        this.text      = text;
	        this.mediaType = mediaType;
        }
		
		@Override
        public final String getContentType(Request request) throws Exception{
	        return createContentType(request, mediaType);
        }
		
		@Override
        protected void doRender(Request request, Response response) throws Exception {
			response.getServletResponse().getWriter().write(text);
        }

		@Override
        public String toString() {
			return "{TextContent:{mediaType:'" + mediaType +  "'}}";
        }
	}
	
	public static class FormattingContent implements Content {

        private final ActionContext  context;
		private final String         name;
		private final Object         value;
		private final ResponseFormat format;
		private final Content		 content;
		private final Class<?>       type;
		
		public FormattingContent(ActionContext context, Object value, String formatName){
            this.context = context;
			this.value   = value;
			this.name    = formatName;
			this.format  = resolveFormat(formatName);
			this.type    = null == value ? Object.class : value.getClass();
			
			try {
	            this.content = format.getContent(context, value);
            } catch (Exception e) {
            	throw new ResultException("Error convert value to content format '" + formatName + "', " + e.getMessage(), e);
            }
			
			if(null == content){
				throw new ResultException("The response format '" + formatName + "' returns null content for the given value :" + value);
			}
		}
		
		@Override
        public String getContentType(Request request) throws Throwable {
	        return content.getContentType(request);
        }

		@Override
        public void render(Request request, Response response) throws Throwable {
			content.render(request, response);
        }
		
		protected ResponseFormat resolveFormat(String formatName){
			Request request = Request.current();
			
			ResponseFormat format = request.getFormatManager().tryGetResponseFormat(formatName);
			
			if(null == format){
				throw new FormatNotFoundException("Response format '" + formatName + "' not found");
			}
			
			return format;
		}

		@Override
        public String toString() {
			return "{" + name + ":" + Strings.abbreviate(Objects2.toStringOrEmpty(value), 20) + "}";
        }
	}
	
	private static class ViewContent extends AbstractTextContent {
		
		private final View view;
		
		public ViewContent(String viewName) throws ViewResolvingException,ViewNotFoundException {
			this.view = resolveView(viewName);
		}

		@Override
	    public String getContentType(Request request) throws Throwable{
	        return view.getContentType(request);
	    }

		@Override
		protected void doRender(Request request, Response response) throws Throwable {
	        view.render(request, response, Result.current().getViewData());
	    }
		
		protected View resolveView(String viewPath) throws ViewNotFoundException{
			Request request = Request.current();
			
			try {
				if(!viewPath.startsWith("/")){
					ActionContext ac = request.getActionContext();
					if(null != ac && request.getServicePath().equals(ac.getRoute().getControllerPath())){
						viewPath = request.getPath() + "/" + viewPath;
					}else{
						viewPath = Paths.applyRelative(request.getServicePath(), viewPath);
					}
				}
				
	            View view = request.getViewSource().getView(viewPath, request.getLocale());
	            
	            if(null == view){
	            	throw new ViewNotFoundException(viewPath, "View '" + viewPath + "' not found");
	            }
	            
	            return view;
	            
            } catch (Throwable e) {
            	if(e instanceof ViewResolvingException){
            		throw (ViewResolvingException)e;
            	}
            	throw new ViewResolvingException(viewPath, "Error resolving view '" + viewPath + "'",e);
            }
		}

		@Override
        public String toString() {
	        return "{View:" + view.toString() + "}";
        }
	}

	protected Contents(){
		
	}
}