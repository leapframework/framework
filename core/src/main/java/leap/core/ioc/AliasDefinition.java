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
package leap.core.ioc;

import leap.lang.Sourced;

class AliasDefinition implements Sourced {

	protected final Object source;
	
	protected final String   id;
	protected final Class<?> type;
	protected final String   name;
	protected final String   alias;
	
	public AliasDefinition(Object source,String alias,String id){
		this.source = source;
		this.id     = id;
		this.type   = null;
		this.name   = null;
		this.alias  = alias;
	}
	
	public AliasDefinition(Object source,String alias, Class<?> type, String name){
		this.source = source;
		this.id     = null;
		this.type   = type;
		this.name   = name;
		this.alias  = alias;
	}
	
	@Override
    public Object getSource() {
	    return source;
    }
	
	public String getId() {
		return id;
	}

	public Class<?> getType() {
		return type;
	}

	public String getName() {
		return name;
	}
	
	public String getAlias() {
		return alias;
	}
}