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

import leap.lang.Strings;
import leap.web.App;
import leap.web.Content;
import leap.web.Contents;
import leap.web.RenderableDownload;
import leap.web.RenderableForward;
import leap.web.RenderableRedirect;
import leap.web.Renderable;
import leap.web.Result;
import leap.web.format.ResponseFormat;
import leap.web.route.RouteBuilder;
import leap.web.view.View;

public final class StringResultProcessor extends AbstractResultProcessor implements ResultProcessor {
	
	public static final int MAX_PREFIX_LENGTH = 10;
	
	private final View			  			view;
	private final FormattingResultProcessor formattingProcessor;
	
	public StringResultProcessor(App app,RouteBuilder route) {
		this.view                = null == route.getDefaultView() ? null : route.getDefaultView();
		this.formattingProcessor = new FormattingResultProcessor(app, route);
	}
	
	@Override
    public void processReturnValue(ActionContext context, Object returnValue, Result result) throws Throwable {
		String string;
		
		if(null != returnValue){
			string = (String)returnValue;
			
			if(string.length() > 0){
				int index = string.indexOf(':');
				
				if(index > 0 && index <= MAX_PREFIX_LENGTH && index < string.length() - 1){
					String prefix = string.substring(0,index+1);
					
					if(prefix.equals(Renderable.VIEW_PREFIX)){
						result.render(Contents.view(string.substring(index+1)));
						return;
					}
					
					if(prefix.equals(Renderable.REDIRECT_PREFIX)){
						result.render(new RenderableRedirect(string.substring(index+1)));
						return;
					}
					
					if(prefix.equals(Renderable.ACTION_PREFIX)) {
					    context.getRequest().forwardToAction(string.substring(index+1));
					    return;
					}
					
					if(prefix.equals(Renderable.FORWARD_PREFIX)){
						result.render(new RenderableForward(string.substring(index+1)));
						return;
					} 
					
					if(prefix.equals(Renderable.DOWNLOAD_PREFIX)) {
						result.render(new RenderableDownload(string.substring(index+1)));
						return;
					}
				}
			}
		}else{
			string = Strings.EMPTY;
		}
		
		if(null != view){
			result.setReturnValue(returnValue);
			result.render(view);
		}else if(formattingProcessor.hasAnnotatedFormats()){
			ResponseFormat format  = formattingProcessor.selectAnnotatedFormat(context);
			Content 	   content = format.getContent(context,returnValue);
			result.render(content);
		}else{
			result.render(Contents.text(string));
		}
    }
}