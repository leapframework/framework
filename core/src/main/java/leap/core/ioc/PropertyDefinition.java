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

import leap.lang.Named;
import leap.lang.beans.BeanProperty;

class PropertyDefinition implements Named {
	
	protected String 		  name;
	protected BeanProperty    property;
	protected ValueDefinition valueDefinition;
	protected String		  defaultValue;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public BeanProperty getProperty() {
		return property;
	}

	public void setProperty(BeanProperty property) {
		this.property = property;
	}

	public ValueDefinition getValueDefinition() {
		return valueDefinition;
	}

	public void setValueDefinition(ValueDefinition valuedDefinition) {
		this.valueDefinition = valuedDefinition;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
}