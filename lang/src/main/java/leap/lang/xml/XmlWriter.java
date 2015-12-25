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

import java.io.Closeable;

import javax.xml.namespace.QName;

public interface XmlWriter extends Closeable {

	XmlWriter startDocument();
	
	XmlWriter startDocument(String version);
	
	XmlWriter startDocument(String version,String encoding);
	
	XmlWriter startElement(String localName);
	
	XmlWriter startElement(String namespaceURI,String localName);
	
	XmlWriter startElement(String prefix,String namespaceURI,String localName);
	
	XmlWriter startElement(QName qName);
	
	XmlWriter emptyElement(String localName);
	
	XmlWriter emptyElement(String namespaceURI,String localName);
	
	XmlWriter emptyElement(String prefix,String namespaceURI,String localName);
	
	XmlWriter emptyElement(QName qName);
	
	/**
	 * Writes the default namespace to the stream.
	 */
	XmlWriter namespace(String namespaceURI);
	
	XmlWriter namespace(String prefix,String namespaceURI);
	
	XmlWriter attribute(String localName,String value);
	
	XmlWriter attribute(String namespaceURI,String localName,String value);
	
	XmlWriter attribute(String prefix,String namespaceURI,String localName,String value);
	
	XmlWriter attribute(QName qName,String value);

	XmlWriter attributeOptional(String localName,String value);
	
	XmlWriter attributeOptional(String namespaceURI,String localName,String value);
	
	XmlWriter attributeOptional(String prefix,String namespaceURI,String localName,String value);
	
	XmlWriter attributeOptional(QName qName,String value);
	
	XmlWriter element(String localName,String text);
	
	XmlWriter element(String namespaceURI,String localName,String text);
	
	XmlWriter element(String prefix,String namespaceURI,String localName,String text);
	
	XmlWriter element(QName qName,String text);

	XmlWriter elementOptional(String localName,String text);
	
	XmlWriter elementOptional(String namespaceURI,String localName,String text);
	
	XmlWriter elementOptional(String prefix,String namespaceURI,String localName,String text);
	
	XmlWriter elementOptional(QName qName,String text);
	
	/**
	 * escape the text and write to output.
	 */
	XmlWriter text(String text);
	
	XmlWriter text(String text,boolean escape);
	
	XmlWriter comment(String comment);
	
	XmlWriter cdata(String text);
	
	XmlWriter endElement();
	
	XmlWriter endDocument();
	
	/**
	 * Write any cached data to the underlying output mechanism.
	 */
	XmlWriter flush();
}