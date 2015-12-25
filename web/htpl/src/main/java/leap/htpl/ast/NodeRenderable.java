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

import leap.htpl.HtplCompiler;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.HtplRenderable;

public abstract class NodeRenderable implements HtplRenderable {

	protected final Node 		   node;
	protected final HtplRenderable renderable;

	public NodeRenderable(HtplEngine engine, HtplDocument doc, Node node) {
		this.node = node;
		this.renderable = compileNode(engine, doc, node);
	}
	
	protected HtplRenderable compileNode(HtplEngine engine,HtplDocument doc, Node node) {
		HtplCompiler compiler = engine.createCompiler();
		node.compile(engine, doc, compiler);
		return compiler.compile();
	}

}