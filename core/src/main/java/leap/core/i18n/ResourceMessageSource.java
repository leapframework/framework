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

import leap.core.AppConfig;
import leap.core.AppConfigAware;
import leap.core.AppResource;
import leap.core.annotation.Inject;
import leap.core.validation.annotations.NotNull;
import leap.lang.Strings;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceMessageSource extends AbstractMessageSource implements AppConfigAware {
	
	protected static final Message UNRESOLVED_MESSAGE = new Message("", "");
	
    protected @NotNull Locale                       defaultLocale;
    protected @NotNull Map<String, Message>         messages             = new HashMap<String, Message>();
    protected @Inject MessageReader[]               readers;
	
	private final Map<Locale, Map<String, Message>> cachedLocaleMessages = new ConcurrentHashMap<Locale, Map<String,Message>>();	

	@Override
    public void setAppConfig(AppConfig config) {
	    this.defaultLocale = config.getDefaultLocale();
    }
	
	@Override
    public String tryGetMessage(Locale locale, String key, Object... args) {
		if(null == locale){
			locale = defaultLocale;	
		}
		
		Map<String, Message> localeMessages = cachedLocaleMessages.get(locale);
		if(null == localeMessages){
			synchronized (this.cachedLocaleMessages) {
				localeMessages = new ConcurrentHashMap<>();
				cachedLocaleMessages.put(locale, localeMessages);
			}			
		}
		
		Message message = localeMessages.get(key);
		if(message == UNRESOLVED_MESSAGE){
			return null;
		}
		
		if(null == message){
			message = doGetMessage(key, locale);
			if(null == message){
				localeMessages.put(key, UNRESOLVED_MESSAGE);
				return null;
			}else{
				localeMessages.put(key, message);
			}
		}
		
	    return formatMessage(message, args);
    }
    
	protected String formatMessage(Message message,Object... args){
		if(args == null || args.length == 0){
			return message.getString();
		}else{
			return Strings.format(message.getString(), args);
		}
	}
	
	protected Message doGetMessage(String key,Locale locale) {
		String lang    = locale.getLanguage();
		String country = locale.getCountry();
		
		Message message;
		
		if(!Strings.isEmpty(country)){
			if(null != (message = messages.get(key + "_" + lang + "_" + country))){
				return message;
			}
		}

		if(null != (message = messages.get(key + "_" + lang))){
			return message;
		}
		
		return messages.get(key);
	}
	
	public ResourceMessageSource readFromResources(AppResource... resources) {
		if(resources.length > 0){
			MessageContext context = new DefaultMessageContext(false, messages);
			for(AppResource resource : resources){
				readFromResource(context, resource);
			}
		}
		return this;
	}
	
	protected void readFromResource(MessageContext context, AppResource ar){
		if(null != ar){
            context.setDefaultOverride(ar.isDefaultOverride());
			for(MessageReader reader : readers){
				if(reader.read(context,ar.getResource())){
					break;
				}
			}
            context.resetDefaultOverride();
		}
	}
}
