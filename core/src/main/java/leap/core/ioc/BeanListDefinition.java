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

import java.util.List;

class BeanListDefinition {
	
	protected final Object   source;
	protected final Class<?> type;
	protected final String   qualifier;
	protected final boolean  override;
	protected final List<ValueDefinition> values;
	
	public BeanListDefinition(Object source,Class<?> type,String qualifier,boolean override,List<ValueDefinition> values) {
		this.source    = source;
		this.type      = type;
		this.qualifier = qualifier;
		this.override  = override;
		this.values    = values;
	}

	public Object getSource() {
		return source;
	}

	public Class<?> getType() {
		return type;
	}

	public String getQualifier() {
		return qualifier;
	}

	public boolean isOverride() {
		return override;
	}

	public List<ValueDefinition> getValues() {
		return values;
	}
}
