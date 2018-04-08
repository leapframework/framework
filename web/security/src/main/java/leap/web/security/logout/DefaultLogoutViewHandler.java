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
package leap.web.security.logout;

import static leap.web.Renderable.FORWARD_PREFIX;
import static leap.web.Renderable.REDIRECT_PREFIX;
import static leap.web.Renderable.VIEW_PREFIX;
import leap.lang.Strings;
import leap.lang.http.Headers;
import leap.lang.net.Urls;
import leap.web.Request;
import leap.web.Response;

public class DefaultLogoutViewHandler implements LogoutViewHandler {
    
    @Override
    public void handleLogoutSuccess(Request request, Response response, LogoutContext context) throws Throwable {
        gotoLocation(request, response, getReturnUrl(context, request));        
    }
	
	protected void gotoLocation(Request request,Response response,String location) throws Throwable {
    	
    	// disable cache
		location = Urls.appendQueryParams(location,"_t_",System.currentTimeMillis()+"");
		
		if(location.startsWith(FORWARD_PREFIX)){
			request.forward(location.substring(FORWARD_PREFIX.length()));
			return;
		}
		
		if(location.startsWith(VIEW_PREFIX)){
			request.forwardToView(location.substring(VIEW_PREFIX.length()));
			return;
		}
		
		if(location.startsWith(REDIRECT_PREFIX)){
			response.sendRedirect(location.substring(REDIRECT_PREFIX.length()));
			return;
		}
		
		response.sendRedirect(location);
	}
	
	protected String getReturnUrl(LogoutContext context, Request request){
        String returnUrl = context.getReturnUrl() ;
        
        if(null == returnUrl){
        	returnUrl = request.getParameter(context.getSecurityConfig().getReturnUrlParameterName());
        }
        
        if(Strings.isEmpty(returnUrl)){
        	returnUrl = context.getSecurityConfig().getLogoutSuccessUrl();
        }
        
        if(Strings.isEmpty(returnUrl)){
        	returnUrl = Strings.isEmpty(request.getContextPath()) ? "/" : request.getContextPath();
        }
        
        return returnUrl;
	}
}