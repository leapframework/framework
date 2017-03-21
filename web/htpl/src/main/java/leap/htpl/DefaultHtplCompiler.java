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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import leap.htpl.escaping.EscapableExpression;
import leap.htpl.escaping.EscapeType;
import leap.htpl.escaping.HtplEscaper;
import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.expression.Expression;


public class DefaultHtplCompiler implements HtplCompiler {
	
	protected final HtplEngine		 	  engine;
	protected final HtplExpressionManager em;
	protected final List<HtplRenderable>  nodes = new ArrayList<HtplRenderable>();
	protected final HtplEscaper			  htmlEscaper;
	protected final HtplEscaper			  attrEscaper;
	protected final HtplEscaper			  jsEscaper;
	
	protected final ParseHandlerImpl textParseHandler;
	protected final ParseHandlerImpl htmlParseHandler;
	protected final ParseHandlerImpl attrParseHandler;
	protected final ParseHandlerImpl jsParseHandler;

	public DefaultHtplCompiler(HtplEngine engine) {
		this.engine      	  = engine;
		this.em          	  = engine.getExpressionManager();
		this.htmlEscaper 	  = engine.getEscaper(EscapeType.HTML);
		this.attrEscaper	  = engine.getEscaper(EscapeType.ATTR);
		this.jsEscaper        = engine.getEscaper(EscapeType.JS);
		this.textParseHandler = new ParseHandlerImpl(null);
		this.htmlParseHandler = new ParseHandlerImpl(htmlEscaper);
		this.attrParseHandler = new ParseHandlerImpl(attrEscaper);
		this.jsParseHandler   = new ParseHandlerImpl(jsEscaper);
	}

	@Override
    public HtplCompiler startElement(String prefix, String name) {
		return append("<" + qname(prefix,name) + "");	
    }

	@Override
    public HtplCompiler closeElement() {
	    return append("/>");
    }

	@Override
    public HtplCompiler closeElement(String prefix, String name) {
		return append("</" + qname(prefix, name) + ">");
    }
	
	@Override
    public HtplCompiler attribute(String prefix, String name, String value, Character quotedCharacter) {
	    return attribute(prefix, name, value, quotedCharacter, false);
    }
	
	@Override
    public HtplCompiler attribute(String prefix, String name, String value, Character quotedCharacter, Expression condition) {
	    return attribute(prefix,name,value,quotedCharacter,false,condition);
    }

	@Override
    public HtplCompiler attribute(String prefix, String name, String value, Character quotedCharacter, boolean inlineExpressions) {
		compileAttrName(prefix,name,inlineExpressions);

		if(null != quotedCharacter){
			append(String.valueOf(quotedCharacter));
		}
		
		if(inlineExpressions){
			em.parseAttributeExpression(engine, value, attrParseHandler);
		}else{
			append(value,false);
		}
		
		if(null != quotedCharacter){
			append(String.valueOf(quotedCharacter));
		}
		
	    return this;
    }
	
	@Override
    public HtplCompiler attribute(String prefix, String name, String value, Character quotedCharacter, boolean inlineExpressions, Expression condition) {
		if(null == condition){
			return attribute(prefix, name, value, quotedCharacter, inlineExpressions);
		}
		
		append(new ConditionalRenderable(condition, newCompiler().attribute(prefix, name, value, quotedCharacter, inlineExpressions).compile()));
		
		return this;
    }
	
	@Override
    public HtplCompiler attribute(String prefix, String name, Expression value, Character quotedCharacter, boolean inlineExpressions) {
		compileAttrName(prefix,name,inlineExpressions);
		
		if(null != quotedCharacter){
			append(String.valueOf(quotedCharacter));
		}
		
		append(new RenderableExpression(value,attrEscaper));

		if(null != quotedCharacter){
			append(String.valueOf(quotedCharacter));
		}
		
		return this;
    }
	
	@Override
    public HtplCompiler attribute(String prefix, String name, Expression value, Character quotedCharacter,  boolean inlineExpressions, Expression condition) {
		if(null == condition){
			return attribute(prefix, name, value, quotedCharacter,inlineExpressions);
		}
		
		append(new ConditionalRenderable(condition, newCompiler().attribute(prefix, name, value, quotedCharacter,inlineExpressions).compile()));
	    return this;
    }
	
	@Override
    public HtplCompiler text(CharSequence text) {
	    return text(text,false);
    }

	@Override
    public HtplCompiler text(CharSequence text, boolean inlineExpressions) {
		if(inlineExpressions){
			em.parseCompositeExpression(engine, text.toString(), textParseHandler);
		}else{
			append(text,true);
		}
	    return this;
    }
	
	@Override
    public HtplCompiler javascript(CharSequence script, boolean inlineExpressions) {
		if(inlineExpressions){
			em.parseCompositeExpression(engine, script.toString(), jsParseHandler);
		}else{
			append(script,false);
		}
		return this;
    }

