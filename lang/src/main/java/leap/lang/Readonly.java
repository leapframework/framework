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
package leap.lang;

import java.io.Serializable;

import leap.lang.exception.ReadonlyException;

public final class Readonly implements Serializable {

	private static final long serialVersionUID = -5043085032440298812L;
	
	private boolean enabled;
	private String  message;
	
	public Readonly(){
		
	}
	
	public Readonly(Object object){
		this("Object '" + object.toString() + "' is readonly");
	}
	
	public Readonly(Class<?> cls){
		this("Class '" + cls.getName() + "' is readonly");
	}
	
	public Readonly(String message){
		this.message = message;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public void enable(){
		this.enabled = true;
	}
	
	public void disable(){
		this.enabled = false;
	}

	public Readonly check() throws ReadonlyException {
		if(enabled){
			throw new ReadonlyException(null == message ? "object readonly" : message);
		}
		return this;
	}
	
	public Readonly check(String message) throws ReadonlyException {
		if(enabled){
			throw new ReadonlyException(message);
		}
		return this;
	}
}