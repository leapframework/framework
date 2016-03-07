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
package leap.web.multipart;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import leap.web.App;
import leap.web.AppBootstrap;
import leap.web.AppHandler;
import leap.web.Request;
import leap.web.action.ActionContext;

public class MultipartServlet extends HttpServlet {

	private static final long serialVersionUID = -55301710141460056L;

	private App		   app;
	private AppHandler appHandler;
	
	@Override
    public void init(ServletConfig config) throws ServletException {
		app        = AppBootstrap.getApp(config.getServletContext());
		appHandler = app.factory().getBean(AppHandler.class);
    }

	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if(null == app) {
			app 	   = Request.current().app();
			appHandler = app.factory().getBean(AppHandler.class);
		}
		
		ActionContext ac = MultipartContext.getMultipartAction(req);
		
		if(null == ac){
			throw new IllegalStateException("No multipart action in context");
		}
		
		Request request = Request.current();
		
		try {
	        appHandler.executeAction(request, request.response(), ac);
        } catch (Throwable e) {
        	if(e instanceof RuntimeException) {
        		throw (RuntimeException)e;
        	}else{
        		throw new ServletException("Error handling multi-part request, " + e.getMessage(), e);
        	}
        }
    }
}