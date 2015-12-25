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

public interface Event {
	
	/**
	 * Returns the event name.
	 */
	String getName();
	
	/**
	 * Returns the event category name.
	 */
	String getCategory();
	
	/**
	 * May be null.
	 */
	Object getContext();
	
	/**
	 * Returns the object which fires this event.
	 */
	Object getSource();

	/**
	 * Returns <code>true</code> if this event is cancelable.
	 * 
	 * <p>
	 * invoke the {@link #cancel()} method will cancel this event if cancelable. 
	 */
	boolean cancelable();
	
	/**
	 * Returns <code>true</code> if this event was cancelled.
	 */
	boolean isCancelled();
	
	/**
	 * Cancels this event
	 * 
	 * <p>
	 * throws {@link IllegalStateException} if this event is not cancelable
	 */
	void cancel() throws IllegalStateException;
}