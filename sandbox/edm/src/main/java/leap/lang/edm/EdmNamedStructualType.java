/*
 * Copyright 2012 the original author or authors.
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
package leap.lang.edm;

import leap.lang.Named;
import leap.lang.Strings;

public abstract class EdmNamedStructualType extends EdmStructualType implements Named {

	protected final String name;
	protected final String title;
	
	protected final boolean isAbstract;
	
	protected EdmNamedStructualType(String name,Iterable<EdmProperty> properties) {
		this(name,null,properties,false);
	}
	
	protected EdmNamedStructualType(String name,Iterable<EdmProperty> properties,boolean isAbstract) {
		this(name,null,properties,isAbstract);
	}
	
	protected EdmNamedStructualType(String name,String title,Iterable<EdmProperty> properties,boolean isAbstract) {
		super(properties);
		this.name       = name;
		this.title      = title;
		this.isAbstract = isAbstract;
	}
	
	public String getName() {
	    return name;
    }
	
	public String getTitle() {
		return title;
	}
	
	public String getTitleOrName(){
		return Strings.firstNotEmpty(title,name);
	}

	public boolean isAbstract() {
    	return isAbstract;
    }
}