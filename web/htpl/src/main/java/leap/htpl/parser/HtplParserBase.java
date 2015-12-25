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
package leap.htpl.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import leap.htpl.HtplEngine;
import leap.htpl.HtplResource;
import leap.htpl.ast.Node;
import leap.htpl.ast.NodeContainer;
import leap.htpl.ast.SetVariables;
import leap.htpl.ast.Text;
import leap.htpl.ast.Text.Type;
import leap.htpl.exception.HtplParseException;
import leap.lang.expression.Expression;
import leap.lang.io.IO;
import leap.lang.value.ImmutableNamedValue;
import leap.lang.value.NamedValue;

public abstract class HtplParserBase {
	
	protected static final String PREFIX_PROPERTIES           = "@";
	protected static final String PREFIX_SINGLE_COMMENTS      = "//";
	protected static final String PREFIX_BLOCK_COMMENTS_BEGIN = "/*";
	protected static final String PREFIX_BLOCK_COMMENTS_CLOSE = "*/";
	protected static final String PREFIX_DIRECTIVE            = "#";
	
	//directives
	protected static final String INCLUDE = "include";
	protected static final String LOAD    = "load";
	protected static final String SET     = "set";    //define a local variable.
	protected static final String ENDSET  = "endset";
	
	protected static final String FOR	    = "for";
	protected static final String BREAK	    = "break"; //break for
	protected static final String EMPTY	    = "empty"; //empty for
	protected static final String ENDFOR	= "endfor";
	
	protected static final String IF		= "if";
	protected static final String ELSEIF	= "elseif";
	protected static final String ELSE   	= "else";
	protected static final String ENDIF	    = "endif";
	
	protected static final String FRAGMENT    = "fragment";
	protected static final String ENDFRAGMENT = "endfragment";
	
	protected static final String RENDER_FRAGMENT = "render-fragment";
	
	protected static final String INLINE_EL   = "inline-el";
	protected static final String INLINE_HTML = "inline-html";
	
	protected static final String BUNDLE    = "bundle";
	protected static final String ENDBUNDLE = "endbundle";
	
	protected final HtplEngine 		     engine;
	protected final HtplResource		 resource;
	protected final String	   		     content;
	protected final Text.Type			 type;
	protected final Map<String, String>  props   = new HashMap<String,String>();
	protected final List<Node> 	         nodes   = new ArrayList<Node>();
	protected final Stack<NodeContainer> parents = new Stack<NodeContainer>();
	protected final Stack<Entry<Boolean, Object>> inlineExpressions = new Stack<>();
	protected final Stack<Entry<Boolean, Object>> inlineHtmls       = new Stack<>();
	
	protected boolean defaultInlineExpression = true;
	
	public HtplParserBase(HtplEngine engine,HtplResource resource) {
		this(engine,resource,Text.Type.HTML);
	}
	
	public HtplParserBase(HtplEngine engine,HtplResource resource,Text.Type type) {
        try {
        	this.engine   = engine;
        	this.resource = resource;
        	this.type     = type;
        	
        	try(Reader r = resource.getReader()){
	        	this.content  = IO.readString(r);
        	}
        } catch (IOException e){
        	throw new HtplParseException("I/O error : " + e.getMessage(),e);
        }
	}
	
	protected void parseProperties(String content){
		parseKeyValues(props,content);
	}
	
	protected void parseDirective(String comment){
		int len = PREFIX_DIRECTIVE.length();
		
		if(comment.length() > len + 1){
			char c1 = comment.charAt(len);
			
			if(Character.isLetter(c1)){
				String declaration = comment.substring(len);
				
				int start = len - 1;
				int i     = len + 1;
				
				for(;i<declaration.length();i++){
					char c = declaration.charAt(i);
					
					if(c == '-' || c == '_' || Character.isLetter(declaration.charAt(i))){
						continue;
					}
					
					break;
				}
				
				String directive = declaration.substring(start,i).toLowerCase();
				String content   = i < declaration.length() ? declaration.substring(i).trim() : null;
				
				if(!parseDirective(directive,content)){
					throw new HtplParseException("Unknow directive '" + directive + "' with content '" + content + "'");
				}
				
				return;
			}
		}
		
		acceptComment(comment);
	}
	
	protected abstract boolean parseDirective(String directive,String content);
	
	protected void acceptComment(String comment){
		acceptHtml("<!--" + comment + "-->");
	}
	
