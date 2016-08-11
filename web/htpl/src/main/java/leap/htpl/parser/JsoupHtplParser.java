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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import leap.htpl.DefaultHtplDocument;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.HtplResource;
import leap.htpl.ast.Attr;
import leap.htpl.ast.BlockComments;
import leap.htpl.ast.Break;
import leap.htpl.ast.Bundle;
import leap.htpl.ast.Else;
import leap.htpl.ast.Empty;
import leap.htpl.ast.For;
import leap.htpl.ast.Fragment;
import leap.htpl.ast.IfCondition;
import leap.htpl.ast.IfParent;
import leap.htpl.ast.Include;
import leap.htpl.ast.Load;
import leap.htpl.ast.Node;
import leap.htpl.ast.NodeContainer;
import leap.htpl.ast.RenderFragment;
import leap.htpl.ast.SetVariables;
import leap.htpl.ast.Text;
import leap.htpl.ast.Text.Type;
import leap.htpl.exception.HtplDefinitionException;
import leap.htpl.exception.HtplParseException;
import leap.htpl.processor.AttrProcessor;
import leap.htpl.resolver.StringHtplResource;
import leap.lang.Strings;
import leap.lang.convert.Converts;
import leap.lang.expression.Expression;
import leap.lang.jsoup.Jsoup;
import leap.lang.jsoup.nodes.Attribute;
import leap.lang.jsoup.nodes.Comment;
import leap.lang.jsoup.nodes.DataNode;
import leap.lang.jsoup.nodes.Document;
import leap.lang.jsoup.nodes.DocumentType;
import leap.lang.jsoup.nodes.Element;
import leap.lang.jsoup.nodes.Entities.EscapeMode;
import leap.lang.jsoup.nodes.TextNode;
import leap.lang.jsoup.parser.HtmlParseMode;
import leap.lang.jsoup.select.NodeVisitor;
import leap.lang.value.SimpleEntry;

public class JsoupHtplParser extends HtplParserBase {
	
	private final Document   	doc;
	private final ParserVisitor visitor = new ParserVisitor();
	
	public JsoupHtplParser(HtplEngine engine,HtplResource resource) {
		this(engine,resource,Type.HTML);
	}
	
	public JsoupHtplParser(HtplEngine engine, HtplResource resource, Text.Type type) {
		super(engine,resource,type);
        try {
        	Document.OutputSettings os = 
        			new Document.OutputSettings().escapeMode(EscapeMode.base)
        										 .prettyPrint(false);
        	
        	this.doc = Jsoup.parse(content,HtmlParseMode.ORIGINAL).outputSettings(os);
        } catch (Exception e) {
            throw new HtplParseException("Html parse error : " + e.getMessage(),e);
        }
    }
	
	public HtplDocument document() {
		try {
			parseHtml();

	        DefaultHtplDocument doc = new DefaultHtplDocument(engine, resource, nodes);
	        
	        doc.putProperties(props);
	        
	        return doc;
		} catch (HtplParseException e){
			throw e;
        } catch (Exception e) {
        	throw new HtplParseException("Error parsing template, " + e.getMessage(), e);
        }
	}
	
	private void parseHtml() {
		doc.traverse(visitor);
	}
	
	private class ParserVisitor implements NodeVisitor {

		@Override
        public void head(leap.lang.jsoup.nodes.Node node, int depth) {
			if(node instanceof Document){
				return;
			}
			
	        if(node instanceof DocumentType) {
	        	acceptHtml(node);
	        	return;
	        }
	        
	        if(node instanceof Comment){
	        	parseComment((Comment)node);
	        	return;
	        }
	        
	        if(node instanceof Element){
	        	startElement((Element)node);
	        	return;
	        }
	        
	        if(node instanceof TextNode){
	        	acceptHtml(((TextNode)node).outerHtml());	
	        	return;
	        }
	        
	        if(node instanceof DataNode){
	        	/*
	        	    A script element :
	        	     
					<script>var i =0;</script>
					
					The content of DataNode is : var i =0;
					
					Or inline html:
					
					<script type=text/x-jquery-tmpl>
						<div>...</div>
					</script>
	        	 */
	        	
	        	//process inline-html
	        	if(inlineHtml()){
	        		parseHtml(((DataNode)node).getWholeData());
	        	}else{
	        		if(isJavascript((DataNode)node)){
	        			parseJavascript(((DataNode)node).getWholeData());
	        		}else{
	        			parseText(((DataNode)node).getWholeData());	
	        		}
	        	}
	        }
        }

