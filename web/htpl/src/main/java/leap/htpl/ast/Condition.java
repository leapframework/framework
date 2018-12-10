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

import java.util.List;
import java.util.function.Function;

import leap.htpl.HtplCompiler;
import leap.htpl.HtplContext;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;

public class Condition extends NodeContainer {
	
	protected Function<HtplContext,Boolean> func;
	
	public Condition(List<Node> childNodes,Function<HtplContext,Boolean> condition) {
	    super(childNodes);
	    this.func = condition;
    }

	public Condition(Node childNode,Function<HtplContext,Boolean> condition) {
	    super(childNode);
	    this.func = condition;
    }

	/*
	@Override
    protected void executeRender(HtplTemplate tpl, HtplContext context, HtplWriter writer) throws IOException {
		if(func.apply(context)){
			super.executeRender(tpl, context, writer);
		}
	}
	*/

	@Override
	public void compile(HtplEngine engine, HtplDocument doc, HtplCompiler compiler) {
	    compiler.conditional(func, (nestedCompiler) -> {
	        super.compile(engine, doc, nestedCompiler);
        });
	}

	@Override
    protected Node doDeepClone(Node parent) {
		return new Condition(deepCloneChildNodes(),func);
    }
}
