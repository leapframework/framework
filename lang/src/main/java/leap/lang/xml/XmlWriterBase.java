/*
 * Copyright 2012 the original author or authors.
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

import javax.xml.namespace.QName;

import leap.lang.Strings;

abstract class XmlWriterBase implements XmlWriter {

	public XmlWriter attributeOptional(String localName, String value) {
		if(!Strings.isEmpty(value)){
			attribute(localName,value);
		}
		return this;
    }

	public XmlWriter attributeOptional(String namespaceURI, String localName, String value) {
		if(!Strings.isEmpty(value)){
			attribute(namespaceURI,localName,value);
		}
		return this;
    }
	
	public XmlWriter attributeOptional(String prefix, String namespaceURI, String localName, String value) {
		if(!Strings.isEmpty(value)){
			attribute(prefix,namespaceURI,localName,value);
		}
		return this;
    }

	public XmlWriter element(String localName, String text) {
	    return startElement(localName).text(text).endElement();
    }

	public XmlWriter element(String namespaceURI, String localName, String text) {
	    return startElement(namespaceURI, localName).text(text).endElement();
    }
	
	public XmlWriter element(String prefix, String namespaceURI, String localName, String text) {
	    return startElement(prefix, namespaceURI, localName).text(text).endElement();
    }

	public XmlWriter elementOptional(String localName, String text) {
		if(!Strings.isEmpty(text)){
			element(localName,text);
		}
		return this; 
	}

	public XmlWriter elementOptional(String namespaceURI, String localName, String text) {
		if(!Strings.isEmpty(text)){
			element(namespaceURI,localName,text);
		}
		return this;
    }

	public XmlWriter elementOptional(String prefix, String namespaceURI, String localName, String text) {
		if(!Strings.isEmpty(text)){
			element(prefix, namespaceURI, localName, text);
		}
		return this;
    }
	
	public XmlWriter attribute(QName qName, String value) {
		return Strings.isEmpty(qName.getPrefix()) ?
					attribute(qName.getNamespaceURI(), qName.getLocalPart(), value) :
					attribute(qName.getPrefix(), qName.getNamespaceURI(), qName.getLocalPart(), value);
    }

	public XmlWriter attributeOptional(QName qName, String value) {
		return Strings.isEmpty(qName.getPrefix()) ?
					attributeOptional(qName.getNamespaceURI(), qName.getLocalPart(), value) :
					attributeOptional(qName.getPrefix(), qName.getNamespaceURI(), qName.getLocalPart(), value);
    }

	public XmlWriter element(QName qName, String text) {
		return Strings.isEmpty(qName.getPrefix()) ?
					element(qName.getNamespaceURI(), qName.getLocalPart(), text) :
					element(qName.getPrefix(), qName.getNamespaceURI(), qName.getLocalPart(), text);
    }

	public XmlWriter elementOptional(QName qName, String text) {
		return Strings.isEmpty(qName.getPrefix()) ?
					elementOptional(qName.getNamespaceURI(), qName.getLocalPart(), text) :
					elementOptional(qName.getPrefix(), qName.getNamespaceURI(), qName.getLocalPart(), text);
    }

	public XmlWriter emptyElement(QName qName) {
		return Strings.isEmpty(qName.getPrefix()) ?
					emptyElement(qName.getNamespaceURI(), qName.getLocalPart()) :
					emptyElement(qName.getPrefix(), qName.getNamespaceURI(), qName.getLocalPart());
    }

	public XmlWriter startElement(QName qName) {
		return Strings.isEmpty(qName.getPrefix()) ?
					startElement(qName.getNamespaceURI(), qName.getLocalPart()) :
					startElement(qName.getPrefix(), qName.getNamespaceURI(), qName.getLocalPart());
    }
}
