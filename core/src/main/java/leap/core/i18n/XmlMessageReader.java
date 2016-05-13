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
package leap.core.i18n;

import java.util.Locale;

import leap.core.AppConfigException;
import leap.lang.Locales;
import leap.lang.Strings;
import leap.lang.io.IO;
import leap.lang.resource.Resource;
import leap.lang.resource.Resources;
import leap.lang.xml.XML;
import leap.lang.xml.XmlReader;

public class XmlMessageReader implements MessageReader {
	
	private static final String MESSAGES_ELEMENT           = "messages";
	private static final String IMPORT_ELEMENT             = "import";
	private static final String MESSAGE_ELEMENT            = "message";
	private static final String RESOURCE_ATTRIBUTE         = "resource";
	private static final String OVERRIDE_ATTRIBUTE         = "override";
	private static final String KEY_ATTRIBUTE              = "key";
	private static final String CHECK_EXISTENCE_ATTRIBUTE  = "check-existence";
	private static final String DEFAULT_OVERRIDE_ATTRIBUTE = "default-override";
	private static final String VALUE_ATTRIBUTE            = "value";

	@Override
    public boolean read(MessageContext context,Resource resource) {
		if(Strings.endsWith(resource.getFilename(), ".xml", true)){
			loadMessages(context, resource, context.isDefaultOverride(), null);
			return true;
		}
		return false;
    }
	
	protected void loadMessages(MessageContext context,Resource resource,boolean defaultOverride,String locale){
		String filename = resource.getFilename();
		
		String resourceLocale = locale; 
		if(null == resourceLocale){
			resourceLocale = Locales.extractFromFilename(filename);
		}
		
		if(resource.isReadable() && resource.exists()){
			XmlReader reader = null;
			try{
				String resourceUrl = resource.getURL().toString();
				
				if(context.containsResourceUrl(resourceUrl)){
					throw new AppConfigException("Cycle importing detected, please check your config : " + resourceUrl);
				}
				
				reader = XML.createReader(resource);

				context.addResourceUrl(resourceUrl);
				
				loadMessages(context, resource, reader, resourceLocale, defaultOverride);
			}catch(AppConfigException e){
				throw e;
			}catch(Exception e){
				throw new AppConfigException("Error loading message from 'classpath:" + resource.getClasspath() + "', msg : " + e.getMessage(),e);
			}finally{
				IO.close(reader);
			}
		}
	}	
	
	protected void loadMessages(MessageContext context, Resource resource, XmlReader reader, String locale, boolean defaultOverride){
		boolean foundValidRootElement = false;
		
		while(reader.next()){
			if(reader.isStartElement(MESSAGES_ELEMENT)){
				foundValidRootElement = true;
				
				while(reader.next()){
					if(reader.isStartElement(IMPORT_ELEMENT)){
						boolean checkExistence = reader.getBooleanAttribute(CHECK_EXISTENCE_ATTRIBUTE, true);
						boolean importDefaultOverride = reader.getBooleanAttribute(DEFAULT_OVERRIDE_ATTRIBUTE, defaultOverride);
						String importResourceName = reader.getRequiredAttribute(RESOURCE_ATTRIBUTE);
						
						Resource importResource = Resources.getResource(resource,importResourceName);
						
						if(null == importResource || !importResource.exists()){
							if(checkExistence){
								throw new AppConfigException("the import resource '" + importResourceName + "' not exists, source : " + reader.getSource());	
							}
						}else{
							loadMessages(context, resource, importDefaultOverride, locale);
							reader.nextToEndElement(IMPORT_ELEMENT);
						}
						continue;
					}
					
					if(reader.isStartElement(MESSAGE_ELEMENT)){
						readMessage(context, resource, reader, locale, defaultOverride);
						continue;
					}
				}
				break;
			}
		}
		
		if(!foundValidRootElement){
			throw new AppConfigException("valid root element not found in file : " + resource.getClasspath());
		}
	}
	
	protected void readMessage(MessageContext context, Resource resource, XmlReader reader, String localeName, boolean defaultOverride){
		String  key      = reader.getAttribute(KEY_ATTRIBUTE);
		String  value    = reader.getAttribute(VALUE_ATTRIBUTE);
		boolean override = reader.getBooleanAttribute(OVERRIDE_ATTRIBUTE, defaultOverride);
		
		if(Strings.isEmpty(value)){
			value = Strings.trim(reader.getElementTextAndEnd());
		}
		
		if(Strings.isEmpty(key)){
			throw new AppConfigException("The 'key' attribute must not be empty in 'message' element, source : " + reader.getSource());
		}
		
		if(Strings.isEmpty(value)){
			throw new AppConfigException("The 'value' attribute of text content must not be empty in 'message' element, source : " + reader.getSource());
		}
		
		Locale locale = Strings.isEmpty(localeName) ? null : Locales.forName(localeName);
		
		if(!override){
			Message message = context.tryGetMessage(locale, key);
			
			if(null != message){
				throw new AppConfigException("Message key '" + key + "' in locale '" + locale + 
											 "' aleady exists in '" + message.getSource() + 
											 "', check the file : " + reader.getSource());	
			}
		}
		
		context.addMessage(locale, key, new Message(reader.getSource(), value));
 	}
}
