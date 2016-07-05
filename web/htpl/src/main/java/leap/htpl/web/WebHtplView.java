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
package leap.htpl.web;

import leap.htpl.HtplTemplate;
import leap.htpl.HtplTemplateLazyCreator;
import leap.lang.Args;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;
import leap.web.App;
import leap.web.Request;
import leap.web.Response;
import leap.web.view.AbstractView;
import leap.web.view.ViewData;

public class WebHtplView extends AbstractView {
	
	private static final Log log = LogFactory.get(WebHtplView.class);
	
	protected HtplTemplate template;
	protected HtplTemplateLazyCreator creator;
	
	public WebHtplView(App app, String path, HtplTemplate template) {
		super(app, path);
		Args.notNull(template,"template");
		this.template = template;
	}

	public WebHtplView(App app, String path, HtplTemplateLazyCreator layzCreator) {
		super(app, path);
		Args.notNull(layzCreator,"layzCreator");
		creator = layzCreator;
	}

	@Override
    protected void doRender(Request request, Response response, ViewData data) throws Exception {
		if(template == null){
			template = creator.create();
		}
		WebHtplContext context = new WebHtplContext(template.getEngine(), template, request);
		context.setErrors(request.getValidation().errors());
		
		boolean pjax = request.isPjax();

		if(log.isDebugEnabled()){
			log.debug("Rendering htpl template(pjax={}) : {}",pjax,path);
		}
		
		if(pjax) {
			context.setRenderLayout(false);
		}
		
		template.render(context, response.getWriter());
    }
	
	/*
	protected void includeJsp(Request request,Response response, ServletResource jsp) throws Exception {
		javax.servlet.http.HttpServletRequest  req  = request.getServletRequest();
		javax.servlet.http.HttpServletResponse resp = response.getServletResponse();
		
		try {
	        req.getRequestDispatcher(jsp.getPathWithinContext()).include(req, resp);
        } catch (javax.servlet.ServletException e) {
        	throw new HtplRenderException("Error including jsp '" + jsp.getPathWithinContext() + "', " + e.getMessage(), e);
        }
	}
	*/
	
	@Override
    protected void exposeViewDataAsRequestAttributes(ViewData model, Request request) throws Exception {
		request.setAttribute("session", request.getSession());
		request.setAttribute("params",  request.getParameters());
	    super.exposeViewDataAsRequestAttributes(model, request);
    }

	@Override
    public String toString() {
		return "htpl:" + path;
	}
}
