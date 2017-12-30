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

import java.util.ArrayList;
import java.util.List;

import leap.lang.Arrays2;
import leap.lang.Out;
import leap.lang.intercepting.State;
import leap.web.*;
import leap.web.annotation.Produces;
import leap.web.format.FormatManager;
import leap.web.format.FormatNotAcceptableException;
import leap.web.format.ResponseFormat;
import leap.web.route.RouteBuilder;
import leap.web.view.View;

public class FormattingResultProcessor extends AbstractResultProcessor implements ResultProcessor {

	protected final App    			 app;
	protected final FormatManager	 formatManager;
	protected final Action 			 action;
	protected final View   		     view;
	protected final ResponseFormat[] annotatedFormats;
	protected final ResponseFormat[] supportedFormats;
    protected final FormattingResultInterceptor[] interceptors;

	public FormattingResultProcessor(App app, RouteBuilder route) {
		this.app              = app;
		this.formatManager    = app.factory().getBean(FormatManager.class);
		this.action           = route.getAction();
		this.view             = null != route.getDefaultView() ? route.getDefaultView() : null;
		this.annotatedFormats = getAnnotatedFormats(); 
		this.supportedFormats = getSupportedFormats();
        this.interceptors     = app.factory().getBeans(FormattingResultInterceptor.class).toArray(new FormattingResultInterceptor[0]);
	}
	
	@Override
    public void processReturnValue(ActionContext context, Object returnValue, Result result) throws Throwable {
        if(returnValue instanceof ResponseEntity) {
            ResponseEntity re = (ResponseEntity)returnValue;

            result.setStatus(re.getStatus().value());
            returnValue = re.getEntity();

            re.getHeaders().forEach(context.getResponse()::addHeader);

            if(null == returnValue) {
                return;
            }
        }

        if(interceptors.length > 0) {
            Out<Object> out = new Out<>();
            out.set(returnValue);

            for (FormattingResultInterceptor interceptor : interceptors) {
                if (State.isIntercepted(interceptor.preProcessReturnValue(context, result, out))) {
                    return;
                }
            }

            returnValue = out.getValue();
        }

        doProcessReturnValue(context, returnValue, result);
    }

    protected void doProcessReturnValue(ActionContext context, Object returnValue, Result result) throws Throwable {
        result.setReturnValue(returnValue);

        if(returnValue instanceof Renderable) {
            ((Renderable) returnValue).render(context.getRequest(), context.getResponse());
            return;
        }

        ResponseFormat format = resolveResponseFormat(context);
        if(null == format){
            format = formatManager.getDefaultResponseFormat();
        }

        if(null == format && null != view){
            result.render(view);
            return;
        }

        result.render(format.getContent(context,returnValue));
    }

	protected boolean hasAnnotatedFormats() {
		return null != annotatedFormats && annotatedFormats.length > 0;
	}

    protected ResponseFormat resolveResponseFormatOrDefault(ActionContext context) throws Throwable {
        ResponseFormat f = resolveResponseFormat(context);
        if(f == null) {
            f = formatManager.getDefaultResponseFormat();
        }
        return f;
    }

	protected ResponseFormat resolveResponseFormat(ActionContext context) throws Throwable {
		
		//Resolve from request
		ResponseFormat format = context.getResponseFormat();
		if(null == format){
			format = formatManager.resolveResponseFormat(context.getRequest());
		}
		
		//Check is acceptable format
		if(null != format && null != annotatedFormats){
			if(!Arrays2.contains(annotatedFormats, format)){
				throw new FormatNotAcceptableException("The response format '" + format.getName() + "' not acceptable by action '" + action + "'");
			}
			return format;
		}
		
		//Select an annotated format.
		if(null == format && null != annotatedFormats){
			format = selectAnnotatedFormat(context);
		}

		//No format found, return null
		return format;
	}
	
	protected ResponseFormat selectAnnotatedFormat(ActionContext context) throws Throwable {
		if(hasAnnotatedFormats()){
			ResponseFormat fmt = formatManager.selectResponseFormat(context.getRequest(), annotatedFormats);
			if(null == fmt){
				fmt = annotatedFormats[0];
			}
			return fmt;
		}else{
			return formatManager.selectResponseFormat(context.getRequest(), supportedFormats);
		}
	}
	
	protected ResponseFormat[] getAnnotatedFormats() {
		Produces produces = action.searchAnnotation(Produces.class);
		if(null != produces){
			ResponseFormat[] formats = new ResponseFormat[produces.value().length];
			
			for(int i=0;i<formats.length;i++){
				formats[i] = formatManager.getResponseFormat(produces.value()[i]);
			}
			
			return formats;
		}
		return null;
	}
	
	protected ResponseFormat[] getSupportedFormats() {
		List<ResponseFormat> supportedFormats = new ArrayList<>();
		
		for(ResponseFormat fmt : app.factory().getBeans(ResponseFormat.class)){
			if(fmt.supports(action)){
				supportedFormats.add(fmt);
			}
		}
		
		return supportedFormats.toArray(new ResponseFormat[]{});
	}
}