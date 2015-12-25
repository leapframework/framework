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
package leap.htpl.processor.core;

import leap.core.el.EL;
import leap.core.el.ExpressionLanguage;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.ast.Attr;
import leap.htpl.ast.Element;
import leap.htpl.ast.If;
import leap.htpl.ast.IfCondition;
import leap.htpl.ast.Node;
import leap.htpl.processor.AbstractNamedAttrProcessor;
import leap.lang.expression.Expression;

public class IfAttrProcessor extends AbstractNamedAttrProcessor {
	
	public static final String ATTR_NAME = "if";

	public IfAttrProcessor() {
		super(ATTR_NAME);
    }

	@Override
	public Node processStartElement(HtplEngine engine, HtplDocument doc, Element e, Attr attr) {
		ExpressionLanguage el = engine.getExpressionManager().getExpressionLanguage();
		
		//if="${boolean expression}" if="boolean expression"
		Expression expr = el.createExpression(EL.removePrefixAndSuffix(attr.getString()));

		e.removeAttribute(attr);
		
		return new If(new IfCondition(attr.getString(), expr, e));
	}

}