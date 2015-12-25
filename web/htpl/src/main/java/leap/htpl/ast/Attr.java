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

import leap.htpl.AbstractHtplObject;
import leap.htpl.processor.AttrProcessor;
import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.expression.Expression;

public class Attr extends AbstractHtplObject {
	public static final String XMLNS = "xmlns"; 
	
	private boolean    	  namespace;	    //indicates this attribute is xmlns attribute
	private String     	  namespacePrefix;	//indicates the 'prefix' part in 'xmlns:{prefix}'
	private String     	  prefix;			//parsed prefix
	private String     	  localName;		//parsed local name
	private String     	  string;		    //parsed value
	private Character	  quotedCharacter = '"'; //quoted character
	private Expression    expression;	    //null if not an expression.
	private Expression	  condition;
	private AttrProcessor processor;
	private boolean		  inlineExpression = true;
	
	protected Attr(){
		
	}
	
	public Attr(String localName,String value){
		this(null,localName,value);
	}
	
	public Attr(String localName,Expression value){
		this(null,localName,value);
	}
	
	public Attr(String prefix,String localName,String value){
		this.string = value;
		this.setPrefix(prefix);
		this.setLocalName(localName);
	}
	
	public Attr(String prefix,String localName,Expression value){
		this.expression = value;
		this.setPrefix(prefix);
		this.setLocalName(localName);
	}
	
	public String getQualifiedName(){
		return Strings.isEmpty(this.prefix) ? this.localName :  (this.prefix + ":" + this.localName);
	}
	
	/**
	 * Returns <code>true</code> if this attribute is a xmlns attribute.
	 */
	public boolean isNamespace(){
		return namespace;
	}
	
	public boolean isEmpty() {
		return null == expression && Strings.isEmpty(string);
	}
	
	public String getNamespacePrefix(){
		return namespacePrefix;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public void setPrefix(String prefix){
		checkLocked();
		this.prefix = prefix;
		evalIsNamespace();
	}

	public String getLocalName() {
		return localName;
	}
	
	public void setLocalName(String localName){
		checkLocked();
		Args.notEmpty(localName,"local name");
		this.localName = localName;
		evalIsNamespace();
	}
	
	/**
	 * Returns a {@link String} or {@link Expression} indicates the value of this attribute.
	 */
	public Object getValue(){
		return null == expression ? string : expression;
	}
	
	public void setValue(String value){
		checkLocked();
		this.string     = value;
		this.expression = null;
	}
	
	public void setValue(Expression value) {
		checkLocked();
		this.expression = value;
		this.string     = null;
	}
	
	public String getString(){
		return string;
	}
	
	public Character getQuotedCharacter() {
		return quotedCharacter;
	}

	public void setQuotedCharacter(Character quotedCharacter) {
		this.quotedCharacter = quotedCharacter;
	}

	public boolean isInlineExpression() {
		return inlineExpression;
	}

	public void setInlineExpression(boolean inlineExpression) {
		this.inlineExpression = inlineExpression;
	}

	/**
	 * Returns the value {@link Expression} of this attribute or 
	 * <code>null</code> if this attribute's value is not an expression.
	 */
	public Expression getExpression(){
		return expression;
	}

	public boolean isExpression(){
		return null != expression;
	}
	
	public Expression getCondition() {
		return condition;
	}

	public void setCondition(Expression condition) {
		this.condition = condition;
	}
	
	public boolean hasCondition(){
		return null != condition;
	}

	public boolean hasProcessor(){
		return null != processor;
	}

	public AttrProcessor getProcessor() {
		return processor;
	}

	public void setProcessor(AttrProcessor processor) {
		checkLocked();
		this.processor = processor;
	}
	
	private void evalIsNamespace(){
		if(null == this.prefix){
			this.namespace = XMLNS.equalsIgnoreCase(localName);
		}else{
			this.namespace 	     = XMLNS.equalsIgnoreCase(prefix);
			this.namespacePrefix = namespace ? this.localName : null;
		}
	}
	
    public Attr clone() {
		Attr clone = new Attr();
		
		clone.prefix           = prefix;
		clone.localName        = localName;
		clone.namespace        = namespace;
		clone.namespacePrefix  = namespacePrefix;
		clone.processor        = processor;
		clone.string     	   = string;
		clone.quotedCharacter  = quotedCharacter;
		clone.expression 	   = expression;
		clone.condition		   = condition;
		clone.inlineExpression = inlineExpression;
		
	    return clone;
    }
	
	@Override
    public String toString() {
		StringBuilder sb = new StringBuilder();
		if(!Strings.isEmpty(prefix)){
			sb.append(prefix).append(':');
		}
		sb.append(localName);
		sb.append("=");
		
		if(null != quotedCharacter){
			sb.append(quotedCharacter);
		}
		
		if(null != string){
			sb.append(string);
		}else if(null != expression){
			sb.append(expression.toString());
		}

		if(null != quotedCharacter){
			sb.append(quotedCharacter);
		}
		
		return sb.toString();
	}
}