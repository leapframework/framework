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
package leap.htpl.jsp;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.jsp.JspException;

import leap.htpl.HtplContext;
import leap.htpl.HtplTemplate;
import leap.htpl.web.WebHtplContext;
import leap.lang.Classes;
import leap.lang.Strings;
import leap.lang.resource.Resource;
import leap.lang.servlet.ServletResource;
import leap.lang.servlet.Servlets;
import leap.web.Request;
import leap.web.Utils;

/**
 * Renders a template file.
 */
public class IncludeTag extends HtplTagBase {
	
	private static final long serialVersionUID = -1193949369497198927L;
	
	private static final String SERVLET_RESOURCE_VIEW_BASE_CLASS_NAME = "leap.web.view.AbstractServletResourceView";
	
	private static final boolean valid = Classes.isPresent(SERVLET_RESOURCE_VIEW_BASE_CLASS_NAME);
	
	private static final Map<String, HtplTemplate> tempaltes = new ConcurrentHashMap<>();
	
	//Tempalte file's path.
	protected String file;
	
	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}
	
	@Override
    public int doEndTag() throws JspException {
		if(!valid){
			throw new JspException("Htpl inlcude tag is invalid, class '" + SERVLET_RESOURCE_VIEW_BASE_CLASS_NAME + "' must be present");
		}
		
		return doEndTag(resolveTemplate());
    }
	
	protected HtplTemplate resolveTemplate() throws JspException {
		HtplTemplate t = tempaltes.get(file);
		if(null == t){
			synchronized (this) {
				t = tempaltes.get(file);
				
				if(null == t){
					Resource jsp = getCurrentJspResource();
					if(null == jsp){
						throw new JspException("Cannot resolve current jsp resource, check the class '" + 
											   SERVLET_RESOURCE_VIEW_BASE_CLASS_NAME + "'");
					}			

					try {
						Resource r = Strings.startsWith(file, "/") ?
								Utils.getResource(pageContext.getServletContext(), file) :
								jsp.createRelative(file);

						if (null == r || !r.exists()) {
							throw new JspException("Htpl template file '" + file + "' cannot be resolved from jsp '" +
									jsp.getPath() + "'");
						}

						t = engine().createTemplate(r);
					}catch (IOException e) {
						throw new JspException("Err resolve template '" + file + "'", e);
					}
				}
            }
		}
		
		return t;
	}
	
	protected Resource getCurrentJspResource() {
		return (Resource) pageContext.getRequest().getAttribute(leap.web.view.AbstractServletResourceView.VIEW_RESOURCE_ATTRIBUTE);
	}
	
	protected int doEndTag(HtplTemplate t) throws JspException {
		HtplContext context = new WebHtplContext(engine(), t, Request.current());
		t.render(context, pageContext.getOut());
		return EVAL_PAGE;
	}
}