	protected String scanQuotedString(String str){
		str = str.trim();
		
		char qc = str.charAt(0);
		
		if(qc == '"' || qc == '\''){
			if(str.charAt(str.length() - 1) != qc){
				throw new HtplParseException("Unclosed string (" + str + "), must be ends with character : " + qc);
			}
			return str.substring(1,str.length() - 1);
		}
		return str;
	}
	
	protected String tryScanIdentifier(String s) {
		s = s.trim();
		
		char c = s.charAt(0);
		
		if(c == '"' || c == '\''){
			return scanQuotedString(s);
		}
		
		for(int i=0;i<s.length();i++) {
			c= s.charAt(i);
			
			if(c == '='){
				return null;
			}
			
			if(Character.isWhitespace(c)) {
				return null;
			}
		}
		
		return s;
	}
	
	protected String scanParenString(String str) {
		str = str.trim();
		
		if(str.charAt(0) == '('){
			if(str.charAt(str.length() - 1) != ')'){
				throw new HtplParseException("Unclosed string '" + str + "', must be ends with ')'");
			}
			return str.substring(1,str.length() - 1);
		}
		return str;
	}
	
	protected void acceptText(String text){
		appendText(text,Type.TEXT,inlineExpression());
	}
	
	protected void acceptJavascript(String text){
		appendText(text,Type.JAVASCRIPT,inlineExpression());
	}
	
	protected void acceptHtml(String text){
		appendText(text,Type.HTML,inlineExpression());
	}
	
	private void appendText(String text, Text.Type type, boolean inlineExpression){
		if(parents.isEmpty()){
			if(nodes.size() > 0){
				Node lastNode = nodes.get(nodes.size() - 1);
				if(lastNode instanceof Text){
					Text textNode = (Text)lastNode;
					if(textNode.getType() == type && textNode.isInlineExpression() == inlineExpression){
						((Text)lastNode).append(text);
						return;
					}
				}
			}
			nodes.add(new Text(text, type));
		}else{
			parents.peek().addChildText(text,type,inlineExpression);
		}
	}
	
	protected boolean inlineExpression() {
		if(inlineExpressions.isEmpty()) {
			return defaultInlineExpression;
		}else{
			return inlineExpressions.peek().getKey();
		}
	}
	
	protected boolean inlineHtml() {
		if(inlineHtmls.isEmpty()) {
			return false;
		}else{
			return inlineHtmls.peek().getKey();
		}
	}
	
	protected void acceptNode(Node node){
		addNode(node);
	}
	
	protected void addNode(Node node){
		addNode(node,false);
	}
	
	protected void addNode(Node node, boolean closed){
		if(parents.isEmpty()){
			nodes.add(node);
		}else{
			parents.peek().addChildNode(node);
		}
		
		if(!closed && node instanceof NodeContainer){
			parents.add((NodeContainer)node);
		}
	}
	