	@Override
    public HtplCompiler html(CharSequence html) {
	    return html(html,false);
    }

	@Override
    public HtplCompiler html(CharSequence html, boolean inlineExpressions) {
		//appends html content, don't escape
		if(inlineExpressions){
			em.parseCompositeExpression(engine, html.toString(), htmlParseHandler);
		}else{
			append(html,false);
		}
	    return this;
    }

	@Override
    public HtplCompiler expr(Expression expr, EscapeType escapeMode) {
		append(new RenderableExpression(expr, engine.getEscaper(escapeMode)));
	    return this;
    }

	@Override
    public HtplCompiler renderable(HtplRenderable renderable) {
		append(renderable);
	    return this;
    }
	
	@Override
    public HtplCompiler newCompiler() {
	    return new DefaultHtplCompiler(engine);
    }

	@Override
    public HtplRenderable compile() {
	    return new HtplRenderableContainer(nodes);
    }

    protected void compileAttrName(String prefix, String name, boolean inlineExpressions){
		if(inlineExpressions){
			append(" ");
			em.parseAttributeExpression(engine, name, attrParseHandler);
			append("=");
		}else{
			append(" " + qname(prefix, name) + "=");
		}
	}

	protected String qname(String prefix,String name){
		return Strings.isEmpty(prefix) ? name : prefix + ":" + name;
	}
	
	protected DefaultHtplCompiler append(CharSequence string){
		append(string,false);
		return this;
	}
	
	protected DefaultHtplCompiler append(CharSequence string, boolean htmlEscape){
		HtplRenderable lastNode = nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);
		
		CharSequence content = htmlEscape ?  htmlEscaper.escape(string) : string;
		
		if(lastNode instanceof RenderableText){
			((RenderableText) lastNode).append(content);
		}else{
			nodes.add(new RenderableText(content));
		}
		
		return this;
	}
	
	protected DefaultHtplCompiler append(HtplRenderable node){
		HtplRenderable lastNode = nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);
		
		if(node instanceof RenderableText){
			if(lastNode instanceof RenderableText){
				((RenderableText) lastNode).append((RenderableText)node);
				return this;
			}
		}

		nodes.add(node);
		return this;
	}
	
	protected class ParseHandlerImpl implements HtplExpressionManager.ParseHandler {
		private final HtplEscaper escaper;
		
		public ParseHandlerImpl(HtplEscaper escaper) {
			this.escaper = escaper;
		}

		@Override
        public void textParsed(String text) {
			append(text);
		}

		@Override
        public void exprParsed(Expression expr) {
			append(new RenderableExpression(expr, escaper));
		}
	}
	
	protected static final class RenderableText implements HtplRenderable {
		private final StringBuilder buf = new StringBuilder();
		
		public RenderableText(CharSequence text) {
			buf.append(text);
		}
		
		public RenderableText append(CharSequence text){
			buf.append(text);
			return this;
		}
		
		public RenderableText append(RenderableText node){
			buf.append(node.buf);
			return this;
		}
		
		@Override
        public void render(HtplTemplate tpl, HtplContext context, HtplWriter writer) throws IOException {
			writer.write(buf);
        }

		@Override
        public String toString() {
			return buf.toString();
		}
	}
	
	protected static class ConditionalRenderable implements HtplRenderable {
		
		private final Expression     condition;
		private final HtplRenderable renderable;
		
		public ConditionalRenderable(Expression condition,HtplRenderable renderable) {
			Args.notNull(condition,"condition");
			Args.notNull(renderable,"renderable");
			this.condition  = condition;
			this.renderable = renderable;
		}

		@Override
        public void render(HtplTemplate tpl, HtplContext context, HtplWriter writer) throws IOException {
	        if(context.evalBoolean(condition)){
	        	renderable.render(tpl, context, writer);
	        }
        }
	}
	
	protected static class RenderableExpression implements HtplRenderable {
		private final Expression  expression;
		private final boolean	  autoEscape;
		private final HtplEscaper escaper;
		
		public RenderableExpression(Expression expression,HtplEscaper escaper) {
			Args.notNull(expression,"expression");
			this.expression  = expression;
			this.escaper     = escaper;

			if(expression instanceof EscapableExpression) {
				this.autoEscape = ((EscapableExpression) expression).isAutoEscape();
			}else{
				this.autoEscape = true;
			}
		}

		@Override
        public void render(HtplTemplate tpl, HtplContext context, HtplWriter writer) throws IOException {
			String string = context.evalString(expression);
			
			if(!Strings.isEmpty(string)){
				if(autoEscape && null != escaper){
					escaper.escapeAndAppend(string, writer);
				}else{
					writer.write(string);
				}
			}
        }

		@Override
        public String toString() {
			return expression.toString();
		}
	}
}