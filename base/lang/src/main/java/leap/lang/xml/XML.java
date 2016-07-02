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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.UnsupportedCharsetException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import leap.lang.Charsets;
import leap.lang.Exceptions;
import leap.lang.exception.NestedIOException;
import leap.lang.io.IO;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XML {
	private static final XMLInputFactory factory = XMLInputFactory.newInstance();
	static {
		factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
	}
	
	protected XML(){
		
	}
	
	public static XmlDocument parse(String text) throws NestedIOException {
		return parse(new StringReader(text));
	}
	
	public static XmlDocument parse(InputStream inputStream) throws NestedIOException {
		return parse(new InputStreamReader(inputStream,Charsets.UTF_8));
	}	
	
	public static XmlDocument parse(InputStream inputStream,String encoding) throws NestedIOException,UnsupportedCharsetException {
		return parse(new InputStreamReader(inputStream,Charsets.forName(encoding)));
	}
	
	public static XmlDocument parse(InputStream inputStream,String encoding,String location) throws NestedIOException,UnsupportedCharsetException {
		return parse(new InputStreamReader(inputStream,Charsets.forName(encoding)),location);
	}
	
	public static XmlDocument parse(Reader reader) throws NestedIOException {
		return parse(reader,null);
	}
	
	public static XmlDocument parse(Reader reader,String location) throws NestedIOException,XmlException {
		return new DomDocument(parseDocument(reader),location);
	}
	
	public static XmlDocument load(String resourceFile) throws NestedIOException {
		return load(Resources.getResource(resourceFile));
	}

	public static XmlDocument load(File file) {
		InputStreamReader reader = null;
		try{
			reader = new InputStreamReader(new FileInputStream(file),Charsets.UTF_8);
			
			Document doc = parseDocument(reader);
			
			return new DomDocument(doc,file.getAbsolutePath());
		}catch(IOException e){
			throw new NestedIOException(e.getMessage(),e);
		}finally{
			IO.close(reader);
		}
	}
	
	public static XmlDocument load(Resource resource) throws NestedIOException {
		InputStreamReader reader = null;
		try{
			reader = resource.getInputStreamReader();
			
			Document doc = parseDocument(reader);
			
			return new DomDocument(doc,null == resource.getURL() ? null : resource.getURL().toString());
		}catch(IOException e){
			throw new NestedIOException(e.getMessage(),e);
		}finally{
			IO.close(reader);
		}
	}
	
	public static XmlWriter createWriter(Writer out) {
		return createStaxWriter(out);
	}
	
	public static XmlReader createReader(Reader in) {
		return createStaxReader(in,null);
	}
	
	public static XmlReader createReader(String xml) {
		return createStaxReader(new StringReader(xml),null);
	}
	
	public static XmlReader createReader(InputStream in) {
		return createStaxReader(new InputStreamReader(in, Charsets.UTF_8),null);
	}
	
	public static XmlReader createReader(InputStream in, String encoding) {
		return createStaxReader(new InputStreamReader(in, Charsets.forName(encoding)),null);
	}
	
	public static XmlReader createReader(File file) {
		try{
			return createStaxReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8),file.getAbsolutePath());
		}catch(IOException e){
			throw new NestedIOException(e);
		}
	}
	
	public static XmlReader createReader(Resource resource) {
		try{
			String resourceLocation = resource.toString();
			return createStaxReader(new InputStreamReader(resource.getInputStream(), Charsets.UTF_8),resourceLocation);
		}catch(IOException e){
			throw new NestedIOException(e);
		}
	}
	
	public static XMLEventReader createEventReader(Reader in) {
        try {
            return factory.createXMLEventReader(in);
        } catch (XMLStreamException e) {
            throw new XmlException(e.getMessage(),e);
        }
	}
	
	static Document parseDocument(Reader reader) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			
            builder.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
					return new InputSource(new StringReader(""));
				}
			});
			
			return builder.parse(new InputSource(reader));
		} catch (IOException e){
			throw Exceptions.wrap(e);
		} catch (Exception e) {
			throw new XmlException(e.getMessage(),e);
		}
	}
	
	static XmlWriter createStaxWriter(Writer out) throws XmlException {
		return new XmlWriterStax(out);
	}
	
	static XmlReader createStaxReader(Reader in,String sourceLocation){
		return new XmlReaderStax(in,sourceLocation);
	}
}