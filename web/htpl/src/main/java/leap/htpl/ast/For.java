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
import java.util.Map;

import leap.htpl.HtplCompiler;
import leap.htpl.HtplConstants;
import leap.htpl.HtplContext;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.HtplRenderable;
import leap.htpl.HtplTemplate;
import leap.htpl.HtplWriter;
import leap.htpl.value.LoopVariable;
import leap.lang.Args;
import leap.lang.Enumerable;
import leap.lang.Enumerables;
import leap.lang.expression.Expression;

public class For extends NodeContainer implements HtplRenderable {
	
	protected final String	   name;
	protected final Expression collection;
	protected final Integer    max;

	private Empty		   emptyBody;
	private HtplRenderable compiledEmptyBody;
	private HtplRenderable compiledChildNodes;
	
	public For(String name, Expression expression) {
		this(name,expression,null,null);
    }
	
	public For(String name, Integer max) {
		this(name, null, max, null);
    }
	
	public For(String name, Expression collection, Integer max) {
		this(name,collection,max,null);
	}
		
	public For(String name, Expression collection, Integer max, Node body) {
		Args.notEmpty(name,"name");
		Args.assertTrue(collection != null || max != null,"collection or max must be exists");	    
	    
	    this.name       = name;
	    this.collection = collection;
	    this.max        = max;
	    
	    if(null != body){
	    	super.addChildNode(body);
	    }
    }

	public String getName() {
		return name;
	}

	public Expression getCollection() {
		return collection;
	}
	
	public Empty getEmptyBody() {
		return emptyBody;
	}

	public void setEmptyBody(Empty emptyBody) {
		this.emptyBody = emptyBody;
	}
	
	@Override
    protected Node doProcess(HtplEngine engine, HtplDocument doc, ProcessCallback callback) throws Throwable {
		if(childNodes.size() > 0){
			Node first = childNodes.get(0);
			if(first instanceof Text){
				((Text) first).removeBlankLineFirst();
			}
		}
		return super.doProcess(engine, doc, callback);
    }

	@Override
    public void compile(HtplEngine engine, HtplDocument doc, HtplCompiler compiler) {
		HtplCompiler childNodesCompiler = compiler.newCompiler();
		compileChildNodes(engine, doc, childNodesCompiler);
		this.compiledChildNodes = childNodesCompiler.compile();
		
		if(null != emptyBody){
			HtplCompiler emptyBodyCompiler = compiler.newCompiler();
			emptyBody.compile(engine, doc, emptyBodyCompiler);
			this.compiledEmptyBody = emptyBodyCompiler.compile();
		}
		
		compiler.renderable(this);
		this.compiled = this;
	}
	
	@Override
    public void render(HtplTemplate tpl, HtplContext context, HtplWriter writer) throws IOException {
		if(null != max) {
			doRenderInteger(tpl, context, writer, max);
			return;
		}
		
		Object col = context.eval(collection);
		
		if(null == col){
			doRenderEmpty(tpl,context,writer);
			return;
		}
		
		if(col instanceof Integer) {
			doRenderInteger(tpl, context, writer, (Integer)col);
			return;	
		}

		doRenderEnumerable(tpl, context, writer, Enumerables.of(col));
    }
	
	protected void doRenderInteger(HtplTemplate tpl, HtplContext context, HtplWriter writer, Integer max) throws IOException {
		int count = max;
		
		LoopVariable loopVariable = new LoopVariable(count);
		
		Map<String, Object> variables = context.pushLocalVariables();
		try{
			variables.put(HtplConstants.LOOP_VARIABLE, loopVariable);
			
			for(int i=1;i<=count;i++){
				loopVariable.setIndex(i);
				loopVariable.setItem(i);
				
				variables.put(name, i);
				
				compiledChildNodes.render(tpl, context, writer);
			}
		}catch(Break.BreakException e){
			//broken,do nothing.
		}finally{
			context.popLocalVariables();
		}
	}
	
	protected void doRenderEnumerable(HtplTemplate tpl, HtplContext context, HtplWriter writer, Enumerable<?> enumerable) throws IOException {
		if(enumerable.isEmpty()){
			doRenderEmpty(tpl, context, writer);
			return;
		}
		
		int i=1;
		
		LoopVariable loopVariable = new LoopVariable(enumerable.size());

		Map<String, Object> variables = context.pushLocalVariables();
		try{
			variables.put(HtplConstants.LOOP_VARIABLE, loopVariable);
			
			for(Object item : enumerable){
				loopVariable.setIndex(i);
				loopVariable.setItem(item);
				
				variables.put(name, item);
				
				compiledChildNodes.render(tpl, context, writer);

				i++;
			}
		}catch(Break.BreakException e){
			//broken,do nothing.
		}finally{
			context.popLocalVariables();
		}
	}
	
	protected void doRenderEmpty(HtplTemplate tpl, HtplContext context, HtplWriter writer) throws IOException  {
		if(null != compiledEmptyBody){
			compiledEmptyBody.render(tpl, context, writer);
		}
	}

	@Override
    protected Node doDeepClone(Node parent) {
	    For node = new For(name,collection,max);
	    if(null != emptyBody){
	    	this.setEmptyBody((Empty)emptyBody.deepClone(this));
	    }
	    node.addChildNodes(super.deepCloneChildNodes());
	    return node;
    }

    @Override
    protected void doWriteTemplate(Appendable out) throws IOException {
        out.append("<!--#for(").append(name).append(" : ").append(collection.toString()).append(")-->");
        super.doWriteTemplate(out);
        out.append("<!--#endfor-->");
    }
	
	
}