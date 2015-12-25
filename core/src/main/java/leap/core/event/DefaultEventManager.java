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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import leap.lang.Args;
import leap.lang.Strings;
import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

public class DefaultEventManager implements EventManager {
	private static final Log log = LogFactory.get(DefaultEventManager.class);
	
	private final Object _lock = new Object();
	
	protected List<EventListener>			   allListeners      = new CopyOnWriteArrayList<EventListener>();
	protected Map<String, List<String>>        categoryEvents    = new ConcurrentHashMap<String, List<String>>();
	protected Map<String, List<EventListener>> categoryListeners = new ConcurrentHashMap<String, List<EventListener>>();
	protected Map<String, List<EventListener>> eventListeners    = new ConcurrentHashMap<String, List<EventListener>>();
	
	@Override
	public boolean isEventCategoryRegistered(String category){
		return categoryEvents.containsKey(category);
	}
	
	@Override
	public boolean isEventNameRegistered(String eventName) {
		return eventListeners.containsKey(eventName);
	}
	
	@Override
    public void registerEventCategory(String category) throws ObjectExistsException {
		Args.notEmpty(category,"category");
		
		if(Strings.equalsIgnoreCase(category, ALL_CATEGORY)){
			throw new IllegalStateException("event category '" + category + "' is reserved, can not use this category name");
		}
		
		synchronized (_lock) {
			
			if(categoryListeners.containsKey(category)){
				throw new ObjectExistsException("event category '" + category + "' aleady registered");
			}
			
			log.debug("Event category '{}' registered",category);
			
			categoryListeners.put(category,new CopyOnWriteArrayList<EventListener>());
			categoryEvents.put(category, new CopyOnWriteArrayList<String>());
        }
    }
	
	@Override
    public void registerEventName(String category,String eventName) throws ObjectExistsException {
		Args.notEmpty(category,"category");
		Args.notEmpty(eventName,"event name");
		
		if(Strings.equalsIgnoreCase(category, ALL_CATEGORY)){
			throw new IllegalStateException("event category '" + category + "' is reserved, can not use this category name");
		}
		
		synchronized (_lock) {
			if(eventListeners.containsKey(eventName)){
				throw new ObjectExistsException("event name '" + eventName + "' aleady registered");
			}
			
			if(!categoryListeners.containsKey(category)){
				registerEventCategory(category);
			}
			
			log.debug("Event name '{}' of category '{}' registered",eventName,category);
			
			categoryEvents.get(category).add(eventName);
			eventListeners.put(eventName,new CopyOnWriteArrayList<EventListener>());
        }
    }
	
	@Override
    public void addEventCategoryListener(String category, EventListener listener) throws ObjectNotFoundException {
		Args.notEmpty(category);
		Args.notNull(listener);
		
		if(Strings.equalsIgnoreCase(category, ALL_CATEGORY)){
			log.debug("Event listener '{}' listens all events",listener.getClass().getName());
			allListeners.add(listener);
			return;
		}
		
		synchronized (_lock) {
			List<EventListener> listeners = categoryListeners.get(category);
			
			if(null == listeners){
				throw new ObjectNotFoundException("event category '" + category + "' not register");
			}
			
			log.debug("Event listener '{}' listens events belong to category '{}'",listener.getClass().getName(),category);
			
			listeners.add(listener);
			
			List<String> eventNames = categoryEvents.get(category);
			for(String eventName : eventNames){
				eventListeners.get(eventName).add(listener);	
			}
        }
    }
	
	@Override
    public void addEventNameListener(String eventName, EventListener listener) throws ObjectNotFoundException {
		Args.notEmpty(eventName);
		Args.notNull(listener);
		
		synchronized (_lock) {
			List<EventListener> listeners = eventListeners.get(eventName);
			
			if(null == listeners){
				throw new ObjectNotFoundException("event name '" + eventName + "' not register");
			}
			
			log.debug("Event listener '{}' listens event '{}'",listener.getClass().getName(),eventName);
			
			listeners.add(listener);
        }
    }
	
	@Override
    public void fireEvent(Event event) throws ObjectNotFoundException {
		List<EventListener> listeners = eventListeners.get(event.getName());
		if(null == listeners){
			throw new ObjectNotFoundException("event name '" + event.getName() + "' not register");
		}
	
		if(!fireEvent(event, listeners)){
			return;
		}
		
		fireEvent(event, allListeners);
    }
	
	protected boolean fireEvent(Event event,List<EventListener> listeners){
		for(int i=0;i<listeners.size();i++){
			EventListener listener = listeners.get(i);
			
			try{
				listener.onEvent(event);
				
				if(event.isCancelled()){
					return false;
				}
				
			}catch(Exception e){
				throw new EventNotifyException("Error notify listener '" + listener.getClass().getName() + "' on event '" + event.getName() + "'",e) ;
			}
			
		}
		
		return true;
	}
}