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
package leap.htpl.processor.html;

import leap.core.validation.annotations.NotNull;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.ast.Attr;
import leap.htpl.ast.Element;
import leap.htpl.ast.Node;
import leap.htpl.processor.AbstractAnyAttrProcessor;
import leap.lang.Strings;
import leap.lang.expression.Expression;

public class AnyAttrProcessor extends AbstractAnyAttrProcessor {
	
	private static final String ATTR_PREFIX = "attr-";
	private static final String IF_SUFFIX   = "-if";
	
	protected @NotNull UrlAttrProcessor       urlProcessor 	     = new UrlAttrProcessor();
	protected @NotNull MinimizedAttrProcessor minimizedProcessor = new MinimizedAttrProcessor();
	
	public AnyAttrProcessor() {
	    super();
    }

	public void setUrlProcessor(UrlAttrProcessor urlProcessor) {
		this.urlProcessor = urlProcessor;
	}

	@Override
    public Node processStartElement(HtplEngine engine, HtplDocument doc, Element e, Attr a) throws Throwable {
		//remove the prefix 'attr-' if the attribute's local name like 'attr-{name}' 
		if(Strings.startsWith(a.getLocalName(), ATTR_PREFIX, true)){
			a.setLocalName(a.getLocalName().substring(ATTR_PREFIX.length()));
			a.setOriginLocalName(a.getOriginLocalName().substring(ATTR_PREFIX.length()));
		}
		
		if(Strings.endsWith(a.getLocalName(), IF_SUFFIX)){
			Expression condition = engine.getExpressionManager().parseExpression(engine, a.getString());
			
			String attrName = a.getLocalName().substring(0,a.getLocalName().length() - IF_SUFFIX.length());
			String originAttrName = a.getOriginLocalName().substring(0,a.getLocalName().length() - IF_SUFFIX.length());
			Attr realAttr = e.getAttribute(attrName); 
			if(null == realAttr){
				a.setCondition(condition);
				a.setLocalName(attrName);
				a.setOriginLocalName(originAttrName);

				if(minimizedProcessor.supports(e, a)){
					a.setValue(a.getLocalName());
				}else{
					a.setValue("");
				}
				
				acceptAttr(engine, doc, e, a);
			}else{
				e.removeAttribute(a);
				realAttr.setCondition(condition);
			}
		}else{
			if(minimizedProcessor.supports(e, a)){
				minimizedProcessor.processStartElement(engine, doc, e, a);
			}
			acceptAttr(engine, doc, e, a);
		}
		
		return e;
    }

    protected void acceptAttr(HtplEngine engine, HtplDocument doc, Element e, Attr a) throws Throwable{
		if(urlProcessor.supports(e, a)){
			urlProcessor.processStartElement(engine, doc, e, a);
		}
		acceptAttrWithProcessor(e, a);
    }
}