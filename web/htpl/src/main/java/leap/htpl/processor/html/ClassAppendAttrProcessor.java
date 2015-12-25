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

import java.util.Map;

import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.ast.Attr;
import leap.htpl.ast.Element;
import leap.htpl.ast.Node;
import leap.htpl.processor.AbstractNamedAttrProcessor;
import leap.lang.Strings;
import leap.lang.expression.AbstractExpression;
import leap.lang.expression.Expression;

public class ClassAppendAttrProcessor extends AbstractNamedAttrProcessor {
	
	public static final String ATTR_NAME = "class-append";

	public ClassAppendAttrProcessor() {
	    super(ATTR_NAME);
    }

	@Override
	public Node processStartElement(HtplEngine engine, HtplDocument doc, Element e, Attr attr) {
		//class-append="expression"
		
		e.removeAttribute(attr);

		Expression classAppendExpression = engine.getExpressionManager().parseAttributeExpression(engine, attr.getString());
		String     classAttributeValue   = e.getAttributeValue("class");
		
		if(Strings.isEmpty(classAttributeValue)) {
			e.setAttribute("class",classAppendExpression);
		}else{
			e.setAttribute("class",new ClassValueExpression(classAttributeValue, classAppendExpression));
		}
		
		return e;
	}

	public static final class ClassValueExpression extends AbstractExpression {
		
		private final String     classAttributeValue;
		private final Expression classAppendExpression;
		
		public ClassValueExpression(String classAttributeValue,Expression classAppendExpression) {
			this.classAttributeValue   = classAttributeValue;
			this.classAppendExpression = classAppendExpression;
		}

		@Override
        protected Object eval(Object context, Map<String, Object> vars) {
			String append = classAppendExpression.getValue(String.class, context, vars);
			if(Strings.isEmpty(append)){
				return classAttributeValue;
			}else{
				return classAttributeValue  + " " + append;
			}
        }
	}
}
