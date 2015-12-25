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

import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Properties;

import leap.core.AppConfig;
import leap.core.AppConfigAware;
import leap.core.AppConfigException;
import leap.lang.Locales;
import leap.lang.Strings;
import leap.lang.resource.Resource;

public class PropertiesMessageReader implements MessageReader,AppConfigAware {
	
	protected Charset charset;
	
	@Override
    public void setAppConfig(AppConfig config) {
		this.charset = config.getDefaultCharset();
    }

	@Override
	public boolean read(MessageContext context, Resource resource) {
		if(Strings.endsWith(resource.getFilename(), ".properties")){
			readMessages(context, resource);
		}
		return false;
	}
	
	protected void readMessages(MessageContext context,Resource resource) {
		Properties props = new Properties();
		try {
			String localeName = null; 
			if(null == localeName){
				localeName = Locales.extractFromFilename(resource.getFilename());
			}
			Locale locale = Strings.isEmpty(localeName) ? null : Locales.forName(localeName);
			
			try(Reader reader = resource.getInputStreamReader(charset)){
				props.load(reader);	
			}
	        
	        for(Object key : props.keySet()){
	        	readMessage(context, resource, locale, props, (String)key);
	        }
	        
        } catch (Exception e) {
        	throw new AppConfigException("Error reading messages from properties resource [" + 
        							    resource.getURLString() + "], " + e.getMessage(), e);
        }
	}
	
	protected void readMessage(MessageContext context,Resource resource,Locale locale,Properties props,String name) {
		Message message = context.tryGetMessage(locale, name);
		
		if(null != message){
			throw new AppConfigException("Message key '" + name + "' in locale '" + locale + 
										 "' aleady exists in '" + message.getSource() + 
										 "', check the file : " + resource.getURLString());	
		}
		
		context.addMessage(locale, name, new Message(resource.getURLString(), props.getProperty(name)));
	}
}
