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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import javax.xml.namespace.QName;

import leap.htpl.HtplCompiler;
import leap.htpl.HtplDocument;
import leap.htpl.HtplEngine;
import leap.htpl.exception.HtplParseException;
import leap.htpl.processor.AttrProcessor;
import leap.htpl.processor.ElementProcessor;
import leap.lang.Strings;
import leap.lang.expression.Expression;

public class Element extends NodeContainer {
	
	/*
	private static final String[] SELF_CLOSING_ELEMENTS = new String[]{
		"hr","br","link","meta","img","input"
	};
	*/
	
	protected String 	 	   prefix;
	protected String     	   localName;
	protected List<Attr> 	   attributes;
	protected boolean    	   selfClosing;
	protected ElementProcessor processor;
	
	private boolean isStartElementProcessed; //prevent cyclic process
	private boolean isEndElementProcessed;   
	private boolean isProcessorsResolved;
	
	protected Element() {
		
	}
	
	public Element(String prefix,String localName) {
		this(prefix,localName,new ArrayList<Attr>());
    }
	
	public Element(String prefix,String localName,List<Attr> attributes) {
		super();
	    this.prefix     = Strings.trimToNull(prefix);
	    this.localName  = localName;
	    this.attributes = null == attributes ? new ArrayList<Attr>() : attributes;
    }
	
	public String getQualifiedName(){
		return Strings.isEmpty(this.prefix) ? this.localName :  (this.prefix + ":" + this.localName);
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix){
		checkLocked();
		this.prefix = prefix;
	}
	
	public String getLocalName() {
		return localName;
	}
	
	public void setLocalName(String localName){
		checkLocked();
		this.localName = localName;
	}

	public List<Attr> attributes() {
		return attributes;
	}
	
	public boolean hasProcessor(){
		return null != processor;
	}
	
	public ElementProcessor getProcessor() {
		return processor;
	}

	public void setProcessor(ElementProcessor processor) {
		checkLocked();
		this.processor = processor;
	}

	public boolean isSelfClosing() {
		return selfClosing;
	}

	public void setSelfClosing(boolean selfClosing) {
		checkLocked();
		this.selfClosing = selfClosing;
	}

	public boolean isElement(QName name){
		String thatPrefix = Strings.trimToNull(name.getPrefix());
		return Strings.equalsIgnoreCase(this.getPrefix(), thatPrefix) && localName.equalsIgnoreCase(name.getLocalPart());
	}
	
	public boolean isElement(String localName){
		return Strings.equalsIgnoreCase(localName, this.localName);
	}
	
	public Attr getAttribute(String prefix,String localName){
		prefix = Strings.nullToEmpty(prefix);
		
		for(int i=0;i<attributes.size();i++){
			Attr a = attributes.get(i);
			
			String attrPrefix = Strings.nullToEmpty(a.getPrefix());
			
			if(Strings.equalsIgnoreCase(attrPrefix, prefix) && Strings.equalsIgnoreCase(a.getLocalName(),localName)){
				return a;
			}
		}
		return null;
	}
	
	public Attr getAttribute(String localName){
		for(int i=0;i<attributes.size();i++){
			Attr a = attributes.get(i);
			if(Strings.equalsIgnoreCase(a.getLocalName(),localName)){
				return a;
			}
		}
		return null;
	}
	
	public String getAttributeValue(String prefix,String localName){
		Attr a = getAttribute(prefix, localName);
		return null == a ? null : a.getString();
	}
	
	public String getAttributeValue(String localName){
		return getAttributeValue(null, localName);
	}
	
	public boolean removeAttribute(Attr a){
		return attributes.remove(a);
	}
	
	public void updateAttribute(Attr oldAttr,Attr newAttr){
		int index = attributes.indexOf(oldAttr);
		
		if(index < 0){
			throw new IllegalStateException("The old attribute '" + oldAttr.getLocalName() + "' not exists in element");
		}
		attributes.set(index, newAttr);
	}
	
	public void setAttribute(Attr attr){
		List<Attr> removes = new ArrayList<Attr>();
		for(Attr a : attributes){
			if(a.getLocalName().equalsIgnoreCase(attr.getLocalName())){
				removes.add(a);
			}
		}
		if(removes.isEmpty()){
			attributes.add(attr);
		}else{
			attributes.set(attributes.indexOf(removes.get(0)), attr);
			for(int i=1;i<removes.size();i++){
				attributes.remove(removes.get(i));
			}
		}
	}

	public void setAttribute(String localName,String value){
		setAttribute(new Attr(localName, value));
	}
	
	public void setAttribute(String localName,Expression value){
		setAttribute(new Attr(localName, value));
	}
	
	public Attr removeAttribute(String prefix,String localName){
		Attr a = getAttribute(prefix, localName);
		if(null != a){
			removeAttribute(a);
		}
		return a;
	}
	
	public void removeAttributes(String localName){
		List<Attr> removes = new ArrayList<Attr>();
		for(Attr a : attributes){
			if(a.getLocalName().equalsIgnoreCase(localName)){
				removes.add(a);
			}
		}
		for(Attr a : removes){
			attributes.remove(a);
		}
	}
	
	public Element findElement(Predicate<Element> cond) {
		return (Element)super.findNode((n) -> {
			if(n instanceof Element && cond.test((Element)n)) {
				return true;
			}
			return false;
		});
	}
	
	@Override
    protected void doLock() {
	    super.doLock();
	    if(null != attributes){
	    	this.attributes = Collections.unmodifiableList(attributes);
	    }
    }
	
