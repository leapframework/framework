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
package leap.lang.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A wrapper of w3c dom {@link Element}.
 */
public class DomElement implements XmlElement {
	
	protected final XmlDocument document;
	protected final Element 	element;

	public DomElement(XmlDocument document, Element element) {
		this.document = document;
		this.element  = element;
	}
	
	/**
	 * Returns the wrapped w3c dom {@link Element}.
	 */
	public Element domElement(){
		return element;
	}
	
	/**
	 * Returns the owner element of this element.
	 */
	@Override
    public XmlDocument document(){
		return document;
	}
	
	/**
	 * Returns the first child element of the given name, or <code>null</code> if not found.
	 */
	@Override
    public XmlElement childElement(String localName){
		NodeList childNodes = element.getChildNodes();
		for(int i=0;i<childNodes.getLength();i++){
			Node node = childNodes.item(i);
			if(node instanceof Element){
				Element e = (Element)node;
				if(e.getLocalName().equals(localName)){
					return new DomElement(document,(Element)node);	
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the text of this element.
	 * 
	 * @see Element#getTextContent().
	 */
	@Override
    public String text(){
		return element.getTextContent();
	}
	
}