		@Override
        public void tail(leap.lang.jsoup.nodes.Node node, int depth) {
			if(node instanceof Document){
				//end document
				return;
			}
			
			if(node instanceof Element){
				endElement((Element)node);
			}
		}
	}
	
	private boolean isJavascript(DataNode node) {
		leap.lang.jsoup.nodes.Node parent = node.parent();
		if(parent instanceof Element) {
			Element e = (Element)parent;
			if(e.tagName().equalsIgnoreCase("script")){
				return true;
			}
		}
		return false;
	}
	
	private void startElement(Element e){
		if(e.tag().isParserCreated()){
			return;
		}
		
		QName1     name       = new QName1(e.tagName());
		List<Attr> attributes = attrs(e);
		
		leap.htpl.ast.Element element = new leap.htpl.ast.Element(name.prefix,name.localName,attributes);
		element.setSelfClosing(e.tag().isSelfClosing());
		
		//process ht-inline-el attribute
		openBooleanInlineAttr(inlineExpressions, element, INLINE_EL);
		
		//process ht-inline-html attribute
		openBooleanInlineAttr(inlineHtmls, element, INLINE_HTML);
		
		element.resolveProcessors(engine);
		
		addNode(element);
	}
	
	protected void openBooleanInlineAttr(Stack<Entry<Boolean,Object>> stack, leap.htpl.ast.Element element, String attrName) {
		Attr inlineAttr = element.getAttribute(engine.getConfig().getPrefix() + "-" + attrName);
		if(null != inlineAttr) {
			element.removeAttribute(inlineAttr);
			if(!Strings.isEmpty(inlineAttr.getString())) {
				Boolean inline = Converts.toBoolean(inlineAttr.getString());
				Entry<Boolean, Object> entry = new SimpleEntry<Boolean,Object>(inline, element);
				stack.add(entry);
			}
		}
	}
	
	private void endElement(Element e){
		if(e.tag().isParserCreated()){
			return;
		}
		
		String name = e.tagName();
		
		leap.htpl.ast.Element openElement = peekParentElement(name);
		if(null == openElement){
			throw new HtplDefinitionException("Invalid end element '" + e + "', cannot found begin element");
		}
		
		//close boolean inline attrs
		closeBooleanInlineAttr(inlineExpressions,openElement);
		closeBooleanInlineAttr(inlineHtmls, openElement);
		
		closeParent(openElement);
	}
	
	protected void closeBooleanInlineAttr(Stack<Entry<Boolean,Object>> stack, leap.htpl.ast.Element openElement) {
		if(!stack.isEmpty()) {
			Entry<Boolean, Object> entry = stack.peek();
			if(entry.getValue() == openElement) {
				stack.pop();
			}
		}
	}
	
	private leap.htpl.ast.Element peekParentElement(String name){
		if(parents.isEmpty()){
			return null;
		}

		for(int i=parents.size() - 1;i>=0;i--){
			NodeContainer p = parents.get(i);

			if(isElement(p, name)){
				return (leap.htpl.ast.Element)p;
			}
			
			if(p instanceof SetVariables){
				continue;
			}
			
			throw new HtplParseException("Node [" + p + "] must be closed"); 
		}
		
		return null;
	}
	
	private boolean isElement(NodeContainer node, String name){
		if(node instanceof leap.htpl.ast.Element){
			return ((leap.htpl.ast.Element) node).isElement(name);
		}
		return false;
	}
	
