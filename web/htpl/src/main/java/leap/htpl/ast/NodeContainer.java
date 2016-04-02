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

import leap.htpl.HtplCompiler;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.ast.Text.Type;
import leap.lang.New;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public abstract class NodeContainer extends Node {

	protected List<Node> childNodes;
	
	public NodeContainer(){
		this(new ArrayList<>());
	}
	
	public NodeContainer(Node... childNodes){
		this(New.arrayList(childNodes));
	}
	
	public NodeContainer(List<Node> childNodes){
		this.setChildNodes(childNodes);
	}
	
	public List<Node> childNodes() {
		return childNodes;
	}
	
	public Node childNode(int index){
		return childNodes.get(index);
	}
	
	@Override
    protected void doLock() {
		super.doLock();
	    this.childNodes = Collections.unmodifiableList(childNodes);
	    
		for(Node node : childNodes){
			node.lock();
		}
    }

	public void addChildNode(Node node){
		node.setParent(this);
		childNodes.add(node);
	}
	
	public void addChildNodes(Collection<Node> nodes){
		if(null != nodes){
			for(Node node : nodes){
				node.setParent(this);
			}
			this.childNodes.addAll(nodes);
		}
	}
	
	public boolean removeChildNode(Node node) {
		return this.childNodes.remove(node);
	}
	
	public Node findNode(Predicate<Node> cond) {
		
		if(cond.test(this)){
			return this;
		}
		
		Node n = null;
		
		for(Node node : childNodes) {
			n = node.findNode(cond);
			if(null != n) {
				return n;
			}
		}
		return null;
	}
	
	public void clearChildNodes(){
		this.childNodes.clear();
	}
	
	public void addChildText(String text){
		addChildText(text,Type.HTML,true);
	}
		
	public void addChildText(String text,Text.Type type, boolean inlineExpression){
		checkLocked();
		if(childNodes.size() > 0){
			Node lastNode = childNodes.get(childNodes.size() - 1);
			if(lastNode instanceof Text){
				Text textNode = (Text)lastNode;
				if(textNode.getType() == type == textNode.isInlineExpression() == inlineExpression){
					((Text)lastNode).append(text);
					return;
				}
			}
		}
		addChildNode(new Text(text,type,inlineExpression));
	}
	
	public void setChildNodes(List<Node> childNodes) {
		checkLocked();
		this.childNodes = childNodes;
		if(null != childNodes){
			for(Node childNode : childNodes){
				childNode.setParent(this);
			}
		}
	}
	
	public void setChildNode(Node node){
		clearChildNodes();
		addChildNode(node);
	}
	
	public List<Node> deepCloneChildNodes(){
		List<Node> clones = new ArrayList<Node>();
		
		for(Node node : childNodes){
			clones.add(node.deepClone(this));
		}
		
		return clones;
	}
	
	@Override
    protected Node doProcess(HtplEngine engine,HtplDocument doc, ProcessCallback callback) throws Throwable {
		List<Node> processedNodes = new ArrayList<>();
		if(null != childNodes){
			for(Node node : childNodes){
				Node processed;
				for(;;){
					processed = node.process(engine, doc, callback);
					if(null == processed){
						break;
					}
					if(processed == node){
						addProcessedNode(processedNodes, processed);
						break;
					}
					node = processed;
				}
			}
		}
		this.childNodes = processedNodes;
		return this;
    }
	
	protected void addProcessedNode(List<Node> nodes,Node node){
		if(!nodes.isEmpty() && node instanceof Text){
			Node lastNode = nodes.get(nodes.size() - 1);
			if(lastNode instanceof Text ){
				((Text) lastNode).append((Text)node);
				return;
			}
		}
		nodes.add(node);	
	}
	
	@Override
    public void compile(HtplEngine engine, HtplDocument doc, HtplCompiler compiler) {
		this.compileChildNodes(engine, doc, compiler);
    }

	protected void compileChildNodes(HtplEngine engine, HtplDocument doc, HtplCompiler compiler) {
		for(Node node : childNodes){
			node.compile(engine, doc, compiler);
		}
	}

	@Override
    protected void doWriteTemplate(Appendable out) throws IOException {
		for(int i=0;i<childNodes.size();i++){
			childNodes.get(i).writeTemplate(out);
		}
    }
}