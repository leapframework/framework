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
import leap.lang.Strings;

public class Text extends Node {
	
	public enum Type {
		HTML,
		TEXT,
		JAVASCRIPT
	}
	
	private StringBuilder buf;
	private Type          type;
	private boolean		  inlineExpression = true;
	
	public Text(String text){
		this(text,Type.HTML,true);
	}
	
	public Text(String text, Type type){
		this(text,type,true);
	}
	
	public Text(String text, Type type, boolean inlineExpression){
		this.buf  = new StringBuilder(text);
		this.type = type;
		this.inlineExpression = inlineExpression;
	}
	
	public void append(CharSequence text){
		checkLocked();
		this.buf.append(text);
	}
	
	public void append(Text node){
		checkLocked();
		this.buf.append(node.buf);
	}
	
	public Type getType() {
		return type;
	}

	public boolean isInlineExpression() {
		return inlineExpression;
	}

	public void setInlineExpression(boolean inlineExpression) {
		this.inlineExpression = inlineExpression;
	}

	public boolean trimStart() {
		if(null != buf){
			String _old = buf.toString();
			String _new = Strings.trimStart(_old);
			
			if(!_old.equals(_new)){
				buf = new StringBuilder(_new);
				return true;
			}
		}
		
		return false;
	}
	
	public boolean trimEnd() {
		if(null != buf){
			String _old = buf.toString();
			String _new = Strings.trimEnd(_old);
			
			if(!_old.equals(_new)){
				buf = new StringBuilder(_new);
				return true;
			}
		}
		return false;
	}
	
	public boolean trim() {
		if(null != buf){
			String _old = buf.toString();
			String _new = Strings.trim(_old);
			
			if(!_old.equals(_new)){
				buf = new StringBuilder(_new);
				return true;
			}
		}
		return false;
	}

	public boolean isBlank() {
		for(int i=0;i<buf.length();i++) {
			if(!Character.isWhitespace(buf.charAt(0))) {
				return false;
			}
		}
		return true;
	}
	
	public boolean removeBlankLineFirst() {
		if(null != buf){
			int i=0;
			for(;i<buf.length();i++){
				char c= buf.charAt(i);
				
				if(!Character.isWhitespace(c)){
					i = 0;
					break;
				}
				
				if(c == '\r'){
					if(i<buf.length()-1 && buf.charAt(i+1) == '\n'){
						i++;
					}
					i++;
					break;
				}
				
				if(c == '\n'){
					i++;
					break;
				}
				
				//eof
				if(i == buf.length() - 1){
					i = 0;
					break;
				}
			}
			if(i > 0){
				buf = buf.delete(0, i);
				return true;
			}
		}
		return false;
	}
	
	public boolean removeBlankLineLast() {
		if(null != buf){
			int max = buf.length() - 1;
			int i=max;
			for(;i>=0;i--){
				char c= buf.charAt(i);
				
				if(!Character.isWhitespace(c)){
					i = max;
					break;
				}
				
				if(c == '\r' || c == '\n'){
					break;
				}
				
				if(i == 0){
					i = max;
					break;
				}
			}
			if(i < max){
				buf = buf.delete(i+1, max + 1);
				return true;
			}
		}
		return false;
	}
	
	public void removeBlankLinesFirstLast() {
		removeBlankLineFirst();
		removeBlankLineLast();
	}
	
	@Override
    public void compile(HtplEngine engine, HtplDocument doc, HtplCompiler compiler) {
		if(type == Type.HTML){
			compiler.html(buf, inlineExpression);
		}else if(type == Type.JAVASCRIPT){
			compiler.javascript(buf, inlineExpression);
		}else{
			compiler.text(buf, inlineExpression);
		}
    }

	@Override
    protected void doWriteTemplate(Appendable out) throws IOException {
		out.append(buf);
	}

	@Override
    protected Node doDeepClone(Node parent) {
	    Text clone = new Text(buf.toString(),type);
	    clone.setInlineExpression(this.inlineExpression);
	    return clone;
    }
}
