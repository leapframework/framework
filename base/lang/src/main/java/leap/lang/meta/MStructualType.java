/*
 * Copyright 2015 the original author or authors.
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
package leap.lang.meta;

import java.util.Collection;

import leap.lang.Args;

public abstract class MStructualType extends MType {
	
	private static final MProperty[] EMPTY_ARRAY = new MProperty[]{};
	
	protected final MProperty[] properties;

	public MStructualType(String summary, String description, Collection<MProperty> properties) {
		super(summary, description);
		
		Args.notNull(properties,"properties");
		
		this.properties = properties.toArray(EMPTY_ARRAY);
	}

	public MProperty[] getProperties() {
		return properties;
	}

}