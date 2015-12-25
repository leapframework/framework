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

import leap.lang.Args;

public class DefaultEvent implements Event {
	
	protected final String  name;
	protected final String  category;
	protected final Object  context;
	protected final Object  source;
	protected final boolean cancelable;
	
	private boolean cancelled;
	
	public DefaultEvent(String name,String category,Object source){
		this(name,category,source,false);
	}
	
	public DefaultEvent(String name,String category,Object context,Object source){
		this(name,category,context,source,false);
	}
	
	public DefaultEvent(String name,String category,Object context,Object source,boolean cancelable){
		Args.notEmpty(name,"event name");
		Args.notEmpty(category,"event category");
		Args.notNull(source,"event source");
		
		this.name       = name;
		this.category   = category;
		this.context    = context;
		this.source     = source;
		this.cancelable = cancelable;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
    public String getCategory() {
	    return category;
    }

	@Override
	public Object getContext() {
		return context;
	}

	@Override
	public Object getSource() {
		return source;
	}

	@Override
	public boolean cancelable() {
		return cancelable;
	}
	
	@Override
    public boolean isCancelled() {
	    return cancelled;
    }

	@Override
	public void cancel() throws IllegalStateException {
		if(!cancelable){
			throw new IllegalStateException("this event '" + name + "' is not cancelable");
		}
		if(cancelled){
			throw new IllegalStateException("this event '" + name + "' aleady canceled");
		}
		this.cancelled = true;
	}
}
