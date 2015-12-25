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
import java.io.Reader;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.XMLEvent;

import leap.lang.io.IO;

final class XmlReaderStax extends XmlReaderBase implements XmlReader {
	private final Reader         in;
    private final XMLEventReader real;
    
    private XMLEvent event;
    
    public XmlReaderStax(Reader in) {
    	this(in,null);
    }
    
    public XmlReaderStax(Reader in,String sourceLocation) {
    	this.in   = in;
    	this.real = XML.createEventReader(in);
        if(null != sourceLocation){
        	this.source = sourceLocation;	
        }
    }    
    
	public boolean isEndElement() {
	    return null != event && event.isEndElement();
    }

	public boolean isEndElement(QName name) {
	    return isEndElement() && event.asEndElement().getName().equals(name);
    }
	
	@Override
    public boolean isProcessingInstruction() {
	    return null != event && event.isProcessingInstruction();
    }

	@Override
    public boolean isProcessingInstruction(String target) {
	    return isProcessingInstruction() && getProcessingInstructionTarget().equals(target);
    }

	public boolean isStartElement() {
	    return null != event && event.isStartElement();
    }

	public boolean isStartElement(QName name) {
	    return isStartElement() && event.asStartElement().getName().equals(name);
    }
	
	public boolean isStartElement(String name) {
		return isStartElement() && event.asStartElement().getName().getLocalPart().equals(name);
	}

	public boolean isEndElement(String name) {
		return isEndElement() && event.asEndElement().getName().getLocalPart().equals(name);
	}

	public boolean isEndDocument() {
	    return null != event && event.isEndDocument();
    }
	
	public String getProcessingInstructionTarget() {
		return ((ProcessingInstruction)event).getTarget();
	}
	
	public String getProcessingInstructionContent() {
		return ((ProcessingInstruction)event).getData();
	}
	
    public QName getElementName() {
	    return event.asStartElement().getName();
    }
    
	public String getElementLocalName() {
		return event.asStartElement().getName().getLocalPart();
	}
	
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<String> getAttributeNames() {
	    return event.asStartElement().getAttributes();
    }

	@Override
    public boolean hasAttribute(QName name) {
		return null == event ? false : event.asStartElement().getAttributeByName(name) != null;
    }

	@Override
	 @SuppressWarnings("rawtypes")
    public boolean hasAttribute(String localName) {
    	if(null == event){
    		return false;
    	}
    	
    	Iterator attrs = event.asStartElement().getAttributes();
    	
    	while(attrs.hasNext()){
    		Attribute attr = (Attribute)attrs.next();

    		if(attr.getName().getLocalPart().equals(localName)){
    			return true;
    		}
    	}

    	return false;
    }

	@Override
	public String doGetElementTextAndEnd() {
        try {
            return real.getElementText();
        } catch (XMLStreamException e) {
        	throw new XmlException(e.getMessage(),e);
        }
    }
    
	@Override
	public String doGetAttribute(QName name) {
        Attribute attr = attr(name);
        return attr == null ? null : attr.getValue();
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public String doGetAttribute(String name) {
    	if(null == event){
    		return null;
    	}
    	
    	Iterator attrs = event.asStartElement().getAttributes();
    	
    	while(attrs.hasNext()){
    		Attribute attr = (Attribute)attrs.next();

    		if(attr.getName().getLocalPart().equals(name)){
    			return attr.getValue();
    		}
    	}

    	return null;
    }
    
	public boolean next() {
    	try {
            if(real.hasNext()){
            	event = real.nextEvent();
            	return true;
            }
            return false;
        } catch (XMLStreamException e) {
        	throw new XmlException(e.getMessage(),e);
        }
    }
	
	@Override
    public void close() throws IOException {
		IO.close(in);
		try {
	        real.close();
        } catch (XMLStreamException e) {
        	throw new XmlException(e.getMessage(),e);
        }
    }
	
	@Override
    public int getLineNumber() {
	    return null == event ? -1 : event.getLocation().getLineNumber();
    }

	private Attribute attr(QName name){
		return null == event ? null : event.asStartElement().getAttributeByName(name);
	}

	@Override
    public String toString() {
		return null == event ? "No event" : (event.getClass().getSimpleName() + " : " + event.toString());
    }
}