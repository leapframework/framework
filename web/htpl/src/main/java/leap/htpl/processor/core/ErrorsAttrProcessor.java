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

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import leap.core.validation.Errors;
import leap.htpl.HtplContext;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.ast.Attr;
import leap.htpl.ast.Condition;
import leap.htpl.ast.Element;
import leap.htpl.ast.Expr;
import leap.htpl.ast.Node;
import leap.htpl.processor.AbstractNamedAttrProcessor;
import leap.lang.Strings;
import leap.lang.expression.AbstractExpression;

public class ErrorsAttrProcessor extends AbstractNamedAttrProcessor {
	
	public static final String ATTR_NAME = "errors";
	
	public ErrorsAttrProcessor() {
	    super(ATTR_NAME);
    }

    @Override
    public boolean required() {
        return false;
    }

    @Override
	public Node processStartElement(HtplEngine engine, HtplDocument doc, Element e,Attr attr) {
		//errors="objectName"
		
		final String objectName = attr.getString();

		if(Strings.isEmpty(objectName)) {
            e.removeAttribute(attr);
            e.setChildNode(Expr.text("errors", new AbstractExpression() {
                @Override
                protected Object eval(Object context, Map<String, Object> vars) {
                    Errors errors = ((HtplContext)context).getErrors();
                    return errors.first().getMessage();
                }
            }));

            return new Condition(e, context -> {
                Errors errors = context.getErrors();
                return null != errors && !errors.isEmpty();
            });
        }else {
            e.removeAttribute(attr);
            e.setChildNode(Expr.text(objectName, new AbstractExpression() {
                @Override
                protected Object eval(Object context, Map<String, Object> vars) {
                    Errors errors = ((HtplContext)context).getErrors();
                    List<String> messages = errors.getMessages(objectName);
                    return messages.isEmpty() ? "" : messages.get(0);
                }
            }));

            return new Condition(e, context -> {
                Errors errors = context.getErrors();
                return null != errors && errors.contains(objectName);
            });
        }
	}

}