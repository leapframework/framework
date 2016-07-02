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
package leap.lang.el.spel.ast;

import leap.lang.el.ElEvalContext;


public abstract class AstNode {
	
	protected Class<?> resultType;
	
	public Class<?> getResultType() {
		return resultType;
	}
	
	public boolean isLiteral() {
		return false;
	}

	/**
	 * Accepts the visitor.
	 */
	public final void accept(AstVisitor visitor) {
		if (visitor == null) {
			throw new IllegalArgumentException();
		}
		visitor.preVisit(this);
		doAccept(visitor);
		visitor.postVisit(this);
	}
	
	/**
	 * Evaluates this node and returns the result value.
	 */
	public abstract Object eval(ElEvalContext context);
	
	public abstract void toString(StringBuilder out);
	
	protected abstract void doAccept(AstVisitor visitor);
	
	protected final void acceptChilds(AstVisitor visitor, AstNode[] childs) {
		for (AstNode child : childs){
			acceptChild(visitor, child);
		}
	}

	protected final void acceptChild(AstVisitor visitor, AstNode child) {
		if (child == null) {
			return;
		}
		child.accept(visitor);
	}

	public String toString() {
		StringBuilder out = new StringBuilder();
		toString(out);
		return out.toString();
	}
}
