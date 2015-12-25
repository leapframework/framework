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
package leap.htpl.ast;

import java.util.ArrayList;
import java.util.List;

import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;

public class IfParent extends NodeContainer {
	
	public IfParent() {
	    super();
    }

	public IfParent(List<Node> childNodes) {
	    super(childNodes);
    }

	public IfParent(Node... childNodes) {
	    super(childNodes);
    }
	
	@Override
    protected Node doProcess(HtplEngine engine, HtplDocument doc, ProcessCallback callback) throws Throwable {
		List<IfCondition> conditions = new ArrayList<>();
		
		Else elseNode = null;
		
		for(Node child : childNodes){
			
			if(child instanceof IfCondition){
				conditions.add((IfCondition)child);
				continue;
			}
			
			if(child instanceof Else){
				elseNode = (Else)child;
				break;
			}
		}
		
		If ifNode = new If(conditions);
		ifNode.setElseBody(elseNode);
		ifNode.process(engine, doc, callback);
		
		return ifNode;
	}

	@Override
	protected Node doDeepClone(Node parent) {
		return new IfParent(super.deepCloneChildNodes());
	}

}