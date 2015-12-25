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
package leap.htpl.ast;

import java.io.IOException;
import java.util.List;

import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.lang.expression.Expression;

public class IfCondition extends NodeContainer {
	
	protected final String     text;
	protected final Expression expression;
	
	public IfCondition(String text,Expression expression) {
	    super();
	    this.text = text;
	    this.expression = expression;
    }

	public IfCondition(String text,Expression expression,List<Node> childNodes) {
	    super(childNodes);
	    this.text = text;
	    this.expression = expression;
    }
	
	public IfCondition(String text,Expression expression,Node body) {
	    super(body);
	    this.text = text;
	    this.expression = expression;
    }

	public String getText() {
		return text;
	}

	public Expression getExpression() {
		return expression;
	}
	
	@Override
    protected Node doProcess(HtplEngine engine, HtplDocument doc, ProcessCallback callback) throws Throwable {
		//removes withspaces at first
		if(childNodes.size() > 0){
			Node first = childNodes.get(0);
			if(first instanceof Text){
				((Text) first).removeBlankLineFirst();
			}
		}
		return super.doProcess(engine, doc, callback);
    }

	@Override
	protected Node doDeepClone(Node parent) {
		return new IfCondition(text,expression,deepCloneChildNodes());
	}

	@Override
    protected void doWriteTemplate(Appendable out) throws IOException {
	    out.append(text);
    }
}