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
package leap.core.sys;

import leap.lang.Args;
import leap.lang.Sourced;

public class SysPermissionDef implements Sourced {

	private final Object   						 source;
	private final Class<? extends SysPermission> permType;
	private final SysPermission 				 permObject;
	private final boolean  					     granted;
	
	public SysPermissionDef(Object source, Class<? extends SysPermission> permType, SysPermission permObject, boolean granted) {
	    Args.notNull(permType);
	    Args.notNull(permObject);
	    
		this.source     = source;
	    this.permType   = permType;
	    this.permObject = permObject; 
	    this.granted    = granted;
    }
	
	@Override
    public Object getSource() {
	    return source;
    }

	public Class<?> getPermType() {
		return permType;
	}

	public SysPermission getPermObject() {
		return permObject;
	}

	public boolean isGranted() {
		return granted;
	}

	public boolean isDenied(){
		return !granted;
	}

	@Override
    public String toString() {
	    StringBuilder sb = new StringBuilder();
	    
	    sb.append(granted ? "grant" : "deny")
	      .append(" permission[")
	      .append("class=").append(permObject.getClass().getName())
	      .append(",name=").append(permObject.getName())
	      .append(",actions=").append(permObject.getActions())
	      .append("]");
	    
	    return sb.toString();
    }
}