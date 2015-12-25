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

import leap.htpl.HtplCompiler;
import leap.htpl.HtplContext;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.HtplRenderable;
import leap.htpl.HtplTemplate;
import leap.htpl.HtplWriter;
import leap.lang.New;

public class Fragment extends NodeContainer implements HtplRenderable {
	
	private final String  name;
	private final boolean render;
	
	private boolean compiling;
	
	public Fragment(String name, boolean render){
		super();
		this.name   = name;
		this.render = render;
	}
	
	public Fragment(String name, boolean render, Node childNode){
		this(name,render, New.arrayList(childNode));
	}

	public Fragment(String name, boolean render, List<Node> childNodes){
		super(childNodes);
		this.name   = name;
		this.render = render;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isCompiled() {
		return null != compiled;
	}
	
	public boolean isCompiling() {
		return compiling;
	}
	
	@Override
    public void compileSelf(HtplEngine engine, HtplDocument doc) throws IllegalStateException {
		if(compiling) {
			return;
		}
		
		compiling = true;
		HtplCompiler compiler = engine.createCompiler();
		this.compileChildNodes(engine, doc, compiler);
		this.compiled = compiler.compile();
		compiling = false;
    }

	@Override
    public void compile(HtplEngine engine, HtplDocument doc, HtplCompiler compiler) {
		this.compileSelf(engine, doc);
		compiler.renderable(this);
	}
	
	@Override
    public void render(HtplTemplate tpl, HtplContext context, HtplWriter writer) throws IOException, IllegalStateException {
	    super.render(tpl, context, writer);
    }

	@Override
    protected Node doProcess(HtplEngine engine, HtplDocument doc, ProcessCallback callback) throws Throwable {
		doc.addFragment(name, this);
		
		if(!render){
			return null;
		}
		
	    return super.doProcess(engine, doc, callback);
    }

	@Override
	protected Node doDeepClone(Node parent) {
		return new Fragment(name,render,deepCloneChildNodes());
	}
}