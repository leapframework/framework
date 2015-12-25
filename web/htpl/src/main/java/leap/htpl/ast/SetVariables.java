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

import java.io.IOException;
import java.util.Map;

import leap.htpl.HtplCompiler;
import leap.htpl.HtplContext;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.HtplRenderable;
import leap.htpl.HtplTemplate;
import leap.htpl.HtplWriter;
import leap.lang.Args;
import leap.lang.expression.Expression;
import leap.lang.value.ImmutableNamedValue;
import leap.lang.value.NamedValue;

public class SetVariables extends NodeContainer implements HtplRenderable {
	
	protected final NamedValue<Expression>[] declaredVariables;
	
	private HtplRenderable compiledChildNodes;
	
	public SetVariables(String name,Expression value) {
		this(new ImmutableNamedValue<>(name, value));
	}
	
	@SuppressWarnings("unchecked")
    public SetVariables(NamedValue<Expression> declaredVariable) {
		this(new NamedValue[]{declaredVariable},(Node)null);
	}
	
    public SetVariables(NamedValue<Expression>[] declaredVariables) {
		this(declaredVariables,(Node)null);
	}

	public SetVariables(NamedValue<Expression>[] declaredVariables,Node body) {
	    super();
	    
	    Args.notEmpty(declaredVariables,"declaredVariables");
	    
	    this.declaredVariables = declaredVariables;
	    
	    if(null != body){
	    	this.addChildNode(body);
	    }
    }

	public NamedValue<Expression>[] getDeclaredVariables() {
		return declaredVariables;
	}

	@Override
    public void compile(HtplEngine engine, HtplDocument doc, HtplCompiler compiler) {
		HtplCompiler childNodesCompiler = compiler.newCompiler();
		compileChildNodes(engine, doc, childNodesCompiler);
		this.compiledChildNodes = childNodesCompiler.compile();
		
		compiler.renderable(this);
		this.compiled = this;
	}
	
	@Override
    public void render(HtplTemplate tpl, HtplContext context, HtplWriter writer) throws IOException, IllegalStateException {
		Map<String, Object> variables = context.pushLocalVariables();
		try{
			for(int i=0;i<declaredVariables.length;i++){
				NamedValue<Expression> declaredVariable = declaredVariables[i];
				
				String 	   name  = declaredVariable.getName();
				Expression value = declaredVariable.getValue();
				
				variables.put(name, context.eval(value));
			}
			compiledChildNodes.render(tpl, context, writer);
		}finally{
			context.popLocalVariables();
		}
	}

	@Override
    protected Node doDeepClone(Node parent) {
	    SetVariables with = new SetVariables(declaredVariables);
	    
	    with.addChildNodes(super.deepCloneChildNodes());
	    
	    return with;
    }
}
