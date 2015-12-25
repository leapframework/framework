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

import org.w3c.dom.Document;

/**
 * A wrapper of w3c {@link Document}.
 */
public class DomDocument implements XmlDocument {

	protected final String	   location;
	protected final Document   document;
	protected final XmlElement root;
	
	public DomDocument(Document document){
		this(document,"<inline>");
	}
	
	public DomDocument(Document document,String location){
		this.document = document;
		this.location = location;
		this.root     = new DomElement(this,document.getDocumentElement());
	}
	
	/**
	 * Returns the location of this document.
	 */
	@Override
    public String location(){
		return location;
	}
	
	/**
	 * Returns the wrapped w3c dom {@link Document}.
	 */
	public Document domDocument(){
		return document;
	}
	
	/**
	 * Returns the root element.
	 */
	@Override
    public XmlElement rootElement(){
		return root;
	}
	
	/**
	 * Returns a string indicates the xml content of this document.
	 */
	@Override
    public String toXml(){
		return document.toString();
	}

	@Override
    public String toString() {
	    return document.toString();
    }
}