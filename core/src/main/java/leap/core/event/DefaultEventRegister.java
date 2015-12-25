/*
 * Copyright 2013 the original author or authors.
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
package leap.core.event;

import java.util.Map.Entry;

import leap.core.AppContext;
import leap.core.AppContextInitializable;
import leap.core.annotation.Inject;
import leap.core.ioc.BeanDefinition;
import leap.lang.Arrays2;

public class DefaultEventRegister implements AppContextInitializable {
	
	protected @Inject EventManager eventManager;
	
	@Override
    public void postInit(AppContext context) throws Exception {
		loadEventRegistrations(context);
	    loadListenerRegistrations(context);
    }
	
	protected void loadEventRegistrations(AppContext context){
		for(Entry<EventRegistration,BeanDefinition> entry : context.getBeanFactory().getBeansWithDefinition(EventRegistration.class).entrySet()){
			register(entry.getKey(),entry.getValue());
		}
	}
	
	protected void loadListenerRegistrations(AppContext context){
		for(Entry<EventListenerRegistration,BeanDefinition> entry : context.getBeanFactory().getBeansWithDefinition(EventListenerRegistration.class).entrySet()){
			register(entry.getKey(),entry.getValue());
		}
	}
	
	protected void register(EventRegistration reg,BeanDefinition bd){
		String category = reg.getCategory();
		
		String[] eventNames = reg.getEventNames();
		if(null == eventNames || eventNames.length == 0){
			eventManager.registerEventCategory(category);
		}else{
			for(String eventName : eventNames){
				if(eventManager.isEventNameRegistered(eventName)){
					throw new EventRegistrationException("event name '" + eventName + "' aleady registered, check the event registration bean in source : " + bd.getSource());
				}
				eventManager.registerEventName(category, eventName);
			}
		}
	}
	
	protected void register(EventListenerRegistration reg,BeanDefinition bd){
		String[] categories = reg.getCategories();
		String[] eventNames = reg.getEventNames();
		
		if(Arrays2.isEmpty(categories) && Arrays2.isEmpty(eventNames)){
			throw new EventRegistrationException("'categories' or 'eventNames' must be defined in listener registration, source : " + bd.getSource());
		}

		EventListener listener = reg.getListener();
		
		if(null != categories){
			for(String category : categories){
				if(!eventManager.isEventCategoryRegistered(category)){
					throw new EventRegistrationException("event category '" + category + "' not register, make sure you type the correct name, source : " + bd.getSource());
				}
				eventManager.addEventCategoryListener(category, listener);
			}	
		}
		
		if(null != eventNames){
			for(String eventName : eventNames){
				if(!eventManager.isEventNameRegistered(eventName)){
					throw new EventRegistrationException("event name '" + eventName + "' not register, make sure you type the correct name, source : " + bd.getSource());
				}
				eventManager.addEventNameListener(eventName, listener);
			}
		}
	}
}