	private List<Attr> attrs(Element e){
		List<Attr> attrs = new ArrayList<>();
		for(Attribute a : e.attributes()){
			QName1 name = new QName1(a.getKey());
			Attr attr = new Attr(name.prefix,name.localName,a.getValue());
			attr.setQuotedCharacter(a.getQuotedCharacter());
			attr.setInlineExpression(inlineExpression());
			attrs.add(attr);
		}
		return attrs;
	}
	
	private void parseJavascript(String script) {
		if(Strings.isEmpty(script)){
			return;
		}
		acceptJavascript(script);
	}
	
	private void parseText(String text) {
		if(Strings.isEmpty(text)){
			return;
		}
		acceptHtml(text);
	}
	
	private void parseHtml(String html) {
		if(Strings.isEmpty(html)){
			return;
		}
		JsoupHtplParser parser = new JsoupHtplParser(engine, new StringHtplResource(html));
		parser.defaultInlineExpression = inlineExpression();
		parser.parseHtml();
		for(Node node : parser.nodes){
			addNode(node,true);
		}
	}
	
	private void parseComment(Comment comment){
		String text = comment.getData().trim();
		
		if(text.length() > 1){
			//Document Properties
			if(text.startsWith(PREFIX_PROPERTIES)){
				parseProperties(text.substring(PREFIX_PROPERTIES.length()));
				return;
			}
			
			//Parser level single comments, ignore it
			if(text.startsWith(PREFIX_SINGLE_COMMENTS)){
				return;
			}
			
			//Parser level block comments (begin)
			if(text.startsWith(PREFIX_BLOCK_COMMENTS_BEGIN)){
				startBlockComments(comment.getData());
				return;
			}
			
			//Parser level block comments (close)
			if(text.startsWith(PREFIX_BLOCK_COMMENTS_CLOSE)){
				endBlockComments(comment.getData());
				return;
			}
			
			//Directives, such as #include, #foreach ...
			if(text.startsWith(PREFIX_DIRECTIVE)){
				parseDirective(text);
				return;
			}
			
			//IE conditional comment
			if(parseConditionalComment(text)){
				return;
			}
			
		}
		
		//Html comment, accept it as text.
		acceptComment(comment.getData());
	}
	
	protected void startBlockComments(String comment) {
		addNode(new BlockComments());
		acceptComment(comment);
	}
	
	protected void endBlockComments(String comment) {
		BlockComments block = peekParent(BlockComments.class);
		if(null == block){
			throw new HtplParseException("Invalid block comments end directive '*/', no block comments begin directive '/*'");
		}
		closeParent(block);
	}
	
	/* IE conditional comment
    <!--[if lt IE 9]>
    <script src="t.js"></script>
    <![endif]-->
	 */
	protected boolean parseConditionalComment(String comment) {
		if(comment.startsWith("[if ") && comment.endsWith("<![endif]")) {
			int start = comment.indexOf("]>");
			
			if(start > 0){
				start = start + 2;
				int end = comment.length() - "<![endif]".length();
				String html = comment.substring(start,end);
				
				acceptText("<!--" + comment.substring(0,start));
				
				parseHtml(html);
				
				acceptText("<![endif]-->");
				
				return true;
			}
		}
		return false;
	}
	
