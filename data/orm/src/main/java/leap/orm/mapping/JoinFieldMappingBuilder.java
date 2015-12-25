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
package leap.orm.mapping;

import leap.lang.Buildable;

public class JoinFieldMappingBuilder implements Buildable<JoinFieldMapping> {
	
	protected String localFieldName;
	protected String referencedFieldName;
	protected String localColumnName;
	
	public String getLocalFieldName() {
		return localFieldName;
	}

	public JoinFieldMappingBuilder setLocalFieldName(String localFieldName) {
		this.localFieldName = localFieldName;
		return this;
	}

	public String getReferencedFieldName() {
		return referencedFieldName;
	}

	public JoinFieldMappingBuilder setReferencedFieldName(String referencedFieldName) {
		this.referencedFieldName = referencedFieldName;
		return this;
	}

	public String getLocalColumnName() {
		return localColumnName;
	}

	public JoinFieldMappingBuilder setLocalColumnName(String column) {
		this.localColumnName = column;
		return this;
	}

	@Override
	public JoinFieldMapping build() {
		return new JoinFieldMapping(localFieldName, referencedFieldName);
	}
}
