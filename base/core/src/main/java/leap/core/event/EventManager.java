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

import leap.lang.exception.ObjectExistsException;
import leap.lang.exception.ObjectNotFoundException;

public interface EventManager {
	
	/**
	 * A default category name indicates all events.
	 * 
	 * <p>
	 * An event listener listen on this category will receives all the events fired by this event manager.
	 */
	public String ALL_CATEGORY = "all";
	
	/**
	 * Returns <code>true</code> if the given category aleady registered.
	 */
	boolean isEventCategoryRegistered(String category);
	
	/**
	 * Returns <code>true</code> if the given event name aleady registered.
	 */
	boolean isEventNameRegistered(String eventName);
	
	/**
	 * Register an event category.
	 * 
	 * <p>
	 * Throws {@link ObjectExistsException} if the given category aleady registered.
	 */
	void registerEventCategory(String category) throws ObjectExistsException;
	
	/**
	 * Registers an event name.
	 * 
	 * <p>
	 * Automatic register the category if the given category was not registered, 
	 *
	 * <p>
	 * Throws {@link ObjectExistsException} if the given event name aleady registered.
	 */
	void registerEventName(String category,String eventName) throws ObjectExistsException;
	
	/**
	 * Adds a listener to the listener's list of the given event name.
	 * 
	 * <p>
	 * Throws {@link ObjectNotFoundException} if the given event name not exists.
	 */
	void addEventNameListener(String eventName,EventListener listener) throws ObjectNotFoundException;
	
	/**
	 * <p>
	 * Throws {@link ObjectNotFoundException} if the given event category not exists.
	 * 
	 */
	void addEventCategoryListener(String category,EventListener listener) throws ObjectNotFoundException;
	
	/**
	 * Fires the given event to it's listeners.
	 * 
	 * <p>
	 * Throws {@link ObjectNotFoundException} if the given event's name not exists.
	 */
	void fireEvent(Event event) throws ObjectNotFoundException;
	
}