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

class InitDefinition implements Sourced {

	protected final Object source;
	protected final String initClassName;
	protected final String initMethodName;
	
	public InitDefinition(Object source,String initClassName, String initMethodName) {
	    this.source         = source;
	    this.initClassName  = initClassName;
	    this.initMethodName = initMethodName;
    }
	
	@Override
    public Object getSource() {
	    return source;
    }

	public String getInitClassName() {
		return initClassName;
	}

	public String getInitMethodName() {
		return initMethodName;
	}
}