	@Override
    protected Node doDeepClone(Node parent) {
		Element clone = new Element();
		
		clone.parent     = parent;
		clone.prefix     = prefix;
		clone.localName  = localName;
		clone.attributes = cloneAttrs();
		clone.childNodes = super.deepCloneChildNodes();
		clone.selfClosing = selfClosing;

		return clone;
    }
	
	public Element shallowClone(){
		Element clone = new Element();
		clone.prefix      = prefix;
		clone.localName   = localName;
		clone.attributes  = null == attributes ? null : new ArrayList<Attr>(attributes);
		clone.childNodes  = new ArrayList<Node>(childNodes);
		clone.selfClosing = selfClosing;
		return clone;
	}
	
	public List<Attr> cloneAttrs(){
		if(null == attributes){
			return null;
		}
		
		List<Attr> clones = new ArrayList<Attr>();
		
		for(Attr a : this.attributes){
			clones.add(a.clone());
		}
		
		return clones;
	}
	
	public void resolveProcessors(HtplEngine engine) {
	    if(isProcessorsResolved) {
	        throw new IllegalStateException("Processor aleady inited in this element : " + getQualifiedName());
	    }
	    isProcessorsResolved = true;
	    
	    engine.resolveElementProcessor(this);
	    
        if (null != attributes) {
            for (int i = 0; i < attributes.size(); i++) {
                Attr a = attributes.get(i);
                if (engine.resolveAttrProcessor(this, a)) {
                    //check empty
                    AttrProcessor attrTag = a.getProcessor();
                    if (attrTag.required() && Strings.isEmpty(a.getString())) {
                        throw new HtplParseException("Value must not be empty of attribute '" + a.getQualifiedName() + " in element '" + getQualifiedName() + "'");
                    }
                }
            }
        }
	}
	
	@Override
    protected Node doProcess(HtplEngine engine,HtplDocument doc, ProcessCallback callback) throws Throwable {
		Node node;
		
		if(!isProcessorsResolved) {
		    resolveProcessors(engine);
		}
		
		if(null != processor && !isStartElementProcessed){
			node = processor.processStartElement(engine, doc, this);
			this.isStartElementProcessed = true;
			if(null == node){
				return null;
			}
			if(node != this){
				return node.process(engine, doc, callback);
			}
		}
		
		if(null != attributes){
			List<Attr> attrs = new ArrayList<Attr>(attributes);
			for(Attr attr : attrs){
				if(attr.hasProcessor()){
					node = attr.getProcessor().processStartElement(engine, doc, this, attr);
					
					if(null == node){
						return null;
					}
					
					if(node != this){
						return node.process(engine, doc, callback);
					}
				}
			}
		}

		//Process childnodes
		super.doProcess(engine, doc, callback);
		
		if(null != attributes){
			List<Attr> attrs = new ArrayList<Attr>(attributes);
			for(Attr attr : attrs){
				if(attr.hasProcessor()){
					node = attr.getProcessor().processEndElement(engine, doc, this, attr);
					
					if(null == node){
						return null;
					}
					
					if(node != this){
						return node.process(engine, doc, callback);
					}
				}
			}
		}
		
		if(null != processor && !isEndElementProcessed){
			node = processor.processEndElement(engine, doc, this);
			this.isEndElementProcessed = true;
			if(null == node){
				return null;
			}
			if(node != this){
				return node.process(engine, doc, callback);
			}
		}
		
		return this;
    }
	
	@Override
    public void compile(HtplEngine engine, HtplDocument doc, HtplCompiler compiler) {
		compiler.startElement(prefix, localName);
		
		for(int i=0;i<attributes.size();i++){
			Attr a = attributes.get(i);
			
			if(a.isExpression()){
				compiler.attribute(a.getPrefix(), a.getOriginLocalName(), a.getExpression(), a.getQuotedCharacter(), a.isInlineExpression() ,a.getCondition());
			}else{
				compiler.attribute(a.getPrefix(), a.getOriginLocalName(), a.getString(), a.getQuotedCharacter(), a.isInlineExpression(), a.getCondition());
			}
		}
		
		if(selfClosing && (null == childNodes || childNodes.isEmpty())){
			compiler.closeElement();
		}else{
			compiler.html(">");
			
			for(int i=0;i<childNodes.size();i++){
				childNodes.get(i).compile(engine, doc, compiler);
			}
			
			compiler.closeElement(prefix, localName);
		}
    }

	@Override
    protected void doWriteTemplate(Appendable out) throws IOException {
		out.append('<');
		
		if(!Strings.isEmpty(prefix)){
			out.append(prefix).append(':');
		}
		
		out.append(localName);
		
		for(int i=0;i<attributes.size();i++){
			Attr a = attributes.get(i);
			out.append(' ');
			if(!Strings.isEmpty(a.getPrefix())){
				out.append(a.getPrefix()).append(':');
			}
			out.append(a.getLocalName()).append("=");
			
			if(null != a.getQuotedCharacter()){
				out.append(a.getQuotedCharacter());
			}
			
			out.append(Objects.toString(a.getValue(),Strings.EMPTY));

			if(null != a.getQuotedCharacter()){
				out.append(a.getQuotedCharacter());
			}
		}
		
		if(selfClosing && (null == childNodes || childNodes.isEmpty())){
			out.append("/>");
		}else{
			out.append('>');
			super.doWriteTemplate(out);
			out.append("</");
			if(null != prefix){
				out.append(prefix).append(':');
			}
			out.append(localName).append('>');
		}
    }
}