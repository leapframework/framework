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
package leap.htpl.processor.html;

import java.util.Set;

import leap.core.el.EL;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.ast.Attr;
import leap.htpl.ast.Element;
import leap.htpl.ast.Node;
import leap.htpl.processor.AbstractAttrProcessor;
import leap.lang.New;
import leap.lang.Strings;

/**
 * A processor for processing html minimized attributes.
 */
public class MinimizedAttrProcessor extends AbstractAttrProcessor {
	
	private static final Set<String> minAttrs;
	
	static {
		minAttrs = New.hashSet("compact","checked","declare","readonly","disabled","selected",
							   "defer","ismap","nohref","noshade","nowrap","multiple","noresize");
	}

	@Override
    public boolean required() {
		return false;
	}

	@Override
	public boolean supports(Element e, Attr a) {
		return minAttrs.contains(a.getLocalName());
	}

	@Override
	public Node processStartElement(HtplEngine engine, HtplDocument doc, Element e, Attr a) throws Throwable {
		if(EL.hasPrefixAndSuffix(a.getString())){
			a.setCondition(EL.createExpression(engine.getExpressionManager().getExpressionLanguage(), a.getString()));
			a.setValue(a.getLocalName());
		}else if(Strings.isEmpty(a.getString())){
			a.setValue(a.getLocalName());
		}
		return e;
	}
}
