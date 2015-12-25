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
package leap.web.security.csrf;

import java.util.Map;

import leap.core.annotation.Inject;
import leap.core.web.RequestBase;
import leap.htpl.HtplContext;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.ast.Element;
import leap.htpl.ast.If;
import leap.htpl.ast.IfCondition;
import leap.htpl.interceptor.ElementInterceptor;
import leap.lang.expression.AbstractExpression;
import leap.web.security.SecurityConfig;

public class CsrfFormInterceptor extends ElementInterceptor {

	protected @Inject SecurityConfig config;

	@Override
    protected void preProcessElement(HtplEngine engine, HtplDocument document, Element e) throws Throwable {
		final String param = config.getCsrfParameterName();
		
		if(e.getLocalName().equalsIgnoreCase("form")) {
			if(null != e.findElement((e1) -> e1.getLocalName().equalsIgnoreCase("input") && param.equals(e1.getAttributeValue("name")))){
				return;
			}
			
			IfCondition cond = new IfCondition("csrf_token", new AbstractExpression() {
				@Override
				protected Object eval(Object context, Map<String, Object> vars) {
					
					if(config.isCsrfEnabled()) {
						HtplContext hc = (HtplContext)context;
						RequestBase request = hc.getRequest();
						if(null != request) {
							CsrfToken token = CSRF.getGeneratedToken(request);

							if(null != token) {
								hc.setLocalVariable("csrf_token_string", token.getToken());
							}
							
							return true;
						}
					}
					
					return false;
				}
			});
			
			Element csrfElement = new Element(null, "input");
			csrfElement.setAttribute("name", param);
			csrfElement.setAttribute("type", "hidden");
			csrfElement.setAttribute("value","${csrf_token_string}");
			csrfElement.setSelfClosing(true);
			
			cond.addChildNode(csrfElement);
			
			e.childNodes().add(0,new If(cond));
		}
	}
}