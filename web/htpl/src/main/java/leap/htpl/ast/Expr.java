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
import leap.htpl.escaping.EscapeType;
import leap.lang.Args;
import leap.lang.expression.Expression;

/**
 * An expression node.
 */
public class Expr extends Node{
	
	public static Expr text(String text, Expression expression){
		return new Expr(text, expression, EscapeType.HTML);
	}
	
	public static Expr html(String text, Expression expression){
		return new Expr(text, expression, EscapeType.NONE);
	}
	
	protected final EscapeType escapeMode;
	protected final String	       text;
	protected final Expression 	   expression;
	
	public Expr(String text, Expression expression,EscapeType escapeMode){
		Args.notEmpty(text,"text");
		Args.notNull(expression,"expression");
		Args.notNull(escapeMode,"escapeMode");
		this.text	    = text;
		this.expression = expression;
		this.escapeMode = escapeMode;
	}
	
	public Expression getExpression() {
		return expression;
	}

	@Override
    public void compile(HtplEngine engine, HtplDocument doc, HtplCompiler compiler) {
		compiler.expr(expression,escapeMode);
    }

	@Override
	protected Node doDeepClone(Node parent) {
		return new Expr(text, expression, escapeMode);
	}

	@Override
	protected void doWriteTemplate(Appendable out) throws IOException {
		out.append(text);
	}
}
