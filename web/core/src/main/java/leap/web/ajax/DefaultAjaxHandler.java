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
package leap.web.ajax;

import leap.core.annotation.Inject;
import leap.lang.Strings;
import leap.lang.http.MimeTypes;
import leap.lang.json.JSON;
import leap.lang.json.JsonWriter;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.Contents;
import leap.web.Request;
import leap.web.Response;
import leap.web.error.ErrorCodes;
import leap.web.json.JsonConfig;
import leap.web.json.Jsonp;

public class DefaultAjaxHandler implements AjaxHandler {
	
	private static final Log log = LogFactory.get(DefaultAjaxHandler.class);
	
	protected @Inject ErrorCodes errorCodes;
	protected @Inject JsonConfig jsonConfig;

	@Override
	public void handleError(Request request, Response response, int status, String message, Throwable exception) {
		
		if(null != exception) {
			log.error("Error handling ajax request '{}', {}", request.getPath(), message, exception);	
		}
		
		response.setStatus(status);
		
		final String errorCode = null != exception ? errorCodes.getErrorCode(exception.getClass()) : null;
		if(null == errorCode) {
			response.setContentType("text/plain;charset=utf-8");
			if(!Strings.isEmpty(message)) {
				response.getWriter().write(message);
			}
		}else{
			String m = errorCodes.getErrorMessage(errorCode, exception, request.getMessageSource(), request.getLocale());
			if(null == m){
				m = message;
			}
			
			try {
				response.setContentType(Contents.createContentType(request, MimeTypes.APPLICATION_JSON));
				
	            Jsonp.write(request, response, jsonConfig, (w) -> {
	            	JsonWriter json = JSON.createWriter(response.getWriter());	
	            	
	            	json.startObject()
	            	    .property("code", errorCode)
	            	    .separator()
	            	    .property("msg", message)
	            	    .endObject();
	            	
	            });
            } catch (Throwable e) {
            	log.error("Error writing json error on ajax request '{}', {}", request.getPath(), e.getMessage(), e);
            }
		}
		
		//TODO : error stack trace
	}

}