	protected boolean parseDirective(String directive,String content){
		if(directive.equals(INCLUDE)){
			parseInclude(content);
			return true;
		}
		
		if(directive.equals(LOAD)) {
			parseLoad(content);
			return true;
		}
		
		if(directive.equals(SET)){
			startSetVariables(content);
			return true;
		}
		
		if(directive.equals(ENDSET)){
			endSetVariables(content);
			return true;
		}
		
		if(directive.equals(FOR)){
			startFor(content);
			return true;
		}
		
		if(directive.equals(BREAK)){
			breakFor(content);
			return true;
		}
		
		if(directive.equals(EMPTY)){
			emptyFor(content);
			return true;
		}
		
		if(directive.equals(ENDFOR)){
			endFor(content);
			return true;
		}
		
		if(directive.equals(IF)){
			parseIf(content);
			return true;
		}
		
		if(directive.equals(ELSEIF)){
			parseElseIf(content);
			return true;
		}
		
		if(directive.equals(ELSE)){
			parseElse(content);
			return true;
		}
		
		if(directive.equals(ENDIF)){
			parseEndIf(content);
			return true;
		}
		
		if(directive.equals(FRAGMENT)){
			startFragment(content);
			return true;
		}
		
		if(directive.equals(ENDFRAGMENT)){
			endFragment(content);
			return true;
		}
		
		if(directive.equals(RENDER_FRAGMENT)){
			parseRenderFragment(content);
			return true;
		}
		
		if(directive.equals(BUNDLE)) {
		    startBundle(content);
		    return true;
		}
		
		if(directive.equals(ENDBUNDLE)) {
		    endBundle(content);
		    return true;
		}
		
		return false;
	}
	
	protected void parseInclude(String content){
		if(Strings.isEmpty(content)){
			throw new HtplParseException("Invalid 'include' directive, the content must not be empty");
		}
		
		String[] parts = Strings.split(scanQuotedString(content),'#',true);
		
		Include inc = parts.length == 1 ? new Include(parts[0]) : new Include(parts[0],parts[1]);
		
		acceptNode(inc);
	}
	
	protected void parseLoad(String content){
		if(Strings.isEmpty(content)){
			throw new HtplParseException("Invalid 'load' directive, the content must not be empty");
		}
		
		Map<String, String> params = parseParams(content, "file");
		
		String file = params.remove("file");
		
		if(Strings.isEmpty(file)) {
			throw new HtplParseException("The 'file' of 'load' directive must not be empty");
		}
		
		String variable = params.remove("variable");
		
		acceptNode(new Load(file,variable,params));
	}
	
	protected void parseRenderFragment(String content){
		if(Strings.isEmpty(content)){
			throw new HtplParseException("Invalid 'render-fragment' directive, the content must not be empty");
		}

		RenderFragment render;
		
		Map<String, String> params = parseParams(content,"name");
			
		String  name     = params.remove("name");
		boolean required = Converts.toBoolean(params.remove("required"),false);
		
		if(Strings.isEmpty(name)) {
			throw new HtplParseException("The 'name' of 'render-fragment' must not be empty");
		}
		
		render = new RenderFragment(name,required, params);
		
		addNode(render,true);
	}
	
	protected void startFragment(String content) {
		if(Strings.isEmpty(content)){
			throw new HtplDefinitionException("Invalid #fragment, the name of fragment must not be empty");
		}
		
		Fragment fragment = null;
		
		Map<String, String> params = parseParams(content,"name");
		
		String  name   = params.remove("name");
		boolean render = Converts.toBoolean(params.remove("render"),true);

		fragment = new Fragment(name,render);
		
		acceptNode(fragment);
	}
	
	protected void endFragment(String content) {
		Fragment f = peekParent(Fragment.class);
		if(null == f){
			throw new HtplDefinitionException("Invalid #endfragment, must declare '#fragment' before it");
		}
		closeParent(f);
	}
	
	protected void startBundle(String content) {
        if (Strings.isEmpty(content)) {
            throw new HtplDefinitionException("Invalid #bundle, the path of bundle must not be empty");
        }
	       
	    Bundle bundle;
	    
	    if(!Strings.isEmpty(content)) {
	        Map<String, String> params = parseParams(content, "path");
	        String path = params.remove("path");
	        
	        bundle = new Bundle(path);
	    }else{
	        bundle = new Bundle(null);
	    }
	    
	    acceptNode(bundle);
	}
	