	@SuppressWarnings("unchecked")
    protected <T extends NodeContainer> T peekParent(Class<T> type){
		if(parents.isEmpty()){
			return null;
		}

		for(int i=parents.size() - 1;i>=0;i--){
			NodeContainer p = parents.get(i);

			if(type.isAssignableFrom(p.getClass())){
				return (T)p;
			}
			
			if(p instanceof SetVariables){
				continue;
			}
			
			throw new HtplParseException("Node [" + p + "] must be closed"); 
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
    protected <T extends NodeContainer> T tryPeekParent(Class<T> type){
		if(parents.isEmpty()){
			return null;
		}

		for(int i=parents.size() - 1;i>=0;i--){
			NodeContainer p = parents.get(i);

			if(type.isAssignableFrom(p.getClass())){
				return (T)p;
			}
			
			if(p instanceof SetVariables){
				continue;
			}
			
			return null; 
		}
		
		return null;
	}


	protected void closeParent(NodeContainer parent){
		while(!parents.isEmpty()){
			NodeContainer p = parents.pop();
			
			if(p == parent){
				return;
			}
			
			//SetVariables can be not close.
			if(p instanceof SetVariables){
				continue;
			}
			
			throw new HtplParseException("Node [" + p + "] must be closed");
		}
		
		throw new IllegalStateException("Cannot close parent [" + parent + "]");
	}
	
	protected void removeParent(Node node) {
		Node last = nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);
		
		if(node != last){
			throw new HtplParseException("Invalid last node [" + last + "], must be [" + node + ']');
		}
		
		nodes.remove(nodes.size() - 1);
		parents.pop();
	}
	
	public static NamedValue<Expression>[] parseNamedExpressions(HtplEngine engine, String content) {
		return parseNamedExpressions(engine, content, '=');
	}
	
	@SuppressWarnings("unchecked")
    public static NamedValue<Expression>[] parseNamedExpressions(HtplEngine engine, String content, char seperator) {
		Map<String, String> declarations = new LinkedHashMap<String, String>(1);
		
		parseExpressionPairs(declarations, content, seperator);
	
		int i=0;
		NamedValue<Expression>[] dvs = new ImmutableNamedValue[declarations.size()];
		for(Entry<String, String> entry : declarations.entrySet()){
			String name  = entry.getKey();
			String value = entry.getValue();
			
			Expression expr = engine.getExpressionManager().parseExpression(engine, value);
			dvs[i++] = new ImmutableNamedValue<Expression>(name, expr);
		}
		
		return dvs;
	}
	
	public static void parseKeyValues(Map<String,String> props, String string){
		parseKeyValues(props, string, '=');
	}
	
	// left=right; left=right ...
    public static void parseExpressionPairs(Map<String,String> props, String string, char seperator){
        char semi = ';';
        
        string = string.trim();
        int  mark;
        char ch;
        for(int i=0;i<string.length();i++){
            //skip white spaces
            for(;i<string.length();i++){
                ch = string.charAt(i);
                if(!Character.isWhitespace(ch)){
                    break;
                }
            }
            //scan left
            mark = i;
            String left  = null;
            String right = null;
            for(;i<string.length();i++){
                ch = string.charAt(i);
                if(ch == seperator){
                    left = string.substring(mark,i).trim();
                    i++;
                    break;
                }
            }
            //skip white spaces
            for(;i<string.length();i++){
                ch = string.charAt(i);
                if(!Character.isWhitespace(ch)){
                    break;
                }
            }
            //scan value
            mark = i;
            for(;i<string.length();i++){
                ch = string.charAt(i);
                if(ch == semi || ch == '\r' || ch=='\n'){
                    right = string.substring(mark,i).trim();
                    break;
                }
            }
            if(null == right && mark < i){
                right = string.substring(mark,i).trim();
            }
            
            if(null == left || null == right){
                throw new HtplParseException("Invalid expression pairs -> " + string + " <-, must be 'key1=value1; key2=value2; ...' format");
            }
            
            props.put(left,right);
        }
    }
	
	// key=value key=value ...
	public static void parseKeyValues(Map<String,String> props, String string, char seperator){
		string = string.trim();
		int  mark;
		char ch;
		for(int i=0;i<string.length();i++){
			//skip white spaces
			for(;i<string.length();i++){
				ch = string.charAt(i);
				if(!Character.isWhitespace(ch)){
					break;
				}
			}
			//scan key
			mark = i;
			String key  = null;
			String value = null;
			for(;i<string.length();i++){
				ch = string.charAt(i);
				if(ch == seperator){
					key = string.substring(mark,i).trim();
					i++;
					break;
				}
			}
			//skip white spaces
			for(;i<string.length();i++){
				ch = string.charAt(i);
				if(!Character.isWhitespace(ch)){
					break;
				}
			}
			//scan value
			mark = i;
			ch = string.charAt(i);
			if(ch == '"' || ch == '\''){
				char qc = ch;
				i++;
				mark = i;
				for(;i<string.length();i++){
					ch = string.charAt(i);
					if(ch == qc){
						value = string.substring(mark,i).trim();
						break;
					}
				}
				if(null == value){
					throw new HtplParseException("Unclosed value for key '" + key + "', please checks the content : " + string);
				}
			}else{
				for(;i<string.length();i++){
					ch = string.charAt(i);
					if(Character.isWhitespace(ch)){
						value = string.substring(mark,i).trim();
						break;
					}
				}
			}
			if(null == value && mark < i){
				value = string.substring(mark,i).trim();
			}
			if(null == key || null == value){
				throw new HtplParseException("Invalid properties string '" + string + "', must be 'key=value' format");
			}
			props.put(key,value);
		}
	}	
	
	protected static final class QName1 {
		String prefix;
		String localName;
		
		QName1(String name){
			int index = name.indexOf(':');
			if(index > 0){
				prefix = name.substring(0,index);
				localName = name.substring(index+1);
			}else{
				localName = name;
			}
		}
	}
	
}
