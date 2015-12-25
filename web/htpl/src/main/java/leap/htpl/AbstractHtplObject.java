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
package leap.htpl;

import leap.lang.annotation.Internal;

@Internal
public abstract class AbstractHtplObject {
	
	private boolean locked; //a locked object cannot be modified.
	
	public final boolean isLocked(){
		return locked;
	}
	
	/**
	 * Marks this object's internal state to readonly.
	 * 
	 * @throws IllegalStateException if this object aleady locked.
	 */
	public final void lock() throws IllegalStateException {
		checkLocked();
		this.doLock();
		this.locked = true;
	}

	protected final void checkLocked(){
		if(locked){
			throw new IllegalStateException("This object is locked, cannot be modified");
		}
	}
	
	protected void doLock() {
		
	}
}