	protected void endBundle(String content) {
	    Bundle b = peekParent(Bundle.class);
	    if(null == b) {
	        throw new HtplDefinitionException("Invlaid #endbundle, must declare '#bundle' before it");
	    }
	    closeParent(b);
	}
	
    protected void startSetVariables(String content) {
		if(Strings.isEmpty(content)){
			throw new HtplDefinitionException("Invalid #set, the content must not be empty");
		}
		
		addNode(new SetVariables(parseNamedExpressions(engine, content)));
	}
	
	protected void endSetVariables(String content) {
		SetVariables sv = peekParent(SetVariables.class);
		if(null == sv){
			throw new HtplDefinitionException("Invalid #endset, must declare '#set' before it");
		}
		closeParent(sv);
	}
	
	protected void startFor(String content) {
		//#for name : expression || #for (name : ${expression})
		String forExpr = scanParenString(content);
		
		String[] parts = Strings.split(forExpr, ":");

		if(parts.length != 2){
			throw new HtplDefinitionException("Invalid for expression '" + content + "', must be for \"name : expression\"");
		}
		
		String variableName     = parts[0];
		String collectionString = parts[1];
		
		For node = null;
		
		if(Strings.isDigits(collectionString)) {
			Integer max = Integer.parseInt(collectionString);
			node = new For(variableName, max);
		}else{
			Expression expression = engine.getExpressionManager().parseExpression(engine, collectionString);
			node = new For(variableName, expression);
		}
		
		addNode(node);
	}
	
	protected void breakFor(String content) {
		Expression condition = Strings.isEmpty(content) ? null : engine.getExpressionManager().parseExpression(engine, content);
		acceptNode(new Break(condition));
	}
	
	protected void emptyFor(String content) {
		acceptNode(new Empty());
	}
	
	protected void endFor(String content) {
		Empty empty = tryPeekParent(Empty.class);
		if(null != empty){
			parents.pop();
		}
		
		For forNode = peekParent(For.class);
		if(null == forNode){
			throw new HtplParseException("Invalid '<!--#endfor-->', cannot found begin for <!--#for .. -->");
		}
		forNode.setEmptyBody(empty);
		
		closeParent(forNode);
	}
	
	protected void parseIf(String content) {
		if(Strings.isEmpty(content)){
			throw new HtplDefinitionException("Invalid #if, content must not be empty");
		}
		
		addNode(new IfParent());
		createIfCondition(content);
	}
	
	protected void parseElseIf(String content) {
		if(Strings.isEmpty(content)){
			throw new HtplDefinitionException("Invalid #elseif, content must not be empty");
		}
		
		NodeContainer p = parents.peek();
		if(p instanceof IfCondition){
			closeParent(p);
		}
		
		createIfCondition(content);
	}
	
	protected void parseElse(String content) {
		NodeContainer p = parents.peek();
		if(p instanceof IfCondition){
			closeParent(p);
		}
		
		addNode(new Else());
	}
	
	protected void parseEndIf(String content) {
		NodeContainer p = parents.peek();
		if(p instanceof IfCondition || p instanceof Else){
			closeParent(p);
		}
		
		IfParent ifp = peekParent(IfParent.class);
		
		if(null == ifp){
			throw new HtplDefinitionException("Invalid #endif, no #if exists");
		}
		
		closeParent(ifp);
	}
	
	protected void createIfCondition(String content) {
		Expression expr = engine.getExpressionManager().parseExpression(engine, content);
		addNode(new IfCondition(content, expr));
	}
	
	protected Map<String, String> parseParams(String content, String defaultParamName) {
		Map<String, String> params = new HashMap<String, String>(2);
		
		String defaultParamValue = tryScanIdentifier(content);
		if(null != defaultParamValue) {
			params.put(defaultParamName, defaultParamValue);			
		}else{
			parseKeyValues(params, content);
		}
		
		return params;
	}
	
	private void acceptHtml(leap.lang.jsoup.nodes.Node node){
		acceptHtml(node.outerHtml());
	}
}