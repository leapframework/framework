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

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import leap.lang.io.IO;

class XmlWriterStax extends XmlWriterBase {
	
	private static XMLOutputFactory factory = XMLOutputFactory.newInstance();
	
	private final Writer		  out;
	private final XMLStreamWriter writer;
	
	XmlWriterStax(Writer out) {
		try {
			this.out    = out;
	        this.writer = factory.createXMLStreamWriter(out);
        } catch (Exception e) {
        	throw new XmlException("Error create XMLStreamWriter : " + e.getMessage(),e);
        }
    }
	
	public XmlWriter startDocument() {
	    try {
	        writer.writeStartDocument();
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }
	
	public XmlWriter startDocument(String version) {
	    try {
	        writer.writeStartDocument(version);
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }

	public XmlWriter startDocument(String version, String encoding) {
	    try {
	        writer.writeStartDocument(version,encoding);
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }
	
	public XmlWriter startElement(String localName) {
	    try {
	        writer.writeStartElement(localName);
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }
	
	public XmlWriter startElement(String namespaceURI, String localName) {
	    try {
	        writer.writeStartElement(namespaceURI,localName);
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }
	
	public XmlWriter startElement(String prefix, String namespaceURI, String localName) {
	    try {
	        writer.writeStartElement(prefix,localName,namespaceURI);
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }
	
	public XmlWriter emptyElement(String localName) {
	    try {
	        writer.writeEmptyElement(localName);
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }
	
	public XmlWriter emptyElement(String namespaceURI, String localName) {
	    try {
	        writer.writeEmptyElement(namespaceURI, localName);
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }
	
	public XmlWriter emptyElement(String prefix, String namespaceURI, String localName) {
	    try {
	        writer.writeEmptyElement(prefix, localName, namespaceURI);
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }

	public XmlWriter namespace(String namespaceURI) {
	    try {
	        writer.writeDefaultNamespace(namespaceURI);
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }
	
	public XmlWriter namespace(String prefix, String namespaceURI) {
	    try {
	        writer.writeNamespace(prefix, namespaceURI);
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }
	
	public XmlWriter attribute(String localName, String value) {
	    try {
	        writer.writeAttribute(localName, value);
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }
	
	public XmlWriter attribute(String namespaceURI, String localName, String value) {
	    try {
	        writer.writeAttribute(namespaceURI, localName, value);
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }

	public XmlWriter attribute(String prefix, String namespaceURI, String localName, String value) {
	    try {
	        writer.writeAttribute(prefix, namespaceURI, localName, value);
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }
	
	public XmlWriter text(String text) {
		//TODO : escape text
	    try {
	        writer.writeCharacters(text);
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }
	
	public XmlWriter text(String text, boolean escape) {
		//TODO : escape text
	    try {
	        writer.writeCharacters(text);
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }
	
	public XmlWriter cdata(String text) {
	    try {
	        writer.writeCData(text);
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }

	public XmlWriter comment(String comment) {
	    try {
	        writer.writeComment(comment);
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }

	public XmlWriter endElement() {
	    try {
	        writer.writeEndElement();
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }
	
	public XmlWriter endDocument() {
	    try {
	        writer.writeEndDocument();
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }
	
	public XmlWriter flush() {
	    try {
	        writer.flush();
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }
	    return this;
    }
	
	@Override
    public void close() throws IOException {
		IO.close(out);
	    try {
	        writer.close();
        } catch (XMLStreamException e) {
        	throw wrap(e);
        }	    
    }

	private static XmlException wrap(XMLStreamException e){
		return new XmlException(e.getMessage(),e);
	}
}
