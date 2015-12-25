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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import leap.htpl.HtplCompiler;
import leap.htpl.HtplContext;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.HtplRenderable;
import leap.htpl.HtplTemplate;
import leap.htpl.HtplWriter;

public class If extends Node implements HtplRenderable {
	
	private List<IfCondition> conditions;
	private NodeContainer	  elseBody;
	
	public If(IfCondition condition){
		this(new ArrayList<IfCondition>());
		this.conditions.add(condition);
	}
	
	public If(List<IfCondition> conditions) {
	    super();
	    this.conditions = conditions;
	    this.elseBody   = null;
    }
	
	public List<IfCondition> getConditions() {
		return conditions; 
	}

	public void addCondition(IfCondition condition){
		conditions.add(condition);
	}
	
	public NodeContainer getElseBody() {
		return elseBody;
	}

	public void setElseBody(NodeContainer elseBody) {
		this.elseBody = elseBody;
	}
	
	@Override
    public Node findNode(Predicate<Node> cond) {
		if(cond.test(this)) {
			return this;
		}

		Node n = null;
		
		for(IfCondition ifc : conditions) {
			n = ifc.findNode(cond);
			if(n != ifc) {
				return n;
			}
		}
		
		if(null != elseBody) {
			n = elseBody.findNode(cond);
		}
		
		return n;
    }

	@Override
    protected Node doProcess(HtplEngine engine, HtplDocument doc, ProcessCallback callback) throws Throwable {
		for(IfCondition condition : conditions){
			condition.process(engine, doc, callback);
		}
		
		if(null != elseBody){
			elseBody.process(engine, doc, callback);
		}
		
		return super.doProcess(engine, doc, callback);
    }

	@Override
    public void compile(HtplEngine engine, HtplDocument doc, HtplCompiler compiler) {
		for(IfCondition condition : conditions){
			condition.compileSelf(engine, doc);
		}
		
		if(null != elseBody){
			elseBody.compileSelf(engine, doc);
		}
		
		compiler.renderable(this);
	}
	
	@Override
    public void render(HtplTemplate tpl, HtplContext context, HtplWriter writer) throws IOException {
		for(IfCondition condition : conditions){
			if(context.evalBoolean(condition.getExpression())){
				condition.render(tpl, context, writer);
				return;
			}
		}
		
		if(null != elseBody){
			elseBody.render(tpl, context, writer);
		}	    
    }

	@Override
	protected Node doDeepClone(Node parent) {
		If clone = new If(deepCloneIfConditions(parent));
		
		if(null != elseBody){
			clone.setElseBody((NodeContainer)elseBody.deepClone(parent));
		}
		
		return clone;
	}
	
	@Override
	protected void doWriteTemplate(Appendable out) throws IOException {
		IfCondition cond = conditions.get(0);
		
		out.append("<!-- #if (").append(cond.text).append(") -->");
		for(int i=0;i<cond.childNodes().size();i++){
			cond.childNodes().get(i).writeTemplate(out);
		}
		
		for(int i=1;i<conditions.size();i++) {
			cond = conditions.get(i);
			out.append("<!-- #elseif (").append(cond.text).append(") -->");
			for(int j=0;j<cond.childNodes().size();j++){
				cond.childNodes().get(j).writeTemplate(out);
			}
		}
		
		if(null != elseBody) {
			out.append("<!-- #else -->");
			elseBody.writeTemplate(out);
		}
		
		out.append("<!-- #endif -->");
	}
	
	private List<IfCondition> deepCloneIfConditions(Node parent){
		List<IfCondition> clones = new ArrayList<IfCondition>();
		
		for(IfCondition ifCondition : conditions){
			clones.add((IfCondition)ifCondition.deepClone(parent));
		}
		
		return clones;
	}
}