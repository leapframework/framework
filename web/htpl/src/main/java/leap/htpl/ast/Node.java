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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import leap.htpl.AbstractHtplObject;
import leap.htpl.HtplCompiler;
import leap.htpl.HtplContext;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.HtplRenderable;
import leap.htpl.HtplTemplate;
import leap.htpl.HtplWriter;
import leap.htpl.exception.HtplProcessException;
import leap.lang.Exceptions;

public abstract class Node extends AbstractHtplObject {
	
	public static interface ProcessCallback {
		void preProcess(Node node);
		
		void postProcess(Node node, Node result);
	}
	
    protected Map<String,Object> variables;	//local varibles defined in this node.
    protected Node 				 parent;
    protected HtplRenderable     compiled;
    
	public Node getParent() {
		return parent;
	}
	
	public HtplRenderable getCompiled() {
		return compiled;
	}

	public void setParent(Node parent) {
		checkLocked();
		this.parent = parent;
	}
	
	@SuppressWarnings("unchecked")
    public <T extends Node> T findParent(Class<T> type){
		Node p = parent;
		for(;;){
			if(null == p){
				return null;
			}
			if(type.equals(p.getClass())){
				return (T)p;
			}
			p = p.getParent();
		}
	}
	
	public Node findNode(Predicate<Node> cond) {
		if(cond.test(this)) {
			return this;
		}
		return null;
	}

    /**
     * Returns the variables map or <code>null</code> in this node.
     */
    public Map<String, Object> getVariables(){
    	return variables;
    }
    
    /**
     * Sets a local variable in this node.
     */
    public void setVariable(String name,Object value){
    	checkLocked();
    	if(null == variables){
    		variables = new HashMap<String, Object>();
    	}
    	variables.put(name, value);
    }
    
    /**
     * Sets all the variables in the given map. 
     */
    public void putVariables(Map<String,? extends Object> map){
    	checkLocked();
    	if(null == variables){
    		variables = new HashMap<String, Object>();
    	}
    	variables.putAll(map);
    }
    
    public final Node process(HtplEngine engine, HtplDocument doc, ProcessCallback callback) {
    	checkLocked();
    	
    	//Sets empty variables map to null
    	if(null != variables && variables.isEmpty()){
    		variables = null;
    	}
    	
    	try {
    		callback.preProcess(this);
    		
	        Node result = doProcess(engine, doc, callback);
	        
	        callback.postProcess(this, result);
	        
	        return result;
    	} catch (RuntimeException e){
    		throw e;
        } catch (Throwable e) {
        	throw new HtplProcessException("Error processing node," + e.getMessage(), e);
        }
    }
    
	protected Node doProcess(HtplEngine engine,HtplDocument doc, ProcessCallback callback) throws Throwable {
		return this;
	}
	
    public void compileSelf(HtplEngine engine, HtplDocument doc) throws IllegalStateException{
    	if(null != compiled){
    		throw new IllegalStateException("This node aleady compiled");
    	}
    	HtplCompiler newCompiler = engine.createCompiler();
    	this.compile(engine, doc, newCompiler);
    	this.compiled = newCompiler.compile();
    }
    
    /**
     * Compiles this node to {@link HtplRenderable} objects.
     */
    public abstract void compile(HtplEngine engine, HtplDocument doc, HtplCompiler compiler);
    
    public void render(HtplTemplate tpl,HtplContext context,HtplWriter writer) throws IOException,IllegalStateException {
    	if(null == compiled){
    		throw new IllegalStateException("Node [" + this + "] must be compiled for rendering");
    	}
    	this.compiled.render(tpl, context, writer);
    }
    
	/**
	 * Deep clones a new {@link Node} of this node.
	 */
	@SuppressWarnings("unchecked")
    public final <T extends Node> T deepClone(Node parent) {
		Node clone = doDeepClone(parent);
		
		clone.parent = parent;
		
		if(null != this.variables){
			clone.variables = new HashMap<String, Object>(this.variables);
		}
		
		return (T)clone;
	}
	
	/**
	 * Writes this node's template content to the given {@link Appendable} object.
	 */
	public final void writeTemplate(Appendable out) {
		try {
	        doWriteTemplate(out);
        } catch (IOException e) {
        	Exceptions.wrapAndThrow(e);
        }
	}
	
	/**
	 * Returns <code>true</code> if the node is {@link Element} node.
	 */
	public boolean isElement() {
	    return this instanceof Element;
	}
	
    @Override
	protected void doLock(){
		if(null != variables){
			variables = Collections.unmodifiableMap(variables);
		}
	}
	
	@Override
    public String toString() {
		StringBuilder out = new StringBuilder();
		writeTemplate(out);
		return out.toString();
    }
	
	protected abstract Node doDeepClone(Node parent);
	
	protected abstract void doWriteTemplate(Appendable out) throws IOException;
}