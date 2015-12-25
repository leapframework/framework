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

import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.ast.Attr;
import leap.htpl.ast.Element;
import leap.htpl.ast.For;
import leap.htpl.ast.Node;
import leap.htpl.exception.HtplDefinitionException;
import leap.htpl.processor.AbstractNamedAttrProcessor;
import leap.lang.Strings;
import leap.lang.expression.Expression;

public class ForAttrProcessor extends AbstractNamedAttrProcessor {

	public static final String ATTR_NAME = "for";
	
	public ForAttrProcessor() {
	    super(ATTR_NAME);
    }
	
	@Override
    public Node processStartElement(HtplEngine engine, HtplDocument doc, Element e, Attr attr) {
		//for="name : expression" || for="name : ${expression}"
		
		String[] parts = Strings.split(attr.getString(), ":");

		if(parts.length != 2){
			throw new HtplDefinitionException("Invalid for expression in element '" + e.getQualifiedName() + "', must be for=\"name : expression \"");
		}
		
		e.removeAttribute(attr);
		
		String variableName     = parts[0];
		String collectionString = parts[1];
		
		if(Strings.isDigits(collectionString)) {
			Integer max = Integer.parseInt(collectionString);
			return new For(variableName, null, max, e);
		}else{
			Expression expression = engine.getExpressionManager().parseExpression(engine, collectionString);
			return new For(variableName, expression, null, e);
		}
    }

}