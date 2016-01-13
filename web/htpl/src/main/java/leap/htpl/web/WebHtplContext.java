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
package leap.htpl.web;

import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import leap.core.i18n.MessageSource;
import leap.core.web.RequestBase;
import leap.web.assets.AssetSource;
import leap.htpl.AbstractHtplContext;
import leap.htpl.HtplContext;
import leap.htpl.HtplEngine;
import leap.htpl.HtplPage;
import leap.htpl.HtplTemplate;
import leap.web.Request;

public class WebHtplContext extends AbstractHtplContext {
	
	protected final Request  request;
	protected final HtplPage page;
	
	public WebHtplContext(HtplEngine engine, HtplTemplate template, Request request){
		super(engine);
		this.request = request;
		this.page    = template.createPage();
		this.extractVariablesFromTemplate(template);
		this.extractVariablesFromRequest();
		
		request.setAttribute(HtplContext.class.getName(), this);
	}

	@Override
    public String getContextPath() {
        return request.getContextPath();
    }

	@Override
    public RequestBase getRequest() {
        return request;
    }

	@Override
    public Locale getLocale() {
        return request.getLocale();
    }

	@Override
    public MessageSource getMessageSource() {
        return request.getMessageSource();
    }
	
	@Override
    public AssetSource getAssetSource() {
        return request.getAssetSource();
    }
	
	@Override
    public boolean isDebug() {
        return request.isDebug();
    }
	
	private void extractVariablesFromTemplate(HtplTemplate template) {
		variables.put("page",page);
	}

	private void extractVariablesFromRequest(){
		//TODO : hard code built-in variables
		variables.put("param", request.getParameters());
		variables.put("user",  request.getUser());
		
		HttpServletRequest req = request.getServletRequest();
		Enumeration<String> names = req.getAttributeNames();
		while(names.hasMoreElements()){
			String name  = names.nextElement();
			Object value = req.getAttribute(name);
			variables.put(name, value);
		}
	}
}
