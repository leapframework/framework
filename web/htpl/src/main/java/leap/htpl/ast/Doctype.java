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

import leap.htpl.HtplCompiler;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;

public class Doctype extends Node {
	
	private String  declaration;
	private boolean inlineExpressions;
	
	public Doctype(String declaration){
		this(declaration,true);
	}

	public Doctype(String declaration,boolean inlineExpressions) {
		this.declaration = declaration;
		this.inlineExpressions = inlineExpressions;
    }

	public String getDeclaration() {
		return declaration;
	}

	public void setDeclaration(String declaration) {
		checkLocked();
		this.declaration = declaration;
	}

	public boolean isInlineExpressions() {
		return inlineExpressions;
	}

	public void setInlineExpressions(boolean inlineExpressions) {
		checkLocked();
		this.inlineExpressions = inlineExpressions;
	}

	@Override
    public void compile(HtplEngine engine, HtplDocument doc, HtplCompiler compiler) {
		compiler.html(declaration, inlineExpressions);
	}
	
	@Override
    protected void doWriteTemplate(Appendable out) throws IOException {
		out.append(declaration);
    }

	@Override
    protected Node doDeepClone(Node parent) {
	    return new Doctype(declaration);
    }
}