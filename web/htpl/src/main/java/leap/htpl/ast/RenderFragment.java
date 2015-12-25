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
import java.util.Map;

import leap.htpl.HtplCompiler;
import leap.htpl.HtplContext;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.HtplRenderable;
import leap.htpl.HtplTemplate;
import leap.htpl.HtplWriter;
import leap.htpl.exception.HtplDefinitionException;

/**
 * Renders a fragment.
 */
public class RenderFragment extends Node implements HtplRenderable {

	private final String  			  name;
	private final boolean 			  required;
	private final Map<String, String> parameters;
	
	private Fragment fragment;
	
	public RenderFragment(String name){
		this(name,false);
	}
	
	public RenderFragment(String name, boolean required){
		this(name,required,null);
	}
	
	public RenderFragment(String name, boolean required,Map<String, String> parameters){
		super();
		this.name       = name;
		this.required   = required;
		this.parameters = parameters == null ? Collections.emptyMap() : Collections.unmodifiableMap(parameters);
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isRequired() {
		return required;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}
	
	@Override
    public void compile(HtplEngine engine, HtplDocument doc, HtplCompiler compiler) {
		fragment = doc.getFragment(name);
		
		if(null != fragment && !(fragment.isCompiling() || fragment.isCompiled())){
			fragment.compileSelf(engine, doc);
		}
		
		compiler.renderable(this);
    }
	
	@Override
    public void render(HtplTemplate tpl, HtplContext context, HtplWriter writer) throws IOException, IllegalStateException {
		if(null == fragment && required) {
			throw new HtplDefinitionException("Cannot found fragment '" + name + "' in template '" + tpl.getSource() + "'");
		}
		
		if(null != fragment) {
			fragment.render(tpl, context, writer);
		}
    }

	@Override
    protected void doWriteTemplate(Appendable out) throws IOException {
		//TODO : 
		out.append("<!--#render-fragment ").append(name).append(" -->");
    }

	@Override
	protected Node doDeepClone(Node parent) {
		return new RenderFragment(name,required,parameters);
	}
}