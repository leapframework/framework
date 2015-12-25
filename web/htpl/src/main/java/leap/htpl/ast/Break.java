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

import leap.htpl.HtplCompiler;
import leap.htpl.HtplContext;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.HtplRenderable;
import leap.htpl.HtplTemplate;
import leap.htpl.HtplWriter;
import leap.lang.expression.Expression;

public class Break extends Node implements HtplRenderable {
	
	private final Expression condition;
	
	public Break() {
	    super();
	    this.condition = null;
    }
	
	public Break(Expression condition) {
	    super();
	    this.condition = condition;
    }

	@Override
	public void compile(HtplEngine engine, HtplDocument doc, HtplCompiler compiler) {
		compiler.renderable(this);
		this.compiled = this;
	}

	@Override
	protected Node doDeepClone(Node parent) {
		return new Break(condition);
	}

	@Override
	protected void doWriteTemplate(Appendable out) throws IOException {
		if(condition != null){
			out.append("<!--#break(" + condition.toString() + ")-->");
		}else{
			out.append("<!--#break-->");	
		}
	}

	@Override
    public void render(HtplTemplate tpl, HtplContext context, HtplWriter writer) throws IOException, IllegalStateException {
		if(null != condition){
			if(!context.evalBoolean(condition)){
				return;
			}
		}
		
		throw BreakException.e;
	}
	
	public static final class BreakException extends RuntimeException {
		private static final long serialVersionUID = -2793296415632885048L;
		
		protected static BreakException e = new BreakException();
		
		protected BreakException() {
			
		}
	}
}
