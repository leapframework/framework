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

import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;

public abstract class NodeWrapper extends Node {
	
	protected Node wrappedNode;
	
	public NodeWrapper() {
	    super();
    }
	
	public NodeWrapper(Node wrappedNode){
		this.setWrappedNode(wrappedNode);
	}

	public Node getWrappedNode() {
		return wrappedNode;
	}

	public void setWrappedNode(Node wrappedNode) {
		checkLocked();
		this.wrappedNode = wrappedNode;
		this.wrappedNode.setParent(this);
	}

	@Override
	protected final Node doDeepClone(Node parent) {
		NodeWrapper clone = executeDeepCloneNodeWrapper(parent);
		clone.wrappedNode = wrappedNode.deepClone(this);
		return clone;
	}
	
	protected abstract NodeWrapper executeDeepCloneNodeWrapper(Node parent);
	
	@Override
    protected Node doProcess(HtplEngine engine,HtplDocument doc, ProcessCallback callback) {
		Node node = wrappedNode.process(engine, doc, callback);
		if(null == node){
			return null;
		}
		
		if(node != wrappedNode){
			wrappedNode = node.process(engine, doc, callback);
		}
		
		return this;
    }

	@Override
	protected void doWriteTemplate(Appendable out) throws IOException {
		wrappedNode.writeTemplate(out);	
	}
}
