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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import leap.lang.Charsets;
import leap.lang.Exceptions;
import leap.lang.Strings;
import leap.lang.exception.NestedIOException;
import leap.lang.resource.Resource;

import org.xml.sax.SAXException;

public class XmlValidator {
	
	public static XmlValidator of(Resource schemaResource) throws NestedIOException {
		try {
	        return of(schemaResource.getInputStream());
        } catch (IOException e) {
        	throw Exceptions.wrap(e);
        }
	}
	
	public static XmlValidator of(InputStream schemaStream) throws NestedIOException {
		return of(new InputStreamReader(schemaStream,Charsets.UTF_8));
	}
	
	public static XmlValidator of(Reader schemaReader) throws NestedIOException,XmlException {
		try {
	        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	        Source source = new StreamSource(schemaReader);
	        
	        Schema schema = factory.newSchema(source);
	        
	        return new XmlValidator(schema);
        } catch (SAXException e) {
        	throw new XmlException(Strings.format("Error create validator from schema : {0}",e.getMessage()),e);
        }
	}
	
	private Schema	schema;
	
	protected XmlValidator(Schema schema){
		this.schema = schema;
	}
	
	public void validate(DomDocument doc) throws XmlValidationException,NestedIOException {
		Validator validator = schema.newValidator();
		
		try {
			Source source = null;
	        if(null != doc.domDocument()){
	        	source = new DOMSource(doc.domDocument());
	        }else{
	        	source = new StreamSource(new StringReader(doc.toXml()));
	        }
	        validator.validate(source);
        } catch (SAXException e) {
        	throw new XmlValidationException(Strings.format("Error validating xml : {0} \n {1}",e.getMessage(),doc.location()),e);
        } catch (IOException e) {
        	throw Exceptions.uncheck(e);
        }
	}
}