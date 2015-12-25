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
package leap.htpl.processor.form;

import java.util.Set;

import leap.htpl.HtplConstants;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.ast.Attr;
import leap.htpl.ast.Element;
import leap.htpl.ast.Node;
import leap.htpl.processor.core.AbstractFieldAttrProcessor;
import leap.htpl.processor.core.ModelAttrProcessor;
import leap.lang.New;
import leap.lang.Strings;
import leap.lang.expression.Expression;

public class InputFieldAttrProcessor extends AbstractFieldAttrProcessor {
	
	public static final String NAME_ATTRIBUTE  = "name";
	public static final String VALUE_ATTRIBUTE = "value";

    public static final String[] GENERAL_FIELD_INPUT_TYPES = new String[]{
    	"text","hidden",
    	//HTMLT-specific input types
    	"datetime","datetime-local","date","month","time","week",
    	"number","range",
    	"email","url","search","tel","color"
    };
    
    private static final Set<String> types = New.hashSet(GENERAL_FIELD_INPUT_TYPES);

	@Override
	protected boolean checkFieldSupports(Element e, Attr a) {
		if(e.isElement(HtplConstants.INPUT_ELEMENT)){
			String type = e.getAttributeValue(HtplConstants.TYPE_ATTRIBUTE);
			return Strings.isEmpty(type) || types.contains(type.toLowerCase());
		}
		return false;
	}
    
	@Override
	public Node processStartElement(HtplEngine engine, HtplDocument doc, Element e, Attr attr) {
		//Sets the 'value' attribute in the given 'input' element.
		//The value of the 'value' attribute must be an field name in the form model.
		String     modelName  = ModelAttrProcessor.getModelName(doc);
		String     fieldName  = attr.getString();
		Expression fieldValue = engine.getExpressionManager().getExpressionLanguage().createExpression(modelName + "." + fieldName);
		
		e.removeAttribute(attr);
		e.setAttribute(NAME_ATTRIBUTE,fieldName);
		e.setAttribute(VALUE_ATTRIBUTE,fieldValue);
		return e;
	}
}