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
package leap.htpl;

import leap.htpl.escaping.EscapeType;
import leap.lang.expression.Expression;

public interface HtplCompiler {

	/**
	 * Adds start element html '&lt;prefix:name'.
	 */
	HtplCompiler startElement(String prefix, String name);
	
	/**
	 * Adds self closing element html '/&gt;'. 
	 */
	HtplCompiler closeElement();
	
	/**
	 * Adds close element html '/prefix:name&gt;'.
	 */
	HtplCompiler closeElement(String prefix, String name);
	
	/**
	 * Adds an attribute.
	 */
	HtplCompiler attribute(String prefix, String name, String value, Character quotedCharacter);

	/**
	 * Adds an attribute with or without condition.
	 */
	HtplCompiler attribute(String prefix, String name, String value, Character quotedCharacter, Expression condition);
	
	/**
	 * Adds an attribute with or withou inline expression..
	 */
	HtplCompiler attribute(String prefix, String name, String value, Character quotedCharacter, boolean inlineExpressions);
	
	/**
	 * Adds an attribute html with or without inline expressions and condition.
	 */
	HtplCompiler attribute(String prefix, String name, String value, Character quotedCharacter, boolean inlineExpressions, Expression condition);
	
	/**
	 * Adds an attribute with an expression value.
	 */
	HtplCompiler attribute(String prefix, String name, Expression value, Character quotedCharacter, boolean inlineExpressions);
	
	/**
	 * Adds an attribute with an expression value and with or without condition.
	 */
	HtplCompiler attribute(String prefix, String name, Expression value, Character quotedCharacter, boolean inlineExpressions, Expression condition);
	
	/**
	 * Adds a text content without inline expressions.
	 */
	HtplCompiler text(CharSequence text);
	
	/**
	 * Adds a text content with or without inline expressions.
	 */
	HtplCompiler text(CharSequence text,boolean inlineExpression);
	
	/**
	 * Adds a javascript content with or without inline expressions.
	 */
	HtplCompiler javascript(CharSequence script,boolean inlineExpressions);
	
	/**
	 * Adds a html content without inline expressions.
	 */
	HtplCompiler html(CharSequence html);
	
	/**
	 * Adds a html content with or without inline expressions.
	 */
	HtplCompiler html(CharSequence html,boolean inlineExpression);
	
	/**
	 * Adds a expression with or without escaping.
	 */
	HtplCompiler expr(Expression expr,EscapeType escapeMode);
	
	/**
	 * Adds a renderable object.
	 */
	HtplCompiler renderable(HtplRenderable renderable);

	/**
	 * Creates a new compiler.
	 */
	HtplCompiler newCompiler();
	
	/**
	 * Compiles all the content and returns the compiled renderable object for rendering.
	 */
	HtplRenderable compile();
}